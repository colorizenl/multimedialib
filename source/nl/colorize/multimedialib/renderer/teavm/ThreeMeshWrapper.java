//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import lombok.Getter;
import nl.colorize.multimedialib.math.Region;
import nl.colorize.multimedialib.renderer.teavm.ThreeBridge.ThreeObject;
import nl.colorize.multimedialib.scene.Timer;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.Image;
import nl.colorize.multimedialib.stage.Mesh;
import nl.colorize.multimedialib.stage.Sprite;
import nl.colorize.multimedialib.stage.Transform3D;
import nl.colorize.util.Subject;
import org.teavm.jso.dom.html.HTMLImageElement;

import static nl.colorize.multimedialib.renderer.teavm.TeaImage.IMAGE_LOADING_REGION;

/**
 * Wrapper around a Three.js Mesh/Object3D instance in JavaScript, since that
 * cannot implement {@link Mesh} directly. If the model is being loaded
 * asynchronously, instances of this class can be used even when the model
 * is still loading.
 */
@Getter
public class ThreeMeshWrapper implements Mesh {

    private Subject<ThreeObject> meshPromise;
    private ThreeObject threeObject;
    private Transform3D transform;
    private Transform3D globalTransform;
    private Sprite dynamicTexture;

    public ThreeMeshWrapper(Subject<ThreeObject> meshPromise) {
        this.meshPromise = meshPromise;
        this.transform = new Transform3D();
        this.globalTransform = new Transform3D();

        meshPromise.subscribe(event -> threeObject = event);
    }

    @Override
    public void animate(Timer animationTimer) {
        if (dynamicTexture != null) {
            dynamicTexture.animate(animationTimer);
            TeaImage image = (TeaImage) dynamicTexture.getCurrentGraphics();
            if (threeObject != null && image.getImageElement().isPresent()) {
                applyTexture(image);
            }
        }
    }

    @Override
    public void applyColor(ColorRGB color) {
        ThreeBridge threeBridge = Browser.getThreeBridge();
        if (threeObject == null) {
            meshPromise.subscribe(model -> threeBridge.applyColor(model, color.toHex()));
        } else {
            threeBridge.applyColor(threeObject, color.toHex());
        }
    }

    @Override
    public void applyTexture(Image texture) {
        TeaImage teaImage = (TeaImage) texture;
        Region region = teaImage.getRegion();

        if (teaImage.getImageElement().isPresent()) {
            applyTexture(teaImage.getImageElement().get(), region);
        } else {
            teaImage.getImagePromise().subscribe(image -> applyTexture(image, region));
        }
    }

    private void applyTexture(HTMLImageElement image, Region region) {
        ThreeBridge threeBridge = Browser.getThreeBridge();
        int x = region.x();
        int y = region.y();
        int w = region.equals(IMAGE_LOADING_REGION) ? image.getWidth() : region.width();
        int h = region.equals(IMAGE_LOADING_REGION) ? image.getHeight() : region.height();

        if (threeObject == null) {
            meshPromise.subscribe(model -> threeBridge.applyTexture(model, image, x, y, w, h));
        } else {
            threeBridge.applyTexture(threeObject, image, x, y, w, h);
        }
    }

    @Override
    public void applyDynamicTexture(Sprite sprite) {
        dynamicTexture = sprite;
        applyTexture(dynamicTexture.getCurrentGraphics());
    }

    @Override
    public ThreeMeshWrapper copy() {
        return new ThreeMeshWrapper(meshPromise.map(original -> {
            ThreeBridge threeBridge = Browser.getThreeBridge();
            return threeBridge.cloneObject(original);
        }));
    }
}
