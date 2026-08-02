package openconsignment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import net.sf.jasperreports.engine.JRException;
import openconsignment.common.Constants;
import openconsignment.common.Utils;
import openconsignment.model.TransportLedgerRow;
import openconsignment.repository.TransportRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransportServiceTest {

    @Mock
    private TransportRepository transportRepository;

    @InjectMocks
    private TransportService transportService;

    @Test
    void getAllTransportsReturnsRepositoryResult() throws SQLException {
        List<String> transports = List.of("Transport A");
        when(transportRepository.getAllTransports()).thenReturn(transports);

        assertEquals(transports, transportService.getAllTransports());
    }

    @Test
    void saveTransportDelegatesToRepository() throws SQLException {
        transportService.saveTransport("Transport A");

        verify(transportRepository).saveTransport("Transport A");
    }

    @Test
    void deleteTransportDelegatesToRepository() throws SQLException {
        transportService.deleteTransport("Transport A");

        verify(transportRepository).deleteTransport("Transport A");
    }

    @Test
    void renameTransportDelegatesToRepository() throws SQLException {
        transportService.renameTransport("Old", "New");

        verify(transportRepository).renameTransport("Old", "New");
    }

    @Test
    void generatePdfBuildsPayloadAndForwardsRows() throws SQLException, JRException {
        List<TransportLedgerRow> rows =
                List.of(TransportLedgerRow.builder().lr("LR1").build());
        when(transportRepository.getTransportReportFor("Transport A", "01-01-2026", "31-01-2026"))
                .thenReturn(rows);

        try (MockedStatic<Utils> utils = mockStatic(Utils.class)) {
            transportService.generatePdf("Transport A", "01-01-2026", "31-01-2026");

            ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
            utils.verify(() -> Utils.generatePdf(eq("TransportLedger.jrxml"), payloadCaptor.capture(), eq(rows)));

            Map<String, Object> payload = payloadCaptor.getValue();
            assertEquals(Constants.ORGANIZATION_NAME, payload.get(Constants.ORGANIZATION_NAME_KEY));
            assertEquals("Transport A", payload.get("transportName"));
            assertEquals("01-01-2026", payload.get("fromDate"));
            assertEquals("31-01-2026", payload.get("toDate"));
        }
    }
}
