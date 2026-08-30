//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene.effect;

import nl.colorize.multimedialib.math.Line;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.math.SegmentedLine;
import nl.colorize.multimedialib.renderer.FrameStats;
import nl.colorize.multimedialib.scene.Actor;
import nl.colorize.multimedialib.scene.GraphicsProvider;
import nl.colorize.multimedialib.stage.Align;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.Container;
import nl.colorize.multimedialib.stage.Primitive;
import nl.colorize.multimedialib.stage.Spatial2D;
import nl.colorize.multimedialib.stage.Text;
import nl.colorize.util.TextUtils;

import java.util.ArrayList;
import java.util.List;

import static nl.colorize.multimedialib.stage.ColorRGB.BLACK;
import static nl.colorize.multimedialib.stage.ColorRGB.WHITE;
import static nl.colorize.multimedialib.stage.FontFace.DEFAULT_FONT;

/**
 * Depicts various performance statistics, both in terms of overall
 * performance and on a frame-by-frame basis. When the widget is active, it
 * will automatically capture and visualize every frame update. If the widget
 * is marked as inactive, it will disable this logic (monitoring performance
 * is, ironically, bad for performance).
 * <p>
 * This widget is included as part of the library so that it can be used as a
 * debugging tool in applications.
 */
public class PerformanceMonitor implements Actor, GraphicsProvider {

    private FrameStats stats;
    private boolean detailed;

    private Container container;
    private Text framerate;
    private Container frameDataContainer;

    private static final ColorRGB FRAME_COLOR = ColorRGB.parseHex("#e45d61");
    private static final ColorRGB UPDATE_COLOR = ColorRGB.parseHex("#DC9498");
    private static final ColorRGB RENDER_COLOR = ColorRGB.parseHex("#DCBEC0");
    private static final ColorRGB LINE_COLOR = ColorRGB.parseHex("#adadad");

    public PerformanceMonitor(FrameStats stats, boolean detailed) {
        this.stats = stats;
        this.detailed = detailed;
        initGraphics();
    }

    public PerformanceMonitor(FrameStats stats) {
        this(stats, true);
    }

    private void initGraphics() {
        container = new Container();
        container.addChild(new Primitive(new Rect(0, 0, 300, 100), BLACK, 50));
        frameDataContainer = new Container();
        container.addChild(frameDataContainer);

        framerate = new Text("", DEFAULT_FONT.derive(30), Align.RIGHT);
        framerate.getTransform().setPosition(290, 30);
        container.addChild(framerate);

        for (int i = 0; i <= 5; i++) {
            container.addChild(new Primitive(new Line(0, i * 20, 300, i * 20), LINE_COLOR));

            if (i > 0) {
                Text label = new Text((i * 10) + "ms", DEFAULT_FONT.derive(10).derive(LINE_COLOR));
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
    public void update(double deltaTime) {
        if (isActive() && stats.getBufferSize() >= 10) {
            container.getTransform().setPosition(20, 20);
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

    private Spatial2D depictFrameStats(Iterable<Long> frameTimes, ColorRGB color) {
        double x = 0;
        List<Point2D> points = new ArrayList<>();

        for (long frameTime : frameTimes) {
            double y = Math.clamp(100f - frameTime * 2f, 0f, 100f);
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

    @Override
    public Spatial2D getGraphics() {
        return container;
    }
}
