package openconsignment.controller.entry;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javax.inject.Inject;
import openconsignment.common.ViewLoader;

@SuppressWarnings("unused")
public class EntryController implements Initializable {

    @FXML
    private Button bill_entry_btn;

    @FXML
    private Button collection_entry_btn;

    @FXML
    private Button payment_entry_btn;

    @FXML
    private Group root;

    private final ViewLoader viewLoader;

    @Inject
    public EntryController(ViewLoader viewLoader) {
        this.viewLoader = viewLoader;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {}

    @FXML
    private void showBillEntryScreen(ActionEvent event) {
        viewLoader.loadRoot("/fxml/entry/BillEntry.fxml", root);
    }

    @FXML
    private void showCollectionEntryScreen(ActionEvent event) {
        viewLoader.loadRoot("/fxml/entry/CollectionEntry.fxml", root);
    }

    @FXML
    private void showPaymentEntryScreen(ActionEvent event) {
        viewLoader.loadRoot("/fxml/entry/PaymentEntry.fxml", root);
    }
}
