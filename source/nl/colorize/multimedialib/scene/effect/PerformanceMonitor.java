//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene.effect;

import nl.colorize.multimedialib.math.Line;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.math.SegmentedLine;
import nl.colorize.multimedialib.renderer.FrameStats;
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.scene.SceneContext;
import nl.colorize.multimedialib.stage.Align;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.Container;
import nl.colorize.multimedialib.stage.StageNode2D;
import nl.colorize.multimedialib.stage.FontFace;
import nl.colorize.multimedialib.stage.Primitive;
import nl.colorize.multimedialib.stage.Text;
import nl.colorize.util.TextUtils;

import java.util.ArrayList;
import java.util.List;

import static nl.colorize.multimedialib.stage.ColorRGB.BLACK;
import static nl.colorize.multimedialib.stage.ColorRGB.WHITE;

/**
 * Depicts various performance statistics, both in terms of overall performance
 * and on a frame-by-frame basis. When the widget is active, it will
 * automatically capture and visualize every frame update. If the widget is
 * marked as inactive, it will disable this logic, ironically in order to
 * conserve performance.
 * <p>
 * This widget is included as part of the library so that it can be used as a
 * debugging tool in applications.
 */
public class PerformanceMonitor implements Scene {

    private Container container;
    private Text framerate;
    private Container frameDataContainer;
    private boolean detailed;

    private static final ColorRGB FRAME_COLOR = ColorRGB.parseHex("#e45d61");
    private static final ColorRGB UPDATE_COLOR = ColorRGB.parseHex("#DC9498");
    private static final ColorRGB RENDER_COLOR = ColorRGB.parseHex("#DCBEC0");
    private static final ColorRGB LINE_COLOR = ColorRGB.parseHex("#adadad");

    public PerformanceMonitor(boolean detailed) {
        this.detailed = detailed;
        container = new Container();
        container.addChild(new Primitive(new Rect(0, 0, 300, 100), BLACK, 50));
    }

    @Override
    public void start(SceneContext context) {
        context.getStage().getRoot().addChild(container);

        frameDataContainer = new Container();
        container.addChild(frameDataContainer);

        FontFace font = context.getMediaLoader().loadDefaultFont(12, WHITE);
        framerate = new Text("", font.derive(30), Align.RIGHT);
        framerate.getTransform().setPosition(290, 30);
        container.addChild(framerate);

        for (int i = 0; i <= 5; i++) {
            container.addChild(new Primitive(new Line(0, i * 20, 300, i * 20), LINE_COLOR));

            if (i > 0) {
                Text label = new Text((i * 10) + "ms", font.derive(10).derive(LINE_COLOR));
                label.getTransform().setPosition(5, (5 - i) * 20 + 12);
                container.addChild(label);
            }
        }

        container.addChild(new Primitive(new Line(0, 0, 300, 0), WHITE));
        container.addChild(new Primitive(new Line(0, 100, 300, 100), WHITE));
        container.addChild(new Primitive(new Line(0, 0, 0, 100), LINE_COLOR));
        container.addChild(new Primitive(new Line(300, 0, 300, 100), LINE_COLOR));
    }

    @Override
    public void update(SceneContext context, float deltaTime) {
        FrameStats stats = context.getSceneManager().getFrameStats();

        if (isActive() && stats.getBufferSize() >= 10) {
            container.setPosition(20, context.getCanvas().getHeight() - 120);
            framerate.setText(TextUtils.numberFormat(stats.getAverageFramerate(), 1));

            Iterable<Long> frameTimes = stats.getFrameTimes(FrameStats.PHASE_FRAME_TIME);
            Iterable<Long> frameUpdateTimes = stats.getFrameTimes(FrameStats.PHASE_FRAME_UPDATE);
            Iterable<Long> frameRenderTimes = stats.getFrameTimes(FrameStats.PHASE_FRAME_RENDER);

            frameDataContainer.clearChildren();
            if (detailed) {
                frameDataContainer.addChild(depictFrameStats(frameRenderTimes, RENDER_COLOR));
                frameDataContainer.addChild(depictFrameStats(frameUpdateTimes, UPDATE_COLOR));
            }
            frameDataContainer.addChild(depictFrameStats(frameTimes, FRAME_COLOR));
        }
    }

    private StageNode2D depictFrameStats(Iterable<Long> frameTimes, ColorRGB color) {
        float x = 0;
        List<Point2D> points = new ArrayList<>();

        for (long frameTime : frameTimes) {
            float y = Math.clamp(100f - frameTime * 2f, 0f, 100f);
            points.add(new Point2D(x, y));
            x += 300f / FrameStats.BUFFER_CAPACITY;
        }

        Primitive line = new Primitive(new SegmentedLine(points), color);
        line.setStroke(3f);
        return line;
    }

    public void setActive(boolean active) {
        container.getTransform().setVisible(active);
    }

    public boolean isActive() {
        return container.getTransform().isVisible();
    }
}
