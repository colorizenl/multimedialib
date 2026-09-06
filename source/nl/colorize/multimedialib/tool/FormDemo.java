//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.tool;

import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.KeyCode;
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.scene.SceneContext;
import nl.colorize.multimedialib.scene.effect.Effects;
import nl.colorize.multimedialib.scene.ui.DeclarativeStyle;
import nl.colorize.multimedialib.scene.ui.Form;
import nl.colorize.multimedialib.stage.FontFace;
import nl.colorize.multimedialib.stage.Stage;
import nl.colorize.multimedialib.stage.Text;
import nl.colorize.util.LogHelper;

import java.util.List;
import java.util.logging.Logger;

import static nl.colorize.multimedialib.stage.Align.CENTER;
import static nl.colorize.multimedialib.stage.ColorRGB.BLACK;
import static nl.colorize.multimedialib.stage.ColorRGB.WHITE;
import static nl.colorize.multimedialib.stage.FontFace.DEFAULT_FONT;
import static nl.colorize.multimedialib.tool.Demo2D.RED_BUTTON;

/**
 * Demo application for testing the form-based user interface subsystem.
 */
public class FormDemo implements Scene {

    private Stage stage;
    private double nextMessageX;
    private double nextMessageY;

    private static final int FORM_WIDTH = 300;
    private static final FontFace FONT = DEFAULT_FONT.derive(14);
    private static final DeclarativeStyle LABEL_STYLE = new DeclarativeStyle(FONT);
    private static final DeclarativeStyle TITLE_STYLE = LABEL_STYLE.withFontSize(20);
    private static final DeclarativeStyle BUTTON_STYLE = LABEL_STYLE.withBackgroundColor(RED_BUTTON);
    private static final DeclarativeStyle CHECKBOX_STYLE = BUTTON_STYLE.withBorder(WHITE, 1);
    private static final DeclarativeStyle INPUT_STYLE = LABEL_STYLE.withFont(FONT.derive(BLACK))
        .withBackgroundColor(WHITE)
        .withBorderColor(BLACK)
        .withBorderSize(1);

    private static final Logger LOGGER = LogHelper.getLogger(FormDemo.class);

    @Override
    public void start(SceneContext context) {
        stage = context.getStage();
        Canvas canvas = context.getCanvas();
        nextMessageX = canvas.getCenter().x();
        nextMessageY = canvas.getCenter().y() + 50;

        Form form = new Form(context.getInput(), FORM_WIDTH, 30);
        form.setGap(5, 10);
        form.addTitle("Form Demo", TITLE_STYLE);
        form.addLabel("Button:", LABEL_STYLE);
        form.addButton("Click here", BUTTON_STYLE)
            .subscribe(_ -> showMessage("Button clicked"));
        form.addLabel("Checkbox:", LABEL_STYLE);
        form.addCheckbox("Label", false, CHECKBOX_STYLE)
            .subscribe(value -> showMessage("Checkbox: " + value));
        form.addLabel("Input:", LABEL_STYLE);
        form.addInputField("Click here", INPUT_STYLE)
            .subscribe(value -> showMessage("Input field: " + value));
        form.addLabel("Select:", LABEL_STYLE);
        form.addSelectField("One", List.of("One", "Two", "Three"), INPUT_STYLE)
            .subscribe(value -> showMessage("Select field: " + value));

        stage.getRoot().addChild(form.getGraphics());
        context.attach(form);
        context.attach(Effects.keepTopRight(form.getGraphics(), canvas, FORM_WIDTH + 10, 10));
    }

    @Override
    public void update(SceneContext context, double deltaTime) {
        InputDevice input = context.getInput();

        if (input.isKeyReleased(KeyCode.N1)) {
            LOGGER.info("\n----\n" + context.getStage() + "\n----\n");
        }
    }

    private void showMessage(String message) {
        Text messageText = new Text(message, FONT.derive(WHITE).derive(10), CENTER);
        messageText.getTransform().setPosition(nextMessageX, nextMessageY);
        stage.getRoot().addChild(messageText);

        nextMessageY += 15;
    }
}
