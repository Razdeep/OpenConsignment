package openconsignment.controller.entry;

import static openconsignment.common.Utils.formatDate;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javax.inject.Inject;
import net.sf.jasperreports.engine.JRException;
import openconsignment.common.Constants;
import openconsignment.common.Utils;
import openconsignment.entity.BillEntity;
import openconsignment.entity.CollectionEntity;
import openconsignment.entity.CollectionItemEntity;
import openconsignment.model.CollectionItem;
import openconsignment.service.BillService;
import openconsignment.service.BuyerService;
import openconsignment.service.CollectionService;

public class CollectionEntryController implements Initializable {

    private List<CollectionItem> collectionItemList;
    private List<String> buyerNameComboList;
    private List<String> billNoComboList;
    private int previouslyDue;

    private static final Logger logger = Logger.getLogger(CollectionEntryController.class.getName());

    @FXML
    private TextField dd_no_field;

    @FXML
    private DatePicker dd_date_field;

    @FXML
    private TextField voucher_no_field;

    @FXML
    private DatePicker voucher_date_field;

    @FXML
    private TableView<CollectionItem> collection_tableview;

    @FXML
    private TableColumn<CollectionItem, String> bill_no_col;

    @FXML
    private TableColumn<CollectionItem, String> bill_amt_col;

    @FXML
    private TableColumn<CollectionItem, String> bill_date_col;

    @FXML
    private TableColumn<CollectionItem, String> due_col;

    @FXML
    private TableColumn<CollectionItem, String> supplier_col;

    @FXML
    private TableColumn<CollectionItem, String> amount_collection_col;

    @FXML
    private TableColumn<CollectionItem, String> bank_col;

    @FXML
    private TableColumn<CollectionItem, String> dd_no_col;

    @FXML
    private TableColumn<CollectionItem, String> dd_date_col;

    @FXML
    private TextField bill_date;

    @FXML
    private TextField bill_amount;

    @FXML
    private ComboBox<String> buyer_name;

    @FXML
    private TextField supplier_name;

    @FXML
    private ComboBox<String> bill_no_combo;

    @FXML
    private TextField bank_field;

    @FXML
    private TextField total_amount_field;

    @FXML
    private TextField amount_collected_field;

    @FXML
    private TextField collection_due_field;

    @FXML
    private Label last_voucher_field;

    private final BillService billService;
    private final BuyerService buyerService;
    private final CollectionService collectionService;

    @Inject
    public CollectionEntryController(
            BillService billService, BuyerService buyerService, CollectionService collectionService) {
        this.billService = billService;
        this.buyerService = buyerService;
        this.collectionService = collectionService;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        collectionItemList = new ArrayList<>();
        buyerNameComboList = new ArrayList<>();
        billNoComboList = new ArrayList<>();
        bill_no_col.setCellValueFactory(new PropertyValueFactory<>("billNo"));
        bill_amt_col.setCellValueFactory(new PropertyValueFactory<>("billAmount"));
        bill_date_col.setCellValueFactory(new PropertyValueFactory<>("billDate"));
        due_col.setCellValueFactory(new PropertyValueFactory<>("due"));
        supplier_col.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        amount_collection_col.setCellValueFactory(new PropertyValueFactory<>("amountCollected"));
        dd_no_col.setCellValueFactory(new PropertyValueFactory<>("ddNo"));
        bank_col.setCellValueFactory(new PropertyValueFactory<>("bank"));
        dd_date_col.setCellValueFactory(new PropertyValueFactory<>("ddDate"));
        refreshCollectionTableView();
        fillBuyerNameCombo();
        updateLastVoucher();
    }

    private void refreshCollectionTableView() {
        collection_tableview.setItems(FXCollections.observableArrayList(collectionItemList));
    }

    private void fillBuyerNameCombo() {
        try {
            buyerNameComboList = buyerService.getAllBuyers();
            buyer_name.setItems(FXCollections.observableArrayList(buyerNameComboList));
        } catch (SQLException ex) {
            Utils.showAlert(ex.toString());
            logger.log(Level.SEVERE, ex.toString(), ex);
        }
    }

    @FXML
    private void fillBillNoCombo() {
        if (buyer_name.getValue() == null) {
            return;
        }
        try {
            billNoComboList = collectionService.fetchPendingBillNosForBuyer(buyer_name.getValue());
            bill_no_combo.setItems(FXCollections.observableArrayList(billNoComboList));
        } catch (SQLException ex) {
            Utils.showAlert(ex.toString());
            logger.log(Level.SEVERE, ex.toString(), ex);
        }
    }

    @FXML
    private void fetchData() {
        if (null == bill_no_combo.getValue()) {
            return;
        }
        try {
            BillEntity billEntity = billService.getBill(bill_no_combo.getValue());
            supplier_name.setText(billEntity.getSupplierName());
            bill_date.setText(billEntity.getBillDate());
            bill_amount.setText(billEntity.getBillAmount());
            previouslyDue = collectionService.fetchPendingAmountForBillNo(bill_no_combo.getValue());
            updateCollectionDue();
        } catch (SQLException ex) {
            Utils.showAlert(ex.toString());
            logger.log(Level.SEVERE, ex.toString(), ex);
        }
    }

