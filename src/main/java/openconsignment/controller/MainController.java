package openconsignment.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import openconsignment.common.ViewLoader;

public class MainController implements Initializable {

    @FXML
    private Button input_btn;

    @FXML
    private BorderPane root;

    @FXML
    private Button report_btn;

    @FXML
    private Button master_btn;

    @FXML
    private MenuItem welcome_screen;

    private ViewLoader viewLoader;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        viewLoader = new ViewLoader();
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
}
