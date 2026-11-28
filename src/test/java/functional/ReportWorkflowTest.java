package functional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import openconsignment.common.Constants;
import openconsignment.common.Utils;
import openconsignment.controller.report.BuyerLedgerController;
import openconsignment.controller.report.SupplierLedgerController;
import openconsignment.controller.report.TransportLedgerController;
import openconsignment.service.BuyerService;
import openconsignment.service.SupplierService;
import openconsignment.service.TransportService;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.testfx.framework.junit5.ApplicationTest;

class ReportWorkflowTest extends ApplicationTest {
    private Stage stage;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        stage.setScene(new Scene(new Pane(), 800, 600));
        stage.show();
    }

    @Test
    void buyerReportUsesSelectedBuyerAndReportType() throws Exception {
        BuyerService service = mock(BuyerService.class);
        when(service.getAllBuyers()).thenReturn(List.of("Buyer A"));
        interact(() -> {
            Parent root = load("BuyerLedger.fxml", new BuyerLedgerController(service));
            combo(root, "#buyer_name_combo").setValue("Buyer A");
            try (MockedStatic<Utils> utils = mockStatic(Utils.class)) {
                button(root, "#generate_pdf_btn").fire();
                ((RadioButton) root.lookup("#agewise_outstanding_radio")).setSelected(true);
                button(root, "#generate_pdf_btn").fire();
                utils.verify(() -> Utils.launchPdf(Constants.REPORT_FILE_NAME), times(2));
            }
        });
        verify(service).generatePdf("Buyer A", false);
        verify(service).generatePdf("Buyer A", true);
    }

    @Test
    void supplierReportUsesSelectedSupplierAndReportType() throws Exception {
        SupplierService service = mock(SupplierService.class);
        when(service.getAllSuppliers()).thenReturn(List.of("Supplier A"));
        interact(() -> {
            Parent root = load("SupplierLedger.fxml", new SupplierLedgerController(service));
            combo(root, "#supplier_name_combo").setValue("Supplier A");
            try (MockedStatic<Utils> utils = mockStatic(Utils.class)) {
                button(root, "#generate_pdf_btn").fire();
                ((RadioButton) root.lookup("#agewise_outstanding_radio")).setSelected(true);
                button(root, "#generate_pdf_btn").fire();
                utils.verify(() -> Utils.launchPdf(Constants.REPORT_FILE_NAME), times(2));
            }
        });
        verify(service).generatePdf("Supplier A", false);
        verify(service).generatePdf("Supplier A", true);
    }

    @Test
    void transportReportValidatesDatesAndSupportsAllTime() throws Exception {
        TransportService service = mock(TransportService.class);
        when(service.getAllTransports()).thenReturn(List.of("Carrier"));
        interact(() -> {
            Parent root = load("TransportLedger.fxml", new TransportLedgerController(service));
            combo(root, "#transport_name_combo").setValue("Carrier");
            try (MockedStatic<Utils> utils = mockStatic(Utils.class)) {
                utils.when(() -> Utils.formatDate(anyString())).thenCallRealMethod();
                button(root, "#generate_pdf_report_btn").fire();
                utils.verify(() -> Utils.showAlert("Select dates properly"));
                ((DatePicker) root.lookup("#from_date")).setValue(LocalDate.of(2026, 1, 2));
                ((DatePicker) root.lookup("#to_date")).setValue(LocalDate.of(2026, 2, 3));
                button(root, "#generate_pdf_report_btn").fire();
                ((CheckBox) root.lookup("#all_time_checkbox")).setSelected(true);
                button(root, "#generate_pdf_report_btn").fire();
                utils.verify(() -> Utils.launchPdf(Constants.REPORT_FILE_NAME), times(2));
            }
        });
        verify(service, times(2)).generatePdf(eq("Carrier"), anyString(), anyString());
        verify(service).generatePdf("Carrier", "02-01-2026", "03-02-2026");
        verify(service).generatePdf("Carrier", "01-01-2000", "01-01-2100");
    }

    private Parent load(String file, Object controller) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/report/" + file));
            loader.setControllerFactory(type -> controller);
            Parent root = loader.load();
            stage.getScene().setRoot(root);
            root.applyCss();
            root.layout();
            return root;
        } catch (Exception e) {
            throw new AssertionError("Could not load " + file, e);
        }
    }

    private ComboBox<String> combo(Parent root, String id) {
        @SuppressWarnings("unchecked")
        ComboBox<String> combo = (ComboBox<String>) root.lookup(id);
        assertEquals(1, combo.getItems().size());
        return combo;
    }

    private Button button(Parent root, String id) {
        return (Button) root.lookup(id);
    }
}
