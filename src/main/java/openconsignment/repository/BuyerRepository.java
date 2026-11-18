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
public class BuyerRepository {

    private final DatabaseManager databaseManager;

    @Inject
    public BuyerRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void saveBuyer(String buyerName) throws SQLException {
        try {
            String sql = SqlLoader.load(SqlQuery.BUYER_SAVE);
            PreparedStatement ps = databaseManager.getConnection().prepareStatement(sql);
            ps.setString(1, buyerName);
            ps.execute();
        } catch (SQLException ex) {
            log.error(ex.toString(), ex);
            throw ex;
        }
    }

    public void deleteBuyer(String buyerName) throws SQLException {
        try {
            String sql = SqlLoader.load(SqlQuery.BUYER_DELETE);
            PreparedStatement ps = databaseManager.getConnection().prepareStatement(sql);
            ps.setString(1, buyerName);
            ps.execute();
        } catch (SQLException ex) {
            log.error(ex.toString(), ex);
            throw ex;
        }
    }

    public void renameBuyer(String oldName, String newName) throws SQLException {
        try {
            String sql = SqlLoader.load(SqlQuery.BUYER_RENAME);
            PreparedStatement ps = databaseManager.getConnection().prepareStatement(sql);
            ps.setString(1, newName);
            ps.setString(2, oldName);
            ps.execute();
        } catch (SQLException ex) {
            log.error(ex.toString(), ex);
            throw ex;
        }
    }

    public List<String> getAllBuyers() throws SQLException {
        try {
            List<String> buyerList = new ArrayList<>();
            ResultSet buyerResultSet = databaseManager
                    .getConnection()
                    .prepareStatement(SqlLoader.load(SqlQuery.BUYER_GET_ALL))
                    .executeQuery();
            while (buyerResultSet.next()) {
                buyerList.add(buyerResultSet.getString("name"));
            }
            return buyerList;
        } catch (SQLException ex) {
            log.error(ex.toString(), ex);
            throw ex;
        }
    }
}
