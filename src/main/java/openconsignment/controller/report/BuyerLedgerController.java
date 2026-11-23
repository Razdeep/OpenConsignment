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
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import openconsignment.common.Constants;
import openconsignment.common.Utils;
import openconsignment.service.BuyerService;

@Slf4j
public class BuyerLedgerController implements Initializable {

    @FXML
    private RadioButton agewise_outstanding_radio;

    @FXML
    private ComboBox<String> buyer_name_combo;

    private final BuyerService buyerService;

    @Inject
    public BuyerLedgerController(BuyerService buyerService) {
        this.buyerService = buyerService;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            List<String> buyerNames = buyerService.getAllBuyers();
            buyer_name_combo.setItems(FXCollections.observableArrayList(buyerNames));
        } catch (SQLException ex) {
            Utils.showAlert(ex.toString());
            log.error(ex.toString(), ex);
        }
    }

    @FXML
    private void generatePDF(ActionEvent event) {
        try {
            buyerService.generatePdf(
                    buyer_name_combo.getSelectionModel().getSelectedItem(), agewise_outstanding_radio.isSelected());
            Utils.launchPdf(Constants.REPORT_FILE_NAME);
            Utils.showAlert("Report Successfully Generated", AlertType.INFORMATION);
        } catch (SQLException | IOException | JRException ex) {
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
