package openconsignment.repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import openconsignment.common.SqlLoader;
import openconsignment.common.SqlQuery;
import openconsignment.di.DatabaseManager;

@Slf4j
public class SupplierRepository {

    private final DatabaseManager databaseManager;

    @Inject
    public SupplierRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void saveSupplier(String supplierName) throws SQLException {
        try {
            String sql = SqlLoader.load(SqlQuery.SUPPLIER_SAVE);
            PreparedStatement ps = databaseManager.getConnection().prepareStatement(sql);
            ps.setString(1, supplierName);
            ps.execute();
        } catch (SQLException ex) {
            log.error(ex.toString(), ex);
            throw ex;
        }
    }

    public void deleteSupplier(String supplierName) throws SQLException {
        try {
            String sql = SqlLoader.load(SqlQuery.SUPPLIER_DELETE);
            PreparedStatement ps = databaseManager.getConnection().prepareStatement(sql);
            ps.setString(1, supplierName);
            ps.execute();
        } catch (SQLException ex) {
            log.error(ex.toString(), ex);
            throw ex;
        }
    }

    public void renameSupplier(String oldName, String newName) throws SQLException {
        try {
            String sql = SqlLoader.load(SqlQuery.SUPPLIER_RENAME);
            PreparedStatement ps = databaseManager.getConnection().prepareStatement(sql);
            ps.setString(1, newName);
            ps.setString(2, oldName);
            ps.execute();
        } catch (SQLException ex) {
            log.error(ex.toString(), ex);
            throw ex;
        }
    }

    public List<String> getAllSuppliers() throws SQLException {
        try {
            List<String> supplierList = new ArrayList<>();
            ResultSet supplierResultSet = databaseManager
                    .getConnection()
                    .prepareStatement(SqlLoader.load(SqlQuery.SUPPLIER_GET_ALL))
                    .executeQuery();
            while (supplierResultSet.next()) {
                supplierList.add(supplierResultSet.getString("name"));
            }
            return supplierList;
        } catch (SQLException ex) {
            log.error(ex.toString(), ex);
            throw ex;
        }
    }
}
