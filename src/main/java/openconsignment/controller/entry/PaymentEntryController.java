package openconsignment.controller.entry;

import static openconsignment.common.Utils.formatDate;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javax.inject.Inject;
import net.sf.jasperreports.engine.JRException;
import openconsignment.common.Constants;
import openconsignment.common.Utils;
import openconsignment.entity.BillEntity;
import openconsignment.entity.PaymentEntity;
import openconsignment.entity.PaymentItemEntity;
import openconsignment.model.PaymentItem;
import openconsignment.service.BillService;
import openconsignment.service.PaymentService;
import openconsignment.service.SupplierService;

@SuppressWarnings("unused")
public class PaymentEntryController implements Initializable {

    private List<PaymentItem> paymentItems;
    private List<String> supplierNameComboList;
    private List<String> billNoComboList;
    private int previouslyDue;

    private static final Logger logger = Logger.getLogger(PaymentEntryController.class.getName());

    @FXML
    private TextField dd_no_field;

    @FXML
    private DatePicker dd_date_field;

    @FXML
    private DatePicker voucher_date_field;

    @FXML
    private TextField voucher_no_field;

    @FXML
    private Button add_payment_btn;

    @FXML
    private Button replace_payment_btn;

    @FXML
    private Button delete_payment_btn;

    @FXML
    private TableColumn<?, ?> bill_no_col;

    @FXML
    private TableColumn<?, ?> bill_amt_col;

    @FXML
    private TableColumn<?, ?> dd_no_col;

    @FXML
    private TableColumn<?, ?> dd_date_col;

    @FXML
    private TableColumn<?, ?> bill_dt_col;

    @FXML
    private TableColumn<?, ?> buyer_col;

    @FXML
    private TableColumn<?, ?> due_col;

    @FXML
    private TableColumn<?, ?> amount_paid_col;

    @FXML
    private TableView<PaymentItem> payment_tableview;

    @FXML
    private TextField amount_paid_field;

    @FXML
    private TextField buyer_name_field;

    @FXML
    private TextField bill_date_field;

    @FXML
    private TextField bill_amount_field;

    @FXML
    private ComboBox<String> supplier_name_combo;

    @FXML
    private ComboBox<String> bill_no_combo;

    @FXML
    private TextField due_amount_field;

    @FXML
    private TableColumn<?, ?> bank_col;

    @FXML
    private TextField bank_field;

    @FXML
    private TextField total_amount_paid_field;

    @FXML
    private Button save_btn;

    @FXML
    private Button clear_btn;

    @FXML
    private Text last_voucher_field;

    private final BillService billService;
    private final PaymentService paymentService;
    private final SupplierService supplierService;

    @Inject
    public PaymentEntryController(
            BillService billService, PaymentService paymentService, SupplierService supplierService) {
        this.billService = billService;
        this.paymentService = paymentService;
        this.supplierService = supplierService;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        paymentItems = new ArrayList<>();
        supplierNameComboList = new ArrayList<>();
        billNoComboList = new ArrayList<>();

        bill_no_col.setCellValueFactory(new PropertyValueFactory<>("billNo"));
        bill_amt_col.setCellValueFactory(new PropertyValueFactory<>("billAmount"));
        bill_dt_col.setCellValueFactory(new PropertyValueFactory<>("billDate"));
        buyer_col.setCellValueFactory(new PropertyValueFactory<>("buyerName"));
        bank_col.setCellValueFactory(new PropertyValueFactory<>("bank"));
        dd_no_col.setCellValueFactory(new PropertyValueFactory<>("ddNo"));
        dd_date_col.setCellValueFactory(new PropertyValueFactory<>("ddDate"));
        due_col.setCellValueFactory(new PropertyValueFactory<>("due"));
        amount_paid_col.setCellValueFactory(new PropertyValueFactory<>("amountPaid"));

        refreshPaymentTableView();
        fillSupplierCombo();

        updateLastVoucher();
    }

    private void refreshPaymentTableView() {
        payment_tableview.setItems(FXCollections.observableArrayList(paymentItems));
    }

    @FXML
    private void addPayment(ActionEvent ignoredEvent) {
        if (bill_no_combo.getValue().isEmpty()
                || bill_amount_field.getText().isEmpty()
                || buyer_name_field.getText().isEmpty()
                || supplier_name_combo.getValue().isEmpty()
                || bank_field.getText().isEmpty()
                || bank_field.getText().isEmpty()
                || due_amount_field.getText().isEmpty()
                || amount_paid_field.getText().isEmpty()
                || dd_no_field.getText().isEmpty()
                || dd_date_field.getValue().toString().isEmpty()) {
            Utils.showAlert("Please check whether the fields are properly filled or not.", 2);
            return;
        }

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(Constants.DATE_TIME_FORMAT);

        paymentItems.add(new PaymentItem(
                bill_no_combo.getValue(),
                bill_amount_field.getText(),
                bill_date_field.getText(),
                buyer_name_field.getText(),
                due_amount_field.getText(),
                amount_paid_field.getText(),
                bank_field.getText(),
                dd_no_field.getText(),
                dateTimeFormatter.format(dd_date_field.getValue())));

        refreshPaymentTableView();

        clearRepeatingFields();
        updateTotalAmountPaid();
    }

