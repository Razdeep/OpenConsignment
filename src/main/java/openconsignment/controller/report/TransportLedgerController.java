package openconsignment.controller.report;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javax.inject.Inject;
import net.sf.jasperreports.engine.*;
import openconsignment.common.Constants;
import openconsignment.common.Utils;
import openconsignment.service.TransportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransportLedgerController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(TransportLedgerController.class);

    @FXML
    private CheckBox all_time_checkbox;

    @FXML
    private ComboBox<String> transport_name_combo;

    @FXML
    private DatePicker from_date;

    @FXML
    private DatePicker to_date;

    @FXML
    private Button generate_pdf_report_btn;

    @FXML
    private Button launch_report_btn;

    private final TransportService transportService;

    @Inject
    public TransportLedgerController(TransportService transportService) {
        this.transportService = transportService;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            List<String> transportList = transportService.getAllTransports();
            transport_name_combo.setItems(FXCollections.observableArrayList(transportList));
        } catch (SQLException ex) {
            Utils.showAlert(ex.toString());
            logger.error(ex.toString(), ex);
        }
    }

    @FXML
    private void generatePDF(ActionEvent event) {
        String fromDate = "01-01-2000", toDate = "01-01-2100";
        if (!all_time_checkbox.isSelected()) {
            if (from_date.getValue() == null || to_date.getValue() == null) {
                Utils.showAlert("Select dates properly");
                return;
            }
            fromDate = Utils.formatDate(from_date.getValue().toString());
            toDate = Utils.formatDate(to_date.getValue().toString());
        }
        try {
            transportService.generatePdf(
                    transport_name_combo.getSelectionModel().getSelectedItem(), fromDate, toDate);
            Utils.launchPdf(Constants.REPORT_FILE_NAME);
            Utils.showAlert("Report Successfully Generated", AlertType.INFORMATION);
        } catch (JRException | SQLException | IOException ex) {
            Utils.showAlert(ex.toString());
            logger.error(ex.toString(), ex);
        }
    }

    @FXML
    private void launchPdf(ActionEvent event) {
        try {
            Utils.launchPdf(Constants.REPORT_FILE_NAME);
            Utils.showAlert("PDF opened successfully.", AlertType.INFORMATION);
        } catch (IOException ex) {
            Utils.showAlert(ex.toString());
            logger.error(ex.toString(), ex);
        }
    }
}
