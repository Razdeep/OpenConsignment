package openconsignment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import net.sf.jasperreports.engine.JRException;
import openconsignment.common.Constants;
import openconsignment.common.Utils;
import openconsignment.entity.BillEntity;
import openconsignment.model.SupplierLedgerAgeRow;
import openconsignment.model.SupplierLedgerRow;
import openconsignment.repository.SupplierRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SupplierServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private SupplierService supplierService;

    @Test
    void getAllSuppliersReturnsRepositoryResult() throws SQLException {
        List<String> suppliers = List.of("Supplier A");
        when(supplierRepository.getAllSuppliers()).thenReturn(suppliers);

        assertEquals(suppliers, supplierService.getAllSuppliers());
    }

    @Test
    void saveSupplierDelegatesToRepository() throws SQLException {
        supplierService.saveSupplier("Supplier A");

        verify(supplierRepository).saveSupplier("Supplier A");
    }

    @Test
    void deleteSupplierDelegatesToRepository() throws SQLException {
        supplierService.deleteSupplier("Supplier A");

        verify(supplierRepository).deleteSupplier("Supplier A");
    }

    @Test
    void renameSupplierDelegatesToRepository() throws SQLException {
        supplierService.renameSupplier("Old", "New");

        verify(supplierRepository).renameSupplier("Old", "New");
    }

    @Test
    void generatePdfNonAgewiseUsesPaymentDetails() throws SQLException, JRException {
        List<SupplierLedgerRow> rows =
                List.of(SupplierLedgerRow.builder().billNo("B1").build());
        when(paymentService.getPaymentDetailsForSupplier("Supplier A")).thenReturn(rows);

        try (MockedStatic<Utils> utils = mockStatic(Utils.class)) {
            supplierService.generatePdf("Supplier A", false);

            ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
            utils.verify(() -> Utils.generatePdf(eq("SupplierLedger.jrxml"), payloadCaptor.capture(), eq(rows)));

            Map<String, Object> payload = payloadCaptor.getValue();
            assertEquals(Constants.ORGANIZATION_NAME, payload.get(Constants.ORGANIZATION_NAME_KEY));
            assertEquals("Supplier A", payload.get("supplierName"));
        }
    }

    @Test
    void generatePdfAgewiseMapsPendingBillsWithComputedDays() throws SQLException, JRException {
        String billDate = "01-01-2026";
        List<BillEntity> pendingBills = List.of(BillEntity.builder()
                .billNo("B1")
                .billDate(billDate)
                .billAmount("1000")
                .buyerName("Buyer A")
                .build());
        when(paymentService.fetchPendingBillsForSupplier("Supplier A")).thenReturn(pendingBills);

        int expectedDays = (int) ChronoUnit.DAYS.between(Utils.parseDate(billDate), LocalDate.now());

        try (MockedStatic<Utils> utils = mockStatic(Utils.class)) {
            utils.when(() -> Utils.parseDate(billDate)).thenCallRealMethod();

            supplierService.generatePdf("Supplier A", true);

            ArgumentCaptor<List<SupplierLedgerAgeRow>> rowsCaptor = ArgumentCaptor.forClass(List.class);
            ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
            utils.verify(() ->
                    Utils.generatePdf(eq("SupplierLedgerAge.jrxml"), payloadCaptor.capture(), rowsCaptor.capture()));

            Map<String, Object> payload = payloadCaptor.getValue();
            assertEquals(Constants.ORGANIZATION_NAME, payload.get(Constants.ORGANIZATION_NAME_KEY));
            assertEquals("Supplier A", payload.get("supplierName"));

            List<SupplierLedgerAgeRow> rows = rowsCaptor.getValue();
            assertEquals(1, rows.size());
            SupplierLedgerAgeRow row = rows.get(0);
            assertEquals("B1", row.getBillNo());
            assertEquals(billDate, row.getBillDate());
            assertEquals("1000", row.getBillAmount());
            assertEquals("Buyer A", row.getBuyerName());
            assertEquals(expectedDays, row.getDays());
        }
    }
}
