package openconsignment.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SupplierRepositoryTest {

    private Connection conn;
    private SupplierRepository supplierRepository;

    @BeforeEach
    void setUp() throws Exception {
        conn = RepositoryTestSupport.newInMemoryConnection();
        supplierRepository = new SupplierRepository(conn);
    }

    @AfterEach
    void tearDown() throws SQLException {
        conn.close();
    }

    @Test
    void saveAndGetAllSuppliersOrdersCaseInsensitively() throws SQLException {
        supplierRepository.saveSupplier("banana Traders");
        supplierRepository.saveSupplier("Apple Traders");

        assertEquals(List.of("Apple Traders", "banana Traders"), supplierRepository.getAllSuppliers());
    }

    @Test
    void deleteSupplierRemovesIt() throws SQLException {
        supplierRepository.saveSupplier("Supplier A");

        supplierRepository.deleteSupplier("Supplier A");

        assertEquals(List.of(), supplierRepository.getAllSuppliers());
    }

    @Test
    void renameSupplierUpdatesName() throws SQLException {
        supplierRepository.saveSupplier("Old Name");

        supplierRepository.renameSupplier("Old Name", "New Name");

        assertEquals(List.of("New Name"), supplierRepository.getAllSuppliers());
    }
}
