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
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import openconsignment.controller.entry.BillEntryController;
import openconsignment.entity.BillEntity;
import openconsignment.entity.LREntity;
import openconsignment.model.LR;
import openconsignment.service.BillService;
import openconsignment.service.BuyerService;
import openconsignment.service.SupplierService;
import openconsignment.service.TransportService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.testfx.framework.junit5.ApplicationTest;

class BillEntryWorkflowTest extends ApplicationTest {
    private BillService bills;

    @Override
    public void start(Stage stage) throws Exception {
        bills = mock(BillService.class);
        SupplierService suppliers = mock(SupplierService.class);
        BuyerService buyers = mock(BuyerService.class);
        TransportService transports = mock(TransportService.class);
        when(suppliers.getAllSuppliers()).thenReturn(List.of());
        when(buyers.getAllBuyers()).thenReturn(List.of());
        when(transports.getAllTransports()).thenReturn(List.of());
        when(bills.getBill("B1"))
                .thenReturn(BillEntity.builder()
                        .billNo("B1")
                        .supplierName("Supplier A")
                        .buyerName("Buyer A")
                        .billDate("20-10-2026")
                        .transport("Carrier")
                        .lrDate("21-10-2026")
                        .billAmount("1000")
                        .lrEntities(List.of(new LREntity("B1", "LR1", "10")))
                        .build());
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/entry/BillEntry.fxml"));
        loader.setControllerFactory(type -> new BillEntryController(bills, suppliers, buyers, transports));
        Parent root = loader.load();
        stage.setScene(new Scene(root, 717, 600));
        stage.show();
    }

    @Test
    void savePersistsBillAndReplacedLorryReceiptThenClearsForm() throws Exception {
        interact(() -> {
            text("#bill_no_field").setText("B1");
            text("#supplier_field").setText("Supplier A");
            text("#buyer_name_field").setText("Buyer A");
            text("#transport_field").setText("Carrier");
            text("#bill_amount_field").setText("1000");
            date("#date_field").setValue(LocalDate.of(2026, 10, 20));
            date("#lr_date").setValue(LocalDate.of(2026, 10, 21));
            text("#lr_field").setText("LR1");
            text("#pm_field").setText("10");
            button("#add_btn").fire();
            table().getSelectionModel().select(0);
            text("#lr_field").setText("LR2");
            text("#pm_field").setText("20");
            button("#replace_btn").fire();
            assertEquals("LR2", table().getItems().get(0).getLrNo());
        });
        clickOn("#save_btn");
        clickOn("OK");

        ArgumentCaptor<BillEntity> saved = ArgumentCaptor.forClass(BillEntity.class);
        verify(bills).saveBill(saved.capture());
        BillEntity bill = saved.getValue();
        assertEquals("B1", bill.getBillNo());
        assertEquals("Supplier A", bill.getSupplierName());
        assertEquals("Buyer A", bill.getBuyerName());
        assertEquals("20-10-2026", bill.getBillDate());
        assertEquals("21-10-2026", bill.getLrDate());
        assertEquals("1000", bill.getBillAmount());
        assertEquals(1, bill.getLrEntities().size());
        assertEquals("LR2", bill.getLrEntities().get(0).getLrNo());
        assertEquals("20", bill.getLrEntities().get(0).getPm());
        interact(() -> {
            assertEquals("", text("#bill_no_field").getText());
            assertTrue(table().getItems().isEmpty());
        });
    }

    @Test
    void fetchPopulatesBillAndDeleteRequiresConfirmation() throws Exception {
        interact(() -> text("#bill_no_field").setText("B1"));
        clickOn("Fetch");
        interact(() -> {
            assertEquals("Supplier A", text("#supplier_field").getText());
            assertEquals("Buyer A", text("#buyer_name_field").getText());
            assertEquals(LocalDate.of(2026, 10, 20), date("#date_field").getValue());
            assertEquals("LR1", table().getItems().get(0).getLrNo());
        });
        clickOn("Delete");
        clickOn("No");
        verify(bills, never()).deleteBill("B1");
        clickOn("Delete");
        clickOn("Yes");
        clickOn("OK");
        verify(bills).deleteBill("B1");
        interact(() -> assertTrue(table().getItems().isEmpty()));
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

    private TableView<LR> table() {
        return lookup("#lrTable").queryTableView();
    }
}
