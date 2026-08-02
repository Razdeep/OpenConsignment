package openconsignment.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import openconsignment.entity.BillEntity;
import openconsignment.entity.PaymentEntity;
import openconsignment.entity.PaymentItemEntity;
import openconsignment.model.SupplierLedgerRow;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PaymentRepositoryTest {

    private Connection conn;
    private PaymentRepository paymentRepository;
    private BillRepository billRepository;

    @BeforeEach
    void setUp() throws Exception {
        conn = RepositoryTestSupport.newInMemoryConnection();
        paymentRepository = new PaymentRepository(conn);
        billRepository = new BillRepository(conn);
    }

    @AfterEach
    void tearDown() throws SQLException {
        conn.close();
    }

    @Test
    void fetchPendingBillsForSupplierExcludesFullyPaidBills() throws SQLException {
        saveBill("B1", "Supplier A", "1000");
        saveBill("B2", "Supplier A", "500");
        paymentRepository.savePayment(paymentWith(
                "V1", "Supplier A", "400", paymentItem("B1", "1000", "400"), paymentItem("B2", "500", "500")));

        List<BillEntity> pending = paymentRepository.fetchPendingBillsForSupplier("Supplier A");

        assertEquals(1, pending.size());
        assertEquals("B1", pending.get(0).getBillNo());
    }

    @Test
    void fetchPendingAmountForBillNoReturnsBillAmountMinusPaid() throws SQLException {
        saveBill("B1", "Supplier A", "1000");
        paymentRepository.savePayment(paymentWith("V1", "Supplier A", "400", paymentItem("B1", "1000", "400")));

        assertEquals(600, paymentRepository.fetchPendingAmountForBillNo("B1"));
    }

    @Test
    void saveAndGetPaymentRoundTripsWithItems() throws SQLException {
        saveBill("B1", "Supplier A", "1000");
        paymentRepository.savePayment(paymentWith("V1", "Supplier A", "1000", paymentItem("B1", "1000", "1000")));

        PaymentEntity result = paymentRepository.getPayment("V1");

        assertEquals("V1", result.getVoucherNo());
        assertEquals("Supplier A", result.getSupplierName());
        assertEquals("1000", result.getTotalAmount());
        assertEquals(1, result.getItems().size());
        assertEquals("B1", result.getItems().get(0).getBillNo());
        assertEquals("1000", result.getItems().get(0).getAmountPaid());
    }

    @Test
    void getLastVoucherIsALexicographicMaxOnTheTextColumn() throws SQLException {
        paymentRepository.savePayment(paymentWith("V10", "Supplier A", "0"));
        paymentRepository.savePayment(paymentWith("V9", "Supplier A", "0"));

        // "V9" > "V10" lexicographically ('9' > '1'), even though 10 > 9 numerically —
        // documents the real behavior of MAX() on a TEXT voucher-number column.
        assertEquals("V9", paymentRepository.getLastVoucher());
    }

    @Test
    void deletePaymentRemovesItFromPayments() throws SQLException {
        paymentRepository.savePayment(paymentWith("V1", "Supplier A", "0"));

        paymentRepository.deletePayment("V1");

        assertTrue(paymentRepository.getPayments().isEmpty());
    }

    @Test
    void getPaymentsReturnsAllSavedPayments() throws SQLException {
        paymentRepository.savePayment(paymentWith("V1", "Supplier A", "0"));
        paymentRepository.savePayment(paymentWith("V2", "Supplier B", "0"));

        assertEquals(2, paymentRepository.getPayments().size());
    }

    @Test
    void getPaymentDetailsForSupplierJoinsBillAndPaymentRows() throws SQLException {
        saveBill("B1", "Supplier A", "1000");
        paymentRepository.savePayment(paymentWith("V1", "Supplier A", "1000", paymentItem("B1", "1000", "400")));

        List<SupplierLedgerRow> rows = paymentRepository.getPaymentDetailsForSupplier("Supplier A");

        assertEquals(1, rows.size());
        SupplierLedgerRow row = rows.get(0);
        assertEquals("B1", row.getBillNo());
        assertEquals("V1", row.getVoucherNo());
        assertEquals(1000, row.getBillAmount());
        assertEquals(400, row.getAmountPaid());
        assertEquals("Buyer A", row.getBuyerName());
    }

    private void saveBill(String billNo, String supplierName, String billAmount) throws SQLException {
        billRepository.save(BillEntity.builder()
                .billNo(billNo)
                .supplierName(supplierName)
                .buyerName("Buyer A")
                .billDate("01-01-2026")
                .transport("Transport A")
                .lrDate("01-01-2026")
                .billAmount(billAmount)
                .build());
    }

    private static PaymentEntity paymentWith(
            String voucherNo, String supplierName, String totalAmount, PaymentItemEntity... items) {
        return PaymentEntity.builder()
                .voucherNo(voucherNo)
                .voucherDate("01-01-2026")
                .supplierName(supplierName)
                .totalAmount(totalAmount)
                .items(List.of(items))
                .build();
    }

    private static PaymentItemEntity paymentItem(String billNo, String billAmount, String amountPaid) {
        return PaymentItemEntity.builder()
                .buyerName("Buyer A")
                .billNo(billNo)
                .billDate("01-01-2026")
                .billAmount(billAmount)
                .dueAmount("0")
                .amountPaid(amountPaid)
                .bank("")
                .ddNo("")
                .ddDate("")
                .build();
    }
}
