//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene.effect;

import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.graphics.Primitive;
import nl.colorize.multimedialib.graphics.Sprite;
import nl.colorize.multimedialib.graphics.Transform;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.FilePointer;
import nl.colorize.multimedialib.scene.SceneContext;
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
public class TransitionEffect extends Effect {

    private boolean reverse;
    private Canvas canvas;
    private Image particleImage;
    private float duration;
    private ColorRGB fillColor;

    private List<List<Particle>> particles;

    private static final FilePointer PARTICLE_IMAGE = new FilePointer("transition-effect.png");
    private static final Logger LOGGER = LogHelper.getLogger(TransitionEffect.class);

    private TransitionEffect(boolean reverse, Canvas canvas, Image particleImage, float duration) {
        super(new Timeline().addKeyFrame(0f, 0f).addKeyFrame(duration, 1f));

        this.reverse = reverse;
        this.canvas = canvas;
        this.particleImage = particleImage;
        this.duration = duration;

        particles = generateParticles();
        modifyFrameUpdate(this::updateParticles);
    }

    private List<List<Particle>> generateParticles() {
        List<List<Particle>> particles = new ArrayList<>();

        //TODO
        if (Platform.isTeaVM()) {
            LOGGER.warning("Particle effects are disabled on TeaVM due to performance issues");
            return Collections.emptyList();
        }

        for (int x = 0; x <= canvas.getWidth(); x += particleImage.getWidth()) {
            List<Particle> column = new ArrayList<>();
            particles.add(column);

            for (int y = 0; y <= canvas.getHeight(); y += particleImage.getHeight()) {
                float pDelay = (column.size() + 1) * 0.04f;
                Particle particle = new Particle(x, y, pDelay, duration * 0.5f);
                particle.sprite = new Sprite(particleImage);
                column.add(particle);
                displayObject.withGraphics(particle.sprite);
            }
        }

        return particles;
    }

    private void updateParticles(float deltaTime) {
        for (List<Particle> column : particles) {
            for (Particle particle : column) {
                particle.timeline.movePlayhead(deltaTime);
                particle.sprite.setPosition(particle.position);
                particle.sprite.setTransform(getParticleTransform(particle));
            }
        }

        if (fillColor != null && timeline.getDelta() >= 0.8f) {
            displayObject.extendGraphics(Primitive.of(canvas.getBounds(), fillColor));
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

    public static TransitionEffect obscure(SceneContext context, ColorRGB color, float duration) {
        Image particleImage = context.getMediaLoader().loadImage(PARTICLE_IMAGE).tint(color);
        Canvas canvas = context.getCanvas();
        TransitionEffect effect = new TransitionEffect(false, canvas, particleImage, duration);
        effect.fillColor = color;
        return effect;
    }

    public static TransitionEffect obscure(Canvas canvas, Image particleImage, float duration) {
        return new TransitionEffect(false, canvas, particleImage, duration);
    }

    public static TransitionEffect reveal(SceneContext context, ColorRGB color, float duration) {
        Image particleImage = context.getMediaLoader().loadImage(PARTICLE_IMAGE).tint(color);
        Canvas canvas = context.getCanvas();
        return new TransitionEffect(true, canvas, particleImage, duration);
    }

    public static TransitionEffect reveal(Canvas canvas, Image particleImage, float duration) {
        return new TransitionEffect(true, canvas, particleImage, duration);
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
