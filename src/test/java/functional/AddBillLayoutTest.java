package functional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import openconsignment.controller.entry.bill.BillEntryController;
import openconsignment.model.LR;
import openconsignment.service.BillService;
import openconsignment.service.BuyerService;
import openconsignment.service.SupplierService;
import openconsignment.service.TransportService;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

class AddBillLayoutTest extends ApplicationTest {
    @Override
    public void start(Stage stage) throws Exception {
        SupplierService suppliers = mock(SupplierService.class);
        BuyerService buyers = mock(BuyerService.class);
        TransportService transports = mock(TransportService.class);
        when(suppliers.getAllSuppliers()).thenReturn(List.of());
        when(buyers.getAllBuyers()).thenReturn(List.of());
        when(transports.getAllTransports()).thenReturn(List.of());

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/entry/bill/BillEntry.fxml"));
        loader.setControllerFactory(
                type -> new BillEntryController(mock(BillService.class), suppliers, buyers, transports));
        Parent root = loader.load();
        // Match the width available inside the existing bill-entry tab.
        stage.setScene(new Scene(root, 717, 600));
        stage.show();
    }

    @Test
    void layoutFitsTabAndLorryReceiptActionsRemainConnected() {
        interact(() -> {
            TextField billNo = lookup("#bill_no_field").query();
            TextField lrNo = lookup("#lr_field").query();
            TextField pm = lookup("#pm_field").query();
            billNo.setText("B-100");
            lrNo.setText("LR-1");
            pm.setText("10");
            Button save = lookup("#save_btn").queryButton();
            assertTrue(save.localToScene(save.getBoundsInLocal()).getMaxX() <= 717);
            assertTrue(save.localToScene(save.getBoundsInLocal()).getMaxY() <= 600);
        });
        clickOn("#add_btn");
        interact(() -> {
            TableView<LR> table = lookup("#lrTable").queryTableView();
            assertEquals(1, table.getItems().size());
            assertEquals("LR-1", table.getItems().get(0).getLrNo());
            table.getSelectionModel().select(0);
        });
        clickOn("#delete_btn");
        interact(() -> assertTrue(lookup("#lrTable").queryTableView().getItems().isEmpty()));
        clickOn("#clear_btn");
        interact(() -> assertEquals(
                "", lookup("#bill_no_field").queryTextInputControl().getText()));
    }
}
