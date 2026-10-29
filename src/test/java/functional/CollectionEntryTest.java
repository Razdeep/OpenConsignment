package functional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import openconsignment.controller.entry.CollectionEntryController;
import openconsignment.entity.CollectionEntity;
import openconsignment.entity.CollectionItemEntity;
import openconsignment.model.CollectionItem;
import openconsignment.service.BillService;
import openconsignment.service.BuyerService;
import openconsignment.service.CollectionService;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

class CollectionEntryTest extends ApplicationTest {
    private CollectionService collectionService;

    @Override
    public void start(Stage stage) throws Exception {
        collectionService = mock(CollectionService.class);
        BuyerService buyerService = mock(BuyerService.class);
        when(buyerService.getAllBuyers()).thenReturn(List.of("Acme Buyer"));
        when(collectionService.fetchPendingBillNosForBuyer("Acme Buyer")).thenReturn(List.of());
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
                type -> new CollectionEntryController(mock(BillService.class), buyerService, collectionService));
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

    private void fetchVoucher() {
        interact(() -> {
            TextField voucherNo = lookup("#voucher_no_field").query();
            voucherNo.setText("C-100");
        });
        clickOn("#fetch_collection_voucher_btn");
    }
}
