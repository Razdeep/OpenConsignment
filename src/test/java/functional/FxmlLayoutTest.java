package functional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

class FxmlLayoutTest extends ApplicationTest {
    private Stage stage;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        stage.setScene(new Scene(new Pane(), 1100, 700));
        stage.show();
    }

    @Test
    void allScreensLoadWithTheirControllers() {
        List<String> screens = List.of(
                "Main.fxml",
                "WelcomeScreen.fxml",
                "entry/Input.fxml",
                "entry/BillEntry.fxml",
                "entry/CollectionEntry.fxml",
                "entry/PaymentEntry.fxml",
                "master/Master.fxml",
                "master/MasterBuyer.fxml",
                "master/MasterSupplier.fxml",
                "master/MasterTransport.fxml",
                "report/Report.fxml",
                "report/BuyerLedger.fxml",
                "report/SupplierLedger.fxml",
                "report/TransportLedger.fxml",
                "history/BillHistory.fxml",
                "history/CollectionHistory.fxml",
                "history/PaymentHistory.fxml");
        interact(() -> {
            for (String screen : screens) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + screen));
                    loader.setControllerFactory(FxmlLayoutTest::controllerWithMockDependencies);
                    Parent root = loader.load();
                    stage.getScene().setRoot(root);
                    root.applyCss();
                    root.layout();
                    assertNotNull(loader.getController(), screen);
                    assertTrue(root.getLayoutBounds().getWidth() > 0, screen);
                    for (Node node : root.lookupAll(".combo-box-base")) {
                        if (node instanceof ComboBoxBase<?> combo) {
                            assertTrue(
                                    combo.getPromptText() == null
                                            || combo.getPromptText().isEmpty(),
                                    screen);
                        }
                    }
                    if (screen.equals("entry/BillEntry.fxml")) {
                        assertEquals("Enter bill number", ((TextField) root.lookup("#bill_no_field")).getPromptText());
                    }
                    if (screen.equals("entry/CollectionEntry.fxml") || screen.equals("entry/PaymentEntry.fxml")) {
                        assertEquals(
                                "Enter voucher number", ((TextField) root.lookup("#voucher_no_field")).getPromptText());
                        boolean collection = screen.equals("entry/CollectionEntry.fxml");
                        Region form = (Region) root.lookup(collection ? ".collection-entry" : ".payment-entry");
                        assertEquals(
                                Color.web(collection ? "#fffdf5" : "#fbfdff"),
                                form.getBackground().getFills().get(0).getFill(),
                                screen);
                        Button save = (Button) root.lookup("#save_btn");
                        assertTrue(save.getWidth() >= save.minWidth(-1), screen + " save button is clipped");
                        assertTrue(save.localToScene(save.getBoundsInLocal()).getMaxX() <= 967, screen);
                        Node clear = root.lookup("#clear_btn");
                        Node total = root.lookup(collection ? "#total_amount_field" : "#total_amount_paid_field");
                        assertTrue(
                                sceneBounds(total).getMaxX()
                                        < sceneBounds(clear).getMinX(),
                                screen);
                        assertTrue(
                                sceneBounds(clear).getMaxX() < sceneBounds(save).getMinX(), screen);
                    }
                    if (screen.startsWith("master/Master") && !screen.equals("master/Master.fxml")) {
                        assertTrue(((TextField) root.lookup("#add_field"))
                                .getPromptText()
                                .startsWith("Enter "));
                        assertTrue(((TextField) root.lookup("#rename_field"))
                                .getPromptText()
                                .startsWith("Enter "));
                        Map<String, String> icons = Map.of(
                                "#add_btn", "MasterAdd.png",
                                "#rename_btn", "MasterRename.png",
                                "#refresh_btn", "Refresh.png",
                                "#delete_btn", "MasterDelete.png");
                        for (var entry : icons.entrySet()) {
                            Button button = (Button) root.lookup(entry.getKey());
                            assertNotNull(button.getOnAction(), screen + " " + entry.getKey());
                            ImageView graphic = (ImageView) button.getGraphic();
                            assertNotNull(graphic, screen + " " + entry.getKey());
                            assertTrue(graphic.getImage().getUrl().endsWith(entry.getValue()), screen);
                            assertTrue(!graphic.getImage().isError(), screen);
                            Bounds iconBounds = sceneBounds(graphic);
                            Bounds buttonBounds = sceneBounds(button);
                            assertTrue(iconBounds.getMinX() >= buttonBounds.getMinX(), screen);
                            assertTrue(iconBounds.getMaxX() <= buttonBounds.getMaxX(), screen);
                            assertTrue(iconBounds.getMinY() >= buttonBounds.getMinY(), screen);
                            assertTrue(iconBounds.getMaxY() <= buttonBounds.getMaxY(), screen);
                            Text displayedText = (Text) button.lookup(".text");
                            assertNotNull(displayedText, screen);
                            assertEquals(button.getText(), displayedText.getText(), screen + " button text is clipped");
                        }
                    }
                    if (screen.startsWith("report/")
                            || screen.equals("entry/Input.fxml")
                            || screen.equals("master/Master.fxml")) {
                        Region panel = (Region) root.lookup(".screen");
                        List<Node> children = panel.getChildrenUnmodifiable();
                        Node lastChild = children.get(children.size() - 1);
                        double bottomPadding = sceneBounds(panel).getMaxY()
                                - sceneBounds(lastChild).getMaxY();
                        assertTrue(bottomPadding >= 27 && bottomPadding <= 30, screen);
                        assertTrue(panel.getHeight() < 450, screen);
                        boolean navigation = screen.equals("report/Report.fxml")
                                || screen.equals("entry/Input.fxml")
                                || screen.equals("master/Master.fxml");
                        assertTrue(panel.getWidth() < (navigation ? 700 : 460), screen);
                        for (Node button : root.lookupAll(".button")) {
                            assertFitsWithin(button, panel, screen);
                        }
                        for (Node combo : root.lookupAll(".combo-box-base")) {
                            assertFitsWithin(combo, panel, screen);
                        }
                    }
                    if (screen.equals("entry/Input.fxml")
                            || screen.equals("master/Master.fxml")
                            || screen.equals("report/Report.fxml")) {
                        List<Node> cards =
                                root.lookupAll(".navigation-button").stream().toList();
                        assertEquals(3, cards.size(), screen);
                        double top = sceneBounds(cards.get(0)).getMinY();
                        for (Node card : cards) {
                            assertEquals(top, sceneBounds(card).getMinY(), 0.1, screen);
                            assertEquals(200.0, sceneBounds(card).getWidth(), 0.1, screen);
                        }
                    }
                } catch (Exception e) {
                    throw new AssertionError("Could not load " + screen, e);
                }
            }
        });
    }

    private static Bounds sceneBounds(Node node) {
        return node.localToScene(node.getBoundsInLocal());
    }

    private static void assertFitsWithin(Node control, Node panel, String screen) {
        Bounds content = sceneBounds(control);
        Bounds outer = sceneBounds(panel);
        assertTrue(content.getMaxX() <= outer.getMaxX() - 27, screen);
        assertTrue(content.getMaxY() <= outer.getMaxY() - 27, screen);
    }

    private static Object controllerWithMockDependencies(Class<?> type) {
        try {
            Constructor<?> constructor = type.getConstructors()[0];
            Object[] dependencies = new Object[constructor.getParameterCount()];
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            for (int i = 0; i < dependencies.length; i++) {
                dependencies[i] = mock(parameterTypes[i]);
            }
            return constructor.newInstance(dependencies);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not construct " + type.getName(), e);
        }
    }
}
