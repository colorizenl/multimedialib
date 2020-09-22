//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene.action;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.graphics.Transform;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.RandomGenerator;
import nl.colorize.multimedialib.renderer.GraphicsContext2D;
import nl.colorize.util.animation.Timeline;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Action that is active for a certain period of time, and will spawn particle
 * effects during that period. The behavior of the particles can be influenced
 * using a number of parameters, similar to creating effects. 
 */
public class ParticleEmitter extends Effect {

    private float duration;
    private List<Effect> particles;
    
    private List<Image> particleImages;
    private int spawnRate;
    private Point2D spawnOffset;
    private Point2D minVelocity;
    private Point2D maxVelocity;
    
    public ParticleEmitter(float duration, List<Image> particleImages) {
        super(new Timeline()
            .addKeyFrame(0f, 0f)
            .addKeyFrame(duration, 1f));
        
        Preconditions.checkArgument(duration > 0f, "Invalid duration: " + duration);
        Preconditions.checkArgument(particleImages.size() >= 1, "No particle images");
        
        this.duration = duration;
        this.particles = new ArrayList<>();
        
        this.particleImages = ImmutableList.copyOf(particleImages);
        this.spawnRate = 100;
        this.spawnOffset = new Point2D(0f, 0f);
        this.minVelocity = new Point2D(0f, 0f);
        this.maxVelocity = new Point2D(0f, 0f);
    }

    public Point2D getOrigin() {
        return getPosition();
    }

    public void setSpawnRate(int spawnRate) {
        this.spawnRate = spawnRate;
    }

    public int getSpawnRate() {
        return spawnRate;
    }

    public Point2D getSpawnOffset() {
        return spawnOffset;
    }

    public Point2D getMinVelocity() {
        return minVelocity;
    }

    public Point2D getMaxVelocity() {
        return maxVelocity;
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        
        int particlesToSpawn = Math.round(deltaTime * spawnRate);
        spawnParticles(particlesToSpawn);
        
        for (Effect particle : particles) {
            particle.update(deltaTime);
        }

        Iterator<Effect> iterator = particles.iterator();
        while (iterator.hasNext()) {
            Effect next = iterator.next();
            if (next.isCompleted()) {
                iterator.remove();
            }
        }
    }
    
    private void spawnParticles(int count) {
        for (int i = 0; i < count; i++) {
            Image image = RandomGenerator.pick(particleImages);
            float x = getOrigin().getX() + RandomGenerator.getFloat(-spawnOffset.getX() / 2f, 
                spawnOffset.getX() / 2f);
            float y = getOrigin().getY() + RandomGenerator.getFloat(-spawnOffset.getY() / 2f, 
                spawnOffset.getY() / 2f);
            int alpha = RandomGenerator.getInt(50, 75);
            
            Effect particle = Effect.forImage(image, duration);
            particle.setPosition(x, y);
            particle.getTransform().setAlpha(alpha);
            particle.modifyFrameUpdate(deltaTime -> updateParticle(particle, deltaTime));
            particles.add(particle);
        }
    }
    
    private void updateParticle(Effect particle, float deltaTime) {
        if (minVelocity.getX() < maxVelocity.getX()) {
            float deltaX = RandomGenerator.getFloat(minVelocity.getX(), maxVelocity.getX());
            float deltaY = RandomGenerator.getFloat(minVelocity.getY(), maxVelocity.getY());
            particle.getPosition().add(deltaX, deltaY);
        } else {
            float deltaX = RandomGenerator.getFloat(maxVelocity.getX(), minVelocity.getX());
            float deltaY = RandomGenerator.getFloat(maxVelocity.getY(), minVelocity.getY());
            particle.getPosition().add(deltaX, deltaY);
        }
        
        Transform transform = particle.getTransform();
        transform.setAlpha(Math.max(transform.getAlpha() - 5, 1));
    }

    @Override
    public void render(GraphicsContext2D graphics) {
        for (Effect particle : particles) {
            particle.render(graphics);
        }
    }
}
