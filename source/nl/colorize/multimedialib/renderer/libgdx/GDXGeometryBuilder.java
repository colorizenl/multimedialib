//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.libgdx;

import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Point3D;
import nl.colorize.multimedialib.renderer.GeometryBuilder;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.Image;
import nl.colorize.multimedialib.stage.PolygonModel;

import static com.badlogic.gdx.graphics.VertexAttributes.Usage.Normal;
import static com.badlogic.gdx.graphics.VertexAttributes.Usage.Position;
import static com.badlogic.gdx.graphics.VertexAttributes.Usage.TextureCoordinates;

public class GDXGeometryBuilder implements GeometryBuilder {

    private GDXMediaLoader media;

    protected GDXGeometryBuilder(GDXMediaLoader media) {
        this.media = media;
    }

    @Override
    public PolygonModel createQuad(Point2D size, ColorRGB color) {
        return createBox(new Point3D(size.x(), 0.001f, size.y()), color);
    }

    @Override
    public PolygonModel createQuad(Point2D size, Image texture) {
        return createBox(new Point3D(size.x(), 0.001f, size.y()), texture);
    }

    @Override
    public PolygonModel createBox(Point3D size, ColorRGB color) {
        ModelBuilder modelBuilder = new ModelBuilder();
        Model model = modelBuilder.createBox(size.x(), size.y(), size.z(),
            createMaterial(color), Position | Normal);
        return media.createInstance(model);
    }

    @Override
    public PolygonModel createBox(Point3D size, Image texture) {
        ModelBuilder modelBuilder = new ModelBuilder();
        Model model = modelBuilder.createBox(size.x(), size.y(), size.z(),
            createMaterial((GDXImage) texture), Position | Normal | TextureCoordinates);
        return media.createInstance(model);
    }

    @Override
    public PolygonModel createSphere(float diameter, ColorRGB color) {
        ModelBuilder modelBuilder = new ModelBuilder();
        Model model = modelBuilder.createSphere(diameter, diameter, diameter, 32, 32,
            createMaterial(color), Position | Normal);
        return media.createInstance(model);
    }

    @Override
    public PolygonModel createSphere(float diameter, Image texture) {
        ModelBuilder modelBuilder = new ModelBuilder();
        Model model = modelBuilder.createSphere(diameter, diameter, diameter, 32, 32,
            createMaterial((GDXImage) texture), Position | Normal | TextureCoordinates);
        return media.createInstance(model);
    }

    private Material createMaterial(ColorRGB color) {
        ColorAttribute colorAttr = ColorAttribute.createDiffuse(GDXMediaLoader.toColor(color));
        return new Material(colorAttr);
    }

    private Material createMaterial(GDXImage texture) {
        TextureAttribute colorAttr = TextureAttribute.createDiffuse(texture.getTextureRegion());
        return new Material(colorAttr);
    }
}