    @FXML
    private void replacePayment(ActionEvent ignoredEvent) {

        if (payment_tableview.getSelectionModel().getSelectedIndex() == -1) {
            Utils.showAlert("Please select an item from the Payment Table", 2);
            return;
        }

        if (bill_no_combo.getValue().isEmpty()
                || bill_amount_field.getText().isEmpty()
                || buyer_name_field.getText().isEmpty()
                || supplier_name_combo.getValue().isEmpty()
                || bank_field.getText().isEmpty()
                || bank_field.getText().isEmpty()
                || due_amount_field.getText().isEmpty()
                || amount_paid_field.getText().isEmpty()
                || dd_no_field.getText().isEmpty()
                || dd_date_field.getValue().toString().isEmpty()) {
            Utils.showAlert("Please check whether the fields are properly filled or not.", 2);
            return;
        }

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(Constants.DATE_TIME_FORMAT);

        paymentItems.set(
                payment_tableview.getSelectionModel().getSelectedIndex(),
                new PaymentItem(
                        bill_no_combo.getValue(),
                        bill_amount_field.getText(),
                        bill_date_field.getText(),
                        buyer_name_field.getText(),
                        due_amount_field.getText(),
                        amount_paid_field.getText(),
                        bank_field.getText(),
                        dd_no_field.getText(),
                        dateTimeFormatter.format(dd_date_field.getValue())));

        refreshPaymentTableView();

        clearRepeatingFields();
        updateTotalAmountPaid();
    }

    @FXML
    private void deletePayment(ActionEvent ignoredEvent) {
        if (payment_tableview.getSelectionModel().getSelectedIndex() == -1) {
            Utils.showAlert("Please select an item from the Payment Table", 2);
            return;
        }
        paymentItems.remove(payment_tableview.getSelectionModel().getSelectedIndex());
        refreshPaymentTableView();
        updateTotalAmountPaid();
    }

    private void clearRepeatingFields() {
        bill_date_field.setText("");
        buyer_name_field.setText("");
        bill_amount_field.setText("0");
        due_amount_field.setText("0");
        amount_paid_field.setText("0");
        bank_field.setText("");
        dd_no_field.setText("");
        dd_date_field.setValue(null);
    }

    private void updateTotalAmountPaid() {
        int totalAmountPaid = 0;
        for (PaymentItem paymentItem : paymentItems) {
            totalAmountPaid += Integer.parseInt(paymentItem.getAmountPaid());
        }
        total_amount_paid_field.setText(Integer.toString(totalAmountPaid));
    }

    @FXML
    private void clearAllFields() {
        clearRepeatingFields();
        voucher_no_field.setText("");
        voucher_date_field.setValue(null);
        supplier_name_combo.setDisable(false);
        supplier_name_combo.getSelectionModel().clearSelection();
        billNoComboList.clear();
        bill_no_combo.setItems(FXCollections.observableArrayList(billNoComboList));
        total_amount_paid_field.setText("");
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

        List<PaymentItemEntity> paymentItemEntities = paymentItems.stream()
                .map(it -> PaymentItemEntity.builder()
                        .voucherNo(voucher_no_field.getText())
                        .buyerName(it.getBuyerName())
                        .billNo(it.getBillNo())
                        .billDate(formatDate(it.getBillDate()))
                        .billAmount(it.getBillAmount())
                        .dueAmount(null == it.getDue() ? "0" : it.getDue())
                        .amountPaid(it.getAmountPaid())
                        .bank(it.getBank())
                        .ddNo(it.getDdNo())
                        .ddDate(formatDate(it.getDdDate()))
                        .build())
                .toList();

        PaymentEntity paymentEntity = PaymentEntity.builder()
                .voucherNo(voucher_no_field.getText())
                .voucherDate(formatDate(voucher_date_field.getValue().toString()))
                .supplierName(supplier_name_combo.getValue())
                .totalAmount(total_amount_paid_field.getText())
                .items(paymentItemEntities)
                .build();

        try {
            paymentService.savePayment(paymentEntity);
            Utils.showAlert("Saved Successfully", 1);
            clearAllFields();
        } catch (SQLException ex) {
            Utils.showAlert(ex.toString());
            Logger.getLogger(PaymentEntryController.class.getName()).log(Level.SEVERE, ex.toString(), ex);
        }

        clearAllFields();
        paymentItems.clear();
        refreshPaymentTableView();
        updateLastVoucher();
    }

