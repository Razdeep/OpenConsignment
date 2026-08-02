package openconsignment.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import openconsignment.entity.BillEntity;
import openconsignment.entity.LREntity;
import openconsignment.model.TransportLedgerRow;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TransportRepositoryTest {

    private Connection conn;
    private TransportRepository transportRepository;
    private BillRepository billRepository;

    @BeforeEach
    void setUp() throws Exception {
        conn = RepositoryTestSupport.newInMemoryConnection();
        transportRepository = new TransportRepository(conn);
        billRepository = new BillRepository(conn);
    }

    @AfterEach
    void tearDown() throws SQLException {
        conn.close();
    }

    @Test
    void saveAndGetAllTransportsOrdersCaseInsensitively() throws SQLException {
        transportRepository.saveTransport("banana Carriers");
        transportRepository.saveTransport("Apple Carriers");

        assertEquals(List.of("Apple Carriers", "banana Carriers"), transportRepository.getAllTransports());
    }

    @Test
    void deleteTransportRemovesIt() throws SQLException {
        transportRepository.saveTransport("Transport A");

        transportRepository.deleteTransport("Transport A");

        assertEquals(List.of(), transportRepository.getAllTransports());
    }

    @Test
    void renameTransportUpdatesName() throws SQLException {
        transportRepository.saveTransport("Old Name");

        transportRepository.renameTransport("Old Name", "New Name");

        assertEquals(List.of("New Name"), transportRepository.getAllTransports());
    }

    @Test
    void getTransportReportForFiltersByTransportAndDateRange() throws SQLException {
        saveBill("B1", "Transport A", "15-01-2026");
        saveBill("B2", "Transport A", "15-02-2026");
        saveBill("B3", "Transport B", "15-01-2026");

        List<TransportLedgerRow> rows =
                transportRepository.getTransportReportFor("Transport A", "01-01-2026", "31-01-2026");

        assertEquals(1, rows.size());
        TransportLedgerRow row = rows.get(0);
        assertEquals("B1", row.getBillNo());
        assertEquals("LR-B1", row.getLr());
        assertEquals("PM-B1", row.getPm());
        assertEquals("Supplier A", row.getSupplierName());
        assertEquals("Buyer A", row.getBuyerName());
        assertEquals("15-01-2026", row.getLrDate());
    }

    private void saveBill(String billNo, String transport, String lrDate) throws SQLException {
        billRepository.save(BillEntity.builder()
                .billNo(billNo)
                .supplierName("Supplier A")
                .buyerName("Buyer A")
                .billDate(lrDate)
                .transport(transport)
                .lrDate(lrDate)
                .billAmount("1000")
                .lrEntities(List.of(new LREntity(billNo, "LR-" + billNo, "PM-" + billNo)))
                .build());
    }
}
