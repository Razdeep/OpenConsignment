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
import openconsignment.common.Constants;
import openconsignment.common.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WelcomeScreenController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(WelcomeScreenController.class);

    @FXML
    private Text organization_name;

    @Inject
    public WelcomeScreenController() {}

    @FXML
    private void handleOpenGithub(ActionEvent event) {
        Hyperlink link = (Hyperlink) event.getSource();
        String url = link.getText();

        try {
            Utils.launchBrowser(url);
        } catch (IOException ex) {
            Utils.showAlert(ex.toString());
            logger.error(ex.toString(), ex);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        organization_name.setText(Constants.ORGANIZATION_NAME);
    }
}
