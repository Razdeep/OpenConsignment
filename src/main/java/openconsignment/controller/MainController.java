package openconsignment.controller;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import openconsignment.common.Utils;
import openconsignment.common.ViewLoader;
import openconsignment.service.BackupService;

@Slf4j
public class MainController implements Initializable {

    @FXML
    private BorderPane root;

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
    private void showInputScreen(ActionEvent ignore) {
        viewLoader.loadCenter("/fxml/entry/Input.fxml", root);
    }

    @FXML
    private void showReportScreen(ActionEvent ignore) {
        viewLoader.loadCenter("/fxml/report/Report.fxml", root);
    }

    @FXML
    private void showMasterScreen(ActionEvent ignore) {
        viewLoader.loadCenter("/fxml/master/Master.fxml", root);
    }

    @FXML
    private void showWelcomeScreen(ActionEvent ignore) {
        viewLoader.loadCenter("/fxml/WelcomeScreen.fxml", root);
    }

    @FXML
    private void showInputHistory(ActionEvent ignore) {
        viewLoader.loadCenter("/fxml/history/InputHistory.fxml", root);
    }

    @FXML
    private void showCollectionHistory(ActionEvent ignore) {
        viewLoader.loadCenter("/fxml/history/CollectionHistory.fxml", root);
    }

    @FXML
    private void showPaymentHistory(ActionEvent ignore) {
        viewLoader.loadCenter("/fxml/history/PaymentHistory.fxml", root);
    }

    @FXML
    private void showBillEntryScreen(ActionEvent ignore) {
        viewLoader.loadCenter("/fxml/entry/BillEntry.fxml", root);
    }

    @FXML
    private void showCollectionEntryScreen(ActionEvent ignore) {
        viewLoader.loadCenter("/fxml/entry/CollectionEntry.fxml", root);
    }

    @FXML
    private void showPaymentEntryScreen(ActionEvent ignore) {
        viewLoader.loadCenter("/fxml/entry/PaymentEntry.fxml", root);
    }

    @FXML
    private void showSupplierLedger(ActionEvent ignore) {
        viewLoader.loadCenter("/fxml/report/SupplierLedger.fxml", root);
    }

    @FXML
    private void showBuyerLedger(ActionEvent ignore) {
        viewLoader.loadCenter("/fxml/report/BuyerLedger.fxml", root);
    }

    @FXML
    private void showTransportLedger(ActionEvent ignore) {
        viewLoader.loadCenter("/fxml/report/TransportLedger.fxml", root);
    }

    @FXML
    private void showSupplierMaster(ActionEvent ignore) {
        viewLoader.loadCenter("/fxml/master/MasterSupplier.fxml", root);
    }

    @FXML
    private void showBuyerMaster(ActionEvent ignore) {
        viewLoader.loadCenter("/fxml/master/MasterBuyer.fxml", root);
    }

    @FXML
    private void showTransportMaster(ActionEvent ignore) {
        viewLoader.loadCenter("/fxml/master/MasterTransport.fxml", root);
    }

    @FXML
    private void checkSync(ActionEvent ignore) {
        CompletableFuture.runAsync(() -> {
            try {
                boolean synced = backupService.checkSync();
                if (synced) {
                    Platform.runLater(() -> Utils.showAlert("Data is Synced", Alert.AlertType.INFORMATION));
                } else {
                    Platform.runLater(() -> Utils.showAlert("Not Synced", Alert.AlertType.WARNING));
                }
            } catch (Exception e) {
                log.warn(e.getMessage());
                Platform.runLater(() -> Utils.showAlert(e.getMessage(), Alert.AlertType.ERROR));
            }
        });
    }

    @FXML
    private void upload(ActionEvent ignore) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Do you want to upload?", ButtonType.YES, ButtonType.NO);
        if (alert.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                backupService.upload();
                Platform.runLater(() -> Utils.showAlert("Successfully uploaded", Alert.AlertType.INFORMATION));
            } catch (Exception e) {
                log.error(e.toString());
                Platform.runLater(() -> Utils.showAlert(e.toString(), Alert.AlertType.ERROR));
            }
        });
    }

    public void download(ActionEvent ignore) {
        Alert alert =
                new Alert(Alert.AlertType.CONFIRMATION, "Do you want to download?", ButtonType.YES, ButtonType.NO);
        if (alert.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                backupService.download();
                Platform.runLater(() -> Utils.showAlert("Successfully downloaded", Alert.AlertType.INFORMATION));
            } catch (Exception ex) {
                log.error(ex.toString());
                Platform.runLater(() -> Utils.showAlert(ex.toString(), Alert.AlertType.ERROR));
            }
        });
    }
}
