package utils;

import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.function.Consumer;

/**
 * SceneNavigator - Centralised navigation utility.
 * Uses scene.setRoot() so the Stage (window) is never recreated,
 * keeping its position and size stable across all screen transitions.
 * A short fade-in transition is applied to each new screen.
 */
public class SceneNavigator {

    private static Scene mainScene;

    public static void setMainScene(Scene scene) {
        mainScene = scene;
    }

    public static Scene getMainScene() {
        return mainScene;
    }

    public static void navigateTo(String fxml, String title) {
        navigateTo(fxml, title, null);
    }

    @SuppressWarnings("unchecked")
    public static <T> void navigateTo(String fxml, String title, Consumer<T> controllerSetup) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(fxml));
            Parent newRoot = loader.load();

            if (controllerSetup != null) {
                T controller = loader.getController();
                controllerSetup.accept(controller);
            }

            newRoot.setOpacity(0);
            mainScene.setRoot(newRoot);

            if (title != null && mainScene.getWindow() instanceof Stage) {
                ((Stage) mainScene.getWindow()).setTitle(title);
            }

            FadeTransition fade = new FadeTransition(Duration.millis(200), newRoot);
            fade.setFromValue(0.0);
            fade.setToValue(1.0);
            fade.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
