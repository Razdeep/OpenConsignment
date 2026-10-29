package openconsignment.controller.entry;

import static openconsignment.common.Utils.formatDate;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javax.inject.Inject;
import openconsignment.common.Utils;
import openconsignment.entity.BillEntity;
import openconsignment.entity.LREntity;
import openconsignment.model.LR;
import openconsignment.service.BillService;
import openconsignment.service.BuyerService;
import openconsignment.service.SupplierService;
import openconsignment.service.TransportService;
import org.controlsfx.control.textfield.TextFields;

@SuppressWarnings("unused")
public class BillEntryController implements Initializable {

    @FXML
    private TextField supplier_field;

    @FXML
    private TextField buyer_name_field;

    @FXML
    private TextField bill_no_field;

    @FXML
    private TextField transport_field;

    @FXML
    private DatePicker date_field;

    @FXML
    private DatePicker lr_date;

    @FXML
    private TextField bill_amount_field;

    @FXML
    private TextField pm_field;

    @FXML
    private TextField lr_field;

    @FXML
    private Button save_btn;

    @FXML
    private Button clear_btn;

    @FXML
    private Button add_btn;

    @FXML
    private TableView<LR> lrTable;

    @FXML
    private TableColumn<LR, String> lr_no_col;

    @FXML
    private TableColumn<LR, String> pm_col;

    @FXML
    private Button replace_btn;

    @FXML
    private Button delete_btn;

    private List<LR> lrList;

    private final BillService billService;
    private final SupplierService supplierService;
    private final BuyerService buyerService;
    private final TransportService transportService;

