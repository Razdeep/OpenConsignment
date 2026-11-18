package functional;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.sql.SQLException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import openconsignment.common.AppConfig;
import openconsignment.controller.WelcomeScreenController;
import openconsignment.di.DatabaseManager;
import openconsignment.repository.RepositoryTestSupport;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

class WelcomeScreenTest extends ApplicationTest {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/WelcomeScreen.fxml"));
        loader.setControllerFactory(type -> {
            try {
                return new WelcomeScreenController(
                        AppConfig.forOrganization("Configured Organization"),
                        new DatabaseManager(RepositoryTestSupport.newInMemoryConnection()));
            } catch (ClassNotFoundException | IOException | SQLException e) {
                throw new RuntimeException(e);
            }
        });
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
