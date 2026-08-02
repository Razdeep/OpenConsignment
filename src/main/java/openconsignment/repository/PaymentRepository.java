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
import openconsignment.entity.PaymentEntity;
import openconsignment.entity.PaymentItemEntity;
import openconsignment.model.SupplierLedgerRow;

public class PaymentRepository {

    private final Connection conn;

    @Inject
    public PaymentRepository(Connection conn) {
        this.conn = conn;
    }

    public void savePayment(PaymentEntity paymentEntity) throws SQLException {
        try {
            conn.setAutoCommit(false);
            String sql =
                    "INSERT INTO `Payment_Entry_Table`(`Voucher No.`,`Voucher Date`,`Supplier Name`,`Total Amount`) VALUES (?,?,?,?)";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, paymentEntity.getVoucherNo());
            preparedStatement.setString(2, paymentEntity.getVoucherDate());
            preparedStatement.setString(3, paymentEntity.getSupplierName());
            preparedStatement.setString(4, paymentEntity.getTotalAmount());
            preparedStatement.execute();

            sql =
                    "INSERT INTO `Payment_Entry_Extended_Table`(`Voucher No.`,`Buyer Name`,`Bill No.`,`Bill Date`,`Bill Amount`,`Due Amount`,`Amount Paid`,`Bank`,`DD No.`,`DD Date`) VALUES (?,?,?,?,?,?,?,?,?,?)";

            if (null != paymentEntity.getItems()) {
                for (PaymentItemEntity paymentItem : paymentEntity.getItems()) {

                    preparedStatement = conn.prepareStatement(sql);
                    preparedStatement.setString(1, paymentEntity.getVoucherNo());
                    preparedStatement.setString(2, paymentItem.getBuyerName());
                    preparedStatement.setString(3, paymentItem.getBillNo());
                    preparedStatement.setString(4, paymentItem.getBillDate());
                    preparedStatement.setString(5, paymentItem.getBillAmount());
                    preparedStatement.setString(6, paymentItem.getDueAmount());
                    preparedStatement.setString(7, paymentItem.getAmountPaid());
                    preparedStatement.setString(8, paymentItem.getBank());
                    preparedStatement.setString(9, paymentItem.getDdNo());
                    preparedStatement.setString(10, paymentItem.getDdDate());

                    preparedStatement.execute();
                }
            }
            conn.commit();
        } catch (SQLException ex) {
            conn.rollback();
            Logger.getLogger(PaymentRepository.class.getName()).log(Level.SEVERE, ex.toString(), ex);
        }
    }

    public List<BillEntity> fetchPendingBillsForSupplier(String supplierName) throws SQLException {
        try {
            // language=sql
            String sql =
                    """
                        with cte_paid as (
                        select
                            `Bill No.`,
                            sum(`Amount Paid`) as amount_paid
                        from
                            Payment_Entry_Extended_Table
                        group by
                            `Bill No.`
                        ),
                        cte_bill as (
                        select
                             `Bill No.`,
                             `Bill Date`,
                             `Bill Amount`,
                             `Buyer Name`
                        from
                            Bill_Entry_Table
                        WHERE
                            `Supplier Name` = ?
                        ),
                        cte_bill_paid as (
                        select
                            `Bill No.`,
                            `Bill Date`,
                            `Bill Amount`,
                            `Buyer Name`,
                            coalesce(`amount_paid`, 0) as amount_paid
                        from
                            cte_bill
                        left join cte_paid
                            using(`Bill No.`)
                        ),
                        cte_pending as (
                        select
                            `Bill No.`,
                            `Bill Date`,
                            `Bill Amount`,
                            `Buyer Name`,
                            `Bill Amount` - amount_paid as pending_amount
                        from
                            cte_bill_paid
                        ),
                        cte_final as (
                        select
                            `Bill No.`,
                            `Bill Date`,
                            `Bill Amount`,
                            `Buyer Name`
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
            preparedStatement.setString(1, supplierName);
            ResultSet resultSet = preparedStatement.executeQuery();

            List<BillEntity> res = new ArrayList<>();
            while (resultSet.next()) {
                res.add(BillEntity.builder()
                        .billNo(resultSet.getString("Bill No."))
                        .billDate(resultSet.getString("Bill Date"))
                        .billAmount(resultSet.getString("Bill Amount"))
                        .buyerName(resultSet.getString("Buyer Name"))
                        .build());
            }
            return res;
        } catch (SQLException ex) {
            Logger.getLogger(PaymentRepository.class.getName()).log(Level.SEVERE, ex.toString(), ex);
            throw ex;
        }
    }

    public int fetchPendingAmountForBillNo(String billNo) throws SQLException {
        try {
            // language=sql
            String sql =
                    """
                        with cte_paid as (
                        select
                            `Bill No.`,
                            sum(`Amount Paid`) as amount_paid
                        from
                            Payment_Entry_Extended_Table
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
                        cte_bill_paid as (
                        select
                            `Bill No.`,
                            `Bill Amount`,
                            coalesce(`amount_paid`, 0) as amount_paid
                        from
                            cte_bill
                        left join cte_paid
                            using(`Bill No.`)
                        ),
                        cte_pending as (
                        select
                            `Bill No.`,
                            `Bill Amount` - amount_paid as pending_amount
                        from
                            cte_bill_paid
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
            Logger.getLogger(PaymentRepository.class.getName()).log(Level.SEVERE, ex.toString(), ex);
            throw ex;
        }
    }

    public String getLastVoucher() throws SQLException {
        try {
            // language=sql
            String sql = "SELECT MAX(`Voucher No.`) from `PAYMENT_ENTRY_TABLE`;";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.getString("Max(`Voucher No.`)");
        } catch (SQLException ex) {
            Logger.getLogger(PaymentRepository.class.getName()).log(Level.SEVERE, ex.toString(), ex);
            throw ex;
        }
    }

    public PaymentEntity getPayment(String voucherNo) throws SQLException {
        try {
            String sql = "select * from `Payment_Entry_Extended_Table` where `Voucher No.`=? collate nocase";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, voucherNo);
            ResultSet rs = ps.executeQuery();

            List<PaymentItemEntity> list = new ArrayList<>();
            while (rs.next()) {
                PaymentItemEntity paymentItem = PaymentItemEntity.builder()
                        .billNo(rs.getString("Bill No."))
                        .billAmount(rs.getString("Bill Amount"))
                        .billDate(rs.getString("Bill Date"))
                        .buyerName(rs.getString("Buyer Name"))
                        .dueAmount(rs.getString("due amount"))
                        .amountPaid(rs.getString("amount paid"))
                        .bank(rs.getString("bank"))
                        .ddNo(rs.getString("DD No."))
                        .ddDate(rs.getString("DD Date"))
                        .build();

                list.add(paymentItem);
            }

            sql = "select * from `Payment_Entry_Table` where `Voucher No.`=? collate nocase";

            ps = conn.prepareStatement(sql);
            ps.setString(1, voucherNo);
            rs = ps.executeQuery();

            if (rs.isClosed()) {
                return null;
            }

            return PaymentEntity.builder()
                    .voucherNo(rs.getString("Voucher No."))
                    .voucherDate(rs.getString("Voucher Date"))
                    .supplierName(rs.getString("Supplier Name"))
                    .totalAmount(rs.getString("Total Amount"))
                    .items(list)
                    .build();

        } catch (SQLException ex) {
            Utils.showAlert(ex.toString());
            Logger.getLogger(PaymentRepository.class.getName()).log(Level.SEVERE, ex.toString(), ex);
            throw ex;
        }
    }

    public void deletePayment(String voucherNo) throws SQLException {
        try {
            conn.setAutoCommit(false);

            String sql = "DELETE FROM `Payment_Entry_Table` where `Voucher No.`=? collate nocase";

            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, voucherNo);
            preparedStatement.execute();

            sql = "DELETE FROM `Payment_Entry_Extended_Table` where `Voucher No.`=? collate nocase";

            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, voucherNo);
            preparedStatement.execute();
            conn.commit();
        } catch (SQLException ex) {
            Utils.showAlert(ex.toString());
            Logger.getLogger(PaymentRepository.class.getName()).log(Level.SEVERE, ex.toString(), ex);
            conn.rollback();
        }
    }

    public List<PaymentEntity> getPayments() throws SQLException {
        try {
            List<PaymentEntity> res = new ArrayList<>();
            String sql = "SELECT * FROM `Payment_Entry_Table`;";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                res.add(PaymentEntity.builder()
                        .voucherNo(rs.getString("Voucher No."))
                        .voucherDate(rs.getString("Voucher Date"))
                        .supplierName(rs.getString("Supplier Name"))
                        .totalAmount(rs.getString("Total Amount"))
                        // .items() omitted as not used
                        .build());
            }
            return res;
        } catch (SQLException ex) {
            Logger.getLogger(PaymentRepository.class.getName()).log(Level.SEVERE, ex.toString(), ex);
            throw ex;
        }
    }

    public List<SupplierLedgerRow> getPaymentDetailsForSupplier(String supplierName) throws SQLException {
        try {
            // language=sql
            String sql =
                    """
                        select * from `Bill_Entry_Table` left join
                        (`Payment_Entry_Extended_Table`
                        join `Payment_Entry_Table` using ("Voucher No."))
                        using("Bill No.") where `Bill_Entry_Table`.`Supplier Name`=?
                        order by `Bill_Entry_Table`.`Bill No.`
                    """;
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, supplierName);
            ResultSet resultSet = preparedStatement.executeQuery();

            List<SupplierLedgerRow> res = new ArrayList<>();
            while (resultSet.next()) {
                res.add(SupplierLedgerRow.builder()
                        .billNo(resultSet.getString("Bill No."))
                        .billDate(resultSet.getString("Bill Date"))
                        .billAmount(resultSet.getInt("Bill Amount"))
                        .buyerName(resultSet.getString("Buyer Name"))
                        .voucherNo(resultSet.getString("Voucher No."))
                        .amountPaid(resultSet.getInt("Amount Paid"))
                        .bank(resultSet.getString("Bank"))
                        .ddNo(resultSet.getString("DD No."))
                        .ddDate(resultSet.getString("DD Date"))
                        .dueAmount(resultSet.getInt("Due Amount"))
                        .build());
            }
            return res;
        } catch (SQLException ex) {
            Logger.getLogger(PaymentRepository.class.getName()).log(Level.SEVERE, ex.toString(), ex);
            throw ex;
        }
    }
}
