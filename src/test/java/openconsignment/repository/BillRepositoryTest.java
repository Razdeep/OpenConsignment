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
import openconsignment.entity.LREntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BillRepositoryTest {

    private Connection conn;
    private BillRepository billRepository;

    @BeforeEach
    void setUp() throws Exception {
        conn = RepositoryTestSupport.newInMemoryConnection();
        billRepository = new BillRepository(new DatabaseManager(conn));
    }

    @AfterEach
    void tearDown() throws SQLException {
        conn.close();
    }

    @Test
    void saveAndGetBillRoundTripsWithLrEntities() throws SQLException {
        billRepository.save(BillEntity.builder()
                .billNo("B1")
                .supplierName("Supplier A")
                .buyerName("Buyer A")
                .billDate("01-01-2026")
                .transport("Transport A")
                .lrDate("02-01-2026")
                .billAmount("1000")
                .lrEntities(List.of(new LREntity("B1", "LR1", "PM1")))
                .build());

        BillEntity result = billRepository.getBill("B1");

        assertEquals("B1", result.getBillNo());
        assertEquals("Supplier A", result.getSupplierName());
        assertEquals("Buyer A", result.getBuyerName());
        assertEquals("01-01-2026", result.getBillDate());
        assertEquals("Transport A", result.getTransport());
        assertEquals("02-01-2026", result.getLrDate());
        assertEquals("1000", result.getBillAmount());
        assertEquals(1, result.getLrEntities().size());
        assertEquals("LR1", result.getLrEntities().get(0).getLrNo());
        assertEquals("PM1", result.getLrEntities().get(0).getPm());
    }

    @Test
    void saveWithExistingBillNoUpsertsFields() throws SQLException {
        billRepository.save(BillEntity.builder()
                .billNo("B1")
                .supplierName("Supplier A")
                .buyerName("Buyer A")
                .billDate("01-01-2026")
                .transport("Transport A")
                .lrDate("02-01-2026")
                .billAmount("1000")
                .build());

        billRepository.save(BillEntity.builder()
                .billNo("B1")
                .supplierName("Supplier B")
                .buyerName("Buyer B")
                .billDate("05-01-2026")
                .transport("Transport B")
                .lrDate("06-01-2026")
                .billAmount("2000")
                .build());

        BillEntity result = billRepository.getBill("B1");

        assertEquals("Supplier B", result.getSupplierName());
        assertEquals("Buyer B", result.getBuyerName());
        assertEquals("05-01-2026", result.getBillDate());
        assertEquals("Transport B", result.getTransport());
        assertEquals("06-01-2026", result.getLrDate());
        assertEquals("2000", result.getBillAmount());
        assertEquals(1, billRepository.getBills().size());
    }

    @Test
    void replacingBillReplacesItsLrsWithoutAffectingOtherBills() throws SQLException {
        billRepository.save(bill("B1", "1000", List.of(new LREntity("B1", "old", "1"))));
        billRepository.save(bill("B2", "500", List.of(new LREntity("B2", "other", "2"))));
        billRepository.save(bill("B1", "2000", List.of(new LREntity("B1", "new", "3"))));

        BillEntity replaced = billRepository.getBill("B1");
        assertEquals("2000", replaced.getBillAmount());
        assertEquals(1, replaced.getLrEntities().size());
        assertEquals("new", replaced.getLrEntities().get(0).getLrNo());
        assertEquals(
                "other", billRepository.getBill("B2").getLrEntities().get(0).getLrNo());

        billRepository.save(bill("B1", "2000", List.of()));
        assertTrue(billRepository.getBill("B1").getLrEntities().isEmpty());
    }

    @Test
    void failedReplacementRestoresBillAndOriginalLrs() throws SQLException {
        billRepository.save(bill("B1", "1000", List.of(new LREntity("B1", "old", "1"))));
        assertThrows(
                SQLException.class,
                () -> billRepository.save(bill(
                        "B1", "2000", List.of(new LREntity("B1", "new", "2"), new LREntity("B1", "invalid", null)))));

        BillEntity restored = billRepository.getBill("B1");
        assertEquals("1000", restored.getBillAmount());
        assertEquals(1, restored.getLrEntities().size());
        assertEquals("old", restored.getLrEntities().get(0).getLrNo());
        assertEquals("1", restored.getLrEntities().get(0).getPm());
        assertTrue(conn.getAutoCommit());
    }

    @Test
    void failedLrDeletionRollsBackBillDeletion() throws SQLException {
        billRepository.save(bill("B1", "1000", List.of(new LREntity("B1", "old", "1"))));
        try (var statement = conn.createStatement()) {
            statement.execute("CREATE TRIGGER fail_lr_delete BEFORE DELETE ON LR_Table "
                    + "BEGIN SELECT RAISE(ABORT, 'delete failed'); END");
        }
        assertThrows(SQLException.class, () -> billRepository.deleteBill("B1"));
        assertEquals("old", billRepository.getBill("B1").getLrEntities().get(0).getLrNo());
        assertTrue(conn.getAutoCommit());
    }

    @Test
    void deleteBillRemovesBillAndLrRows() throws SQLException {
        billRepository.save(BillEntity.builder()
                .billNo("B1")
                .supplierName("Supplier A")
                .buyerName("Buyer A")
                .billDate("01-01-2026")
                .transport("Transport A")
                .lrDate("02-01-2026")
                .billAmount("1000")
                .lrEntities(List.of(new LREntity("B1", "LR1", "PM1")))
                .build());

        billRepository.deleteBill("B1");

        assertEquals(List.of(), billRepository.getBills());
        assertNull(billRepository.getBill("B1"));
    }

    @Test
    void getBillsReturnsAllSavedBills() throws SQLException {
        billRepository.save(BillEntity.builder()
                .billNo("B1")
                .supplierName("Supplier A")
                .buyerName("Buyer A")
                .billDate("01-01-2026")
                .transport("Transport A")
                .lrDate("02-01-2026")
                .billAmount("1000")
                .build());
        billRepository.save(BillEntity.builder()
                .billNo("B2")
                .supplierName("Supplier B")
                .buyerName("Buyer B")
                .billDate("03-01-2026")
                .transport("Transport B")
                .lrDate("04-01-2026")
                .billAmount("2000")
                .build());

        List<BillEntity> bills = billRepository.getBills();

        assertEquals(2, bills.size());
    }

    private static BillEntity bill(String billNo, String amount, List<LREntity> lrs) {
        return BillEntity.builder()
                .billNo(billNo)
                .supplierName("Supplier A")
                .buyerName("Buyer A")
                .billDate("01-01-2026")
                .transport("Transport A")
                .lrDate("02-01-2026")
                .billAmount(amount)
                .lrEntities(lrs)
                .build();
    }
}
