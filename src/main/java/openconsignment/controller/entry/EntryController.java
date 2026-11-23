package openconsignment.controller.entry;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javax.inject.Inject;
import openconsignment.common.ViewLoader;

public class EntryController implements Initializable {

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
    private void showBillEntryScreen(ActionEvent ignoredEvent) {
        viewLoader.loadRoot("/fxml/entry/BillEntry.fxml", root);
    }

    @FXML
    private void showCollectionEntryScreen(ActionEvent ignoredEvent) {
        viewLoader.loadRoot("/fxml/entry/CollectionEntry.fxml", root);
    }

    @FXML
    private void showPaymentEntryScreen(ActionEvent ignoredEvent) {
        viewLoader.loadRoot("/fxml/entry/PaymentEntry.fxml", root);
    }
}
