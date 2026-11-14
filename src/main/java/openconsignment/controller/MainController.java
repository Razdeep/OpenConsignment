package openconsignment.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import openconsignment.common.Utils;
import openconsignment.common.ViewLoader;
import openconsignment.service.BackupService;
import org.eclipse.jgit.api.errors.EmptyCommitException;

@Slf4j
@SuppressWarnings("unused")
public class MainController implements Initializable {

    @FXML
    private Button input_btn;

    @FXML
    private BorderPane root;

    @FXML
    private Button report_btn;

    @FXML
    private Button master_btn;

    private final ViewLoader viewLoader;
    private final BackupService backupService;

    @Inject
    public MainController(ViewLoader viewLoader, BackupService backupService) {
        this.viewLoader = viewLoader;
        this.backupService = backupService;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        viewLoader.loadCenter("/fxml/WelcomeScreen.fxml", root);
    }

    @FXML
    private void showInputScreen(ActionEvent event) {
        viewLoader.loadCenter("/fxml/entry/Input.fxml", root);
    }

    @FXML
    private void showReportScreen(ActionEvent event) {
        viewLoader.loadCenter("/fxml/report/Report.fxml", root);
    }

    @FXML
    private void showMasterScreen(ActionEvent event) {
        viewLoader.loadCenter("/fxml/master/Master.fxml", root);
    }

    @FXML
    private void showWelcomeScreen(ActionEvent event) {
        viewLoader.loadCenter("/fxml/WelcomeScreen.fxml", root);
    }

    @FXML
    private void showInputHistory(ActionEvent event) {
        viewLoader.loadCenter("/fxml/history/InputHistory.fxml", root);
    }

    @FXML
    private void showCollectionHistory(ActionEvent event) {
        viewLoader.loadCenter("/fxml/history/CollectionHistory.fxml", root);
    }

    @FXML
    private void showPaymentHistory(ActionEvent event) {
        viewLoader.loadCenter("/fxml/history/PaymentHistory.fxml", root);
    }

    @FXML
    private void showBillEntryScreen(ActionEvent event) {
        viewLoader.loadCenter("/fxml/entry/BillEntry.fxml", root);
    }

    @FXML
    private void showCollectionEntryScreen(ActionEvent event) {
        viewLoader.loadCenter("/fxml/entry/CollectionEntry.fxml", root);
    }

    @FXML
    private void showPaymentEntryScreen(ActionEvent event) {
        viewLoader.loadCenter("/fxml/entry/PaymentEntry.fxml", root);
    }

    @FXML
    private void showSupplierLedger(ActionEvent event) {
        viewLoader.loadCenter("/fxml/report/SupplierLedger.fxml", root);
    }

    @FXML
    private void showBuyerLedger(ActionEvent event) {
        viewLoader.loadCenter("/fxml/report/BuyerLedger.fxml", root);
    }

    @FXML
    private void showTransportLedger(ActionEvent event) {
        viewLoader.loadCenter("/fxml/report/TransportLedger.fxml", root);
    }

    @FXML
    private void showSupplierMaster(ActionEvent event) {
        viewLoader.loadCenter("/fxml/master/MasterSupplier.fxml", root);
    }

    @FXML
    private void showBuyerMaster(ActionEvent event) {
        viewLoader.loadCenter("/fxml/master/MasterBuyer.fxml", root);
    }

    @FXML
    private void showTransportMaster(ActionEvent event) {
        viewLoader.loadCenter("/fxml/master/MasterTransport.fxml", root);
    }

    @FXML
    private void checkSync(ActionEvent event) {
        boolean synced = false;
        try {
            synced = backupService.checkSync();
        } catch (Exception e) {
            Utils.showAlert("Error", Alert.AlertType.ERROR);
        }

        if (synced) {
            Utils.showAlert("Data is Synced", Alert.AlertType.INFORMATION);
        } else {
            Utils.showAlert("Not Synced", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void upload(ActionEvent event) {
        try {
            Alert alert =
                    new Alert(Alert.AlertType.CONFIRMATION, "Do you want to upload?", ButtonType.YES, ButtonType.NO);
            if (alert.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) {
                return;
            }
            backupService.upload();
            Utils.showAlert("Successfully uploaded", Alert.AlertType.INFORMATION);
        } catch (EmptyCommitException e) {
            log.info(e.toString());
            Utils.showAlert("Data is already synced. " + e, Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            log.error(e.toString());
            Utils.showAlert(e.toString(), Alert.AlertType.ERROR);
        }
    }

    public void download(ActionEvent actionEvent) {
        try {
            Alert alert =
                    new Alert(Alert.AlertType.CONFIRMATION, "Do you want to download?", ButtonType.YES, ButtonType.NO);
            if (alert.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) {
                return;
            }
            backupService.download();
            Utils.showAlert("Successfully downloaded", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            log.error(e.toString());
            Utils.showAlert(e.toString(), Alert.AlertType.ERROR);
        }
    }
}
