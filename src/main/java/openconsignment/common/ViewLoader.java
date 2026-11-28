package openconsignment.common;

import java.io.IOException;
import java.util.Objects;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import javafx.util.Duration;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ViewLoader {

    private static final Duration FADE_DURATION = Duration.millis(180);
    private static final Duration OVERSHOOT_TIME = Duration.millis(320);
    private static final Duration TRANSITION_DURATION = Duration.millis(420);
    private static final double SLIDE_DISTANCE = 24;
    private static final double START_SCALE = 0.96;
    private static final double OVERSHOOT_SCALE = 1.008;
    private static final double OVERSHOOT_DISTANCE = 2;
    private static final Interpolator ENTER_EASING = Interpolator.SPLINE(0.16, 1, 0.3, 1);
    private static final Object TRANSITION_KEY = new Object();

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
            // Animate the existing host, rather than its child, so Group's bounds
            // do not change throughout the transition and cause layout jitter.
            transition(root, () -> root.getChildren().setAll(parent));
        } catch (IOException ex) {
            Utils.showAlert(ex.toString());
            log.error(ex.toString(), ex);
        }
    }

    public void loadCenter(String resourcePath, BorderPane center) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource(resourcePath)));
            loader.setControllerFactory(controllerFactory);
            Parent parent = loader.load();
            finishTransition(center.getCenter());
            transition(parent, () -> center.setCenter(parent));
        } catch (IOException ex) {
            Utils.showAlert(ex.toString());
            log.error(ex.toString(), ex);
        }
    }

    private void transition(Node node, Runnable replaceView) {
        finishTransition(node);
        double opacity = node.getOpacity();
        double translateY = node.getTranslateY();
        double scaleX = node.getScaleX();
        double scaleY = node.getScaleY();
        node.setOpacity(0);
        node.setTranslateY(translateY + SLIDE_DISTANCE);
        node.setScaleX(scaleX * START_SCALE);
        node.setScaleY(scaleY * START_SCALE);
        replaceView.run();

        Timeline animation = new Timeline(
                new KeyFrame(FADE_DURATION, new KeyValue(node.opacityProperty(), opacity, Interpolator.EASE_OUT)),
                new KeyFrame(
                        OVERSHOOT_TIME,
                        new KeyValue(node.translateYProperty(), translateY - OVERSHOOT_DISTANCE, ENTER_EASING),
                        new KeyValue(node.scaleXProperty(), scaleX * OVERSHOOT_SCALE, ENTER_EASING),
                        new KeyValue(node.scaleYProperty(), scaleY * OVERSHOOT_SCALE, ENTER_EASING)),
                new KeyFrame(
                        TRANSITION_DURATION,
                        new KeyValue(node.translateYProperty(), translateY, Interpolator.EASE_BOTH),
                        new KeyValue(node.scaleXProperty(), scaleX, Interpolator.EASE_BOTH),
                        new KeyValue(node.scaleYProperty(), scaleY, Interpolator.EASE_BOTH)));
        node.getProperties().put(TRANSITION_KEY, new ScreenTransition(animation, opacity, translateY, scaleX, scaleY));
        animation.setOnFinished(event -> finishTransition(node));
        animation.play();
    }

    private void finishTransition(Node node) {
        if (node == null) {
            return;
        }
        Object transition = node.getProperties().remove(TRANSITION_KEY);
        if (transition instanceof ScreenTransition running) {
            running.animation().stop();
            node.setOpacity(running.opacity());
            node.setTranslateY(running.translateY());
            node.setScaleX(running.scaleX());
            node.setScaleY(running.scaleY());
        }
    }

    private record ScreenTransition(
            Timeline animation, double opacity, double translateY, double scaleX, double scaleY) {}
}