    @FXML
    private void addCollection(ActionEvent ignoredEvent) {
        if (bill_no_combo.getValue().isEmpty()
                || bill_amount.getText().isEmpty()
                || buyer_name.getValue().isEmpty()
                || supplier_name.getText().isEmpty()
                || bank_field.getText().isEmpty()
                || collection_due_field.getText().isEmpty()
                || amount_collected_field.getText().isEmpty()
                || dd_no_field.getText().isEmpty()
                || dd_date_field.getValue().toString().isEmpty()) {
            Utils.showAlert("Please check whether the fields are properly filled or not.", 2);
            return;
        }

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(Constants.DATE_TIME_FORMAT);

        collectionItemList.add(new CollectionItem(
                bill_no_combo.getValue(),
                bill_date.getText(),
                bill_amount.getText(),
                supplier_name.getText(),
                collection_due_field.getText(),
                amount_collected_field.getText(),
                bank_field.getText(),
                dd_no_field.getText(),
                dateTimeFormatter.format(dd_date_field.getValue())));

        refreshCollectionTableView();
        updateTotalAmount();
        clearRepeatingFields();
    }

    @FXML
    private void updateCollectionDue() {
        int amountCollected = 0;
        try {
            amountCollected = Integer.parseInt(amount_collected_field.getText());
        } catch (Exception ex) {
            logger.log(Level.WARNING, "cannot be converted to integer");
        }
        collection_due_field.setText(Integer.toString(previouslyDue - amountCollected));
    }

    private void updateTotalAmount() {
        int totalAmount = 0;
        for (CollectionItem collectionItem : collectionItemList) {
            totalAmount += Integer.parseInt(collectionItem.getAmountCollected());
        }
        total_amount_field.setText(Integer.toString(totalAmount));
    }

    private void clearRepeatingFields() {
        bill_date.setText("");
        supplier_name.setText("");
        bill_amount.setText("");
        collection_due_field.setText("0");
        amount_collected_field.setText("0");
        bank_field.setText("");
        dd_no_field.setText("");

        dd_date_field.setValue(null);
    }

    @FXML
    private void replaceCollection(ActionEvent ignoredEvent) {
        if (collection_tableview.getSelectionModel().getSelectedIndex() == -1) {
            Utils.showAlert("Please select an item from the Collection Table", 2);
            return;
        }
        if (bill_no_combo.getValue().isEmpty()
                || bill_amount.getText().isEmpty()
                || buyer_name.getValue().isEmpty()
                || supplier_name.getText().isEmpty()
                || bank_field.getText().isEmpty()
                || collection_due_field.getText().isEmpty()
                || amount_collected_field.getText().isEmpty()
                || dd_no_field.getText().isEmpty()
                || dd_date_field.getValue().toString().isEmpty()) {
            Utils.showAlert("Please check whether the fields are properly filled or not.", 2);
            return;
        }
        collectionItemList.set(
                collection_tableview.getSelectionModel().getSelectedIndex(),
                CollectionItem.builder()
                        .billNo(bill_no_combo.getValue())
                        .billDate(bill_date.getText())
                        .billAmount(bill_amount.getText())
                        .supplierName(supplier_name.getText())
                        .due(collection_due_field.getText())
                        .amountCollected(amount_collected_field.getText())
                        .bank(bank_field.getText())
                        .ddNo(dd_no_field.getText())
                        .ddDate(dd_date_field.getValue().toString())
                        .build());
        refreshCollectionTableView();
        updateTotalAmount();
    }

    @FXML
    private void deleteCollection(ActionEvent ignoredEvent) {
        if (collection_tableview.getSelectionModel().getSelectedIndex() == -1) {
            Utils.showAlert("Please select an item from the LR Table", 2);
            return;
        }
        collectionItemList.remove(collection_tableview.getSelectionModel().getSelectedIndex());
        refreshCollectionTableView();
        updateTotalAmount();
    }

    @FXML
    private void clearAllFields() {
        clearRepeatingFields();
        previouslyDue = 0;
        collectionItemList.clear();
        refreshCollectionTableView();
        updateTotalAmount();
        voucher_no_field.setText("");
        voucher_date_field.setValue(null);
        buyer_name.setDisable(false);
        buyer_name.getSelectionModel().clearSelection();
        billNoComboList = new ArrayList<>();
        bill_no_combo.setItems(FXCollections.observableArrayList(billNoComboList));
    }

