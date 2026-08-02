package openconsignment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import net.sf.jasperreports.engine.JRException;
import openconsignment.common.Utils;
import openconsignment.entity.BillEntity;
import openconsignment.entity.PaymentEntity;
import openconsignment.entity.PaymentItemEntity;
import openconsignment.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void fetchPendingBillNosForSupplierMapsBillEntitiesToBillNos() throws SQLException {
        List<BillEntity> bills = List.of(
                BillEntity.builder().billNo("B1").build(),
                BillEntity.builder().billNo("B2").build());
        when(paymentRepository.fetchPendingBillsForSupplier("Supplier A")).thenReturn(bills);

        assertEquals(List.of("B1", "B2"), paymentService.fetchPendingBillNosForSupplier("Supplier A"));
    }

    @Test
    void fetchPendingBillsForSupplierReturnsRepositoryResult() throws SQLException {
        List<BillEntity> bills = List.of(BillEntity.builder().billNo("B1").build());
        when(paymentRepository.fetchPendingBillsForSupplier("Supplier A")).thenReturn(bills);

        assertEquals(bills, paymentService.fetchPendingBillsForSupplier("Supplier A"));
    }

    @Test
    void fetchPendingAmountForBillNoReturnsRepositoryResult() throws SQLException {
        when(paymentRepository.fetchPendingAmountForBillNo("B1")).thenReturn(500);

        assertEquals(500, paymentService.fetchPendingAmountForBillNo("B1"));
    }

    @Test
    void savePaymentDelegatesToRepository() throws SQLException {
        PaymentEntity paymentEntity = PaymentEntity.builder().voucherNo("V1").build();

        paymentService.savePayment(paymentEntity);

        verify(paymentRepository).savePayment(paymentEntity);
    }

    @Test
    void getLastVoucherReturnsRepositoryResult() throws SQLException {
        when(paymentRepository.getLastVoucher()).thenReturn("V1");

        assertEquals("V1", paymentService.getLastVoucher());
    }

    @Test
    void deletePaymentDelegatesToRepository() throws SQLException {
        paymentService.deletePayment("V1");

        verify(paymentRepository).deletePayment("V1");
    }

    @Test
    void getPaymentsReturnsRepositoryResult() throws SQLException {
        List<PaymentEntity> payments =
                List.of(PaymentEntity.builder().voucherNo("V1").build());
        when(paymentRepository.getPayments()).thenReturn(payments);

        assertEquals(payments, paymentService.getPayments());
    }

    @Test
    void generatePdfBuildsPayloadAndForwardsItems() throws SQLException, JRException {
        List<PaymentItemEntity> items = List.of(PaymentItemEntity.builder()
                .billNo("B1")
                .buyerName("Buyer A")
                .billAmount("1000")
                .build());
        PaymentEntity paymentEntity = PaymentEntity.builder()
                .voucherNo("V1")
                .voucherDate("01-02-2026")
                .supplierName("Supplier A")
                .totalAmount("1000")
                .items(items)
                .build();
        when(paymentRepository.getPayment("V1")).thenReturn(paymentEntity);

        try (MockedStatic<Utils> utils = mockStatic(Utils.class)) {
            paymentService.generatePdf("V1");

            ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
            utils.verify(() -> Utils.generatePdf(eq("Payment.jrxml"), payloadCaptor.capture(), eq(items)));

            Map<String, Object> payload = payloadCaptor.getValue();
            assertEquals("V1", payload.get("voucherNo"));
            assertEquals("01-02-2026", payload.get("voucherDate"));
            assertEquals("Supplier A", payload.get("supplierName"));
            assertEquals("1000", payload.get("billAmount"));
        }
    }
}
