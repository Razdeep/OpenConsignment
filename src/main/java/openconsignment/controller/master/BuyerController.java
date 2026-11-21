package openconsignment.controller.master;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import openconsignment.common.Utils;
import openconsignment.service.BuyerService;

@Slf4j
public class BuyerController implements Initializable {

    @FXML
    private ListView<String> listView;

    @FXML
    private TextField add_field;

    @FXML
    private TextField rename_field;

    private final BuyerService buyerService;

    @Inject
    public BuyerController(BuyerService buyerService) {
        this.buyerService = buyerService;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        refreshList();
    }

    @FXML
    private void addMaster(ActionEvent ignoredEvent) {
        if (add_field.getText().compareTo("") == 0) {
            Utils.showAlert("Add field is left blank. Please ensure to fill up the field properly.");
            return;
        }
        try {
            buyerService.saveBuyer(add_field.getText());
            refreshList();
        } catch (SQLException ex) {
            Utils.showAlert(ex.toString());
            log.error(ex.toString(), ex);
        }
    }

    @FXML
    private void deleteMaster(ActionEvent ignoredEvent) {
        if (listView.getSelectionModel().getSelectedItem() == null) {
            Utils.showAlert("Buyer to be deleted not selected. Please Retry");
            return;
        }
        try {
            buyerService.deleteBuyer(listView.getSelectionModel().getSelectedItem());
            refreshList();
        } catch (SQLException ex) {
            Utils.showAlert(ex.toString());
            log.error(ex.toString(), ex);
        }
    }

    @FXML
    private void refreshList() {
        try {
            List<String> buyerList = buyerService.getAllBuyers();
            listView.getItems().setAll(buyerList);
        } catch (SQLException ex) {
            Utils.showAlert(ex.toString());
            log.error(ex.toString(), ex);
        }
    }

    @FXML
    private void renameMaster(ActionEvent ignoredEvent) {
        if (rename_field.getText().compareTo("") == 0) {
            Utils.showAlert("Rename field is left blank. Please ensure to fill up the field properly.");
            return;
        }
        try {
            buyerService.renameBuyer(listView.getSelectionModel().getSelectedItem(), rename_field.getText());
            Utils.showAlert("Success", AlertType.INFORMATION);
            refreshList();
        } catch (SQLException ex) {
            Utils.showAlert(ex.toString());
            log.error(ex.toString(), ex);
        }
    }
}
