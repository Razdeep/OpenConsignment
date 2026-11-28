package functional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.time.LocalDate;
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
import openconsignment.controller.entry.CollectionEntryController;
import openconsignment.entity.BillEntity;
import openconsignment.entity.CollectionEntity;
import openconsignment.entity.CollectionItemEntity;
import openconsignment.model.CollectionItem;
import openconsignment.service.BillService;
import openconsignment.service.BuyerService;
import openconsignment.service.CollectionService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.testfx.framework.junit5.ApplicationTest;

class CollectionEntryTest extends ApplicationTest {
    private CollectionService collectionService;

    @Override
    public void start(Stage stage) throws Exception {
        collectionService = mock(CollectionService.class);
        BillService billService = mock(BillService.class);
        BuyerService buyerService = mock(BuyerService.class);
        when(buyerService.getAllBuyers()).thenReturn(List.of("Acme Buyer"));
        when(collectionService.fetchPendingBillNosForBuyer("Acme Buyer")).thenReturn(List.of("B1"));
        when(collectionService.fetchPendingAmountForBillNo("B1")).thenReturn(1000);
        when(billService.getBill("B1"))
                .thenReturn(BillEntity.builder()
                        .billNo("B1")
                        .supplierName("Acme Supplier")
                        .billDate("20-10-2026")
                        .billAmount("1000")
                        .build());
        when(collectionService.getLastVoucher()).thenReturn("C-100");
        when(collectionService.getCollection("C-100"))
                .thenReturn(CollectionEntity.builder()
                        .voucherNo("C-100")
                        .voucherDate("27-10-2026")
                        .buyerName("Acme Buyer")
                        .totalAmount("250")
                        .items(List.of(CollectionItemEntity.builder()
                                .billNo("B-1")
                                .billDate("20-10-2026")
                                .billAmount("1000")
                                .supplierName("Acme Supplier")
                                .due("750")
                                .amountCollected("250")
                                .bank("Bank")
                                .ddNo("DD-1")
                                .ddDate("27-10-2026")
                                .build()))
                        .build());

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/entry/CollectionEntry.fxml"));
        loader.setControllerFactory(
                type -> new CollectionEntryController(billService, buyerService, collectionService));
        Parent root = loader.load();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @Test
    void fetchLoadsVoucherAndAllowsRemovingRowsAndClearingTheForm() {
        fetchVoucher();

        interact(() -> {
            TableView<CollectionItem> table = lookup("#collection_tableview").queryTableView();
            assertEquals(1, table.getItems().size());
            assertEquals("750", table.getItems().get(0).getDue());
            assertEquals("20-10-2026", table.getItems().get(0).getBillDate());
            assertEquals(
                    "250", lookup("#total_amount_field").queryTextInputControl().getText());
            DatePicker date = lookup("#voucher_date_field").query();
            assertEquals(LocalDate.of(2026, 10, 27), date.getValue());
            assertEquals("Acme Buyer", lookup("#buyer_name").queryComboBox().getValue());
            table.getSelectionModel().select(0);
        });
        clickOn("#delete_collection_btn");
        interact(() -> {
            assertTrue(
                    lookup("#collection_tableview").queryTableView().getItems().isEmpty());
            assertEquals(
                    "0", lookup("#total_amount_field").queryTextInputControl().getText());
        });

        clickOn("#fetch_collection_voucher_btn");
        clickOn("#clear_btn");
        interact(() -> {
            assertEquals("", lookup("#voucher_no_field").queryTextInputControl().getText());
            assertTrue(
                    lookup("#collection_tableview").queryTableView().getItems().isEmpty());
            assertEquals(
                    "0", lookup("#total_amount_field").queryTextInputControl().getText());
        });
    }

    @Test
    void deleteRequiresConfirmationAndClearsVoucherAfterSuccess() throws Exception {
        fetchVoucher();
        clickOn("#delete_collection_voucher_btn");
        clickOn("No");
        verify(collectionService, never()).deleteCollection("C-100");

        clickOn("#delete_collection_voucher_btn");
        clickOn("Yes");
        clickOn("OK");
        verify(collectionService).deleteCollection("C-100");
        interact(() -> {
            assertTrue(
                    lookup("#collection_tableview").queryTableView().getItems().isEmpty());
            assertEquals(
                    "0", lookup("#voucher_no_field").queryTextInputControl().getText());
        });
    }

    @Test
    void addReplaceAndSaveCollectionUsesUpdatedAmountAndDue() throws Exception {
        interact(() -> {
            lookup("#voucher_no_field").queryTextInputControl().setText("C-200");
            ((DatePicker) lookup("#voucher_date_field").query()).setValue(LocalDate.of(2026, 10, 27));
            lookup("#buyer_name").queryComboBox().setValue("Acme Buyer");
            fillBill("250");
            assertEquals(
                    "750",
                    lookup("#collection_due_field").queryTextInputControl().getText());
            ((Button) lookup("#add_collection_btn").query()).fire();
            TableView<CollectionItem> table = lookup("#collection_tableview").queryTableView();
            assertEquals(1, table.getItems().size());
            assertEquals(
                    "250", lookup("#total_amount_field").queryTextInputControl().getText());

            table.getSelectionModel().select(0);
            fillBill("400");
            ((Button) lookup("#replace_collection_btn").query()).fire();
            assertEquals("600", table.getItems().get(0).getDue());
            assertEquals(
                    "400", lookup("#total_amount_field").queryTextInputControl().getText());
        });
        clickOn("#save_btn");
        clickOn("Yes");
        clickOn("OK");

        ArgumentCaptor<CollectionEntity> saved = ArgumentCaptor.forClass(CollectionEntity.class);
        verify(collectionService).saveCollection(saved.capture());
        CollectionEntity voucher = saved.getValue();
        assertEquals("C-200", voucher.getVoucherNo());
        assertEquals("27-10-2026", voucher.getVoucherDate());
        assertEquals("Acme Buyer", voucher.getBuyerName());
        assertEquals("400", voucher.getTotalAmount());
        assertEquals(1, voucher.getItems().size());
        CollectionItemEntity item = voucher.getItems().get(0);
        assertEquals("B1", item.getBillNo());
        assertEquals("Acme Supplier", item.getSupplierName());
        assertEquals("600", item.getDue());
        assertEquals("400", item.getAmountCollected());
        interact(() -> assertTrue(
                lookup("#collection_tableview").queryTableView().getItems().isEmpty()));
    }

    @Test
    void failedSaveKeepsTheVoucherAndItemsForRetry() throws Exception {
        doThrow(new SQLException("save failed"))
                .when(collectionService)
                .saveCollection(org.mockito.ArgumentMatchers.any());
        interact(() -> {
            lookup("#voucher_no_field").queryTextInputControl().setText("C-200");
            ((DatePicker) lookup("#voucher_date_field").query()).setValue(LocalDate.of(2026, 10, 27));
            lookup("#buyer_name").queryComboBox().setValue("Acme Buyer");
            fillBill("250");
            ((Button) lookup("#add_collection_btn").query()).fire();
        });
        clickOn("#save_btn");
        clickOn("Yes");
        clickOn("OK");
        interact(() -> {
            assertEquals(
                    "C-200", lookup("#voucher_no_field").queryTextInputControl().getText());
            assertEquals(
                    "250", lookup("#total_amount_field").queryTextInputControl().getText());
            assertEquals(
                    1,
                    lookup("#collection_tableview").queryTableView().getItems().size());
        });
    }

    private void fillBill(String amount) {
        ComboBox<String> bill = lookup("#bill_no_combo").queryComboBox();
        bill.setValue(null);
        bill.setValue("B1");
        TextField collected = lookup("#amount_collected_field").query();
        collected.setText(amount);
        collected.fireEvent(new KeyEvent(KeyEvent.KEY_RELEASED, "", "", KeyCode.DIGIT1, false, false, false, false));
        lookup("#bank_field").queryTextInputControl().setText("Bank");
        lookup("#dd_no_field").queryTextInputControl().setText("DD-1");
        ((DatePicker) lookup("#dd_date_field").query()).setValue(LocalDate.of(2026, 10, 27));
    }

    private void fetchVoucher() {
        interact(() -> {
            TextField voucherNo = lookup("#voucher_no_field").query();
            voucherNo.setText("C-100");
        });
        clickOn("#fetch_collection_voucher_btn");
    }
}
