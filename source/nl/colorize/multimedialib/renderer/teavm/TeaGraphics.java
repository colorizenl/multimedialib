//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import nl.colorize.multimedialib.math.Box;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Point3D;
import nl.colorize.multimedialib.math.Shape3D;
import nl.colorize.multimedialib.renderer.GraphicsMode;
import nl.colorize.multimedialib.scene.SceneContext;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.Mesh;
import nl.colorize.multimedialib.stage.StageVisitor;
import org.teavm.jso.browser.Window;

/**
 * Base interface for the different JavaScript graphics frameworks supported by
 * the TeaVM renderer.
 */
public interface TeaGraphics extends StageVisitor {

    public GraphicsMode getGraphicsMode();

    public void init(SceneContext context);

    public int getDisplayWidth();

    public int getDisplayHeight();

    default float getDevicePixelRatio() {
        return (float) Window.current().getDevicePixelRatio();
    }

    public Mesh createMesh(Shape3D shape, ColorRGB color);

    public Point2D project(Point3D position);

    public boolean castPickRay(Point2D canvasPosition, Box area);
}
