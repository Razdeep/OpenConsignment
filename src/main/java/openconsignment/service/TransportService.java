package openconsignment.service;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.jasperreports.engine.*;
import openconsignment.common.Utils;
import openconsignment.model.TransportLedgerRow;
import openconsignment.repository.TransportRepository;

public class TransportService {

    private final TransportRepository transportRepository;

    public TransportService(TransportRepository transportRepository) {
        this.transportRepository = transportRepository;
    }

    public List<String> getAllTransports() throws SQLException {
        return transportRepository.getAllTransports();
    }

    public void saveTransport(String transportName) throws SQLException {
        transportRepository.saveTransport(transportName);
    }

    public void deleteTransport(String transportName) throws SQLException {
        transportRepository.deleteTransport(transportName);
    }

    public void renameTransport(String oldName, String newName) throws SQLException {
        transportRepository.renameTransport(oldName, newName);
    }

    public void generatePdf(String transportName, String fromDate, String toDate) throws SQLException, JRException {
        List<TransportLedgerRow> transportLedgerRows =
                transportRepository.getTransportReportFor(transportName, fromDate, toDate);

        Map<String, Object> payload = new HashMap<>();
        payload.put("transportName", transportName);
        payload.put("fromDate", fromDate);
        payload.put("toDate", toDate);

        String jrxmlFileName = "TransportLedger.jrxml";
        Utils.generatePdf(jrxmlFileName, payload, transportLedgerRows);
    }
}
