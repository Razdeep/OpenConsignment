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
import openconsignment.common.SqlLoader;
import openconsignment.common.SqlQuery;

public class BuyerRepository {

    private final Connection conn;

    @Inject
    public BuyerRepository(Connection conn) {
        this.conn = conn;
    }

    public void saveBuyer(String buyerName) throws SQLException {
        try {
            String sql = SqlLoader.load(SqlQuery.BUYER_SAVE);
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, buyerName);
            ps.execute();
        } catch (SQLException ex) {
            Logger.getLogger(BuyerRepository.class.getName()).log(Level.SEVERE, ex.toString(), ex);
            throw ex;
        }
    }

    public void deleteBuyer(String buyerName) throws SQLException {
        try {
            String sql = SqlLoader.load(SqlQuery.BUYER_DELETE);
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, buyerName);
            ps.execute();
        } catch (SQLException ex) {
            Logger.getLogger(BuyerRepository.class.getName()).log(Level.SEVERE, ex.toString(), ex);
            throw ex;
        }
    }

    public void renameBuyer(String oldName, String newName) throws SQLException {
        try {
            String sql = SqlLoader.load(SqlQuery.BUYER_RENAME);
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, newName);
            ps.setString(2, oldName);
            ps.execute();
        } catch (SQLException ex) {
            Logger.getLogger(BuyerRepository.class.getName()).log(Level.SEVERE, ex.toString(), ex);
            throw ex;
        }
    }

    public List<String> getAllBuyers() throws SQLException {
        try {
            List<String> buyerList = new ArrayList<>();
            ResultSet buyerResultSet = conn.prepareStatement(SqlLoader.load(SqlQuery.BUYER_GET_ALL))
                    .executeQuery();
            while (buyerResultSet.next()) {
                buyerList.add(buyerResultSet.getString("name"));
            }
            return buyerList;
        } catch (SQLException ex) {
            Logger.getLogger(BuyerRepository.class.getName()).log(Level.SEVERE, ex.toString(), ex);
            throw ex;
        }
    }
}
