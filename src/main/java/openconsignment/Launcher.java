package openconsignment;

import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import openconsignment.common.Utils;

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
            Parent root = FXMLLoader.load(resource);
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
            InputStream resourceAsStream = getClass().getResourceAsStream("/icons/opaque_logo.png");
            if (resourceAsStream != null) {
                stage.getIcons().add(new Image(resourceAsStream));
            }
            stage.setTitle("OpenConsignment");
        } catch (Exception ex) {
            Utils.showAlert(ex.toString());
            Logger.getLogger(Launcher.class.getName()).log(Level.SEVERE, ex.toString(), ex);
        }
    }
}
