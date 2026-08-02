package openconsignment.controller.entry.payment;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javax.inject.Inject;
import net.sf.jasperreports.engine.*;
import openconsignment.common.Constants;
import openconsignment.common.Utils;
import openconsignment.entity.PaymentEntity;
import openconsignment.model.PaymentItem;
import openconsignment.service.PaymentService;

public class ViewDeletePaymentController implements Initializable {

    @FXML
    private TextField voucher_no_field;

    @FXML
    private TableColumn<?, ?> bill_no_col;

    @FXML
    private TableColumn<?, ?> bill_amt_col;

    @FXML
    private TableColumn<?, ?> bill_date_col;

    @FXML
    private TableColumn<?, ?> buyer_col;

    @FXML
    private TableColumn<?, ?> amount_paid_col;

    @FXML
    private TableColumn<?, ?> bank_col;

    @FXML
    private TableColumn<?, ?> dd_no_col;

    @FXML
    private TableColumn<?, ?> dd_date_col;

    @FXML
    private Label display_board_label;

    @FXML
    private TextField total_amount_field;

    @FXML
    private Button get_details_btn;

    @FXML
    private Button delete_entry_btn;

    @FXML
    private TextField voucher_date;

    @FXML
    private TextField supplier_name;

    @FXML
    private TableColumn<?, ?> due_col;

    @FXML
    private TableView<PaymentItem> payment_tableview;

    @FXML
    private Button print_payment_btn;

    private final PaymentService paymentService;
    private final Connection conn;

    @Inject
    public ViewDeletePaymentController(PaymentService paymentService, Connection conn) {
        this.paymentService = paymentService;
        this.conn = conn;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        bill_no_col.setCellValueFactory(new PropertyValueFactory<>("billNo"));
        bill_amt_col.setCellValueFactory(new PropertyValueFactory<>("billAmount"));
        buyer_col.setCellValueFactory(new PropertyValueFactory<>("buyerName"));
        bill_date_col.setCellValueFactory(new PropertyValueFactory<>("billDate"));
        due_col.setCellValueFactory(new PropertyValueFactory<>("due"));
        amount_paid_col.setCellValueFactory(new PropertyValueFactory<>("amountPaid"));
        dd_no_col.setCellValueFactory(new PropertyValueFactory<>("ddNo"));
        bank_col.setCellValueFactory(new PropertyValueFactory<>("bank"));
        dd_date_col.setCellValueFactory(new PropertyValueFactory<>("ddDate"));
    }

    @FXML
    private void getDetails(ActionEvent event) {

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

            List<PaymentItem> paymentItemList = paymentEntity.getItems().stream()
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
                    .toList();

            voucher_date.setText(paymentEntity.getVoucherDate());
            supplier_name.setText(paymentEntity.getSupplierName());
            total_amount_field.setText(paymentEntity.getTotalAmount());
            display_board_label.setText(paymentEntity.getSupplierName());

            payment_tableview.setItems(FXCollections.observableArrayList(paymentItemList));
            delete_entry_btn.setDisable(false);
        } catch (SQLException ex) {
            Utils.showAlert(ex.toString());
            Logger.getLogger(ViewDeletePaymentController.class.getName()).log(Level.SEVERE, ex.toString(), ex);
        }
    }

    @FXML
    private void deleteEntry(ActionEvent event) {
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

            payment_tableview.setItems(FXCollections.observableArrayList());

            voucher_date.setText("");
            supplier_name.setText("");
            display_board_label.setText("");
            total_amount_field.setText("");
            Utils.showAlert(voucher_no_field.getText().toUpperCase() + " Entry was successfully deleted.", 1);
        } catch (SQLException ex) {
            Utils.showAlert(ex.toString());
            try {
                conn.rollback();
            } catch (SQLException ex1) {
                Utils.showAlert(ex1.toString());
                Logger.getLogger(PaymentEntryController.class.getName()).log(Level.SEVERE, ex1.toString(), ex1);
            }
            Logger.getLogger(PaymentEntryController.class.getName()).log(Level.SEVERE, ex.toString(), ex);
        }
    }

    @FXML
    private void printPayment(ActionEvent event) {
        try {
            paymentService.generatePdf(voucher_no_field.getText());
            Utils.launchPdf(Constants.REPORT_FILE_NAME);
            Utils.showAlert("Report Successfully Generated", 1);
        } catch (IOException | JRException | SQLException ex) {
            Utils.showAlert(ex.toString());
            Logger.getLogger(PaymentEntryController.class.getName()).log(Level.SEVERE, ex.toString(), ex);
        }
    }
}
