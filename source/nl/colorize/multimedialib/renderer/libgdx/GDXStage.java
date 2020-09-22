//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.libgdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.graphics.AnimationInfo;
import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.graphics.PolygonMesh;
import nl.colorize.multimedialib.graphics.PolygonModel;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Point3D;
import nl.colorize.multimedialib.renderer.Stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.badlogic.gdx.graphics.VertexAttributes.Usage.Normal;
import static com.badlogic.gdx.graphics.VertexAttributes.Usage.Position;
import static com.badlogic.gdx.graphics.VertexAttributes.Usage.TextureCoordinates;

public class GDXStage implements Stage {

    private PerspectiveCamera camera;
    private DirectionalLight light;
    private Environment environment;

    private GDXMediaLoader mediaLoader;
    private List<PolygonModel> models;
    private Map<PolygonModel, ModelInstance> modelInstances;
    private Map<PolygonModel, AnimationController> animationsPlaying;

    private static final int FIELD_OF_VIEW = 75;
    private static final float NEAR_PLANE = 1f;
    private static final float FAR_PLANE = 300f;
    private static final ColorRGB AMBIENT_LIGHT_COLOR = new ColorRGB(100, 100, 100);
    private static final ColorRGB DEFAULT_LIGHT_COLOR = new ColorRGB(200, 200, 200);
    private static final Point3D DEFAULT_LIGHT_POS = new Point3D(-1f, -0.8f, -0.2f);

    protected GDXStage(GDXMediaLoader mediaLoader) {
        this.mediaLoader = mediaLoader;
        this.models = new ArrayList<>();
        this.modelInstances = new HashMap<>();
        this.animationsPlaying = new HashMap<>();

        initStage();
    }

