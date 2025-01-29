//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.jfx;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import nl.colorize.multimedialib.renderer.FrameStats;
import nl.colorize.multimedialib.scene.SceneManager;
import nl.colorize.util.LogHelper;
import nl.colorize.util.swing.Utils2D;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles the JavaFX application, window, and animation loop. Instances of
 * this class are managed by JavaFX, and operate on the JavaFX application
 * thread.
 */
public class JFXAnimationLoop extends Application {

    private JFXRenderer renderer;
    private JFXGraphics graphics;
    private JFXInput input;

    private Stage stage;

    private static final Logger LOGGER = LogHelper.getLogger(JFXAnimationLoop.class);

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;

        renderer = JFXRenderer.accessInstance();
        graphics = renderer.getGraphics();
        input = renderer.getInput();

        populateUI(stage);
        startAnimationLoop();
    }

    private void populateUI(Stage stage) {
        int width = renderer.getCanvas().getWidth();
        int height = renderer.getCanvas().getHeight();
        Canvas fxCanvas = new Canvas(width, height);

        Group root = new Group();
        root.getChildren().add(fxCanvas);
        Scene fxScene = new Scene(root);

        graphics.init(fxCanvas);
        attachEventHandlers(fxScene, fxCanvas);

        stage.setScene(fxScene);
        stage.setTitle(renderer.getConfig().getWindowOptions().getTitle());
        stage.setMaximized(renderer.getConfig().getWindowOptions().isFullscreen());
        stage.requestFocus();
        stage.show();
    }

    private void attachEventHandlers(Scene fxScene, Canvas fxCanvas) {
        fxScene.widthProperty().addListener((value, old, width) -> resizeWidth(fxCanvas, width));
        fxScene.heightProperty().addListener((value, old, height) -> resizeHeight(fxCanvas, height));

        fxScene.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> input.mouseEventQueue.add(e));
        fxScene.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> input.mouseEventQueue.add(e));
        fxScene.addEventFilter(MouseEvent.MOUSE_MOVED, e -> input.mouseEventQueue.add(e));

        fxScene.addEventFilter(KeyEvent.KEY_PRESSED, e -> input.keyEventQueue.add(e));
        fxScene.addEventFilter(KeyEvent.KEY_RELEASED, e -> input.keyEventQueue.add(e));
    }

    private void startAnimationLoop() {
        AnimationTimer animationLoop = new AnimationTimer() {
            @Override
            public void handle(long time) {
                if (!handleFrameUpdate()) {
                    stop();
                }
            }
        };
        animationLoop.start();
    }

    private boolean handleFrameUpdate() {
        SceneManager sceneManager = renderer.getSceneManager();

        try {
            if (sceneManager.requestFrameUpdate(renderer) > 0) {
                sceneManager.getFrameStats().markStart(FrameStats.PHASE_FRAME_RENDER);
                renderer.getStage().visit(graphics);
                sceneManager.getFrameStats().markEnd(FrameStats.PHASE_FRAME_RENDER);
            }

            while (!renderer.getScreenshotQueue().isEmpty()) {
                takeScreenshot(renderer.getScreenshotQueue().removeFirst());
            }

            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception in JavaFX animation loop", e);
            renderer.getConfig().getErrorHandler().onError(renderer, e);
            return false;
        }
    }

    private void resizeWidth(Canvas fxCanvas, Number width) {
        int screenHeight = renderer.getCanvas().getScreenSize().height();
        renderer.getCanvas().resizeScreen(width.intValue(), screenHeight);
        fxCanvas.setWidth(width.intValue());
    }

    private void resizeHeight(Canvas fxCanvas, Number height) {
        int screenWidth = renderer.getCanvas().getScreenSize().width();
        renderer.getCanvas().resizeScreen(screenWidth, height.intValue());
        fxCanvas.setHeight(height.intValue());
    }

    private void takeScreenshot(File file) {
        WritableImage buffer = stage.getScene().snapshot(null);
        BufferedImage screenshot = new BufferedImage((int) Math.round(buffer.getWidth()),
            (int) Math.round(buffer.getHeight()), BufferedImage.TYPE_INT_ARGB);

        for (int x = 0; x < buffer.getWidth(); x++) {
            for (int y = 0; y < buffer.getHeight(); y++) {
                screenshot.setRGB(x, y, buffer.getPixelReader().getArgb(x, y));
            }
        }

        try {
            Utils2D.savePNG(screenshot, file);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to save screenshot", e);
        }
    }
}
