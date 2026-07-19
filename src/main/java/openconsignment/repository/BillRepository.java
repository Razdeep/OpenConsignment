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
import openconsignment.entity.BillEntity;
import openconsignment.entity.LREntity;

public class BillRepository {

    public void save(BillEntity billEntity) throws SQLException {
        Connection connection = DatabaseHandler.getInstance().getConnection();
        try {
            // language=sql
            String sql =
                    "INSERT INTO `Bill_Entry_Table`(`Supplier Name`,`Buyer Name`,`Bill No.`,`Bill Date`,`Transport`,`LR Date`,`Bill Amount`,`Collection Due`,`Due`) "
                            + "VALUES (?,?,?,?,?,?,?,?,?) ON CONFLICT(`Bill No.`) DO UPDATE SET "
                            + "`Supplier Name`=excluded.`Supplier Name`, `Buyer Name`=excluded.`Buyer Name`, `Bill Date`=excluded.`Bill Date`, "
                            + "`Transport`=excluded.`Transport`, `LR Date`=excluded.`LR Date`, `Bill Amount`=excluded.`Bill Amount`, "
                            + "`Collection Due`=excluded.`Collection Due`, `Due`=excluded.`Due`;";

            connection.setAutoCommit(false);

            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, billEntity.getSupplierName());
            preparedStatement.setString(2, billEntity.getBuyerName());
            preparedStatement.setString(3, billEntity.getBillNo());
            preparedStatement.setString(4, billEntity.getBillDate());
            preparedStatement.setString(5, billEntity.getTransport());
            preparedStatement.setString(6, billEntity.getLrDate());
            preparedStatement.setString(7, billEntity.getBillAmount());
            preparedStatement.setString(8, billEntity.getBillAmount());
            preparedStatement.setString(9, billEntity.getBillAmount());
            preparedStatement.execute();

            if (null != billEntity.getLrEntities()) {
                for (LREntity lr : billEntity.getLrEntities()) {
                    sql = "INSERT INTO `LR_Table`(`Bill No.`,`LR No.`,`PM`) VALUES (?,?,?)";
                    preparedStatement = connection.prepareStatement(sql);
                    preparedStatement.setString(1, billEntity.getBillNo());
                    preparedStatement.setString(2, lr.getLrNo());
                    preparedStatement.setString(3, lr.getPm());
                    preparedStatement.execute();
                }
            }
            connection.commit();
        } catch (SQLException ex) {
            connection.rollback();
            Logger.getLogger(BillRepository.class.getName()).log(Level.SEVERE, ex.toString(), ex);
            throw ex;
        }
    }

    public BillEntity getBill(String billNo) throws SQLException {
        try {
            Connection connection = DatabaseHandler.getInstance().getConnection();
            String sql = "select * from `Bill_Entry_Table` where `Bill No.`=? collate nocase";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, billNo);
            ResultSet billResultSet = preparedStatement.executeQuery();

            if (billResultSet.isClosed()) {
                return null;
            }

            BillEntity billEntity = BillEntity.builder()
                    .billNo(billResultSet.getString("Bill No."))
                    .supplierName(billResultSet.getString("Supplier Name"))
                    .buyerName(billResultSet.getString("Buyer Name"))
                    .billDate(billResultSet.getString("Bill Date"))
                    .transport(billResultSet.getString("Transport"))
                    .lrDate(billResultSet.getString("LR Date"))
                    .billAmount(billResultSet.getString("Bill Amount"))
                    .build();

            sql = "select * from `LR_Table` where `Bill No.`=? collate nocase";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, billNo);
            ResultSet lrPmResultSet = preparedStatement.executeQuery();

            List<LREntity> lrpmList = new ArrayList<>();
            while (lrPmResultSet.next()) {
                lrpmList.add(new LREntity(
                        lrPmResultSet.getString("Bill No."),
                        lrPmResultSet.getString("LR No."),
                        lrPmResultSet.getString("PM")));
            }

            billEntity.setLrEntities(lrpmList);
            return billEntity;
        } catch (SQLException ex) {
            Logger.getLogger(BillRepository.class.getName()).log(Level.SEVERE, ex.toString(), ex);
            throw ex;
        }
    }

    public void deleteBill(String billNo) throws SQLException {

        Connection connection = DatabaseHandler.getInstance().getConnection();

        try {
            connection.setAutoCommit(false);
            String sql = "DELETE FROM `Bill_Entry_Table` where `Bill No.`=? collate nocase";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, billNo);
            preparedStatement.execute();

            String sql2 = "DELETE FROM `LR_Table` where `Bill No.`=? collate nocase";
            preparedStatement = connection.prepareStatement(sql2);
            preparedStatement.setString(1, billNo);
            preparedStatement.execute();

            connection.commit();
        } catch (SQLException ex) {
            connection.rollback();
            Logger.getLogger(BillRepository.class.getName()).log(Level.SEVERE, ex.toString(), ex);
            throw ex;
        }
    }

    public List<BillEntity> getBills() throws SQLException {
        List<BillEntity> res = new ArrayList<>();
        try {
            String sql = "SELECT * FROM `Bill_Entry_Table`;";
            Connection conn = DatabaseHandler.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                res.add(BillEntity.builder()
                        .supplierName(rs.getString("Supplier Name"))
                        .buyerName(rs.getString("Buyer Name"))
                        .billNo(rs.getString("Bill No."))
                        .billDate(rs.getString("Bill Date"))
                        .transport(rs.getString("Transport"))
                        .lrDate(rs.getString("LR Date"))
                        .billAmount(rs.getString("Bill Amount"))
                        // .lrEntities() skipped as it is not required
                        .build());
            }
            return res;
        } catch (SQLException ex) {
            Logger.getLogger(BillRepository.class.getName()).log(Level.SEVERE, ex.toString(), ex);
            throw ex;
        }
    }
}
