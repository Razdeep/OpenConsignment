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
import openconsignment.model.BuyerLedgerAgeRow;
import openconsignment.model.BuyerLedgerRow;
import openconsignment.repository.BuyerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BuyerServiceTest {

    @Mock
    private BuyerRepository buyerRepository;

    @Mock
    private CollectionService collectionService;

    @InjectMocks
    private BuyerService buyerService;

    @Test
    void getAllBuyersReturnsRepositoryResult() throws SQLException {
        List<String> buyers = List.of("Buyer A");
        when(buyerRepository.getAllBuyers()).thenReturn(buyers);

        assertEquals(buyers, buyerService.getAllBuyers());
    }

    @Test
    void saveBuyerDelegatesToRepository() throws SQLException {
        buyerService.saveBuyer("Buyer A");

        verify(buyerRepository).saveBuyer("Buyer A");
    }

    @Test
    void deleteBuyerDelegatesToRepository() throws SQLException {
        buyerService.deleteBuyer("Buyer A");

        verify(buyerRepository).deleteBuyer("Buyer A");
    }

    @Test
    void renameBuyerDelegatesToRepository() throws SQLException {
        buyerService.renameBuyer("Old", "New");

        verify(buyerRepository).renameBuyer("Old", "New");
    }

    @Test
    void generatePdfNonAgewiseUsesCollectionDetails() throws SQLException, JRException {
        List<BuyerLedgerRow> rows =
                List.of(BuyerLedgerRow.builder().billNo("B1").build());
        when(collectionService.getCollectionDetailsForBuyer("Buyer A")).thenReturn(rows);

        try (MockedStatic<Utils> utils = mockStatic(Utils.class)) {
            buyerService.generatePdf("Buyer A", false);

            ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
            utils.verify(() -> Utils.generatePdf(eq("BuyerLedger.jrxml"), payloadCaptor.capture(), eq(rows)));

            Map<String, Object> payload = payloadCaptor.getValue();
            assertEquals(Constants.ORGANIZATION_NAME, payload.get(Constants.ORGANIZATION_NAME_KEY));
            assertEquals("Buyer A", payload.get("buyerName"));
        }
    }

    @Test
    void generatePdfAgewiseMapsPendingBillsWithComputedDays() throws SQLException, JRException {
        String billDate = "01-01-2026";
        List<BillEntity> pendingBills = List.of(BillEntity.builder()
                .billNo("B1")
                .billDate(billDate)
                .billAmount("1000")
                .supplierName("Supplier A")
                .build());
        when(collectionService.fetchPendingBillsForBuyer("Buyer A")).thenReturn(pendingBills);

        int expectedDays = (int) ChronoUnit.DAYS.between(Utils.parseDate(billDate), LocalDate.now());

        try (MockedStatic<Utils> utils = mockStatic(Utils.class)) {
            utils.when(() -> Utils.parseDate(billDate)).thenCallRealMethod();

            buyerService.generatePdf("Buyer A", true);

            ArgumentCaptor<List<BuyerLedgerAgeRow>> rowsCaptor = ArgumentCaptor.forClass(List.class);
            ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
            utils.verify(
                    () -> Utils.generatePdf(eq("BuyerLedgerAge.jrxml"), payloadCaptor.capture(), rowsCaptor.capture()));

            Map<String, Object> payload = payloadCaptor.getValue();
            assertEquals(Constants.ORGANIZATION_NAME, payload.get(Constants.ORGANIZATION_NAME_KEY));
            assertEquals("Buyer A", payload.get("buyerName"));

            List<BuyerLedgerAgeRow> rows = rowsCaptor.getValue();
            assertEquals(1, rows.size());
            BuyerLedgerAgeRow row = rows.get(0);
            assertEquals("B1", row.getBillNo());
            assertEquals(billDate, row.getBillDate());
            assertEquals("1000", row.getBillAmount());
            assertEquals("Supplier A", row.getSupplierName());
            assertEquals(expectedDays, row.getDays());
        }
    }
}
