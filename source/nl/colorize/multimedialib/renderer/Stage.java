//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import nl.colorize.multimedialib.graphics.AnimationInfo;
import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.graphics.PolygonMesh;
import nl.colorize.multimedialib.graphics.PolygonModel;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Point3D;

/**
 * The stage contains everything that should be displayed. The stage can contain
 * 2D graphics, 3D graphics, or a combination of the two.
 * <p>
 * The contents of a stage consist of a camera, lighting, and a number of
 * models. Models can be loaded from files using the {@link MediaLoader}, but
 * the stage can also contain simple geometry programatically.
 */
public interface Stage extends Updatable {

    public void moveCamera(Point3D position, Point3D direction);

    public Point3D getCameraPosition();

    public void changeAmbientLight(ColorRGB color);

    public void changeLight(ColorRGB color, Point3D target);

    public void add(PolygonModel model);

    public void remove(PolygonModel model);

    public void clear();

    public void playAnimation(PolygonModel model, AnimationInfo animation, boolean loop);

    public PolygonMesh createQuad(Point2D size, ColorRGB color);

    public PolygonMesh createQuad(Point2D size, Image texture);

    public PolygonMesh createBox(Point3D size, ColorRGB color);

    public PolygonMesh createBox(Point3D size, Image texture);

    public PolygonMesh createSphere(float diameter, ColorRGB color);

    public PolygonMesh createSphere(float diameter, Image texture);
}
