package openconsignment.controller.history;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javax.inject.Inject;
import openconsignment.common.Utils;
import openconsignment.entity.BillEntity;
import openconsignment.service.BillService;

public class BillHistoryController implements Initializable {

    @FXML
    private Group root;

    @FXML
    private TableColumn<BillEntity, String> supplier_name_col;

    @FXML
    private TableColumn<BillEntity, String> buyer_name_col;

    @FXML
    private TableColumn<BillEntity, String> bill_no_col;

    @FXML
    private TableColumn<BillEntity, String> bill_date_col;

    @FXML
    private TableColumn<BillEntity, String> transport_col;

    @FXML
    private TableColumn<BillEntity, String> lr_date_col;

    @FXML
    private TableColumn<BillEntity, String> bill_amount_col;

    @FXML
    private TableView<BillEntity> tableView;

    private final BillService billService;

    @Inject
    public BillHistoryController(BillService billService) {
        this.billService = billService;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        supplier_name_col.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        buyer_name_col.setCellValueFactory(new PropertyValueFactory<>("buyerName"));
        bill_no_col.setCellValueFactory(new PropertyValueFactory<>("billNo"));
        bill_date_col.setCellValueFactory(new PropertyValueFactory<>("billDate"));
        transport_col.setCellValueFactory(new PropertyValueFactory<>("transport"));
        lr_date_col.setCellValueFactory(new PropertyValueFactory<>("lrDate"));
        bill_amount_col.setCellValueFactory(new PropertyValueFactory<>("billAmount"));

        try {
            List<BillEntity> billList = billService.getBills();
            tableView.setItems(FXCollections.observableArrayList(billList));
        } catch (SQLException ex) {
            Utils.showAlert(ex.toString());
            Logger.getLogger(BillHistoryController.class.getName()).log(Level.SEVERE, ex.toString(), ex);
        }
    }
}
