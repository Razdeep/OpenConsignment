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
import openconsignment.entity.PaymentEntity;
import openconsignment.repository.PaymentRepository;
import openconsignment.service.PaymentService;

public class PaymentHistoryController implements Initializable {

    @FXML
    private Group root;

    @FXML
    private TableView<PaymentEntity> tableView;

    @FXML
    private TableColumn<PaymentEntity, String> voucher_no_col;

    @FXML
    private TableColumn<PaymentEntity, String> voucher_date_col;

    @FXML
    private TableColumn<PaymentEntity, String> supplier_name_col;

    @FXML
    private TableColumn<PaymentEntity, String> total_amount_col;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        PaymentService paymentService = new PaymentService(new PaymentRepository());

        voucher_no_col.setCellValueFactory(new PropertyValueFactory<>("voucherNo"));
        voucher_date_col.setCellValueFactory(new PropertyValueFactory<>("voucherDate"));
        supplier_name_col.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        total_amount_col.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));

        try {
            List<PaymentEntity> paymentEntityList = paymentService.getPayments();
            tableView.setItems(FXCollections.observableArrayList(paymentEntityList));
        } catch (SQLException ex) {
            Utils.showAlert(ex.toString());
            Logger.getLogger(PaymentHistoryController.class.getName()).log(Level.SEVERE, ex.toString(), ex);
        }
    }
}
