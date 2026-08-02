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
import openconsignment.entity.CollectionEntity;
import openconsignment.entity.CollectionItemEntity;
import openconsignment.repository.CollectionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CollectionServiceTest {

    @Mock
    private CollectionRepository collectionRepository;

    @InjectMocks
    private CollectionService collectionService;

    @Test
    void fetchPendingBillNosForBuyerMapsBillEntitiesToBillNos() throws SQLException {
        List<BillEntity> bills = List.of(
                BillEntity.builder().billNo("B1").build(),
                BillEntity.builder().billNo("B2").build());
        when(collectionRepository.fetchPendingBillsForBuyer("Buyer A")).thenReturn(bills);

        assertEquals(List.of("B1", "B2"), collectionService.fetchPendingBillNosForBuyer("Buyer A"));
    }

    @Test
    void fetchPendingBillsForBuyerReturnsRepositoryResult() throws SQLException {
        List<BillEntity> bills = List.of(BillEntity.builder().billNo("B1").build());
        when(collectionRepository.fetchPendingBillsForBuyer("Buyer A")).thenReturn(bills);

        assertEquals(bills, collectionService.fetchPendingBillsForBuyer("Buyer A"));
    }

    @Test
    void fetchPendingAmountForBillNoReturnsRepositoryResult() throws SQLException {
        when(collectionRepository.fetchPendingAmountForBillNo("B1")).thenReturn(500);

        assertEquals(500, collectionService.fetchPendingAmountForBillNo("B1"));
    }

    @Test
    void saveCollectionDelegatesToRepository() throws SQLException {
        CollectionEntity collectionEntity =
                CollectionEntity.builder().voucherNo("V1").build();

        collectionService.saveCollection(collectionEntity);

        verify(collectionRepository).saveCollection(collectionEntity);
    }

    @Test
    void getLastVoucherReturnsRepositoryResult() throws SQLException {
        when(collectionRepository.getLastVoucher()).thenReturn("V1");

        assertEquals("V1", collectionService.getLastVoucher());
    }

    @Test
    void deleteCollectionDelegatesToRepository() throws SQLException {
        collectionService.deleteCollection("V1");

        verify(collectionRepository).deleteCollection("V1");
    }

    @Test
    void getCollectionsReturnsRepositoryResult() throws SQLException {
        List<CollectionEntity> collections =
                List.of(CollectionEntity.builder().voucherNo("V1").build());
        when(collectionRepository.getCollections()).thenReturn(collections);

        assertEquals(collections, collectionService.getCollections());
    }

    @Test
    void generatePdfBuildsPayloadAndForwardsItems() throws SQLException, JRException {
        List<CollectionItemEntity> items =
                List.of(new CollectionItemEntity("B1", "01-01-2026", "1000", "Supplier A", "0", "1000", "", "", ""));
        CollectionEntity collectionEntity = CollectionEntity.builder()
                .voucherNo("V1")
                .voucherDate("01-02-2026")
                .buyerName("Buyer A")
                .totalAmount("1000")
                .items(items)
                .build();
        when(collectionRepository.getCollection("V1")).thenReturn(collectionEntity);

        try (MockedStatic<Utils> utils = mockStatic(Utils.class)) {
            collectionService.generatePdf("V1");

            ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
            utils.verify(() -> Utils.generatePdf(eq("Collection.jrxml"), payloadCaptor.capture(), eq(items)));

            Map<String, Object> payload = payloadCaptor.getValue();
            assertEquals("V1", payload.get("voucherNo"));
            assertEquals("01-02-2026", payload.get("voucherDate"));
            assertEquals("Buyer A", payload.get("buyerName"));
            assertEquals("1000", payload.get("billAmount"));
        }
    }
}
