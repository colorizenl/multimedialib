//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene.effect;

import com.google.common.base.Preconditions;
import lombok.Getter;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.scene.Actor;
import nl.colorize.multimedialib.scene.GraphicsProvider;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.Container;
import nl.colorize.multimedialib.stage.Image;
import nl.colorize.multimedialib.stage.Spatial2D;
import nl.colorize.multimedialib.stage.Sprite;
import nl.colorize.util.ResourceFile;
import nl.colorize.util.animation.Timeline;

import java.util.ArrayList;
import java.util.List;

/**
 * Uses a particle wipe effect that can be used for screen transitions. There
 * are two modes: one where the particles slowly obscure the screen, and one
 * where the particles slowly reveal the screen. Both effects would typically
 * be used on either side of the transition.
 */
public class ParticleWipe implements Actor, GraphicsProvider {

    private Canvas canvas;
    private Image particleImage;
    @Getter private ColorRGB particleColor;
    @Getter private double duration;
    @Getter private boolean reverse;

    private Container container;
    private List<Particle> particles;

    public static final ResourceFile DIAMOND = new ResourceFile("effects/particle-diamond.png");
    public static final ResourceFile CIRCLE = new ResourceFile("effects/particle-circle.png");

    private static final int PARTICLE_SIZE = 64;
    private static final int PADDING = PARTICLE_SIZE / 2;
    private static final double DEFAULT_DURATION = 1.2;

    public ParticleWipe(Canvas canvas, Image particleImage, ColorRGB particleColor) {
        this.canvas = canvas;
        this.particleImage = particleImage;
        this.particleColor = particleColor;
        this.duration = DEFAULT_DURATION;
        this.reverse = false;

        container = new Container();
        particles = new ArrayList<>();
    }

    @Override
    public void update(double deltaTime) {
        if (particles.isEmpty()) {
            spawnParticles();
        }

        for (Particle particle : particles) {
            particle.timeline.movePlayhead(deltaTime);
            particle.sprite.getTransform().setMaskColor(particleColor);
            particle.sprite.getTransform().setPosition(particle.position);
            particle.sprite.getTransform().setScale(getParticleScale(particle));
            particle.sprite.getTransform().setVisible(particle.sprite.getTransform().getScaleX() > 1);
        }

        if (isCompleted()) {
            container.detach();
        }
    }

    private void spawnParticles() {
        int columnIndex = 0;
        int endX = canvas.getWidth() + PADDING;
        int endY = canvas.getHeight() + PADDING;

        for (int x = -PADDING; x <= endX; x += PARTICLE_SIZE) {
            columnIndex++;

            for (int y = -PADDING; y <= endY; y += PARTICLE_SIZE) {
                Particle particle = new Particle(x, y, columnIndex * 0.04f, duration * 0.5f);
                particle.sprite = new Sprite(particleImage);
                particles.add(particle);
                container.addChild(particle.sprite);
            }
        }
    }

    private double getParticleScale(Particle particle) {
        double delta = particle.timeline.getValue();
        if (reverse) {
            delta = 1f - delta;
        }
        return Math.max(delta * 200f, 1f);
    }

    @Override
    public boolean isCompleted() {
        if (particles.isEmpty()) {
            spawnParticles();
        }

        return particles.stream()
            .allMatch(particle -> particle.timeline.isCompleted());
    }

    @Override
    public Spatial2D getGraphics() {
        return container;
    }

    /**
     * Returns a new {@link ParticleWipe} with the same configuration as this
     * one, but that plays the animation in reverse. If this instance is
     * already reversed, it will return a non-reversed instance.
     */
    public ParticleWipe reversed() {
        ParticleWipe copy = new ParticleWipe(canvas, particleImage, particleColor);
        copy.reverse = !reverse;
        return copy;
    }

    /**
     * Returns a new {@link ParticleWipe} with the same configuration as this
     * one, but with the specified custom animation duration.
     */
    public ParticleWipe withDuration(double duration) {
        Preconditions.checkArgument(duration > 0.0, "Invalid duration: " + duration);

        ParticleWipe copy = new ParticleWipe(canvas, particleImage, particleColor);
        copy.duration = duration;
        return copy;
    }

    /**
     * Factory method that returns a {@link ParticleWipe} based on the default
     * diamond-shaped particles in the specified color.
     */
    public static ParticleWipe diamonds(MediaLoader mediaLoader, Canvas canvas, ColorRGB color) {
        Image diamondImage = mediaLoader.loadImage(DIAMOND);
        return new ParticleWipe(canvas, diamondImage, color);
    }

    /**
     * Factory method that returns a {@link ParticleWipe} based on the default
     * circle-shaped particles in the specified color.
     */
    public static ParticleWipe circles(MediaLoader mediaLoader, Canvas canvas, ColorRGB color) {
        Image circleImage = mediaLoader.loadImage(CIRCLE);
        return new ParticleWipe(canvas, circleImage, color);
    }

    /**
     * Internal data structure used to keep track of each particle.
     */
    private static class Particle {

        private Point2D position;
        private Timeline timeline;
        private Sprite sprite;

        public Particle(double x, double y, double delay, double duration) {
            this.position = new Point2D(x, y);
            this.timeline = new Timeline()
                .addKeyFrame(0f, 0f)
                .addKeyFrame(delay, 0f)
                .addKeyFrame(delay + duration, 1f);
        }
    }
}
