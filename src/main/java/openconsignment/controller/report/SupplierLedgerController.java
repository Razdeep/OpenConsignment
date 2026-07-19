package openconsignment.controller.report;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import net.sf.jasperreports.engine.*;
import openconsignment.common.Constants;
import openconsignment.common.Utils;
import openconsignment.repository.PaymentRepository;
import openconsignment.repository.SupplierRepository;
import openconsignment.service.PaymentService;
import openconsignment.service.SupplierService;

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

    private SupplierService supplierService;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        supplierService = new SupplierService(new SupplierRepository(), new PaymentService(new PaymentRepository()));
        try {
            List<String> supplierList = supplierService.getAllSuppliers();
            supplier_name_combo.setItems(FXCollections.observableArrayList(supplierList));
        } catch (SQLException ex) {
            Utils.showAlert(ex.toString());
            Logger.getLogger(SupplierLedgerController.class.getName()).log(Level.SEVERE, ex.toString(), ex);
        }
    }

    @FXML
    private void generatePDF(ActionEvent event) {
        try {
            supplierService.generatePdf(
                    supplier_name_combo.getSelectionModel().getSelectedItem(), agewise_outstanding_radio.isSelected());
            Utils.launchPdf(Constants.REPORT_FILE_NAME);
            Utils.showAlert("Report Successfully Generated", 1);
        } catch (IOException | SQLException | JRException ex) {
            Utils.showAlert(ex.toString());
            Logger.getLogger(SupplierLedgerController.class.getName()).log(Level.SEVERE, ex.toString(), ex);
        }
    }

    @FXML
    private void launchPdf(ActionEvent event) {
        try {
            Utils.launchPdf(Constants.REPORT_FILE_NAME);
            Utils.showAlert("PDF opened successfully.", 1);
        } catch (IOException ex) {
            Utils.showAlert(ex.toString());
            Logger.getLogger(SupplierLedgerController.class.getName()).log(Level.SEVERE, ex.toString(), ex);
        }
    }
}
