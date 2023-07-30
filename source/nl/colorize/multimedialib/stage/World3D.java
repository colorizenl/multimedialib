//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import lombok.Data;
import nl.colorize.multimedialib.math.Point3D;
import nl.colorize.multimedialib.scene.Updatable;

import java.util.ArrayList;
import java.util.List;

/**
 * Access to all 3D graphicson the stage, only avalailable when using a
 * renderer that supports 3D graphics. If both 2D and 3D graphics are used,
 * the 3D graphics will appear "below" all 2D graphics.
 */
@Data
public class World3D implements Updatable {

    private Point3D cameraPosition;
    private Point3D cameraTarget;

    private ColorRGB ambientLight;
    private ColorRGB lightColor;
    private Point3D lightPosition;

    private final List<PolygonModel> children;

    protected World3D() {
        this.cameraPosition = new Point3D(10f, 10f, 10f);
        this.cameraTarget = new Point3D(0f, 0f, 0f);

        this.ambientLight = new ColorRGB(100, 100, 100);
        this.lightColor = new ColorRGB(200, 200, 200);
        this.lightPosition = new Point3D(-1f, -0.8f, -0.2f);

        this.children = new ArrayList<>();
    }

    @Override
    public void update(float deltaTime) {
        for (PolygonModel model : children) {
            model.update(deltaTime);
        }
    }

    @Override
    public String toString() {
        return "World3D";
    }
}
