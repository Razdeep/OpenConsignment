package openconsignment.service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.sf.jasperreports.engine.*;
import openconsignment.common.Constants;
import openconsignment.common.Utils;
import openconsignment.model.SupplierLedgerAgeRow;
import openconsignment.model.SupplierLedgerRow;
import openconsignment.repository.SupplierRepository;

@Singleton
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final PaymentService paymentService;

    @Inject
    public SupplierService(SupplierRepository supplierRepository, PaymentService paymentService) {
        this.supplierRepository = supplierRepository;
        this.paymentService = paymentService;
    }

    public List<String> getAllSuppliers() throws SQLException {
        return supplierRepository.getAllSuppliers();
    }

    public void saveSupplier(String supplierName) throws SQLException {
        supplierRepository.saveSupplier(supplierName);
    }

    public void deleteSupplier(String supplierName) throws SQLException {
        supplierRepository.deleteSupplier(supplierName);
    }

    public void renameSupplier(String oldName, String newName) throws SQLException {
        supplierRepository.renameSupplier(oldName, newName);
    }

    public void generatePdf(String supplierName, boolean isAgewise) throws SQLException, JRException {

        Map<String, Object> payload = new HashMap<>();
        payload.put(Constants.ORGANIZATION_NAME_KEY, Constants.ORGANIZATION_NAME);
        payload.put("supplierName", supplierName);

        if (isAgewise) {
            String jrxmlFileName = "SupplierLedgerAge.jrxml";
            List<SupplierLedgerAgeRow> supplierLedgerAgeRows =
                    paymentService.fetchPendingBillsForSupplier(supplierName).stream()
                            .map(it -> SupplierLedgerAgeRow.builder()
                                    .billNo(it.getBillNo())
                                    .billDate(it.getBillDate())
                                    .billAmount(it.getBillAmount())
                                    .buyerName(it.getBuyerName())
                                    .days((int)
                                            ChronoUnit.DAYS.between(Utils.parseDate(it.getBillDate()), LocalDate.now()))
                                    .build())
                            .toList();

            Utils.generatePdf(jrxmlFileName, payload, supplierLedgerAgeRows);
        } else {
            String jrxmlFileName = "SupplierLedger.jrxml";
            List<SupplierLedgerRow> supplierLedgerRows = paymentService.getPaymentDetailsForSupplier(supplierName);
            Utils.generatePdf(jrxmlFileName, payload, supplierLedgerRows);
        }
    }
}
