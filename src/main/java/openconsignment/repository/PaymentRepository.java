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
import openconsignment.entity.PaymentEntity;
import openconsignment.entity.PaymentItemEntity;
import openconsignment.model.SupplierLedgerRow;

@Slf4j
public class PaymentRepository {

    private final DatabaseManager databaseManager;

    @Inject
    public PaymentRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void savePayment(PaymentEntity paymentEntity) throws SQLException {
        try {
            databaseManager.getConnection().setAutoCommit(false);
            deletePayment(paymentEntity.getVoucherNo());
            String sql = SqlLoader.load(SqlQuery.PAYMENT_SAVE);
            PreparedStatement preparedStatement =
                    databaseManager.getConnection().prepareStatement(sql);
            preparedStatement.setString(1, paymentEntity.getVoucherNo());
            preparedStatement.setString(2, paymentEntity.getVoucherDate());
            preparedStatement.setString(3, paymentEntity.getSupplierName());
            preparedStatement.setString(4, paymentEntity.getTotalAmount());
            preparedStatement.execute();

            sql = SqlLoader.load(SqlQuery.PAYMENT_SAVE_ITEM);

            if (null != paymentEntity.getItems()) {
                for (PaymentItemEntity paymentItem : paymentEntity.getItems()) {

                    preparedStatement = databaseManager.getConnection().prepareStatement(sql);
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
            databaseManager.getConnection().commit();
        } catch (SQLException ex) {
            databaseManager.getConnection().rollback();
            log.error(ex.toString(), ex);
        }
    }

    public List<BillEntity> fetchPendingBillsForSupplier(String supplierName) throws SQLException {
        try {
            String sql = SqlLoader.load(SqlQuery.PAYMENT_PENDING_BILLS_FOR_SUPPLIER);
            PreparedStatement preparedStatement =
                    databaseManager.getConnection().prepareStatement(sql);
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
            log.error(ex.toString(), ex);
            throw ex;
        }
    }

    public int fetchPendingAmountForBillNo(String billNo) throws SQLException {
        try {
            String sql = SqlLoader.load(SqlQuery.PAYMENT_PENDING_AMOUNT_FOR_BILL);
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

    public String getLastVoucher() throws SQLException {
        try {
            String sql = SqlLoader.load(SqlQuery.PAYMENT_LAST_VOUCHER);
            PreparedStatement preparedStatement =
                    databaseManager.getConnection().prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.getString("Max(`Voucher No.`)");
        } catch (SQLException ex) {
            log.error(ex.toString(), ex);
            throw ex;
        }
    }

    public PaymentEntity getPayment(String voucherNo) throws SQLException {
        try {
            String sql = SqlLoader.load(SqlQuery.PAYMENT_GET_ITEMS);
            PreparedStatement ps = databaseManager.getConnection().prepareStatement(sql);
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

            sql = SqlLoader.load(SqlQuery.PAYMENT_GET);

            ps = databaseManager.getConnection().prepareStatement(sql);
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
            log.error(ex.toString(), ex);
            throw ex;
        }
    }

    public void deletePayment(String voucherNo) throws SQLException {
        try {
            databaseManager.getConnection().setAutoCommit(false);

            String sql = SqlLoader.load(SqlQuery.PAYMENT_DELETE);

            PreparedStatement preparedStatement =
                    databaseManager.getConnection().prepareStatement(sql);
            preparedStatement.setString(1, voucherNo);
            preparedStatement.execute();

            sql = SqlLoader.load(SqlQuery.PAYMENT_DELETE_ITEMS);

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

    public List<PaymentEntity> getPayments() throws SQLException {
        try {
            List<PaymentEntity> res = new ArrayList<>();
            String sql = SqlLoader.load(SqlQuery.PAYMENT_GET_ALL);
            PreparedStatement ps = databaseManager.getConnection().prepareStatement(sql);
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
            log.error(ex.toString(), ex);
            throw ex;
        }
    }

    public List<SupplierLedgerRow> getPaymentDetailsForSupplier(String supplierName) throws SQLException {
        try {
            String sql = SqlLoader.load(SqlQuery.PAYMENT_DETAILS_FOR_SUPPLIER);
            PreparedStatement preparedStatement =
                    databaseManager.getConnection().prepareStatement(sql);
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
            log.error(ex.toString(), ex);
            throw ex;
        }
    }
}
