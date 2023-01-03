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
import nl.colorize.util.LogHelper;
import nl.colorize.util.Platform;
import nl.colorize.util.animation.Timeline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * Uses a particle wipe effect that can be used for screen transitions. There
 * are two modes: one where the particles slowly obscure the screen, and one
 * where the particles slowly reveal the screen. Both effects would typically
 * be used on either side of the transition.
 */
public class WipeTransition extends Effect {

    private Timeline timeline;
    private boolean reverse;
    private Canvas canvas;
    private Image particleImage;
    private float duration;
    private ColorRGB fillColor;
    private List<List<Particle>> particles;

    public static final String LAYER = "$$WipeTransition";
    private static final FilePointer PARTICLE_IMAGE = new FilePointer("transition-effect.png");
    private static final Logger LOGGER = LogHelper.getLogger(WipeTransition.class);

    private WipeTransition(boolean reverse, Canvas canvas, Image particleImage, float duration) {
        this.timeline = new Timeline()
            .addKeyFrame(0f, 0f)
            .addKeyFrame(duration, 1f);
        stopAfter(timeline.getDuration());

        this.reverse = reverse;
        this.canvas = canvas;
        this.particleImage = particleImage;
        this.duration = duration;
        this.particles = generateParticles();
    }

    private List<List<Particle>> generateParticles() {
        //TODO
        if (Platform.isTeaVM()) {
            LOGGER.warning("Particle effects are disabled on TeaVM due to performance issues");
            return Collections.emptyList();
        }

        List<List<Particle>> particles = new ArrayList<>();

        for (int x = 0; x <= canvas.getWidth(); x += particleImage.getWidth()) {
            List<Particle> column = new ArrayList<>();
            particles.add(column);

            for (int y = 0; y <= canvas.getHeight(); y += particleImage.getHeight()) {
                float pDelay = (column.size() + 1) * 0.04f;
                Particle particle = new Particle(x, y, pDelay, duration * 0.5f);
                particle.sprite = new Sprite(particleImage);
                column.add(particle);
            }
        }

        return particles;
    }

    @Override
    public void start(SceneContext context) {
        super.start(context);

        particles.stream()
            .flatMap(column -> column.stream())
            .forEach(particle -> addParticleSprite(context, particle.sprite));
    }

    private void addParticleSprite(SceneContext context, Sprite sprite) {
        Layer2D layer = context.getStage().retrieveLayer(LAYER);
        layer.add(sprite);
        removeAfterwards(sprite);
    }

    @Override
    public void update(SceneContext context, float deltaTime) {
        super.update(context, deltaTime);

        for (List<Particle> column : particles) {
            for (Particle particle : column) {
                particle.timeline.movePlayhead(deltaTime);
                particle.sprite.setPosition(particle.position);
                particle.sprite.setTransform(getParticleTransform(particle));
            }
        }

        if (fillColor != null && timeline.getDelta() >= 0.8f) {
            Primitive fill = Primitive.of(canvas.getBounds(), fillColor);
            removeAfterwards(fill);
            context.getStage().retrieveLayer(LAYER).add(fill);
        }
    }

    private Transform getParticleTransform(Particle particle) {
        float delta = particle.timeline.getValue();
        if (reverse) {
            delta = 1f - delta;
        }

        float scale = Math.max(delta * 200f, 1f);
        return Transform.withScale(scale);
    }

    public static WipeTransition obscure(SceneContext context, ColorRGB color, float duration) {
        Image particleImage = context.getMediaLoader().loadImage(PARTICLE_IMAGE).tint(color);
        Canvas canvas = context.getCanvas();
        WipeTransition effect = new WipeTransition(false, canvas, particleImage, duration);
        effect.fillColor = color;
        return effect;
    }

    public static WipeTransition obscure(Canvas canvas, Image particleImage, float duration) {
        return new WipeTransition(false, canvas, particleImage, duration);
    }

    public static WipeTransition reveal(SceneContext context, ColorRGB color, float duration) {
        Image particleImage = context.getMediaLoader().loadImage(PARTICLE_IMAGE).tint(color);
        Canvas canvas = context.getCanvas();
        return new WipeTransition(true, canvas, particleImage, duration);
    }

    public static WipeTransition reveal(Canvas canvas, Image particleImage, float duration) {
        return new WipeTransition(true, canvas, particleImage, duration);
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
