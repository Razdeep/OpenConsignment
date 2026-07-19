package openconsignment.controller.entry;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.Button;
import openconsignment.common.ViewLoader;

public class EntryController implements Initializable {

    @FXML
    private Button bill_entry_btn;

    @FXML
    private Button collection_entry_brn;

    @FXML
    private Button payment_entry_btn;

    @FXML
    private Group root;

    private ViewLoader viewLoader;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        viewLoader = new ViewLoader();
    }

    @FXML
    private void showBillEntryScreen(ActionEvent event) {
        viewLoader.loadRoot("/fxml/entry/bill/BillEntry.fxml", root);
    }

    @FXML
    private void showCollectionEntryScreen(ActionEvent event) {
        viewLoader.loadRoot("/fxml/entry/collection/CollectionEntry.fxml", root);
    }

    @FXML
    private void showPaymentEntryScreen(ActionEvent event) {
        viewLoader.loadRoot("/fxml/entry/payment/PaymentEntry.fxml", root);
    }
}
