//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene.effect;

import lombok.Getter;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.renderer.FilePointer;
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.scene.SceneContext;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.Container;
import nl.colorize.multimedialib.stage.Image;
import nl.colorize.multimedialib.stage.Sprite;
import nl.colorize.util.animation.Timeline;

import java.util.ArrayList;
import java.util.List;

/**
 * Uses a particle wipe effect that can be used for screen transitions. There
 * are two modes: one where the particles slowly obscure the screen, and one
 * where the particles slowly reveal the screen. Both effects would typically
 * be used on either side of the transition.
 */
public class ParticleWipe implements Scene {

    @Getter private Container container;

    private boolean reverse;
    private Image particleImage;
    private float duration;
    private ColorRGB fillColor;
    private List<Particle> particles;

    public static final FilePointer DIAMOND = new FilePointer("effects/particle-diamond.png");
    public static final FilePointer CIRCLE = new FilePointer("effects/particle-circle.png");

    private static final int PARTICLE_SIZE = 64;
    private static final int PADDING = PARTICLE_SIZE / 2;

    /**
     * Creates a new wipe transition based on the specified particle image. If
     * {@code reverse} is true, the transition will start fully obscured and
     * will then play backwards, slowly revealing the stage.
     */
    public ParticleWipe(Image particleImage, ColorRGB color, float duration, boolean reverse) {
        this.container = new Container();
        this.duration = duration;
        this.particleImage = particleImage;
        this.fillColor = color;
        this.reverse = reverse;
        this.particles = new ArrayList<>();
    }

    @Override
    public void start(SceneContext context) {
        int columnIndex = 0;
        int endX = context.getCanvas().getWidth() + PADDING;
        int endY = context.getCanvas().getHeight() + PADDING;

        for (int x = -PADDING; x <= endX; x += PARTICLE_SIZE) {
            columnIndex++;

            for (int y = -PADDING; y <= endY; y += PARTICLE_SIZE) {
                Particle particle = new Particle(x, y, columnIndex * 0.04f, duration * 0.5f);
                particle.sprite = new Sprite(particleImage);
                particles.add(particle);
                container.addChild(particle.sprite);
            }
        }

        context.getStage().getRoot().addChild(container);
    }

    @Override
    public void update(SceneContext context, float deltaTime) {
        for (Particle particle : particles) {
            particle.timeline.movePlayhead(deltaTime);
            particle.sprite.getTransform().setMaskColor(fillColor);
            particle.sprite.getTransform().setPosition(particle.position);
            particle.sprite.getTransform().setScale(getParticleScale(particle));
            particle.sprite.getTransform().setVisible(particle.sprite.getTransform().getScaleX() > 1);
        }
    }

    private float getParticleScale(Particle particle) {
        float delta = particle.timeline.getValue();
        if (reverse) {
            delta = 1f - delta;
        }
        return Math.max(delta * 200f, 1f);
    }

    @Override
    public boolean isCompleted() {
        return particles.stream()
            .allMatch(particle -> particle.timeline.isCompleted());
    }

    @Override
    public void end(SceneContext context) {
        container.getLocation().detach();
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
