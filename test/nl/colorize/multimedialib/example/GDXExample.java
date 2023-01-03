//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.example;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;

import java.util.ArrayList;
import java.util.List;

public class GDXExample implements ApplicationListener {

    private PerspectiveCamera camera;
    private CameraInputController cameraController;
    private Environment environment;

    private AssetManager assetManager;
    private boolean loading;

    private List<Model> models;
    private List<ModelInstance> instances;
    private List<AnimationController> animations;
    private ModelBatch modelBatch;

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setWindowedMode(800, 600);
        config.setDecorated(true);
        config.setIdleFPS(60);
        config.setTitle("GDX Example");

        GDXExample app = new GDXExample();
        new Lwjgl3Application(app, config);
    }

    @Override
    public void create() {
        models = new ArrayList<>();
        instances = new ArrayList<>();
        animations = new ArrayList<>();

        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(10f, 10f, 10f);
        camera.lookAt(0, 0, 0);
        camera.near = 1f;
        camera.far = 300f;
        camera.update();

        cameraController = new CameraInputController(camera);
        Gdx.input.setInputProcessor(cameraController);

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        loading = true;
        assetManager = new AssetManager();
        assetManager.load("model.g3db", Model.class);

        modelBatch = new ModelBatch();
    }

    private void onLoadingComplete() {
        loading = false;

        Model model = assetManager.get("model.g3db", Model.class);
        models.add(model);

        ModelInstance instance = new ModelInstance(model);
        instances.add(instance);

        AnimationController animation = new AnimationController(instance);
        animation.animate("Cube|Spin", 1, 1f, null, 0f);
        animations.add(animation);
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void render() {
        if (loading && assetManager.update()) {
            onLoadingComplete();
        }

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        cameraController.update();

        for (ModelInstance instance : instances) {
            instance.transform.rotate(1f, 0f, 0f, 1f);
        }

        for (AnimationController animation : animations) {
            animation.update(1f / 60f);
        }

        modelBatch.begin(camera);
        modelBatch.render(instances, environment);
        modelBatch.end();
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        models.forEach(model -> model.dispose());
        models.clear();
    }
}
