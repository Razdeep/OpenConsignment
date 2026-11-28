package functional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.Window;
import openconsignment.controller.master.BuyerController;
import openconsignment.controller.master.SupplierController;
import openconsignment.controller.master.TransportController;
import openconsignment.service.BuyerService;
import openconsignment.service.SupplierService;
import openconsignment.service.TransportService;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

class MasterWorkflowTest extends ApplicationTest {
    private Stage stage;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        stage.setScene(new Scene(new Pane(), 800, 600));
        stage.show();
    }

    @Test
    void buyerActionsValidateAndDelegate() throws Exception {
        BuyerService service = mock(BuyerService.class);
        when(service.getAllBuyers()).thenReturn(List.of("Existing"));
        exercise("MasterBuyer.fxml", new BuyerController(service), service);
        verify(service).saveBuyer("New");
        verify(service).renameBuyer("Existing", "Renamed");
        verify(service).deleteBuyer("Existing");
    }

    @Test
    void supplierActionsValidateAndDelegate() throws Exception {
        SupplierService service = mock(SupplierService.class);
        when(service.getAllSuppliers()).thenReturn(List.of("Existing"));
        exercise("MasterSupplier.fxml", new SupplierController(service), service);
        verify(service).saveSupplier("New");
        verify(service).renameSupplier("Existing", "Renamed");
        verify(service).deleteSupplier("Existing");
    }

    @Test
    void transportActionsValidateAndDelegate() throws Exception {
        TransportService service = mock(TransportService.class);
        when(service.getAllTransports()).thenReturn(List.of("Existing"));
        exercise("MasterTransport.fxml", new TransportController(service), service);
        verify(service).saveTransport("New");
        verify(service).renameTransport("Existing", "Renamed");
        verify(service).deleteTransport("Existing");
    }

    private void exercise(String fxml, Object controller, Object service) {
        interact(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/master/" + fxml));
                loader.setControllerFactory(type -> controller);
                Parent root = loader.load();
                stage.getScene().setRoot(root);
                root.applyCss();
                root.layout();

                @SuppressWarnings("unchecked")
                ListView<String> list = (ListView<String>) root.lookup("#listView");
                TextField add = (TextField) root.lookup("#add_field");
                TextField rename = (TextField) root.lookup("#rename_field");
                assertEquals(List.of("Existing"), list.getItems());
                clearInvocations(service);

                // Empty fields and a missing selection must not reach the service.
                ((Button) root.lookup("#add_btn")).fire();
                closeAlerts();
                ((Button) root.lookup("#rename_btn")).fire();
                closeAlerts();
                ((Button) root.lookup("#delete_btn")).fire();
                closeAlerts();
                verifyNoInteractions(service);

                add.setText("New");
                ((Button) root.lookup("#add_btn")).fire();
                list.getSelectionModel().select("Existing");
                rename.setText("Renamed");
                ((Button) root.lookup("#rename_btn")).fire();
                closeAlerts();
                list.getSelectionModel().select("Existing");
                ((Button) root.lookup("#delete_btn")).fire();
                ((Button) root.lookup("#refresh_btn")).fire();
                assertEquals(List.of("Existing"), list.getItems());
            } catch (Exception e) {
                throw new AssertionError("Could not exercise " + fxml, e);
            }
        });
    }

    private void closeAlerts() {
        Window.getWindows().stream().filter(window -> window != stage).toList().forEach(Window::hide);
    }
}
