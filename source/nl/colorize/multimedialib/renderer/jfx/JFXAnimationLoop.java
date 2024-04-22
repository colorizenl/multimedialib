//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.jfx;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import nl.colorize.multimedialib.renderer.FrameStats;
import nl.colorize.multimedialib.scene.SceneContext;
import nl.colorize.util.LogHelper;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles the JavaFX application, window, and animation loop. Instances of
 * this class are managed by JavaFX, and operate on the JavaFX application
 * thread.
 */
public class JFXAnimationLoop extends Application {

    private JFXRenderer renderer;
    private SceneContext context;
    private JFXGraphics graphics;
    private JFXInput input;

    private static final Logger LOGGER = LogHelper.getLogger(JFXAnimationLoop.class);

    @Override
    public void start(Stage stage) throws Exception {
        renderer = JFXRenderer.accessInstance();
        context = renderer.getContext();
        graphics = renderer.getGraphics();
        input = renderer.getInput();

        populateUI(stage);
        startAnimationLoop();
    }

    private void populateUI(Stage stage) {
        int width = context.getCanvas().getWidth();
        int height = context.getCanvas().getHeight();
        Canvas fxCanvas = new Canvas(width, height);

        Group root = new Group();
        root.getChildren().add(fxCanvas);
        Scene fxScene = new Scene(root);

        graphics.init(fxCanvas);
        attachEventHandlers(fxScene, fxCanvas);

        stage.setScene(fxScene);
        stage.setTitle(renderer.getWindowOptions().getTitle());
        stage.setMaximized(renderer.getWindowOptions().isFullscreen());
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
                try {
                    if (context.syncFrame() > 0) {
                        context.getFrameStats().markStart(FrameStats.PHASE_FRAME_RENDER);
                        context.getStage().visit(graphics, context.getSceneTime());
                        context.getFrameStats().markEnd(FrameStats.PHASE_FRAME_RENDER);
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Exception in JavaFX animation loop", e);
                    renderer.getErrorHandler().onError(context, e);
                    stop();
                }
            }
        };
        animationLoop.start();
    }

    private void resizeWidth(Canvas fxCanvas, Number width) {
        int screenHeight = context.getCanvas().getScreenHeight();
        context.getCanvas().resizeScreen(width.intValue(), screenHeight);
        fxCanvas.setWidth(width.intValue());
    }

    private void resizeHeight(Canvas fxCanvas, Number height) {
        int screenWidth = context.getCanvas().getScreenWidth();
        context.getCanvas().resizeScreen(screenWidth, height.intValue());
        fxCanvas.setHeight(height.intValue());
    }
}
