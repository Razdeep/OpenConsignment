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
        collectionRepository = new CollectionRepository(new DatabaseManager(conn));
        billRepository = new BillRepository(new DatabaseManager(conn));
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
    void balancesReflectMultipleCollectionsReplacementAndDeletion() throws SQLException {
        saveBill("B1", "Buyer A", "1000");
        saveBill("B2", "Buyer B", "2000");
        collectionRepository.saveCollection(
                collectionWith("V1", "Buyer A", "300", collectionItem("B1", "1000", "300")));
        collectionRepository.saveCollection(
                collectionWith("V2", "Buyer A", "200", collectionItem("B1", "1000", "200")));

        assertEquals(500, collectionRepository.fetchPendingAmountForBillNo("B1"));
        assertEquals(2000, collectionRepository.fetchPendingAmountForBillNo("B2"));
        assertEquals(
                List.of("B1"),
                collectionRepository.fetchPendingBillsForBuyer("Buyer A").stream()
                        .map(BillEntity::getBillNo)
                        .toList());

        collectionRepository.saveCollection(
                collectionWith("V2", "Buyer A", "700", collectionItem("B1", "1000", "700")));
        assertEquals(0, collectionRepository.fetchPendingAmountForBillNo("B1"));
        assertTrue(collectionRepository.fetchPendingBillsForBuyer("Buyer A").isEmpty());
        assertEquals(1, collectionRepository.getCollection("V2").getItems().size());

        collectionRepository.deleteCollection("V1");
        assertEquals(300, collectionRepository.fetchPendingAmountForBillNo("B1"));
        assertEquals(
                1, collectionRepository.fetchPendingBillsForBuyer("Buyer A").size());
        assertNull(collectionRepository.getCollection("V1"));
    }

    @Test
    void missingCollectionAndEmptyTablesReturnNoResults() throws SQLException {
        assertNull(collectionRepository.getLastVoucher());
        assertNull(collectionRepository.getCollection("missing"));
        assertTrue(collectionRepository.getCollections().isEmpty());
        assertTrue(collectionRepository.fetchPendingBillsForBuyer("missing").isEmpty());
    }

    @Test
    void invalidItemRollsBackNewVoucher() throws SQLException {
        CollectionItemEntity invalid = CollectionItemEntity.builder()
                .billNo("B1")
                .billDate("01-01-2026")
                .billAmount("1000")
                .due("750")
                .amountCollected("250")
                .bank("Bank")
                .ddNo("DD1")
                .ddDate("01-01-2026")
                .build();
        assertThrows(
                SQLException.class,
                () -> collectionRepository.saveCollection(collectionWith("V1", "Buyer A", "250", invalid)));
        assertNull(collectionRepository.getCollection("V1"));
        assertTrue(collectionRepository.getCollections().isEmpty());
        assertTrue(conn.getAutoCommit());
    }

    @Test
    void failedReplacementRestoresOriginalHeaderAndItems() throws SQLException {
        saveBill("B1", "Buyer A", "1000");
        collectionRepository.saveCollection(
                collectionWith("V1", "Buyer A", "300", collectionItem("B1", "1000", "300")));
        boolean originalAutoCommit = conn.getAutoCommit();
        CollectionItemEntity invalid =
                CollectionItemEntity.builder().billNo("B2").build();

        assertThrows(
                SQLException.class,
                () -> collectionRepository.saveCollection(
                        collectionWith("V1", "Buyer B", "500", collectionItem("B1", "1000", "200"), invalid)));

        CollectionEntity restored = collectionRepository.getCollection("V1");
        assertEquals("Buyer A", restored.getBuyerName());
        assertEquals("300", restored.getTotalAmount());
        assertEquals(1, restored.getItems().size());
        assertEquals("300", restored.getItems().get(0).getAmountCollected());
        assertEquals(700, collectionRepository.fetchPendingAmountForBillNo("B1"));
        assertEquals(originalAutoCommit, conn.getAutoCommit());
    }

    @Test
    void deleteFailureRollsBackHeaderAndPropagates() throws SQLException {
        collectionRepository.saveCollection(
                collectionWith("V1", "Buyer A", "300", collectionItem("B1", "1000", "300")));
        try (var statement = conn.createStatement()) {
            statement.execute("CREATE TRIGGER fail_collection_delete BEFORE DELETE ON Collection_Entry_Extended_Table "
                    + "BEGIN SELECT RAISE(ABORT, 'delete failed'); END");
        }
        assertThrows(SQLException.class, () -> collectionRepository.deleteCollection("V1"));
        assertEquals(
                "300",
                collectionRepository.getCollection("V1").getItems().get(0).getAmountCollected());
        assertTrue(conn.getAutoCommit());
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
