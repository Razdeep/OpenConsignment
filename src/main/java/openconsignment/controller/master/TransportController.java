package openconsignment.controller.master;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javax.inject.Inject;
import openconsignment.common.Utils;
import openconsignment.service.TransportService;

public class TransportController implements Initializable {

    @FXML
    private Button add_btn;

    @FXML
    private ListView<String> listView;

    @FXML
    private Button delete_btn;

    @FXML
    private Button refresh_btn;

    @FXML
    private TextField add_field;

    @FXML
    private Button rename_btn;

    @FXML
    private TextField rename_field;

    private final TransportService transportService;

    @Inject
    public TransportController(TransportService transportService) {
        this.transportService = transportService;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        refreshList();
    }

    @FXML
    private void addMaster(ActionEvent event) {
        if (add_field.getText().compareTo("") == 0) {
            Utils.showAlert("Add field is left blank. Please ensure to fill up the field properly.");
            return;
        }
        try {
            transportService.saveTransport(add_field.getText());
            refreshList();
        } catch (SQLException ex) {
            Utils.showAlert(ex.toString());
            Logger.getLogger(TransportController.class.getName()).log(Level.SEVERE, ex.toString(), ex);
        }
    }

    @FXML
    private void deleteMaster(ActionEvent event) {
        if (listView.getSelectionModel().getSelectedItem() == null) {
            Utils.showAlert("Supplier to be deleted not selected. Please Retry");
            return;
        }
        try {
            transportService.deleteTransport(listView.getSelectionModel().getSelectedItem());
            refreshList();
        } catch (SQLException ex) {
            Utils.showAlert(ex.toString());
            Logger.getLogger(TransportController.class.getName()).log(Level.SEVERE, ex.toString(), ex);
        }
    }

    @FXML
    private void refreshList() {
        try {
            List<String> transportList = transportService.getAllTransports();
            listView.getItems().setAll(transportList);
        } catch (SQLException ex) {
            Utils.showAlert(ex.toString());
            Logger.getLogger(TransportController.class.getName()).log(Level.SEVERE, ex.toString(), ex);
        }
    }

    @FXML
    private void renameMaster(ActionEvent event) {
        if (rename_field.getText().compareTo("") == 0) {
            Utils.showAlert("Rename field is left blank. Please ensure to fill up the field properly.");
            return;
        }
        try {
            transportService.renameTransport(listView.getSelectionModel().getSelectedItem(), rename_field.getText());
            Utils.showAlert("Success", 1);
            refreshList();
        } catch (SQLException ex) {
            Utils.showAlert(ex.toString());
            Logger.getLogger(TransportController.class.getName()).log(Level.SEVERE, ex.toString(), ex);
        }
    }
}
