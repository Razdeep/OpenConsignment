package openconsignment.controller;

import java.io.IOException;
import java.net.URL;
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

@Slf4j
public class WelcomeScreenController implements Initializable {
    private final AppConfig configuration;

    @FXML
    private Text organization_name;

    @Inject
    public WelcomeScreenController(AppConfig configuration) {
        this.configuration = configuration;
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
    }
}
