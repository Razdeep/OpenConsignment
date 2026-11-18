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
import openconsignment.entity.BillEntity;
import openconsignment.entity.LREntity;

@Slf4j
public class BillRepository {

    private final DatabaseManager databaseManager;

    @Inject
    public BillRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void save(BillEntity billEntity) throws SQLException {
        try {
            String sql = SqlLoader.load(SqlQuery.BILL_SAVE);

            databaseManager.getConnection().setAutoCommit(false);

            deleteBill(billEntity.getBillNo());

            PreparedStatement preparedStatement =
                    databaseManager.getConnection().prepareStatement(sql);
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
                    sql = SqlLoader.load(SqlQuery.BILL_SAVE_LR);
                    preparedStatement = databaseManager.getConnection().prepareStatement(sql);
                    preparedStatement.setString(1, billEntity.getBillNo());
                    preparedStatement.setString(2, lr.getLrNo());
                    preparedStatement.setString(3, lr.getPm());
                    preparedStatement.execute();
                }
            }
            databaseManager.getConnection().commit();
        } catch (SQLException ex) {
            databaseManager.getConnection().rollback();
            log.error(ex.toString(), ex);
            throw ex;
        }
    }

    public BillEntity getBill(String billNo) throws SQLException {
        try {
            String sql = SqlLoader.load(SqlQuery.BILL_GET);
            PreparedStatement preparedStatement =
                    databaseManager.getConnection().prepareStatement(sql);
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

            sql = SqlLoader.load(SqlQuery.BILL_GET_LRS);
            preparedStatement = databaseManager.getConnection().prepareStatement(sql);
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
            log.error(ex.toString(), ex);
            throw ex;
        }
    }

    public void deleteBill(String billNo) throws SQLException {
        try {
            databaseManager.getConnection().setAutoCommit(false);
            String sql = SqlLoader.load(SqlQuery.BILL_DELETE);
            PreparedStatement preparedStatement =
                    databaseManager.getConnection().prepareStatement(sql);
            preparedStatement.setString(1, billNo);
            preparedStatement.execute();

            String sql2 = SqlLoader.load(SqlQuery.BILL_DELETE_LRS);
            preparedStatement = databaseManager.getConnection().prepareStatement(sql2);
            preparedStatement.setString(1, billNo);
            preparedStatement.execute();

            databaseManager.getConnection().commit();
        } catch (SQLException ex) {
            databaseManager.getConnection().rollback();
            log.error(ex.toString(), ex);
            throw ex;
        }
    }

    public List<BillEntity> getBills() throws SQLException {
        List<BillEntity> res = new ArrayList<>();
        try {
            String sql = SqlLoader.load(SqlQuery.BILL_GET_ALL);
            PreparedStatement ps = databaseManager.getConnection().prepareStatement(sql);
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
            log.error(ex.toString(), ex);
            throw ex;
        }
    }
}
