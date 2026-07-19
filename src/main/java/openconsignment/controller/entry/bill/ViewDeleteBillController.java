package openconsignment.controller.entry.bill;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import openconsignment.common.Utils;
import openconsignment.entity.BillEntity;
import openconsignment.entity.LREntity;
import openconsignment.repository.BillRepository;
import openconsignment.service.BillService;

public class ViewDeleteBillController implements Initializable {

    private static final Logger logger = Logger.getLogger(ViewDeleteBillController.class.getName());

    @FXML
    private TextField supplier_field;

    @FXML
    private TableView<LREntity> lr_table;

    @FXML
    private TableColumn<LREntity, String> lr_no_col;

    @FXML
    private TableColumn<LREntity, String> pm_col;

    @FXML
    private TextField buyer_name_field;

    @FXML
    private TextField bill_date_field;

    @FXML
    private TextField transport_field;

    @FXML
    private TextField bill_amount_field;

    @FXML
    private TextField lr_date_field;

    @FXML
    private Button get_details_btn;

    @FXML
    private Button delete_entry_btn;

    @FXML
    private TextField bill_no_field;

    private BillService billService;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lr_no_col.setCellValueFactory(new PropertyValueFactory<>("lrNo"));
        pm_col.setCellValueFactory(new PropertyValueFactory<>("pm"));

        billService = new BillService(new BillRepository());
    }

    @FXML
    private void getDetails(ActionEvent event) {
        try {
            BillEntity billEntity = billService.getBill(bill_no_field.getText());

            if (null == billEntity) {
                Utils.showAlert("Record not found. Please try again later.", 2);
                return;
            }

            supplier_field.setText(billEntity.getSupplierName());
            buyer_name_field.setText(billEntity.getBuyerName());
            bill_date_field.setText(billEntity.getBillDate());
            transport_field.setText(billEntity.getTransport());
            lr_date_field.setText(billEntity.getLrDate());
            bill_amount_field.setText(billEntity.getBillAmount());

            lr_table.setItems(FXCollections.observableArrayList(billEntity.getLrEntities()));
            delete_entry_btn.setDisable(false);

        } catch (SQLException ex) {
            Utils.showAlert(ex.toString());
            logger.log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void deleteEntry(ActionEvent event) {
        Alert alert = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Are you sure that you want delete " + bill_no_field.getText() + " ?",
                ButtonType.YES,
                ButtonType.NO,
                ButtonType.CANCEL);
        alert.showAndWait();

        if (alert.getResult() != ButtonType.YES) {
            return;
        }

        try {
            billService.deleteBill(bill_no_field.getText());

            supplier_field.setText("");
            buyer_name_field.setText("");
            bill_date_field.setText("");
            transport_field.setText("");
            lr_date_field.setText("");
            bill_amount_field.setText("");
            lr_table.setItems(FXCollections.observableArrayList());

            Utils.showAlert(bill_no_field.getText().toUpperCase() + " Entry was successfully deleted.", 1);
        } catch (SQLException ex) {
            Utils.showAlert(ex.toString());
            logger.log(Level.SEVERE, ex.toString(), ex);
        }
    }

    @FXML
    public void updateBill(ActionEvent actionEvent) {

        BillEntity billEntity = BillEntity.builder()
                .billNo(bill_no_field.getText())
                .supplierName(supplier_field.getText())
                .buyerName(buyer_name_field.getText())
                .billDate(bill_date_field.getText())
                .transport(transport_field.getText())
                .lrDate(lr_date_field.getText())
                .billAmount(bill_amount_field.getText())
                .build();

        try {
            billService.saveBill(billEntity);
        } catch (SQLException ex) {
            Utils.showAlert(ex.toString());
            logger.log(Level.SEVERE, ex.toString(), ex);
        }
    }
}
