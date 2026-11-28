package openconsignment.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import openconsignment.di.DatabaseManager;
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
        paymentRepository = new PaymentRepository(new DatabaseManager(conn));
        billRepository = new BillRepository(new DatabaseManager(conn));
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
    void balancesReflectMultiplePaymentsReplacementAndDeletion() throws SQLException {
        saveBill("B1", "Supplier A", "1000");
        saveBill("B2", "Supplier B", "2000");
        paymentRepository.savePayment(paymentWith("V1", "Supplier A", "300", paymentItem("B1", "1000", "300")));
        paymentRepository.savePayment(paymentWith("V2", "Supplier A", "200", paymentItem("B1", "1000", "200")));

        assertEquals(500, paymentRepository.fetchPendingAmountForBillNo("B1"));
        assertEquals(2000, paymentRepository.fetchPendingAmountForBillNo("B2"));
        assertEquals(
                List.of("B1"),
                paymentRepository.fetchPendingBillsForSupplier("Supplier A").stream()
                        .map(BillEntity::getBillNo)
                        .toList());

        paymentRepository.savePayment(paymentWith("V2", "Supplier A", "700", paymentItem("B1", "1000", "700")));
        assertEquals(0, paymentRepository.fetchPendingAmountForBillNo("B1"));
        assertTrue(paymentRepository.fetchPendingBillsForSupplier("Supplier A").isEmpty());
        assertEquals(1, paymentRepository.getPayment("V2").getItems().size());

        paymentRepository.deletePayment("V1");
        assertEquals(300, paymentRepository.fetchPendingAmountForBillNo("B1"));
        assertEquals(
                1, paymentRepository.fetchPendingBillsForSupplier("Supplier A").size());
        assertNull(paymentRepository.getPayment("V1"));
    }

    @Test
    void missingPaymentAndEmptyTablesReturnNoResults() throws SQLException {
        assertNull(paymentRepository.getLastVoucher());
        assertNull(paymentRepository.getPayment("missing"));
        assertTrue(paymentRepository.getPayments().isEmpty());
        assertTrue(paymentRepository.fetchPendingBillsForSupplier("missing").isEmpty());
    }

    @Test
    void failedReplacementRestoresOriginalHeaderAndItems() throws SQLException {
        saveBill("B1", "Supplier A", "1000");
        paymentRepository.savePayment(paymentWith("V1", "Supplier A", "300", paymentItem("B1", "1000", "300")));
        boolean originalAutoCommit = conn.getAutoCommit();
        PaymentItemEntity invalid = PaymentItemEntity.builder().billNo("B2").build();

        assertThrows(
                SQLException.class,
                () -> paymentRepository.savePayment(
                        paymentWith("V1", "Supplier B", "500", paymentItem("B1", "1000", "200"), invalid)));

        PaymentEntity restored = paymentRepository.getPayment("V1");
        assertEquals("Supplier A", restored.getSupplierName());
        assertEquals("300", restored.getTotalAmount());
        assertEquals(1, restored.getItems().size());
        assertEquals("300", restored.getItems().get(0).getAmountPaid());
        assertEquals(700, paymentRepository.fetchPendingAmountForBillNo("B1"));
        assertEquals(originalAutoCommit, conn.getAutoCommit());
    }

    @Test
    void failedNewPaymentLeavesNoPartialRowsAndRestoresAutoCommit() throws SQLException {
        assertTrue(conn.getAutoCommit());
        assertThrows(
                SQLException.class,
                () -> paymentRepository.savePayment(paymentWith(
                        "V1",
                        "Supplier A",
                        "500",
                        paymentItem("B1", "1000", "200"),
                        PaymentItemEntity.builder().build())));
        assertNull(paymentRepository.getPayment("V1"));
        try (var statement = conn.createStatement();
                var rows = statement.executeQuery("SELECT COUNT(*) FROM Payment_Entry_Extended_Table")) {
            assertEquals(0, rows.getInt(1));
        }
        assertTrue(conn.getAutoCommit());
    }

    @Test
    void deleteFailureRollsBackHeaderAndPropagates() throws SQLException {
        paymentRepository.savePayment(paymentWith("V1", "Supplier A", "300", paymentItem("B1", "1000", "300")));
        try (var statement = conn.createStatement()) {
            statement.execute("CREATE TRIGGER fail_payment_delete BEFORE DELETE ON Payment_Entry_Extended_Table "
                    + "BEGIN SELECT RAISE(ABORT, 'delete failed'); END");
        }
        assertThrows(SQLException.class, () -> paymentRepository.deletePayment("V1"));
        assertEquals("300", paymentRepository.getPayment("V1").getItems().get(0).getAmountPaid());
        assertTrue(conn.getAutoCommit());
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
