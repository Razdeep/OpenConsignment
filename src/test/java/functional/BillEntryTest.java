package functional;

import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import openconsignment.di.AppComponent;
import openconsignment.di.DaggerAppComponent;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

public class BillEntryTest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        AppComponent component = DaggerAppComponent.create();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Main.fxml"));
        loader.setControllerFactory(component.controllerFactory());

        Parent root = loader.load();
        stage.setScene(new Scene(root));
        stage.setMaximized(true);
        stage.show();
    }

    @Test
    void testIfLoadedProperly() {
        waitForFxEvents();
        //        clickOn("#input_btn");
        //
        //        verifyThat("#bill_entry_btn", NodeMatchers.isVisible());
        //        clickOn("#bill_entry_btn");
        //
        //        verifyThat("#TabButtonAdd", NodeMatchers.isVisible());
        //        clickOn("#TabButtonAdd");
        //
        //        verifyThat("#supplier_field", NodeMatchers.isVisible());
        ////        clickOn("#supplier_field");
        //
        //        interact(() -> {
        //            lookup("#supplier_field").queryTextInputControl().setText("ajanta");
        //        });
        //
        //
        //        verifyThat("#buyer_name_field", NodeMatchers.isVisible());
        //        clickOn("#buyer_name_field");
        //
        //        write("hello");
    }
}
