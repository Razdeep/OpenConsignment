package functional;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import openconsignment.common.AppConfig;
import openconsignment.controller.WelcomeScreenController;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

class WelcomeScreenTest extends ApplicationTest {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/WelcomeScreen.fxml"));
        loader.setControllerFactory(
                type -> new WelcomeScreenController(AppConfig.forOrganization("Configured Organization")));
        Parent root = loader.load();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @Test
    void displaysConfiguredOrganizationName() {
        Text name = lookup("#organization_name").query();
        assertEquals("Configured Organization", name.getText());
    }
}
