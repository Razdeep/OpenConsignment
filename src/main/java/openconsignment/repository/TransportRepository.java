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
import openconsignment.model.TransportLedgerRow;

@Slf4j
public class TransportRepository {

    private final DatabaseManager databaseManager;

    @Inject
    public TransportRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void saveTransport(String transportName) throws SQLException {
        try {
            String sql = SqlLoader.load(SqlQuery.TRANSPORT_SAVE);
            PreparedStatement ps = databaseManager.getConnection().prepareStatement(sql);
            ps.setString(1, transportName);
            ps.execute();
        } catch (SQLException ex) {
            log.error(ex.toString(), ex);
            throw ex;
        }
    }

    public void deleteTransport(String transportName) throws SQLException {
        try {
            String sql = SqlLoader.load(SqlQuery.TRANSPORT_DELETE);
            PreparedStatement ps = databaseManager.getConnection().prepareStatement(sql);
            ps.setString(1, transportName);
            ps.execute();
        } catch (SQLException ex) {
            log.error(ex.toString(), ex);
            throw ex;
        }
    }

    public void renameTransport(String oldName, String newName) throws SQLException {
        try {
            String sql = SqlLoader.load(SqlQuery.TRANSPORT_RENAME);
            PreparedStatement ps = databaseManager.getConnection().prepareStatement(sql);
            ps.setString(1, newName);
            ps.setString(2, oldName);
            ps.execute();
        } catch (SQLException ex) {
            log.error(ex.toString(), ex);
            throw ex;
        }
    }

    public List<String> getAllTransports() throws SQLException {
        try {
            List<String> transportList = new ArrayList<>();
            ResultSet transportResultSet = databaseManager
                    .getConnection()
                    .prepareStatement(SqlLoader.load(SqlQuery.TRANSPORT_GET_ALL))
                    .executeQuery();
            while (transportResultSet.next()) {
                transportList.add(transportResultSet.getString("name"));
            }
            return transportList;
        } catch (SQLException ex) {
            log.error(ex.toString(), ex);
            throw ex;
        }
    }

    public List<TransportLedgerRow> getTransportReportFor(String transportName, String fromDate, String toDate)
            throws SQLException {
        try {
            List<TransportLedgerRow> result = new ArrayList<>();

            String sql = SqlLoader.load(SqlQuery.TRANSPORT_REPORT);

            PreparedStatement ps = databaseManager.getConnection().prepareStatement(sql);
            ps.setString(1, transportName);
            ps.setString(2, fromDate);
            ps.setString(3, fromDate);
            ps.setString(4, fromDate);
            ps.setString(5, toDate);
            ps.setString(6, toDate);
            ps.setString(7, toDate);

            ResultSet transportResultSet = ps.executeQuery();

            while (transportResultSet.next()) {
                result.add(TransportLedgerRow.builder()
                        .lr(transportResultSet.getString("LR No."))
                        .pm(transportResultSet.getString("PM"))
                        .billNo(transportResultSet.getString("Bill No."))
                        .supplierName(transportResultSet.getString("Supplier Name"))
                        .buyerName(transportResultSet.getString("Buyer Name"))
                        .lrDate(transportResultSet.getString("LR Date"))
                        .build());
            }
            return result;
        } catch (SQLException ex) {
            log.error(ex.toString(), ex);
            throw ex;
        }
    }
}
