package openconsignment.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
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
        billRepository = new BillRepository(conn);
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
}
