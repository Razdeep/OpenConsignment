package openconsignment.service;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.sf.jasperreports.engine.*;
import openconsignment.common.Utils;
import openconsignment.entity.BillEntity;
import openconsignment.entity.CollectionEntity;
import openconsignment.entity.CollectionItemEntity;
import openconsignment.model.BuyerLedgerRow;
import openconsignment.repository.CollectionRepository;

@Singleton
public class CollectionService {

    private final CollectionRepository collectionRepository;

    @Inject
    public CollectionService(CollectionRepository collectionRepository) {
        this.collectionRepository = collectionRepository;
    }

    public List<String> fetchPendingBillNosForBuyer(String buyerName) throws SQLException {
        return collectionRepository.fetchPendingBillsForBuyer(buyerName).stream()
                .map(BillEntity::getBillNo)
                .toList();
    }

    public List<BillEntity> fetchPendingBillsForBuyer(String buyerName) throws SQLException {
        return collectionRepository.fetchPendingBillsForBuyer(buyerName);
    }

    public int fetchPendingAmountForBillNo(String billNo) throws SQLException {
        return collectionRepository.fetchPendingAmountForBillNo(billNo);
    }

    public void saveCollection(CollectionEntity collectionEntity) throws SQLException {
        collectionRepository.saveCollection(collectionEntity);
    }

    public String getLastVoucher() throws SQLException {
        return collectionRepository.getLastVoucher();
    }

    public CollectionEntity getCollection(String voucherNo) throws SQLException {
        return collectionRepository.getCollection(voucherNo);
    }

    public void deleteCollection(String voucherNo) throws SQLException {
        collectionRepository.deleteCollection(voucherNo);
    }

    public List<CollectionEntity> getCollections() throws SQLException {
        return collectionRepository.getCollections();
    }

    public List<BuyerLedgerRow> getCollectionDetailsForBuyer(String buyerName) throws SQLException {
        return collectionRepository.getCollectionDetailsForBuyer(buyerName);
    }

    public void generatePdf(String voucherNo) throws SQLException, JRException {

        CollectionEntity collectionEntity = getCollection(voucherNo);

        Map<String, Object> payload = new HashMap<>();

        payload.put("voucherNo", voucherNo);
        payload.put("voucherDate", collectionEntity.getVoucherDate());
        payload.put("buyerName", collectionEntity.getBuyerName());
        payload.put("billAmount", collectionEntity.getTotalAmount());

        List<CollectionItemEntity> dataRows = collectionEntity.getItems();

        Utils.generatePdf("Collection.jrxml", payload, dataRows);
    }
}
