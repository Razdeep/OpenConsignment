package openconsignment;

import java.io.InputStream;
import java.net.URL;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import openconsignment.common.Utils;
import openconsignment.di.AppComponent;
import openconsignment.di.DaggerAppComponent;

@Slf4j
public class Launcher extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        try {
            String mainFxmlPath = "/fxml/Main.fxml";
            URL resource = getClass().getResource(mainFxmlPath);
            if (resource == null) {
                throw new RuntimeException("Resource not found");
            }
            AppComponent component = DaggerAppComponent.create();
            component.configuration();
            FXMLLoader loader = new FXMLLoader(resource);
            loader.setControllerFactory(component.controllerFactory());
            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
            InputStream resourceAsStream = getClass().getResourceAsStream("/icons/opaque_logo.png");
            if (resourceAsStream != null) {
                stage.getIcons().add(new Image(resourceAsStream));
            }
            stage.setTitle("OpenConsignment");
            log.info("OpenConsignment started at {}", java.time.LocalDateTime.now());
        } catch (Exception ex) {
            Utils.showAlert(ex.toString());
            log.error(ex.toString(), ex);
        }
    }
}
