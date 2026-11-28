package functional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import openconsignment.controller.entry.PaymentEntryController;
import openconsignment.entity.BillEntity;
import openconsignment.entity.PaymentEntity;
import openconsignment.entity.PaymentItemEntity;
import openconsignment.model.PaymentItem;
import openconsignment.service.BillService;
import openconsignment.service.PaymentService;
import openconsignment.service.SupplierService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.testfx.framework.junit5.ApplicationTest;

class PaymentEntryTest extends ApplicationTest {
    private PaymentService payments;

    @Override
    public void start(Stage stage) throws Exception {
        payments = mock(PaymentService.class);
        BillService bills = mock(BillService.class);
        SupplierService suppliers = mock(SupplierService.class);
        when(suppliers.getAllSuppliers()).thenReturn(List.of("Supplier A"));
        when(payments.getLastVoucher()).thenReturn("0009");
        when(payments.fetchPendingBillNosForSupplier(nullable(String.class)))
                .thenAnswer(ignored -> new ArrayList<>(List.of("B1")));
        when(payments.fetchPendingAmountForBillNo("B1")).thenReturn(1000);
        when(bills.getBill("B1"))
                .thenReturn(BillEntity.builder()
                        .billNo("B1")
                        .buyerName("Buyer A")
                        .billDate("20-10-2026")
                        .billAmount("1000")
                        .build());
        when(payments.getPayment("P-100"))
                .thenReturn(PaymentEntity.builder()
                        .voucherNo("P-100")
                        .voucherDate("27-10-2026")
                        .supplierName("Supplier A")
                        .totalAmount("250")
                        .items(List.of(PaymentItemEntity.builder()
                                .billNo("B1")
                                .billAmount("1000")
                                .billDate("20-10-2026")
                                .buyerName("Buyer A")
                                .amountPaid("250")
                                .bank("Bank")
                                .ddNo("DD-1")
                                .ddDate("27-10-2026")
                                .build()))
                        .build());

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/entry/PaymentEntry.fxml"));
        loader.setControllerFactory(type -> new PaymentEntryController(bills, payments, suppliers));
        Parent root = loader.load();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @Test
    void addReplaceAndSavePaymentUsesUpdatedAmountAndDue() throws Exception {
        interact(() -> {
            assertEquals("0010", text("#voucher_no_field").getText());
            date("#voucher_date_field").setValue(LocalDate.of(2026, 10, 27));
            combo("#supplier_name_combo").setValue("Supplier A");
            fillBill("250");
            assertEquals("750", text("#due_amount_field").getText());
            button("#add_payment_btn").fire();
            assertEquals("250", text("#total_amount_paid_field").getText());
            assertEquals(1, table().getItems().size());

            table().getSelectionModel().select(0);
            fillBill("400");
            button("#replace_payment_btn").fire();
            assertEquals("400", text("#total_amount_paid_field").getText());
            assertEquals("600", table().getItems().get(0).getDue());
        });
        clickOn("#save_btn");
        clickOn("Yes");
        clickOn("OK");

        ArgumentCaptor<PaymentEntity> saved = ArgumentCaptor.forClass(PaymentEntity.class);
        verify(payments).savePayment(saved.capture());
        PaymentEntity voucher = saved.getValue();
        assertEquals("0010", voucher.getVoucherNo());
        assertEquals("27-10-2026", voucher.getVoucherDate());
        assertEquals("Supplier A", voucher.getSupplierName());
        assertEquals("400", voucher.getTotalAmount());
        assertEquals(1, voucher.getItems().size());
        PaymentItemEntity item = voucher.getItems().get(0);
        assertEquals("B1", item.getBillNo());
        assertEquals("Buyer A", item.getBuyerName());
        assertEquals("600", item.getDueAmount());
        assertEquals("400", item.getAmountPaid());
        assertEquals("27-10-2026", item.getDdDate());
        interact(() -> assertTrue(table().getItems().isEmpty()));
    }

    @Test
    void fetchVoucherAndRemoveItemUpdatesTotal() {
        interact(() -> text("#voucher_no_field").setText("P-100"));
        clickOn("#fetch_payment_voucher_btn");
        interact(() -> {
            assertEquals(LocalDate.of(2026, 10, 27), date("#voucher_date_field").getValue());
            assertEquals("Supplier A", combo("#supplier_name_combo").getValue());
            assertEquals("250", text("#total_amount_paid_field").getText());
            assertEquals("B1", table().getItems().get(0).getBillNo());
            table().getSelectionModel().select(0);
            button("#delete_payment_btn").fire();
            assertTrue(table().getItems().isEmpty());
            assertEquals("0", text("#total_amount_paid_field").getText());
        });
    }

    @Test
    void cancelledSaveDoesNotPersistVoucher() throws Exception {
        clickOn("#save_btn");
        clickOn("No");
        verify(payments, never()).savePayment(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void failedSaveKeepsVoucherAndItemsForRetry() throws Exception {
        doThrow(new SQLException("save failed"))
                .doNothing()
                .when(payments)
                .savePayment(org.mockito.ArgumentMatchers.any());
        interact(() -> {
            date("#voucher_date_field").setValue(LocalDate.of(2026, 10, 27));
            combo("#supplier_name_combo").setValue("Supplier A");
            fillBill("250");
            button("#add_payment_btn").fire();
        });
        clickOn("#save_btn");
        clickOn("Yes");
        clickOn("OK");
        interact(() -> {
            assertEquals("0010", text("#voucher_no_field").getText());
            assertEquals(LocalDate.of(2026, 10, 27), date("#voucher_date_field").getValue());
            assertEquals("Supplier A", combo("#supplier_name_combo").getValue());
            assertEquals("250", text("#total_amount_paid_field").getText());
            assertEquals(1, table().getItems().size());
            assertEquals("250", table().getItems().get(0).getAmountPaid());
        });
        clickOn("#save_btn");
        clickOn("Yes");
        clickOn("OK");
        verify(payments, times(2)).savePayment(org.mockito.ArgumentMatchers.any());
        interact(() -> assertTrue(table().getItems().isEmpty()));
    }

    private void fillBill(String amount) {
        ComboBox<String> bill = combo("#bill_no_combo");
        bill.setValue(null);
        bill.setValue("B1");
        TextField paid = text("#amount_paid_field");
        paid.setText(amount);
        paid.fireEvent(new KeyEvent(KeyEvent.KEY_RELEASED, "", "", KeyCode.DIGIT1, false, false, false, false));
        text("#bank_field").setText("Bank");
        text("#dd_no_field").setText("DD-1");
        date("#dd_date_field").setValue(LocalDate.of(2026, 10, 27));
    }

    private Button button(String id) {
        return lookup(id).queryButton();
    }

    private TextField text(String id) {
        return lookup(id).query();
    }

    private DatePicker date(String id) {
        return lookup(id).query();
    }

    private ComboBox<String> combo(String id) {
        return lookup(id).queryComboBox();
    }

    private TableView<PaymentItem> table() {
        return lookup("#payment_tableview").queryTableView();
    }
}