    @Inject
    public BillEntryController(
            BillService billService,
            SupplierService supplierService,
            BuyerService buyerService,
            TransportService transportService) {
        this.billService = billService;
        this.supplierService = supplierService;
        this.buyerService = buyerService;
        this.transportService = transportService;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            TextFields.bindAutoCompletion(supplier_field, supplierService.getAllSuppliers());
            TextFields.bindAutoCompletion(buyer_name_field, buyerService.getAllBuyers());
            TextFields.bindAutoCompletion(transport_field, transportService.getAllTransports());

            lr_no_col.setCellValueFactory(new PropertyValueFactory<>("lrNo"));
            pm_col.setCellValueFactory(new PropertyValueFactory<>("pm"));

            lrList = new ArrayList<>();

        } catch (Exception ex) {
            Utils.showAlert(ex.toString());
            Logger.getLogger(BillEntryController.class.getName()).log(Level.SEVERE, ex.toString(), ex);
        }
    }

    @FXML
    private void addToList(ActionEvent event) {
        if (bill_no_field.getText().isEmpty()
                || lr_field.getText().isEmpty()
                || pm_field.getText().isEmpty()) {
            Utils.showAlert("Please ensure that the Bill No., LR No. and PM fields are filled up properly.", 2);
            return;
        }
        lrList.add(new LR(bill_no_field.getText(), lr_field.getText(), pm_field.getText()));
        lrTable.setItems(FXCollections.observableList(lrList));
        lr_field.clear();
        pm_field.clear();
    }

    @FXML
    private void save(ActionEvent event) {
        if (supplier_field.getText().isEmpty()
                || buyer_name_field.getText().isEmpty()
                || bill_no_field.getText().isEmpty()
                || date_field.getValue().toString().isEmpty()
                || transport_field.getText().isEmpty()
                || lr_date.getValue().toString().isEmpty()
                || bill_amount_field.getText().isEmpty()
                || lrList.isEmpty()) {
            Utils.showAlert("Please ensure to fill up all the fields");
            return;
        }

        List<LREntity> lrEntityList = lrList.stream()
                .map(it -> new LREntity(it.getBillNo(), it.getLrNo(), it.getPm()))
                .toList();

        BillEntity billEntity = BillEntity.builder()
                .supplierName(supplier_field.getText())
                .buyerName(buyer_name_field.getText())
                .billNo(bill_no_field.getText())
                .billDate(formatDate(date_field.getValue().toString()))
                .transport(transport_field.getText())
                .lrDate(formatDate(lr_date.getValue().toString()))
                .billAmount(bill_amount_field.getText())
                .lrEntities(lrEntityList)
                .build();
        try {
            billService.saveBill(billEntity);
            clearAllFields();
            Utils.showAlert("Saved Successfully", 1);
        } catch (Exception ex) {
            Utils.showAlert(ex.toString());
            Logger.getLogger(BillEntryController.class.getName()).log(Level.SEVERE, ex.toString(), ex);
        }
    }

    @FXML
    private void clearAllFields() {
        supplier_field.clear();
        buyer_name_field.clear();
        bill_no_field.clear();
        transport_field.clear();
        date_field.setValue(null);
        lr_date.setValue(null);
        bill_amount_field.clear();
        pm_field.clear();
        lr_field.clear();
        lrList.clear();
        lrTable.setItems(FXCollections.observableList(lrList));
    }

    @FXML
    private void replaceInList(ActionEvent event) {
        if (lrTable.getSelectionModel().getSelectedIndex() == -1) {
            Utils.showAlert("Please select an item from the LR Table", 2);
            return;
        }
        if (bill_no_field.getText().isEmpty()
                || lr_field.getText().isEmpty()
                || pm_field.getText().isEmpty()) {
            Utils.showAlert("Please ensure that the Bill No., LR No. and PM fields are filled up properly.", 2);
            return;
        }
        lrList.set(
                lrTable.getSelectionModel().getSelectedIndex(),
                new LR(bill_no_field.getText(), lr_field.getText(), pm_field.getText()));
        lr_field.clear();
        pm_field.clear();
        lrTable.setItems(FXCollections.observableList(lrList));
    }

    @FXML
    private void deleteFromList(ActionEvent event) {
        if (lrTable.getSelectionModel().getSelectedIndex() == -1) {
            Utils.showAlert("Please select an item from the LR Table", 2);
            return;
        }
        lrList.remove(lrTable.getSelectionModel().getSelectedIndex());
        lrTable.setItems(FXCollections.observableList(lrList));
    }

    @FXML
    private void fetchBill(ActionEvent event) {
        try {
            BillEntity billEntity = billService.getBill(bill_no_field.getText());

            if (null == billEntity) {
                Utils.showAlert("Record not found. Please try again later.", 2);
                return;
            }

            supplier_field.setText(billEntity.getSupplierName());
            buyer_name_field.setText(billEntity.getBuyerName());
            date_field.setValue(Utils.parseDate(billEntity.getBillDate()));
            transport_field.setText(billEntity.getTransport());
            lr_date.setValue(Utils.parseDate(billEntity.getLrDate()));
            bill_amount_field.setText(billEntity.getBillAmount());

            lrList = billEntity.getLrEntities().stream()
                    .map(i -> new LR(i.getBillNo(), i.getLrNo(), i.getPm()))
                    .collect(Collectors.toCollection(ArrayList::new));

            lrTable.setItems(FXCollections.observableArrayList(lrList));
            //            delete_entry_btn.setDisable(false);

        } catch (SQLException ex) {
            Utils.showAlert(ex.toString());
            Logger.getLogger(BillEntryController.class.getName()).log(Level.SEVERE, ex.toString(), ex);
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
            date_field.setValue(null);
            transport_field.setText("");
            lr_date.setValue(null);
            bill_amount_field.setText("");
            lrTable.setItems(FXCollections.observableArrayList());

            Utils.showAlert(bill_no_field.getText().toUpperCase() + " Entry was successfully deleted.", 1);
        } catch (SQLException ex) {
            Utils.showAlert(ex.toString());
            Logger.getLogger(BillEntryController.class.getName()).log(Level.SEVERE, ex.toString(), ex);
        }
    }
}
