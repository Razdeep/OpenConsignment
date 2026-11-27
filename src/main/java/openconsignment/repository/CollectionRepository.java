package openconsignment.repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import openconsignment.common.SqlLoader;
import openconsignment.common.SqlQuery;
import openconsignment.common.Utils;
import openconsignment.di.DatabaseManager;
import openconsignment.entity.BillEntity;
import openconsignment.entity.CollectionEntity;
import openconsignment.entity.CollectionItemEntity;
import openconsignment.model.BuyerLedgerRow;

@Slf4j
public class CollectionRepository {

    private final DatabaseManager databaseManager;

    @Inject
    public CollectionRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public List<BillEntity> fetchPendingBillsForBuyer(String buyerName) throws SQLException {
        try {
            String sql = SqlLoader.load(SqlQuery.COLLECTION_PENDING_BILLS_FOR_BUYER);
            PreparedStatement preparedStatement =
                    databaseManager.getConnection().prepareStatement(sql);
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
            log.error(ex.toString(), ex);
            throw ex;
        }
    }

    public int fetchPendingAmountForBillNo(String billNo) throws SQLException {
        try {
            String sql = SqlLoader.load(SqlQuery.COLLECTION_PENDING_AMOUNT_FOR_BILL);
            PreparedStatement preparedStatement =
                    databaseManager.getConnection().prepareStatement(sql);
            preparedStatement.setString(1, billNo);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.isClosed()) {
                return 0;
            }

            return resultSet.getInt("pending_amount");
        } catch (SQLException ex) {
            log.error(ex.toString(), ex);
            throw ex;
        }
    }

    public void saveCollection(CollectionEntity collectionEntity) throws SQLException {
        try {
            databaseManager.getConnection().setAutoCommit(false);
            deleteCollection(collectionEntity.getVoucherNo());
            String sql = SqlLoader.load(SqlQuery.COLLECTION_SAVE);
            PreparedStatement preparedStatement =
                    databaseManager.getConnection().prepareStatement(sql);
            preparedStatement.setString(1, collectionEntity.getVoucherNo());
            preparedStatement.setString(2, collectionEntity.getVoucherDate());
            preparedStatement.setString(3, collectionEntity.getBuyerName());
            preparedStatement.setString(4, collectionEntity.getTotalAmount());
            preparedStatement.execute();

            sql = SqlLoader.load(SqlQuery.COLLECTION_SAVE_ITEM);

            if (null != collectionEntity.getItems()) {
                for (CollectionItemEntity collectionItem : collectionEntity.getItems()) {

                    preparedStatement = databaseManager.getConnection().prepareStatement(sql);
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

            databaseManager.getConnection().commit();

        } catch (SQLException ex) {
            databaseManager.getConnection().rollback();
            log.error(ex.toString(), ex);
            throw ex;
        }
    }

    public String getLastVoucher() throws SQLException {
        try {
            String sql = SqlLoader.load(SqlQuery.COLLECTION_LAST_VOUCHER);
            PreparedStatement preparedStatement =
                    databaseManager.getConnection().prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.getString("Max(`Voucher No.`)");
        } catch (SQLException ex) {
            log.error(ex.toString(), ex);
            throw ex;
        }
    }

    public CollectionEntity getCollection(String voucherNo) throws SQLException {

        try {
            String sql = SqlLoader.load(SqlQuery.COLLECTION_GET_ITEMS);
            PreparedStatement preparedStatement =
                    databaseManager.getConnection().prepareStatement(sql);
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

            sql = SqlLoader.load(SqlQuery.COLLECTION_GET);
            preparedStatement = databaseManager.getConnection().prepareStatement(sql);
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
            log.error(ex.toString(), ex);
            throw ex;
        }
    }

    public void deleteCollection(String voucherNo) throws SQLException {
        try {
            databaseManager.getConnection().setAutoCommit(false);

            String sql = SqlLoader.load(SqlQuery.COLLECTION_DELETE);

            PreparedStatement preparedStatement =
                    databaseManager.getConnection().prepareStatement(sql);
            preparedStatement.setString(1, voucherNo);
            preparedStatement.execute();

            sql = SqlLoader.load(SqlQuery.COLLECTION_DELETE_ITEMS);

            preparedStatement = databaseManager.getConnection().prepareStatement(sql);
            preparedStatement.setString(1, voucherNo);
            preparedStatement.execute();
            databaseManager.getConnection().commit();
        } catch (SQLException ex) {
            Utils.showAlert(ex.toString());
            log.error(ex.toString(), ex);
            databaseManager.getConnection().rollback();
        }
    }

    public List<CollectionEntity> getCollections() throws SQLException {
        try {
            List<CollectionEntity> res = new ArrayList<>();
            String sql = SqlLoader.load(SqlQuery.COLLECTION_GET_ALL);
            PreparedStatement ps = databaseManager.getConnection().prepareStatement(sql);
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
            log.error(ex.toString(), ex);
            throw ex;
        }
    }

    public List<BuyerLedgerRow> getCollectionDetailsForBuyer(String buyerName) throws SQLException {
        try {
            String sql = SqlLoader.load(SqlQuery.COLLECTION_DETAILS_FOR_BUYER);
            PreparedStatement preparedStatement =
                    databaseManager.getConnection().prepareStatement(sql);
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
                        .collectionDue(resultSet.getInt("pending_amount"))
                        .build());
            }
            return res;
        } catch (SQLException ex) {
            log.error(ex.toString(), ex);
            throw ex;
        }
    }
}
