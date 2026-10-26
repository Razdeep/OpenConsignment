package openconsignment.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.sf.jasperreports.engine.JRException;
import openconsignment.common.Utils;
import openconsignment.entity.*;
import openconsignment.model.SupplierLedgerRow;
import openconsignment.repository.PaymentRepository;

@Singleton
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Inject
    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public List<String> fetchPendingBillNosForSupplier(String supplierName) throws SQLException {
        return paymentRepository.fetchPendingBillsForSupplier(supplierName).stream()
                .map(BillEntity::getBillNo)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public List<BillEntity> fetchPendingBillsForSupplier(String supplierName) throws SQLException {
        return paymentRepository.fetchPendingBillsForSupplier(supplierName);
    }

    public int fetchPendingAmountForBillNo(String billNo) throws SQLException {
        return paymentRepository.fetchPendingAmountForBillNo(billNo);
    }

    public void savePayment(PaymentEntity paymentEntity) throws SQLException {
        paymentRepository.savePayment(paymentEntity);
    }

    public String getLastVoucher() throws SQLException {
        return paymentRepository.getLastVoucher();
    }

    public PaymentEntity getPayment(String voucherNo) throws SQLException {
        return paymentRepository.getPayment(voucherNo);
    }

    public void deletePayment(String voucherNo) throws SQLException {
        paymentRepository.deletePayment(voucherNo);
    }

    public List<PaymentEntity> getPayments() throws SQLException {
        return paymentRepository.getPayments();
    }

    public List<SupplierLedgerRow> getPaymentDetailsForSupplier(String supplierName) throws SQLException {
        return paymentRepository.getPaymentDetailsForSupplier(supplierName);
    }

    public void generatePdf(String voucherNo) throws SQLException, JRException {

        PaymentEntity paymentEntity = getPayment(voucherNo);

        Map<String, Object> payload = new HashMap<>();

        payload.put("voucherNo", voucherNo);
        payload.put("voucherDate", paymentEntity.getVoucherDate());
        payload.put("supplierName", paymentEntity.getSupplierName());
        payload.put("billAmount", paymentEntity.getTotalAmount());

        List<PaymentItemEntity> dataRows = paymentEntity.getItems();

        Utils.generatePdf("Payment.jrxml", payload, dataRows);
    }
}
