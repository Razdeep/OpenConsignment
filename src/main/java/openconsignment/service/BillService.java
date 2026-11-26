package openconsignment.service;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.sf.jasperreports.engine.JRException;
import openconsignment.common.AppConfig;
import openconsignment.common.Constants;
import openconsignment.common.Utils;
import openconsignment.entity.BillEntity;
import openconsignment.entity.LREntity;
import openconsignment.repository.BillRepository;

@Singleton
public class BillService {

    private final BillRepository billRepository;
    private final AppConfig appConfig;

    @Inject
    public BillService(BillRepository billRepository, AppConfig appConfig) {
        this.billRepository = billRepository;
        this.appConfig = appConfig;
    }

    public void saveBill(BillEntity billEntity) throws SQLException {
        billRepository.save(billEntity);
    }

    public BillEntity getBill(String billNo) throws SQLException {
        return billRepository.getBill(billNo);
    }

    public void deleteBill(String billNo) throws SQLException {
        billRepository.deleteBill(billNo);
    }

    public List<BillEntity> getBills() throws SQLException {
        return billRepository.getBills();
    }

    public void generatePdf(String billNo) throws SQLException, JRException {
        BillEntity billEntity = getBill(billNo);

        Map<String, Object> payload = new HashMap<>();

        String organizationName =
                appConfig.getOrganization() == null ? "Invalid Organization" : appConfig.getOrganization();

        payload.put(Constants.ORGANIZATION_NAME_KEY, organizationName);
        payload.put("billNo", billNo);
        payload.put("billDate", billEntity.getBillDate());
        payload.put("buyerName", billEntity.getBuyerName());
        payload.put("supplierName", billEntity.getSupplierName());
        payload.put("transport", billEntity.getTransport());
        payload.put("lrDate", billEntity.getLrDate());
        payload.put("billAmount", billEntity.getBillAmount());

        List<LREntity> dataRows = billEntity.getLrEntities();

        Utils.generatePdf("Bill.jrxml", payload, dataRows);
    }
}
