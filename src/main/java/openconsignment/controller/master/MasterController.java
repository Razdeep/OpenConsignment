/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package openconsignment.controller.master;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javax.inject.Inject;
import openconsignment.common.ViewLoader;

/**
 * FXML Controller class
 *
 * @author ADMIN
 */
public class MasterController implements Initializable {

    @FXML
    private Button supplier_master_btn;

    @FXML
    private Button buyer_master_btn;

    @FXML
    private Button transport_master_btn;

    @FXML
    private Group root;

    private final ViewLoader viewLoader;

    @Inject
    public MasterController(ViewLoader viewLoader) {
        this.viewLoader = viewLoader;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {}

    @FXML
    private void showSupplierMaster(ActionEvent event) {
        viewLoader.loadRoot("/fxml/master/MasterSupplier.fxml", root);
    }

    @FXML
    private void showBuyerMaster(ActionEvent event) {
        viewLoader.loadRoot("/fxml/master/MasterBuyer.fxml", root);
    }

    @FXML
    private void showTransportMaster(ActionEvent event) {
        viewLoader.loadRoot("/fxml/master/MasterTransport.fxml", root);
    }
}
