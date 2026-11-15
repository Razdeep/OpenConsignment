package openconsignment.controller;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Hyperlink;
import javafx.scene.text.Text;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import openconsignment.common.AppConfig;
import openconsignment.common.Utils;
import openconsignment.di.DatabaseManager;

@Slf4j
public class WelcomeScreenController implements Initializable {
    private final AppConfig configuration;
    private final DatabaseManager databaseManager;

    @FXML
    private Text organization_name;

    @FXML
    private Text year;

    @Inject
    public WelcomeScreenController(AppConfig configuration, DatabaseManager databaseManager) {
        this.configuration = configuration;
        this.databaseManager = databaseManager;
    }

    @FXML
    private void handleOpenGithub(ActionEvent event) {
        Hyperlink link = (Hyperlink) event.getSource();
        String url = link.getText();

        try {
            Utils.launchBrowser(url);
        } catch (IOException ex) {
            Utils.showAlert(ex.toString());
            log.error(ex.toString(), ex);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        organization_name.setText(configuration.getOrganization());
        year.setText(configuration.getYear());
        try {
            databaseManager.getConnection();
        } catch (SQLException e) {
            log.error(e.toString());
            Utils.showAlert(e.toString());
        }
    }
}
