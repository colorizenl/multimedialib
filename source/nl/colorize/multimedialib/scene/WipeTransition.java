//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.FilePointer;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.Image;
import nl.colorize.multimedialib.stage.Layer2D;
import nl.colorize.multimedialib.stage.Primitive;
import nl.colorize.multimedialib.stage.Sprite;
import nl.colorize.multimedialib.stage.Transform;
import nl.colorize.util.animation.Timeline;

import java.util.ArrayList;
import java.util.List;

/**
 * Uses a particle wipe effect that can be used for screen transitions. There
 * are two modes: one where the particles slowly obscure the screen, and one
 * where the particles slowly reveal the screen. Both effects would typically
 * be used on either side of the transition.
 */
public class WipeTransition extends Effect {

    private Timeline timeline;
    private boolean reverse;
    private Image particleImage;
    private float duration;
    private ColorRGB fillColor;
    private List<List<Particle>> particles;

    public static final FilePointer DIAMOND = new FilePointer("effects/particle-diamond.png");
    public static final FilePointer CIRCLE = new FilePointer("effects/particle-circle.png");

    private static final String LAYER = "$$WipeTransition";
    private static final int PARTICLE_SIZE = 64;

    /**
     * Creates a new wipe transition based on the specified particle image. If
     * {@code reverse} is true, the transition will start fully obscured and
     * will then play backwards, slowly revealing the stage.
     */
    public WipeTransition(FilePointer imageFile, ColorRGB color, float duration, boolean reverse) {
        this.timeline = new Timeline()
            .addKeyFrame(0f, 0f)
            .addKeyFrame(duration, 1f);

        this.duration = duration;
        this.fillColor = color;
        this.reverse = reverse;
        this.particles = new ArrayList<>();

        addStartHandler(() -> initialize(imageFile));
        addFrameHandler(timeline::movePlayhead);
        addFrameHandler(this::updateParticles);
        addFrameHandler(this::updateFill);
        stopIf(timeline::isCompleted);
    }

    /**
     * Creates a new wipe transition based on diamond-shaped particles. If
     * {@code reverse} is true, the transition will start fully obscured and
     * will then play backwards, slowly revealing the stage.
     */
    public WipeTransition(ColorRGB color, float duration, boolean reverse) {
        this(DIAMOND, color, duration, reverse);
    }

    private void initialize(FilePointer imageFile) {
        SceneContext context = getContext();
        particleImage = context.getMediaLoader().loadImage(imageFile);

        generateParticles(context.getCanvas());

        particles.stream()
            .flatMap(column -> column.stream())
            .forEach(particle -> addParticleSprite(context, particle.sprite));
    }

    private void generateParticles(Canvas canvas) {
        particles.clear();

        for (int x = 0; x <= canvas.getWidth(); x += PARTICLE_SIZE) {
            List<Particle> column = new ArrayList<>();
            particles.add(column);

            for (int y = 0; y <= canvas.getHeight(); y += PARTICLE_SIZE) {
                float pDelay = (column.size() + 1) * 0.04f;
                Particle particle = new Particle(x, y, pDelay, duration * 0.5f);
                particle.sprite = new Sprite(particleImage);
                column.add(particle);
            }
        }
    }

    private void addParticleSprite(SceneContext context, Sprite sprite) {
        Layer2D layer = context.getStage().retrieveLayer(LAYER);
        layer.add(sprite);
        removeAfterwards(sprite);
    }

    private void updateParticles(float deltaTime) {
        for (List<Particle> column : particles) {
            for (Particle particle : column) {
                particle.timeline.movePlayhead(deltaTime);
                particle.sprite.setPosition(particle.position);
                particle.sprite.setTransform(getParticleTransform(particle));
            }
        }
    }

    private void updateFill(float deltaTime) {
        if (!reverse && fillColor != null && timeline.getDelta() >= 0.8f) {
            Primitive fill = Primitive.of(getContext().getCanvas().getBounds(), fillColor);
            getContext().getStage().retrieveLayer(LAYER).add(fill);
            removeAfterwards(fill);
        }
    }

    private Transform getParticleTransform(Particle particle) {
        float delta = particle.timeline.getValue();
        if (reverse) {
            delta = 1f - delta;
        }

        Transform transform = new Transform();
        transform.setScale(Math.max(delta * 200f, 1f));
        transform.setMask(fillColor);
        return transform;
    }

    /**
     * Internal data structure used to keep track of each particle.
     */
    private static class Particle {

        private Point2D position;
        private Timeline timeline;
        private Sprite sprite;

        public Particle(float x, float y, float delay, float duration) {
            this.position = new Point2D(x, y);
            this.timeline = new Timeline()
                .addKeyFrame(0f, 0f)
                .addKeyFrame(delay, 0f)
                .addKeyFrame(delay + duration, 1f);
        }
    }
}
