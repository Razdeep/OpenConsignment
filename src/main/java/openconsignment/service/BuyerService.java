package openconsignment.service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.jasperreports.engine.*;
import openconsignment.common.Utils;
import openconsignment.model.BuyerLedgerAgeRow;
import openconsignment.model.BuyerLedgerRow;
import openconsignment.repository.BuyerRepository;

public class BuyerService {

    private final BuyerRepository buyerRepository;
    private final CollectionService collectionService;

    public BuyerService(BuyerRepository buyerRepository, CollectionService collectionService) {
        this.buyerRepository = buyerRepository;
        this.collectionService = collectionService;
    }

    public List<String> getAllBuyers() throws SQLException {
        return buyerRepository.getAllBuyers();
    }

    public void saveBuyer(String buyerName) throws SQLException {
        buyerRepository.saveBuyer(buyerName);
    }

    public void deleteBuyer(String buyerName) throws SQLException {
        buyerRepository.deleteBuyer(buyerName);
    }

    public void renameBuyer(String oldName, String newName) throws SQLException {
        buyerRepository.renameBuyer(oldName, newName);
    }

    public void generatePdf(String buyerName, boolean isAgewise) throws SQLException, JRException {

        Map<String, Object> payload = new HashMap<>();
        payload.put("buyerName", buyerName);

        if (isAgewise) {
            String jrxmlFileName = "BuyerLedgerAge.jrxml";
            List<BuyerLedgerAgeRow> buyerLedgerAgeRows = collectionService.fetchPendingBillsForBuyer(buyerName).stream()
                    .map(it -> BuyerLedgerAgeRow.builder()
                            .billNo(it.getBillNo())
                            .billDate(it.getBillDate())
                            .billAmount(it.getBillAmount())
                            .supplierName(it.getSupplierName())
                            .days((int) ChronoUnit.DAYS.between(Utils.parseDate(it.getBillDate()), LocalDate.now()))
                            .build())
                    .toList();
            Utils.generatePdf(jrxmlFileName, payload, buyerLedgerAgeRows);
        } else {
            String jrxmlFileName = "BuyerLedger.jrxml";
            List<BuyerLedgerRow> buyerLedgerRows = collectionService.getCollectionDetailsForBuyer(buyerName);
            Utils.generatePdf(jrxmlFileName, payload, buyerLedgerRows);
        }
    }
}
