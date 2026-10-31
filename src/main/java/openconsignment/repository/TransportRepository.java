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
import openconsignment.model.TransportLedgerRow;

public class TransportRepository {

    private final Connection conn;

    @Inject
    public TransportRepository(Connection conn) {
        this.conn = conn;
    }

    public void saveTransport(String tranportName) throws SQLException {
        try {
            String sql = SqlLoader.load(SqlQuery.TRANSPORT_SAVE);
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, tranportName);
            ps.execute();
        } catch (SQLException ex) {
            Logger.getLogger(TransportRepository.class.getName()).log(Level.SEVERE, ex.toString(), ex);
            throw ex;
        }
    }

    public void deleteTransport(String transportName) throws SQLException {
        try {
            String sql = SqlLoader.load(SqlQuery.TRANSPORT_DELETE);
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, transportName);
            ps.execute();
        } catch (SQLException ex) {
            Logger.getLogger(TransportRepository.class.getName()).log(Level.SEVERE, ex.toString(), ex);
            throw ex;
        }
    }

    public void renameTransport(String oldName, String newName) throws SQLException {
        try {
            String sql = SqlLoader.load(SqlQuery.TRANSPORT_RENAME);
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, newName);
            ps.setString(2, oldName);
            ps.execute();
        } catch (SQLException ex) {
            Logger.getLogger(TransportRepository.class.getName()).log(Level.SEVERE, ex.toString(), ex);
            throw ex;
        }
    }

    public List<String> getAllTransports() throws SQLException {
        try {
            List<String> transportList = new ArrayList<>();
            ResultSet transportResultSet = conn.prepareStatement(SqlLoader.load(SqlQuery.TRANSPORT_GET_ALL))
                    .executeQuery();
            while (transportResultSet.next()) {
                transportList.add(transportResultSet.getString("name"));
            }
            return transportList;
        } catch (SQLException ex) {
            Logger.getLogger(TransportRepository.class.getName()).log(Level.SEVERE, ex.toString(), ex);
            throw ex;
        }
    }

    public List<TransportLedgerRow> getTransportReportFor(String transportName, String fromDate, String toDate)
            throws SQLException {
        try {
            List<TransportLedgerRow> result = new ArrayList<>();

            String sql = SqlLoader.load(SqlQuery.TRANSPORT_REPORT);

            PreparedStatement ps = conn.prepareStatement(sql);
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
            Logger.getLogger(TransportRepository.class.getName()).log(Level.SEVERE, ex.toString(), ex);
            throw ex;
        }
    }
}
