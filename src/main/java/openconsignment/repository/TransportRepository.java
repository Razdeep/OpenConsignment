package openconsignment.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import openconsignment.common.DatabaseHandler;
import openconsignment.model.TransportLedgerRow;

public class TransportRepository {

    public void saveTransport(String tranportName) throws SQLException {
        try {
            String sql = "INSERT INTO `Transport_Master_Table`(`Name`) VALUES (?);";
            Connection conn = DatabaseHandler.getInstance().getConnection();
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
            String sql = "DELETE FROM `Transport_Master_Table` WHERE name=?";
            Connection conn = DatabaseHandler.getInstance().getConnection();
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
            String sql = "UPDATE `Transport_Master_Table` SET Name=? WHERE Name=?";
            Connection conn = DatabaseHandler.getInstance().getConnection();
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
            Connection connection = DatabaseHandler.getInstance().getConnection();
            ResultSet transportResultSet = connection
                    .prepareStatement("select * from Transport_Master_Table order by name collate nocase")
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
            Connection connection = DatabaseHandler.getInstance().getConnection();

            // language=sql
            String sql =
                    """
            select `LR No.`,`PM`,`Bill No.`,`Supplier Name`,`Buyer Name`,`LR Date`
            from `LR_Table` join `Bill_Entry_Table` using (`Bill No.`)
            where `Transport`=?
            and
            substr(`LR Date`, 7, 4)||substr(`LR Date`, 4, 2)||substr(`LR Date`, 1, 2)
            between
            substr(?, 7, 4)||substr(?, 4, 2)||substr(?, 1, 2)
            and
            substr(?, 7, 4)||substr(?, 4, 2)||substr(?, 1, 2)
            order by datetime(substr(`LR Date`, 7, 4)||'-'||substr(`LR Date`, 4, 2)||'-'||substr(`LR Date`, 1, 2))
""";

            PreparedStatement ps = connection.prepareStatement(sql);
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
