package openconsignment.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import openconsignment.common.SqlLoader;
import openconsignment.common.SqlQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SupplierRepository {
    private static final Logger logger = LoggerFactory.getLogger(SupplierRepository.class);

    private final Connection conn;

    @Inject
    public SupplierRepository(Connection conn) {
        this.conn = conn;
    }

    public void saveSupplier(String supplierName) throws SQLException {
        try {
            String sql = SqlLoader.load(SqlQuery.SUPPLIER_SAVE);
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, supplierName);
            ps.execute();
        } catch (SQLException ex) {
            logger.error(ex.toString(), ex);
            throw ex;
        }
    }

    public void deleteSupplier(String supplierName) throws SQLException {
        try {
            String sql = SqlLoader.load(SqlQuery.SUPPLIER_DELETE);
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, supplierName);
            ps.execute();
        } catch (SQLException ex) {
            logger.error(ex.toString(), ex);
            throw ex;
        }
    }

    public void renameSupplier(String oldName, String newName) throws SQLException {
        try {
            String sql = SqlLoader.load(SqlQuery.SUPPLIER_RENAME);
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, newName);
            ps.setString(2, oldName);
            ps.execute();
        } catch (SQLException ex) {
            logger.error(ex.toString(), ex);
            throw ex;
        }
    }

    public List<String> getAllSuppliers() throws SQLException {
        try {
            List<String> supplierList = new ArrayList<>();
            ResultSet supplierResultSet = conn.prepareStatement(SqlLoader.load(SqlQuery.SUPPLIER_GET_ALL))
                    .executeQuery();
            while (supplierResultSet.next()) {
                supplierList.add(supplierResultSet.getString("name"));
            }
            return supplierList;
        } catch (SQLException ex) {
            logger.error(ex.toString(), ex);
            throw ex;
        }
    }
}
