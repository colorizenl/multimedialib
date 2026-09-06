//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene.ui;

import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.mock.MockImage;
import nl.colorize.multimedialib.mock.MockStageVisitor;
import nl.colorize.multimedialib.renderer.headless.HeadlessRenderer;
import nl.colorize.multimedialib.stage.Sprite;
import nl.colorize.multimedialib.stage.Stage;
import nl.colorize.util.PropertyUtils;
import nl.colorize.util.TranslationBundle;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static nl.colorize.multimedialib.math.Point2D.EPSILON;
import static nl.colorize.multimedialib.stage.ColorRGB.BLACK;
import static nl.colorize.multimedialib.stage.ColorRGB.RED;
import static nl.colorize.multimedialib.stage.FontFace.DEFAULT_FONT;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FormTest {

    private static final DeclarativeStyle STYLE = new DeclarativeStyle(DEFAULT_FONT);
    private static final DeclarativeStyle BUTTON_STYLE = STYLE
        .withBackgroundColor(RED)
        .withBorder(BLACK, 1);

    @Test
    void addWidgets() {
        HeadlessRenderer renderer = new HeadlessRenderer();
        Stage stage = renderer.getStage();

        Form form = new Form(renderer, 100, 20);
        form.setBundle(TranslationBundle.from(PropertyUtils.loadProperties("a=something")));
        form.addTitle("a", STYLE);
        form.addLabel("b", STYLE);
        form.addButton("b", BUTTON_STYLE);
        form.addCheckbox("test", true, BUTTON_STYLE);
        form.addInputField("test", STYLE);
        form.addSelectField("one", List.of("one", "two", "three"), STYLE);

        stage.getRoot().addChild(form.getGraphics());

        String expected = """
            Stage
                $$root [1]
                    Form [6]
                        Text [something]
                        Text [b]
                        Container [2]
                            Container [2]
                                Rect [(50, 20, 50, 20)]
                                SegmentedLine
                            Text [b]
                        Container [3]
                            Container [2]
                                Rect [(0, 40, 20, 20)]
                                SegmentedLine
                            Rect [(3, 43, 14, 14)]
                            Text [test]
                        Container [2]
                            Container [0]
                            Text [test]
                        Container [2]
                            Container [0]
                            Text [one]
            """;

        assertEquals(expected, stage.toString());
    }

    @Test
    void useTranslations() {
        HeadlessRenderer renderer = new HeadlessRenderer();
        Stage stage = renderer.getStage();

        Form form = new Form(renderer, 100, 20);
        form.setBundle(TranslationBundle.from(PropertyUtils.loadProperties("a=something")));
        form.addLabel("a", STYLE);
        form.addLabel("b", STYLE);

        stage.getRoot().addChild(form.getGraphics());

        String expected = """
            Stage
                $$root [1]
                    Form [2]
                        Text [something]
                        Text [b]
            """;

        assertEquals(expected, stage.toString());
    }

    @Test
    void addSpacer() {
        HeadlessRenderer renderer = new HeadlessRenderer();
        Stage stage = renderer.getStage();

        Form form = new Form(renderer, 100, 20);
        form.addLabel("a", STYLE);
        form.addSpacer();
        form.addLabel("b", STYLE);

        stage.getRoot().addChild(form.getGraphics());

        String expected = """
            Stage
                $$root [1]
                    Form [3]
                        Text [a]
                        Container [0]
                        Text [b]
            """;

        assertEquals(expected, stage.toString());
    }

    @Test
    void fullWidthRow() {
        HeadlessRenderer renderer = new HeadlessRenderer();

        Form form = new Form(renderer, 100, 20);
        form.addTitle("a", STYLE);
        form.addButton("b", BUTTON_STYLE);

        assertEquals(2, form.getWidgetBounds().size());
        assertEquals("(0, 0, 100, 20)", form.getWidgetBounds().get(0).toString());
        assertEquals("(0, 20, 100, 20)", form.getWidgetBounds().get(1).toString());
    }

    @Test
    void halfWidthRow() {
        HeadlessRenderer renderer = new HeadlessRenderer();

        Form form = new Form(renderer, 100, 20);
        form.addTitle("a", STYLE);
        form.addLabel("b", STYLE);
        form.addButton("c", BUTTON_STYLE);
        form.addButton("d", BUTTON_STYLE);

        assertEquals(4, form.getWidgetBounds().size());
        assertEquals("(0, 0, 100, 20)", form.getWidgetBounds().get(0).toString());
        assertEquals("(0, 20, 50, 20)", form.getWidgetBounds().get(1).toString());
        assertEquals("(50, 20, 50, 20)", form.getWidgetBounds().get(2).toString());
        assertEquals("(0, 40, 100, 20)", form.getWidgetBounds().get(3).toString());
    }

    @Test
    void startWithHalfWidth() {
        HeadlessRenderer renderer = new HeadlessRenderer();

        Form form = new Form(renderer, 100, 20);
        form.addLabel("a", STYLE);
        form.addButton("b", BUTTON_STYLE);
        form.addLabel("c", STYLE);
        form.addLabel("d", STYLE);

        assertEquals(4, form.getWidgetBounds().size());
        assertEquals("(0, 0, 50, 20)", form.getWidgetBounds().get(0).toString());
        assertEquals("(50, 0, 50, 20)", form.getWidgetBounds().get(1).toString());
        assertEquals("(0, 20, 50, 20)", form.getWidgetBounds().get(2).toString());
        assertEquals("(50, 20, 50, 20)", form.getWidgetBounds().get(3).toString());
    }

    @Test
    void setGap() {
        HeadlessRenderer renderer = new HeadlessRenderer();

        Form form = new Form(renderer, 100, 20);
        form.setGap(20, 10);
        form.addLabel("a", STYLE);
        form.addButton("b", BUTTON_STYLE);
        form.addLabel("c", STYLE);
        form.addLabel("d", STYLE);

        assertEquals(4, form.getWidgetBounds().size());
        assertEquals("(0, 0, 40, 20)", form.getWidgetBounds().get(0).toString());
        assertEquals("(60, 0, 40, 20)", form.getWidgetBounds().get(1).toString());
        assertEquals("(0, 30, 40, 20)", form.getWidgetBounds().get(2).toString());
        assertEquals("(60, 30, 40, 20)", form.getWidgetBounds().get(3).toString());
    }

    @Test
    void handleClicks() {
        HeadlessRenderer renderer = new HeadlessRenderer();
        Stage stage = renderer.getStage();
        List<String> events = new ArrayList<>();

        Form form = new Form(renderer, 100, 20);
        form.addButton("a", BUTTON_STYLE).subscribe(_ -> events.add("button"));
        form.addSelectField("select1", List.of("select1", "select2"), BUTTON_STYLE).subscribe(events::add);

        stage.getRoot().addChild(form.getGraphics());
        renderer.attach(form);
        renderer.doFrame(1.0);

        assertEquals(List.of(), events);

        renderer.setPointer(new Point2D(10, 10));
        renderer.setPointerReleased(true);
        renderer.doFrame(1.0);

        assertEquals(List.of("button"), events);

        renderer.setPointer(new Point2D(10, 30));
        renderer.setPointerReleased(true);
        renderer.doFrame(1.0);

        assertEquals(List.of("button", "select2"), events);
    }

    @Test
    void handleClicksIfFormIsMoved() {
        HeadlessRenderer renderer = new HeadlessRenderer();
        Stage stage = renderer.getStage();
        List<String> events = new ArrayList<>();

        Form form = new Form(renderer, 100, 20);
        form.addButton("a", BUTTON_STYLE).subscribe(_ -> events.add("button"));

        stage.getRoot().addChild(form.getGraphics());
        renderer.attach(form);
        form.getGraphics().getTransform().setPosition(1000, 0);
        renderer.doFrame(1.0);

        assertEquals(List.of(), events);

        renderer.setPointer(new Point2D(10, 10));
        renderer.setPointerReleased(true);
        renderer.doFrame(1.0);

        assertEquals(List.of(), events);

        renderer.setPointer(new Point2D(1010, 10));
        renderer.setPointerReleased(true);
        renderer.doFrame(1.0);

        assertEquals(List.of("button"), events);
    }

    @Test
    void pipeInputEvents() {
        HeadlessRenderer renderer = new HeadlessRenderer();
        Stage stage = renderer.getStage();
        List<String> events = new ArrayList<>();

        Form form = new Form(renderer, 100, 20);
        form.addInputField("a", STYLE).subscribe(events::add);

        stage.getRoot().addChild(form.getGraphics());
        renderer.attach(form);
        renderer.doFrame(1.0);

        assertEquals(List.of(), events);

        renderer.setPointer(new Point2D(10, 10));
        renderer.setPointerReleased(true);
        renderer.doFrame(1.0);

        assertEquals(List.of(), events);

        renderer.getTextInputQueue().onNext("abcd");
        renderer.doFrame(1.0);

        assertEquals(List.of("abcd"), events);
    }

    @Test
    void useImageBackground() {
        HeadlessRenderer renderer = new HeadlessRenderer();
        Stage stage = renderer.getStage();

        Form form = new Form(renderer, 100, 20);
        form.setBundle(TranslationBundle.from(PropertyUtils.loadProperties("a=something")));
        form.addButton("a", STYLE.withBackgroundImage(new MockImage()));

        stage.getRoot().addChild(form.getGraphics());

        String expected = """
            Stage
                $$root [1]
                    Form [1]
                        Container [2]
                            Container [1]
                                Sprite [$$default]
                            Text [something]
            """;

        assertEquals(expected, stage.toString());
    }

    @Test
    void autoFitImageBackground() {
        HeadlessRenderer renderer = new HeadlessRenderer();
        Stage stage = renderer.getStage();

        DeclarativeStyle scaleToFitStyle = STYLE
            .withBackgroundImage(new MockImage(200, 100))
            .withBackgroundImageScaleToFit();

        Form form = new Form(renderer, 100, 20);
        form.setBundle(TranslationBundle.from(PropertyUtils.loadProperties("a=something")));
        form.addLabel("a", STYLE);
        form.addButton("b", scaleToFitStyle);

        stage.getRoot().addChild(form.getGraphics());
        MockStageVisitor stageVisitor = new MockStageVisitor();
        stage.visit(stageVisitor);

        Sprite backgroundSprite = stageVisitor.getGraphics().stream()
            .filter(g -> g instanceof Sprite)
            .map(g -> (Sprite) g)
            .findFirst()
            .get();

        assertEquals(25.0, backgroundSprite.getTransform().getScaleX(), EPSILON);
        assertEquals(20.0, backgroundSprite.getTransform().getScaleY(), EPSILON);
    }

    @Test
    void getFormHeight() {
        HeadlessRenderer renderer = new HeadlessRenderer();

        Form form = new Form(renderer, 100, 20);
        form.setGap(0, 10);
        renderer.getStage().getRoot().addChild(form.getGraphics());

        assertEquals(0, form.getFormHeight());

        form.addTitle("a", STYLE);

        assertEquals(20, form.getFormHeight());

        form.addLabel("b", STYLE);

        assertEquals(50, form.getFormHeight());
    }
}
