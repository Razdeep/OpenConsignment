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
import openconsignment.common.Utils;
import openconsignment.entity.CollectionEntity;
import openconsignment.repository.CollectionRepository;
import openconsignment.service.CollectionService;

public class CollectionHistoryController implements Initializable {

    @FXML
    private Group root;

    @FXML
    private TableView<CollectionEntity> tableView;

    @FXML
    private TableColumn<CollectionEntity, String> voucher_no_col;

    @FXML
    private TableColumn<CollectionEntity, String> voucher_date_col;

    @FXML
    private TableColumn<CollectionEntity, String> buyer_name_col;

    @FXML
    private TableColumn<CollectionEntity, String> total_amount_col;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        CollectionService collectionService = new CollectionService(new CollectionRepository());

        voucher_no_col.setCellValueFactory(new PropertyValueFactory<>("voucherNo"));
        voucher_date_col.setCellValueFactory(new PropertyValueFactory<>("voucherDate"));
        buyer_name_col.setCellValueFactory(new PropertyValueFactory<>("buyerName"));
        total_amount_col.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));

        try {
            List<CollectionEntity> collectionEntityList = collectionService.getCollections();
            tableView.setItems(FXCollections.observableArrayList(collectionEntityList));
        } catch (SQLException ex) {
            Utils.showAlert(ex.toString());
            Logger.getLogger(CollectionHistoryController.class.getName()).log(Level.SEVERE, ex.toString(), ex);
        }
    }
}
