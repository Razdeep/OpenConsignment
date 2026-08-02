package openconsignment.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import openconsignment.entity.BillEntity;
import openconsignment.entity.CollectionEntity;
import openconsignment.entity.CollectionItemEntity;
import openconsignment.model.BuyerLedgerRow;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CollectionRepositoryTest {

    private Connection conn;
    private CollectionRepository collectionRepository;
    private BillRepository billRepository;

    @BeforeEach
    void setUp() throws Exception {
        conn = RepositoryTestSupport.newInMemoryConnection();
        collectionRepository = new CollectionRepository(conn);
        billRepository = new BillRepository(conn);
    }

    @AfterEach
    void tearDown() throws SQLException {
        conn.close();
    }

    @Test
    void fetchPendingBillsForBuyerExcludesFullyCollectedBills() throws SQLException {
        saveBill("B1", "Buyer A", "1000");
        saveBill("B2", "Buyer A", "500");
        collectionRepository.saveCollection(collectionWith(
                "V1", "Buyer A", "400", collectionItem("B1", "1000", "400"), collectionItem("B2", "500", "500")));

        List<BillEntity> pending = collectionRepository.fetchPendingBillsForBuyer("Buyer A");

        assertEquals(1, pending.size());
        assertEquals("B1", pending.get(0).getBillNo());
    }

    @Test
    void fetchPendingAmountForBillNoReturnsBillAmountMinusCollected() throws SQLException {
        saveBill("B1", "Buyer A", "1000");
        collectionRepository.saveCollection(
                collectionWith("V1", "Buyer A", "400", collectionItem("B1", "1000", "400")));

        assertEquals(600, collectionRepository.fetchPendingAmountForBillNo("B1"));
    }

    @Test
    void saveAndGetCollectionRoundTripsWithItems() throws SQLException {
        saveBill("B1", "Buyer A", "1000");
        collectionRepository.saveCollection(
                collectionWith("V1", "Buyer A", "1000", collectionItem("B1", "1000", "1000")));

        CollectionEntity result = collectionRepository.getCollection("V1");

        assertEquals("V1", result.getVoucherNo());
        assertEquals("Buyer A", result.getBuyerName());
        assertEquals("1000", result.getTotalAmount());
        assertEquals(1, result.getItems().size());
        assertEquals("B1", result.getItems().get(0).getBillNo());
        assertEquals("1000", result.getItems().get(0).getAmountCollected());
    }

    @Test
    void getLastVoucherIsALexicographicMaxOnTheTextColumn() throws SQLException {
        collectionRepository.saveCollection(collectionWith("V10", "Buyer A", "0"));
        collectionRepository.saveCollection(collectionWith("V9", "Buyer A", "0"));

        // "V9" > "V10" lexicographically ('9' > '1'), even though 10 > 9 numerically —
        // documents the real behavior of MAX() on a TEXT voucher-number column.
        assertEquals("V9", collectionRepository.getLastVoucher());
    }

    @Test
    void deleteCollectionRemovesItFromCollections() throws SQLException {
        collectionRepository.saveCollection(collectionWith("V1", "Buyer A", "0"));

        collectionRepository.deleteCollection("V1");

        assertTrue(collectionRepository.getCollections().isEmpty());
    }

    @Test
    void getCollectionsReturnsAllSavedCollections() throws SQLException {
        collectionRepository.saveCollection(collectionWith("V1", "Buyer A", "0"));
        collectionRepository.saveCollection(collectionWith("V2", "Buyer B", "0"));

        assertEquals(2, collectionRepository.getCollections().size());
    }

    @Test
    void getCollectionDetailsForBuyerJoinsBillAndCollectionRows() throws SQLException {
        saveBill("B1", "Buyer A", "1000");
        collectionRepository.saveCollection(
                collectionWith("V1", "Buyer A", "1000", collectionItem("B1", "1000", "400")));

        List<BuyerLedgerRow> rows = collectionRepository.getCollectionDetailsForBuyer("Buyer A");

        assertEquals(1, rows.size());
        BuyerLedgerRow row = rows.get(0);
        assertEquals("B1", row.getBillNo());
        assertEquals("V1", row.getVoucherNo());
        assertEquals(1000, row.getBillAmount());
        assertEquals(400, row.getAmountCollected());
        assertEquals("Supplier A", row.getSupplierName());
    }

    private void saveBill(String billNo, String buyerName, String billAmount) throws SQLException {
        billRepository.save(BillEntity.builder()
                .billNo(billNo)
                .supplierName("Supplier A")
                .buyerName(buyerName)
                .billDate("01-01-2026")
                .transport("Transport A")
                .lrDate("01-01-2026")
                .billAmount(billAmount)
                .build());
    }

    private static CollectionEntity collectionWith(
            String voucherNo, String buyerName, String totalAmount, CollectionItemEntity... items) {
        return CollectionEntity.builder()
                .voucherNo(voucherNo)
                .voucherDate("01-01-2026")
                .buyerName(buyerName)
                .totalAmount(totalAmount)
                .items(List.of(items))
                .build();
    }

    private static CollectionItemEntity collectionItem(String billNo, String billAmount, String amountCollected) {
        return new CollectionItemEntity(
                billNo, "01-01-2026", billAmount, "Supplier A", "0", amountCollected, "", "", "");
    }
}
