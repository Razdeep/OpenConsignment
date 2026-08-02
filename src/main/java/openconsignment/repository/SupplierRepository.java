package openconsignment.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;

public class SupplierRepository {

    private final Connection conn;

    @Inject
    public SupplierRepository(Connection conn) {
        this.conn = conn;
    }

    public void saveSupplier(String supplierName) throws SQLException {
        try {
            String sql = "INSERT INTO `Supplier_Master_Table`(`Name`) VALUES (?);";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, supplierName);
            ps.execute();
        } catch (SQLException ex) {
            Logger.getLogger(SupplierRepository.class.getName()).log(Level.SEVERE, ex.toString(), ex);
            throw ex;
        }
    }

    public void deleteSupplier(String supplierName) throws SQLException {
        try {
            String sql = "DELETE FROM `Supplier_Master_Table` WHERE name=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, supplierName);
            ps.execute();
        } catch (SQLException ex) {
            Logger.getLogger(SupplierRepository.class.getName()).log(Level.SEVERE, ex.toString(), ex);
            throw ex;
        }
    }

    public void renameSupplier(String oldName, String newName) throws SQLException {
        try {
            String sql = "UPDATE `Supplier_Master_Table` SET Name=? WHERE Name=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, newName);
            ps.setString(2, oldName);
            ps.execute();
        } catch (SQLException ex) {
            Logger.getLogger(SupplierRepository.class.getName()).log(Level.SEVERE, ex.toString(), ex);
            throw ex;
        }
    }

    public List<String> getAllSuppliers() throws SQLException {
        try {
            List<String> supplierList = new ArrayList<>();
            ResultSet supplierResultSet = conn.prepareStatement(
                            "select * from Supplier_Master_Table order by name collate nocase")
                    .executeQuery();
            while (supplierResultSet.next()) {
                supplierList.add(supplierResultSet.getString("name"));
            }
            return supplierList;
        } catch (SQLException ex) {
            Logger.getLogger(SupplierRepository.class.getName()).log(Level.SEVERE, ex.toString(), ex);
            throw ex;
        }
    }
}