    @FXML
    private void fetchCollectionVoucher() {
        String voucherNo = voucher_no_field.getText().trim();
        if (voucherNo.isEmpty()) {
            Utils.showAlert("Please enter a voucher number.", 2);
            return;
        }
        try {
            CollectionEntity voucher = collectionService.getCollection(voucherNo);
            if (voucher == null) {
                Utils.showAlert("Collection voucher not found", 2);
                return;
            }
            List<CollectionItem> items = new ArrayList<>();
            for (CollectionItemEntity item : voucher.getItems()) {
                items.add(CollectionItem.builder()
                        .billNo(item.getBillNo())
                        .billDate(item.getBillDate())
                        .billAmount(item.getBillAmount())
                        .supplierName(item.getSupplierName())
                        .due(item.getDue())
                        .amountCollected(item.getAmountCollected())
                        .bank(item.getBank())
                        .ddNo(item.getDdNo())
                        .ddDate(item.getDdDate())
                        .build());
            }
            clearRepeatingFields();
            previouslyDue = 0;
            bill_no_combo.getSelectionModel().clearSelection();
            voucher_no_field.setText(voucherNo);
            voucher_date_field.setValue(Utils.parseDate(voucher.getVoucherDate()));
            buyer_name.setValue(voucher.getBuyerName());
            collectionItemList = items;
            refreshCollectionTableView();
            total_amount_field.setText(voucher.getTotalAmount());
        } catch (SQLException ex) {
            Utils.showAlert(ex.toString());
            logger.log(Level.SEVERE, "Failed to fetch collection voucher", ex);
        }
    }

    @FXML
    private void printCollectionVoucher() {
        String voucherNo = voucher_no_field.getText().trim();
        if (voucherNo.isEmpty()) {
            Utils.showAlert("Please enter a voucher number.", 2);
            return;
        }
        try {
            if (collectionService.getCollection(voucherNo) == null) {
                Utils.showAlert("Collection voucher not found", 2);
                return;
            }
            collectionService.generatePdf(voucherNo);
            Utils.launchPdf(Constants.REPORT_FILE_NAME);
            Utils.showAlert("Report Successfully Generated", 1);
        } catch (IOException | JRException | SQLException ex) {
            Utils.showAlert(ex.toString());
            logger.log(Level.SEVERE, "Failed to print collection voucher", ex);
        }
    }

    @FXML
    private void deleteCollectionVoucher() {
        String voucherNo = voucher_no_field.getText().trim();
        if (voucherNo.isEmpty()) {
            Utils.showAlert("Please enter a voucher number.", 2);
            return;
        }
        try {
            if (collectionService.getCollection(voucherNo) == null) {
                Utils.showAlert("Collection voucher not found", 2);
                return;
            }
            Alert alert = new Alert(
                    Alert.AlertType.CONFIRMATION,
                    "Are you sure that you want to delete collection voucher " + voucherNo + "?",
                    ButtonType.YES,
                    ButtonType.NO);
            if (alert.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) {
                return;
            }
            collectionService.deleteCollection(voucherNo);
            clearAllFields();
            updateLastVoucher();
            Utils.showAlert(voucherNo + " was successfully deleted.", 1);
        } catch (SQLException ex) {
            Utils.showAlert(ex.toString());
            logger.log(Level.SEVERE, "Failed to delete collection voucher", ex);
        }
    }

    @FXML
    private void saveData(ActionEvent ignoredEvent) {
        Alert alert = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Are you sure that you want save " + voucher_no_field.getText() + " ?",
                ButtonType.YES,
                ButtonType.NO,
                ButtonType.CANCEL);
        alert.showAndWait();

        if (alert.getResult() != ButtonType.YES) {
            return;
        }
        if (voucher_no_field.getText().isEmpty()
                || voucher_date_field.getValue() == null
                || voucher_date_field.getValue().toString().isEmpty()) {
            Utils.showAlert("Check whether the Voucher No. and the Voucher Date is properly filled", 2);
            return;
        }

        List<CollectionItemEntity> collectionItemEntities = collectionItemList.stream()
                .map(it -> CollectionItemEntity.builder()
                        .billNo(it.getBillNo())
                        .billDate(formatDate(it.getBillDate()))
                        .billAmount(it.getBillAmount())
                        .supplierName(it.getSupplierName())
                        .due(it.getDue())
                        .amountCollected(it.getAmountCollected())
                        .bank(it.getBank())
                        .ddNo(it.getDdNo())
                        .ddDate(it.getDdDate())
                        .build())
                .toList();

        CollectionEntity collectionEntity = CollectionEntity.builder()
                .voucherNo(voucher_no_field.getText())
                .voucherDate(formatDate(voucher_date_field.getValue().toString()))
                .buyerName(buyer_name.getValue())
                .totalAmount(total_amount_field.getText())
                .items(collectionItemEntities)
                .build();
        try {
            collectionService.saveCollection(collectionEntity);
            Utils.showAlert("Saved Successfully", 1);
        } catch (SQLException ex) {
            Utils.showAlert(ex.toString());
            logger.log(Level.SEVERE, ex.toString(), ex);
            return;
        }

        clearAllFields();
        updateLastVoucher();
    }

    private void updateLastVoucher() {
        try {
            String lastVoucher = collectionService.getLastVoucher();
            last_voucher_field.setText("Last Voucher No. : " + lastVoucher);
            String upcomingVoucherNo = Utils.nextVoucherNumber(lastVoucher);
            voucher_no_field.setText(upcomingVoucherNo);
        } catch (SQLException ex) {
            Utils.showAlert(ex.toString());
            logger.log(Level.SEVERE, ex.toString(), ex);
        }
    }
}
