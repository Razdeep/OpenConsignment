package openconsignment.common;

import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import openconsignment.controller.MainController;
import openconsignment.controller.entry.EntryController;

public class ViewLoader {
    public void loadRoot(String resourcePath, Group root) {
        try {
            Parent parent = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(resourcePath)));
            root.getChildren().setAll(parent);
        } catch (IOException ex) {
            Utils.showAlert(ex.toString());
            Logger.getLogger(EntryController.class.getName()).log(Level.SEVERE, ex.toString(), ex);
        }
    }

    public void loadCenter(String resourcePath, BorderPane center) {
        try {
            Parent parent = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(resourcePath)));
            center.setCenter(parent);
        } catch (IOException ex) {
            Utils.showAlert(ex.toString());
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, ex.toString(), ex);
        }
    }
}
