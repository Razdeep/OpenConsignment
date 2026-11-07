package openconsignment.common;

import java.io.IOException;
import java.util.Objects;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ViewLoader {
    private static final Logger logger = LoggerFactory.getLogger(ViewLoader.class);

    private final Callback<Class<?>, Object> controllerFactory;

    @Inject
    public ViewLoader(Callback<Class<?>, Object> controllerFactory) {
        this.controllerFactory = controllerFactory;
    }

    public void loadRoot(String resourcePath, Group root) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource(resourcePath)));
            loader.setControllerFactory(controllerFactory);
            Parent parent = loader.load();
            root.getChildren().setAll(parent);
        } catch (IOException ex) {
            Utils.showAlert(ex.toString());
            logger.error(ex.toString(), ex);
        }
    }

    public void loadCenter(String resourcePath, BorderPane center) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource(resourcePath)));
            loader.setControllerFactory(controllerFactory);
            Parent parent = loader.load();
            center.setCenter(parent);
        } catch (IOException ex) {
            Utils.showAlert(ex.toString());
            logger.error(ex.toString(), ex);
        }
    }
}