    private void initStage() {
        camera = new PerspectiveCamera(FIELD_OF_VIEW, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(10f, 10f, 10f);
        camera.lookAt(0, 0, 0);
        camera.near = NEAR_PLANE;
        camera.far = FAR_PLANE;
        camera.update();

        light = new DirectionalLight();
        changeLight(DEFAULT_LIGHT_COLOR, DEFAULT_LIGHT_POS);

        environment = new Environment();
        changeAmbientLight(AMBIENT_LIGHT_COLOR);
        environment.add(light);
    }

    @Override
    public void moveCamera(Point3D position, Point3D target) {
        camera.position.set(position.getX(), position.getY(), position.getZ());
        camera.up.set(0f, 1f, 0f);
        camera.lookAt(target.getX(), target.getY(), target.getZ());
        camera.update();
    }

    @Override
    public Point3D getCameraPosition() {
        return new Point3D(camera.position.x, camera.position.y, camera.position.z);
    }

    @Override
    public void changeAmbientLight(ColorRGB color) {
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, createColor(color)));
    }

    @Override
    public void changeLight(ColorRGB color, Point3D direction) {
        light.set(createColor(color), direction.getX(), direction.getY(), direction.getZ());
    }

    @Override
    public void add(PolygonModel model) {
        models.add(model);

        Model modelData = mediaLoader.getModelData(model.getMesh());
        ModelInstance modelInstance = new ModelInstance(modelData);
        modelInstances.put(model, modelInstance);
    }

    @Override
    public void remove(PolygonModel model) {
        models.remove(model);
        modelInstances.remove(model);
        animationsPlaying.remove(model);
    }

    @Override
    public void clear() {
        models.clear();
        modelInstances.clear();
        animationsPlaying.clear();
    }

    @Override
    public void playAnimation(PolygonModel model, AnimationInfo animation, boolean loop) {
        ModelInstance modelInstance = getModelInstance(model);
        int loopCount = loop ? Integer.MAX_VALUE : 1;

        AnimationController animationController = new AnimationController(modelInstance);
        animationController.animate(animation.getName(), loopCount, 1f, null, 0f);
        animationsPlaying.put(model, animationController);
    }

    protected PerspectiveCamera getCamera() {
        return camera;
    }

    protected Environment getEnvironment() {
        return environment;
    }

    @Override
    public void update(float deltaTime) {
        camera.update();
        updateAnimations(deltaTime);
    }

    private void updateAnimations(float deltaTime) {
        List<PolygonModel> completedAnimations = new ArrayList<>();

        for (Map.Entry<PolygonModel, AnimationController> entry : animationsPlaying.entrySet()) {
            AnimationController animation = entry.getValue();
            animation.update(deltaTime);

            if (isAnimationCompleted(animation)) {
                completedAnimations.add(entry.getKey());
            }
        }

        for (PolygonModel key : completedAnimations) {
            animationsPlaying.remove(key);
        }
    }

    private boolean isAnimationCompleted(AnimationController animation) {
        return animation.current == null || animation.current.loopCount == 0;
    }

    protected List<ModelInstance> getModelDisplayList() {
        return models.stream()
            .map(model -> syncModelInstance(model))
            .collect(Collectors.toList());
    }

    private ModelInstance getModelInstance(PolygonModel model) {
        ModelInstance modelInstance = modelInstances.get(model);
        Preconditions.checkState(modelInstance != null, "Model instance not available");
        return modelInstance;
    }

    private ModelInstance syncModelInstance(PolygonModel model) {
        ModelInstance modelInstance = getModelInstance(model);

        Matrix4 transform = modelInstance.transform;
        transform.setToTranslation(model.getPosition().getX(), model.getPosition().getY(),
            model.getPosition().getZ());
        transform.rotate(model.getRotationX(), model.getRotationY(), model.getRotationZ(),
            model.getRotationAmount());
        transform.scale(model.getScaleX(), model.getScaleY(), model.getScaleZ());

        return modelInstance;
    }

    @Override
    public PolygonMesh createQuad(Point2D size, ColorRGB color) {
        return createBox(new Point3D(size.getX(), 0.001f, size.getY()), color);
    }

    @Override
    public PolygonMesh createQuad(Point2D size, Image texture) {
        return createBox(new Point3D(size.getX(), 0.001f, size.getY()), texture);
    }

    @Override
    public PolygonMesh createBox(Point3D size, ColorRGB color) {
        ModelBuilder modelBuilder = new ModelBuilder();
        Model model = modelBuilder.createBox(size.getX(), size.getY(), size.getZ(),
            createMaterial(color), Position | Normal);
        return mediaLoader.registerMesh("box", model);
    }

    @Override
    public PolygonMesh createBox(Point3D size, Image texture) {
        ModelBuilder modelBuilder = new ModelBuilder();
        Model model = modelBuilder.createBox(size.getX(), size.getY(), size.getZ(),
            createMaterial((GDXImage) texture), Position | Normal | TextureCoordinates);
        return mediaLoader.registerMesh("box", model);
    }

    @Override
    public PolygonMesh createSphere(float diameter, ColorRGB color) {
        ModelBuilder modelBuilder = new ModelBuilder();
        Model model = modelBuilder.createSphere(diameter, diameter, diameter, 32, 32,
            createMaterial(color), Position | Normal);
        return mediaLoader.registerMesh("sphere", model);
    }

    @Override
    public PolygonMesh createSphere(float diameter, Image texture) {
        ModelBuilder modelBuilder = new ModelBuilder();
        Model model = modelBuilder.createSphere(diameter, diameter, diameter, 32, 32,
            createMaterial((GDXImage) texture), Position | Normal | TextureCoordinates);
        return mediaLoader.registerMesh("sphere", model);
    }

    private Material createMaterial(ColorRGB color) {
        ColorAttribute colorAttr = ColorAttribute.createDiffuse(mediaLoader.toColor(color));
        return new Material(colorAttr);
    }

    private Material createMaterial(GDXImage texture) {
        TextureAttribute colorAttr = TextureAttribute.createDiffuse(texture.getTextureRegion());
        return new Material(colorAttr);
    }

    private Color createColor(ColorRGB rgb) {
        return new Color(rgb.getR() / 255f, rgb.getG() / 255f, rgb.getB() / 255f, 1f);
    }
}
