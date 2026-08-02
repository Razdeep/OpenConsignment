package openconsignment.common;

import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import javax.inject.Inject;
import openconsignment.controller.MainController;
import openconsignment.controller.entry.EntryController;

public class ViewLoader {

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
            Logger.getLogger(EntryController.class.getName()).log(Level.SEVERE, ex.toString(), ex);
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
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, ex.toString(), ex);
        }
    }
}
