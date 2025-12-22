//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.mock;

import lombok.Getter;
import lombok.Setter;
import nl.colorize.multimedialib.scene.Timer;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.Group;
import nl.colorize.multimedialib.stage.Image;
import nl.colorize.multimedialib.stage.Mesh;
import nl.colorize.multimedialib.stage.Sprite;
import nl.colorize.multimedialib.stage.Transform3D;

@Getter
public class MockMesh implements Mesh {

    @Setter Group parent;
    private Transform3D transform;
    private Transform3D globalTransform;

    public MockMesh() {
        this.transform = new Transform3D();
        this.globalTransform = new Transform3D();
    }

    @Override
    public void applyColor(ColorRGB color) {
    }

    @Override
    public void applyTexture(Image texture) {
    }

    @Override
    public void applyDynamicTexture(Sprite sprite) {
    }

    @Override
    public Sprite getDynamicTexture() {
        return null;
    }

    @Override
    public Mesh copy() {
        return new MockMesh();
    }

    @Override
    public void animate(Timer animationTimer) {
    }
}
