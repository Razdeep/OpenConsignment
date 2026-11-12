package openconsignment.controller.report;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import openconsignment.common.Constants;
import openconsignment.common.Utils;
import openconsignment.service.SupplierService;

@Slf4j
public class SupplierLedgerController implements Initializable {

    @FXML
    private ComboBox<String> supplier_name_combo;

    @FXML
    private ToggleGroup choice;

    @FXML
    private RadioButton supplier_ledger_radio;

    @FXML
    private RadioButton agewise_outstanding_radio;

    @FXML
    private Button generate_pdf_btn;

    @FXML
    private Button launch_pdf_btn;

    private final SupplierService supplierService;

    @Inject
    public SupplierLedgerController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            List<String> supplierList = supplierService.getAllSuppliers();
            supplier_name_combo.setItems(FXCollections.observableArrayList(supplierList));
        } catch (SQLException ex) {
            Utils.showAlert(ex.toString());
            log.error(ex.toString(), ex);
        }
    }

    @FXML
    private void generatePDF(ActionEvent event) {
        try {
            supplierService.generatePdf(
                    supplier_name_combo.getSelectionModel().getSelectedItem(), agewise_outstanding_radio.isSelected());
            Utils.launchPdf(Constants.REPORT_FILE_NAME);
            Utils.showAlert("Report Successfully Generated", AlertType.INFORMATION);
        } catch (IOException | SQLException | JRException ex) {
            Utils.showAlert(ex.toString());
            log.error(ex.toString(), ex);
        }
    }

    @FXML
    private void launchPdf(ActionEvent event) {
        try {
            Utils.launchPdf(Constants.REPORT_FILE_NAME);
            Utils.showAlert("PDF opened successfully.", AlertType.INFORMATION);
        } catch (IOException ex) {
            Utils.showAlert(ex.toString());
            log.error(ex.toString(), ex);
        }
    }
}
