package openconsignment.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import openconsignment.common.Utils;
import openconsignment.entity.BillEntity;
import openconsignment.entity.CollectionEntity;
import openconsignment.entity.CollectionItemEntity;
import openconsignment.model.BuyerLedgerRow;

public class CollectionRepository {

    private final Connection conn;

    @Inject
    public CollectionRepository(Connection conn) {
        this.conn = conn;
    }

    public List<BillEntity> fetchPendingBillsForBuyer(String buyerName) throws SQLException {
        try {
            // language=sql
            String sql =
                    """
                        with cte_collected as (
                        select
                            `Bill No.`,
                            sum(`Amount Collected`) as amount_collected
                        from
                            Collection_Entry_Extended_Table
                        group by
                            `Bill No.`
                        ),
                        cte_bill as (
                        select
                            `Bill No.`,
                            `Bill Date`,
                            `Bill Amount`,
                            `Supplier Name`
                        from
                            Bill_Entry_Table
                        WHERE
                            `Buyer Name` = ?
                        ),
                        cte_bill_collected as (
                        select
                            `Bill No.`,
                            `Bill Date`,
                            `Bill Amount`,
                            `Supplier Name`,
                            coalesce(`amount_collected`, 0) as amount_collected
                        from
                            cte_bill
                        left join cte_collected
                            using(`Bill No.`)
                        ),
                        cte_pending as (
                        select
                            `Bill No.`,
                            `Bill Date`,
                            `Bill Amount`,
                            `Supplier Name`,
                            `Bill Amount` - amount_collected as pending_amount
                        from
                            cte_bill_collected
                        ),
                        cte_final as (
                        select
                            `Bill No.`,
                            `Bill Date`,
                            `Bill Amount`,
                            `Supplier Name`
                        from
                            cte_pending
                        where
                            pending_amount > 0
                        order by
                            1
                        )
                        select
                            *
                        from
                            cte_final;
                    """;
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, buyerName);
            ResultSet resultSet = preparedStatement.executeQuery();

            List<BillEntity> res = new ArrayList<>();
            while (resultSet.next()) {
                res.add(BillEntity.builder()
                        .billNo(resultSet.getString("Bill No."))
                        .billDate(resultSet.getString("Bill Date"))
                        .billAmount(resultSet.getString("Bill Amount"))
                        .supplierName(resultSet.getString("Supplier Name"))
                        .build());
            }
            return res;
        } catch (SQLException ex) {
            Logger.getLogger(CollectionRepository.class.getName()).log(Level.SEVERE, ex.toString(), ex);
            throw ex;
        }
    }

    public int fetchPendingAmountForBillNo(String billNo) throws SQLException {
        try {
            // language=sql
            String sql =
                    """
                        with cte_collected as (
                        select
                            `Bill No.`,
                            sum(`Amount Collected`) as amount_collected
                        from
                            Collection_Entry_Extended_Table
                        group by
                            `Bill No.`
                        ),
                        cte_bill as (
                        select
                            *
                        from
                            Bill_Entry_Table
                        WHERE
                            `Bill No.` = ?
                        ),
                        cte_bill_collected as (
                        select
                            `Bill No.`,
                            `Bill Amount`,
                            coalesce(`amount_collected`, 0) as amount_collected
                        from
                            cte_bill
                        left join cte_collected
                                using(`Bill No.`)
                        ),
                        cte_pending as (
                        select
                            `Bill No.`,
                            `Bill Amount` - amount_collected as pending_amount
                        from
                            cte_bill_collected
                        ),
                        cte_final as (
                        select
                            pending_amount
                        from
                            cte_pending
                        )
                        select
                            *
                        from
                            cte_final;
                    """;
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, billNo);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.isClosed()) {
                return 0;
            }

            return resultSet.getInt("pending_amount");
        } catch (SQLException ex) {
            Logger.getLogger(CollectionRepository.class.getName()).log(Level.SEVERE, ex.toString(), ex);
            throw ex;
        }
    }

    public void saveCollection(CollectionEntity collectionEntity) throws SQLException {
        try {
            conn.setAutoCommit(false);
            deleteCollection(collectionEntity.getVoucherNo());
            // language=sql
            String sql =
                    """
                        INSERT INTO `Collection_Entry_Table`(`Voucher No.`,`Voucher Date`,`Buyer Name`,`Total Amount`)
                        VALUES (?,?,?,?)
                        on conflict(`Voucher No.`) do update set
                      `Voucher Date` = excluded.`Voucher Date`,
                      `Buyer Name` = excluded.`Buyer Name`,
                      `Total Amount` = excluded.`Total Amount`;
""";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, collectionEntity.getVoucherNo());
            preparedStatement.setString(2, collectionEntity.getVoucherDate());
            preparedStatement.setString(3, collectionEntity.getBuyerName());
            preparedStatement.setString(4, collectionEntity.getTotalAmount());
            preparedStatement.execute();

            // language=sql
            sql =
                    """
                    INSERT
                        INTO
                        `Collection_Entry_Extended_Table`(`Voucher No.`,
                        `Supplier Name`,
                        `Bill No.`,
                        `Bill Date`,
                        `Bill Amount`,
                        `Collection Due`,
                        `Amount Collected`,
                        `Bank`,
                        `DD No.`,
                        `DD Date`)
                    VALUES (?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?);
""";

            if (null != collectionEntity.getItems()) {
                for (CollectionItemEntity collectionItem : collectionEntity.getItems()) {

                    preparedStatement = conn.prepareStatement(sql);
                    preparedStatement.setString(1, collectionEntity.getVoucherNo());
                    preparedStatement.setString(2, collectionItem.getSupplierName());
                    preparedStatement.setString(3, collectionItem.getBillNo());
                    preparedStatement.setString(4, collectionItem.getBillDate());
                    preparedStatement.setString(5, collectionItem.getBillAmount());
                    preparedStatement.setString(6, collectionItem.getDue());
                    preparedStatement.setString(7, collectionItem.getAmountCollected());
                    preparedStatement.setString(8, collectionItem.getBank());
                    preparedStatement.setString(9, collectionItem.getDdNo());
                    preparedStatement.setString(10, collectionItem.getDdDate());

                    preparedStatement.execute();
                }
            }

            conn.commit();

        } catch (SQLException ex) {
            conn.rollback();
            Logger.getLogger(CollectionRepository.class.getName()).log(Level.SEVERE, ex.toString(), ex);
            throw ex;
        }
    }

    public String getLastVoucher() throws SQLException {
        try {
            // language=sql
            String sql = "SELECT MAX(`Voucher No.`) from `COLLECTION_ENTRY_TABLE`;";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.getString("Max(`Voucher No.`)");
        } catch (SQLException ex) {
            Logger.getLogger(CollectionRepository.class.getName()).log(Level.SEVERE, ex.toString(), ex);
            throw ex;
        }
    }

    public CollectionEntity getCollection(String voucherNo) throws SQLException {

        try {
            String sql = "select * from `Collection_Entry_Extended_Table` where `Voucher No.`=? collate nocase";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, voucherNo);
            ResultSet collectionResultSet = preparedStatement.executeQuery();

            List<CollectionItemEntity> collectionItemList = new ArrayList<>();

            while (collectionResultSet.next()) {
                collectionItemList.add(new CollectionItemEntity(
                        collectionResultSet.getString("Bill No."),
                        collectionResultSet.getString("Bill Date"),
                        collectionResultSet.getString("Bill Amount"),
                        collectionResultSet.getString("Supplier Name"),
                        collectionResultSet.getString("collection due"),
                        collectionResultSet.getString("amount collected"),
                        collectionResultSet.getString("bank"),
                        collectionResultSet.getString("DD No."),
                        collectionResultSet.getString("DD Date")));
            }

            sql = "select * from `Collection_Entry_Table` where `Voucher No.`=? collate nocase";
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, voucherNo);
            collectionResultSet = preparedStatement.executeQuery();
            if (collectionResultSet.isClosed()) {
                return null;
            }

            return CollectionEntity.builder()
                    .voucherNo(collectionResultSet.getString("Voucher No."))
                    .voucherDate(collectionResultSet.getString("Voucher Date"))
                    .buyerName(collectionResultSet.getString("Buyer Name"))
                    .totalAmount(collectionResultSet.getString("Total Amount"))
                    .items(collectionItemList)
                    .build();
        } catch (SQLException ex) {
            Logger.getLogger(CollectionRepository.class.getName()).log(Level.SEVERE, ex.toString(), ex);
            throw ex;
        }
    }

    public void deleteCollection(String voucherNo) throws SQLException {
        try {
            conn.setAutoCommit(false);

            String sql = "DELETE FROM `Collection_Entry_Table` where `Voucher No.`=? collate nocase";

            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, voucherNo);
            preparedStatement.execute();

            sql = "DELETE FROM `Collection_Entry_Extended_Table` where `Voucher No.`=? collate nocase";

            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, voucherNo);
            preparedStatement.execute();
            conn.commit();
        } catch (SQLException ex) {
            Utils.showAlert(ex.toString());
            Logger.getLogger(CollectionRepository.class.getName()).log(Level.SEVERE, ex.toString(), ex);
            conn.rollback();
        }
    }

    public List<CollectionEntity> getCollections() throws SQLException {
        try {
            List<CollectionEntity> res = new ArrayList<>();
            String sql = "SELECT * FROM `Collection_Entry_Table`;";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                res.add(CollectionEntity.builder()
                        .voucherNo(rs.getString("Voucher No."))
                        .voucherDate(rs.getString("Voucher Date"))
                        .buyerName(rs.getString("Buyer Name"))
                        .totalAmount(rs.getString("Total Amount"))
                        // .items() omitted as not used
                        .build());
            }
            return res;
        } catch (SQLException ex) {
            Logger.getLogger(CollectionRepository.class.getName()).log(Level.SEVERE, ex.toString(), ex);
            throw ex;
        }
    }

    public List<BuyerLedgerRow> getCollectionDetailsForBuyer(String buyerName) throws SQLException {
        try {
            // language=sql
            String sql =
                    """
                        select * from `Bill_Entry_Table` left join
                        (`Collection_Entry_Extended_Table`
                        join `Collection_Entry_Table` using ("Voucher No."))
                        using("Bill No.") where `Bill_Entry_Table`.`Buyer Name`=?
                    """;
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, buyerName);
            ResultSet resultSet = preparedStatement.executeQuery();

            List<BuyerLedgerRow> res = new ArrayList<>();
            while (resultSet.next()) {
                res.add(BuyerLedgerRow.builder()
                        .billNo(resultSet.getString("Bill No."))
                        .billDate(resultSet.getString("Bill Date"))
                        .billAmount(resultSet.getInt("Bill Amount"))
                        .supplierName(resultSet.getString("Supplier Name"))
                        .voucherNo(resultSet.getString("Voucher No."))
                        .amountCollected(resultSet.getInt("Amount Collected"))
                        .bank(resultSet.getString("Bank"))
                        .ddNo(resultSet.getString("DD No."))
                        .ddDate(resultSet.getString("DD Date"))
                        .collectionDue(resultSet.getInt("Collection Due"))
                        .build());
            }
            return res;
        } catch (SQLException ex) {
            Logger.getLogger(CollectionRepository.class.getName()).log(Level.SEVERE, ex.toString(), ex);
            throw ex;
        }
    }
}
