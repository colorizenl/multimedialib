//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;
import org.teavm.jso.dom.html.HTMLImageElement;

/**
 * TeaVM interface for the {@code three-bridge.js} JavaScript implementation.
 * Includes TeaVM stubs for <a href="https://threejs.org">Three.js</a> classes.
 */
public interface ThreeBridge extends JSObject {

    @JSProperty
    public ThreeObject getScene();

    @JSProperty
    public ThreeLight getAmbientLight();

    public void init();

    public void render();

    public void changeBackgroundColor(String color);

    public void moveCamera(float x, float y, float z, float focusX, float focusY, float focusZ);

    public ThreeObject createBox(float width, float height, float depth, String color);

    public ThreeObject createSphere(float radius, String color);

    public ThreeLight createLight(String color, float intensity);

    public void applyColor(ThreeObject model, String color);

    public void applyTexture(ThreeObject model, HTMLImageElement image, int x, int y, int w, int h);

    public ThreeObject cloneObject(ThreeObject original);

    public void loadGLTF(String url, ModelCallback callback);

    public void loadOBJ(String url, ModelCallback callback);

    public ThreeVector project(float x, float y, float z);

    public boolean castPickRay(float pointX, float pointY,
                               float x0, float y0, float z0, float x1, float y1, float z1);

    /**
     * <a href="https://threejs.org/docs/index.html#api/en/core/Object3D">Object3D</a>
     */
    interface ThreeObject extends JSObject {

        @JSProperty
        public void setVisible(boolean visible);

        @JSProperty
        public ThreeVector getPosition();

        @JSProperty
        public ThreeVector getRotation();

        @JSProperty
        public ThreeVector getScale();

        @JSProperty
        public ThreeMaterial getMaterial();

        public void add(ThreeObject child);

        public void removeFromParent();
    }

    /**
     * <a href="https://threejs.org/docs/index.html#api/en/lights/Light">Light</a>
     */
    interface ThreeLight extends ThreeObject {

        @JSProperty
        public ThreeColor getColor();

        @JSProperty
        public void setIntensity(float intensity);
    }

    /**
     * <a href="https://threejs.org/docs/#api/en/materials/Material">Material</a>
     */
    interface ThreeMaterial extends ThreeObject {

        @JSProperty
        public ThreeColor getColor();
    }

    /**
     * <a href="https://threejs.org/docs/#api/en/textures/Texture">Texture</a>
     */
    interface ThreeTexture extends ThreeObject {
    }

    /**
     * <a href="https://threejs.org/docs/index.html#api/en/math/Vector3">Vector3</a>
     */
    interface ThreeVector extends JSObject {

        @JSProperty
        public float getX();

        @JSProperty
        public float getY();

        @JSProperty
        public float getZ();

        public void set(float x, float y, float z);
    }

    /**
     * <a href="https://threejs.org/docs/#api/en/math/Color">Color</a>
     */
    interface ThreeColor extends ThreeObject {

        public void set(String hex);
    }

    /**
     * <a href="https://threejs.org/docs/#examples/en/loaders/GLTFLoader">GLTFLoader</a>
     */
    @JSFunctor
    @FunctionalInterface
    interface ModelCallback extends JSObject {

        public void onLoadModel(ThreeObject model);
    }
}