    private void updateLastVoucher() {
        try {
            String lastVoucher = paymentService.getLastVoucher();
            last_voucher_field.setText("Last Voucher No. : " + lastVoucher);
            String upcomingVoucherNo = Utils.nextVoucherNumber(lastVoucher);
            voucher_no_field.setText(upcomingVoucherNo);
        } catch (SQLException ex) {
            Utils.showAlert(ex.toString());
            logger.log(Level.SEVERE, ex.toString(), ex);
        }
    }

    private void fillSupplierCombo() {
        try {
            supplierNameComboList = supplierService.getAllSuppliers();
            supplier_name_combo.setItems(FXCollections.observableArrayList(supplierNameComboList));
        } catch (SQLException ex) {
            Utils.showAlert(ex.toString());
            logger.log(Level.SEVERE, "Failed to populate the supplier combo", ex);
        }
    }

    @FXML
    private void fillBillNoCombo(ActionEvent ignoredEvent) {
        try {
            billNoComboList = paymentService.fetchPendingBillNosForSupplier(supplier_name_combo.getValue());
            bill_no_combo.setItems(FXCollections.observableArrayList(billNoComboList));
        } catch (SQLException ex) {
            Utils.showAlert(ex.toString());
            logger.log(Level.SEVERE, "Failed to populate bill number combo", ex);
        }
    }

    @FXML
    private void fetchBillNos(ActionEvent ignoredEvent) {
        if (null == bill_no_combo.getValue()) {
            return;
        }
        try {
            BillEntity billEntity = billService.getBill(bill_no_combo.getValue());
            buyer_name_field.setText(billEntity.getBuyerName());
            bill_date_field.setText(billEntity.getBillDate());
            bill_amount_field.setText(billEntity.getBillAmount());
            previouslyDue = paymentService.fetchPendingAmountForBillNo(bill_no_combo.getValue());
            updateDueAmount();
        } catch (SQLException ex) {
            Utils.showAlert(ex.toString());
            logger.log(Level.SEVERE, ex.toString(), ex);
        }
    }

    @FXML
    private void updateDueAmount() {
        int amountPaid = 0;
        try {
            amountPaid = Integer.parseInt(amount_paid_field.getText());
        } catch (Exception ex) {
            logger.log(Level.WARNING, "cannot be converted to integer");
        }
        due_amount_field.setText(Integer.toString(previouslyDue - amountPaid));
    }

    public void fetchPaymentVoucher() {
        if (voucher_no_field.getText().isEmpty()) {
            Utils.showAlert("Voucher No. Field is kept empty. Please fill the voucher no.");
            return;
        }

        try {
            PaymentEntity paymentEntity = paymentService.getPayment(voucher_no_field.getText());

            if (null == paymentEntity) {
                Utils.showAlert("Payment voucher not found");
                return;
            }

            paymentItems = paymentEntity.getItems().stream()
                    .map(it -> PaymentItem.builder()
                            .billNo(it.getBillNo())
                            .billAmount(it.getBillAmount())
                            .billDate(it.getBillDate())
                            .buyerName(it.getBuyerName())
                            .amountPaid(it.getAmountPaid())
                            .bank(it.getBank())
                            .ddNo(it.getDdNo())
                            .ddDate(it.getDdDate())
                            .build())
                    .collect(Collectors.toCollection(ArrayList::new));

            voucher_date_field.setValue(Utils.parseDate(paymentEntity.getVoucherDate()));
            supplier_name_combo.setValue(paymentEntity.getSupplierName());
            total_amount_paid_field.setText(paymentEntity.getTotalAmount());
            //            display_board_label.setText(paymentEntity.getSupplierName());

            payment_tableview.setItems(FXCollections.observableArrayList(paymentItems));
            //            delete_entry_btn.setDisable(false);
        } catch (SQLException ex) {
            Utils.showAlert(ex.toString());
            Logger.getLogger(PaymentEntryController.class.getName()).log(Level.SEVERE, ex.toString(), ex);
        }
    }

    public void printPaymentVoucher() {
        try {
            paymentService.generatePdf(voucher_no_field.getText());
            Utils.launchPdf(Constants.REPORT_FILE_NAME);
            Utils.showAlert("Report Successfully Generated", 1);
        } catch (IOException | JRException | SQLException ex) {
            Utils.showAlert(ex.toString());
            Logger.getLogger(PaymentEntryController.class.getName()).log(Level.SEVERE, ex.toString(), ex);
        }
    }

    public void deletePaymentVoucher() {
        Alert alert = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Are you sure that you want delete " + voucher_no_field.getText() + " ?",
                ButtonType.YES,
                ButtonType.NO,
                ButtonType.CANCEL);
        alert.showAndWait();

        if (alert.getResult() != ButtonType.YES) {
            return;
        }

        try {
            paymentService.deletePayment(voucher_no_field.getText());

            Utils.showAlert(voucher_no_field.getText().toUpperCase() + " Entry was successfully deleted.", 1);
        } catch (SQLException ex) {
            Utils.showAlert(ex.toString());
            Logger.getLogger(PaymentEntryController.class.getName()).log(Level.SEVERE, ex.toString(), ex);
        }
    }
}
