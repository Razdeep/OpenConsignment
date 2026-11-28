package functional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.animation.PauseTransition;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import openconsignment.common.ViewLoader;
import openconsignment.controller.entry.EntryController;
import openconsignment.controller.master.MasterController;
import openconsignment.controller.report.ReportController;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

class ViewLoaderTransitionTest extends ApplicationTest {
    private BorderPane shell;
    private ViewLoader loader;

    @Override
    public void start(Stage stage) {
        loader = new ViewLoader(type -> {
            if (type == EntryController.class) {
                return new EntryController(loader);
            }
            if (type == ReportController.class) {
                return new ReportController(loader);
            }
            if (type == MasterController.class) {
                return new MasterController(loader);
            }
            throw new IllegalArgumentException("Unexpected controller: " + type);
        });
        shell = new BorderPane();
        stage.setScene(new Scene(shell, 1100, 700));
        stage.show();
    }

    @Test
    void nestedNavigationAnimatesHostAndRestoresItsOriginalProperties() throws Exception {
        Group host = new Group();
        interact(() -> {
            shell.setCenter(host);
            host.setOpacity(0.8);
            host.setTranslateY(4);
            host.setScaleX(1.25);
            host.setScaleY(0.75);
            loader.loadRoot("/fxml/report/Report.fxml", host);
            assertNotNull(host.lookup("#supplier_ledger_btn"));
            assertEquals(0, host.getOpacity(), 0.001);
            assertEquals(28, host.getTranslateY(), 0.001);
            assertEquals(1.2, host.getScaleX(), 0.001);
            assertEquals(0.72, host.getScaleY(), 0.001);

            // A second navigation before the first animation finishes starts from
            // the original properties, not the interrupted animation's values.
            loader.loadRoot("/fxml/master/Master.fxml", host);
            assertNull(host.lookup("#supplier_ledger_btn"));
            assertNotNull(host.lookup("#supplier_master_btn"));
            assertEquals(0, host.getOpacity(), 0.001);
            assertEquals(28, host.getTranslateY(), 0.001);
            assertEquals(1.2, host.getScaleX(), 0.001);
            assertEquals(0.72, host.getScaleY(), 0.001);
        });
        waitForTransition();
        interact(() -> {
            assertEquals(0.8, host.getOpacity(), 0.001);
            assertEquals(4, host.getTranslateY(), 0.001);
            assertEquals(1.25, host.getScaleX(), 0.001);
            assertEquals(0.75, host.getScaleY(), 0.001);
            assertNotNull(host.lookup("#supplier_master_btn"));
        });
    }

    @Test
    void rapidCenterAndNestedNavigationKeepsTheLatestScreen() throws Exception {
        Node[] previous = new Node[1];
        interact(() -> {
            loader.loadCenter("/fxml/entry/Input.fxml", shell);
            Group input = (Group) shell.getCenter();
            previous[0] = input;
            assertEquals(0, input.getOpacity(), 0.001);
            loader.loadRoot("/fxml/report/Report.fxml", input);
            assertNotNull(input.lookup("#supplier_ledger_btn"));
            loader.loadCenter("/fxml/master/Master.fxml", shell);
            assertEquals(1, input.getOpacity(), 0.001);
            assertEquals(0, input.getTranslateY(), 0.001);
            assertEquals(1, input.getScaleX(), 0.001);
            assertEquals(1, input.getScaleY(), 0.001);
            assertNotNull(shell.getCenter().lookup("#supplier_master_btn"));
            assertEquals(0, shell.getCenter().getOpacity(), 0.001);
        });
        waitForTransition();
        interact(() -> {
            assertNotNull(shell.getCenter().lookup("#supplier_master_btn"));
            assertEquals(1, shell.getCenter().getOpacity(), 0.001);
            assertEquals(0, shell.getCenter().getTranslateY(), 0.001);
            assertEquals(1, shell.getCenter().getScaleX(), 0.001);
            assertEquals(1, shell.getCenter().getScaleY(), 0.001);
            assertEquals(1, previous[0].getOpacity(), 0.001);
        });
    }

    private void waitForTransition() throws InterruptedException {
        CountDownLatch finished = new CountDownLatch(1);
        interact(() -> {
            PauseTransition pause = new PauseTransition(Duration.millis(650));
            pause.setOnFinished(event -> finished.countDown());
            pause.play();
        });
        assertTrue(finished.await(3, TimeUnit.SECONDS), "JavaFX animation did not finish");
    }
}
