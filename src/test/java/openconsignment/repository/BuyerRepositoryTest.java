package openconsignment.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BuyerRepositoryTest {

    private Connection conn;
    private BuyerRepository buyerRepository;

    @BeforeEach
    void setUp() throws Exception {
        conn = RepositoryTestSupport.newInMemoryConnection();
        buyerRepository = new BuyerRepository(conn);
    }

    @AfterEach
    void tearDown() throws SQLException {
        conn.close();
    }

    @Test
    void saveAndGetAllBuyersOrdersCaseInsensitively() throws SQLException {
        buyerRepository.saveBuyer("banana Traders");
        buyerRepository.saveBuyer("Apple Traders");

        assertEquals(List.of("Apple Traders", "banana Traders"), buyerRepository.getAllBuyers());
    }

    @Test
    void deleteBuyerRemovesIt() throws SQLException {
        buyerRepository.saveBuyer("Buyer A");

        buyerRepository.deleteBuyer("Buyer A");

        assertEquals(List.of(), buyerRepository.getAllBuyers());
    }

    @Test
    void renameBuyerUpdatesName() throws SQLException {
        buyerRepository.saveBuyer("Old Name");

        buyerRepository.renameBuyer("Old Name", "New Name");

        assertEquals(List.of("New Name"), buyerRepository.getAllBuyers());
    }
}
