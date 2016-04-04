//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.app;

import nl.colorize.multimedialib.graphics.DisplayList;
import nl.colorize.multimedialib.renderer.AudioQueue;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.RenderCallback;
import nl.colorize.multimedialib.renderer.RenderStatistics;
import nl.colorize.multimedialib.renderer.Renderer;

/**
 * Entry point for applications using MultimediaLib. Once started, the animation
 * loop will use a renderer to perform frame updates as close to the targeted
 * framerate as possible, to play a number of scenes.
 * <p>
 * The animation loop runs in a separate thread. MultimediaLib applications are
 * not thread safe, interacting with the contents of the animation loop from any
 * other thread may result in a {@code RendererException}. 
 */
public final class AnimationLoop implements RenderCallback {
	
	private Renderer renderer;
	private ResourceLoader resourceLoader;
	private SceneAnimator sceneAnimator;
	
	private Scene activeScene;
	
	/**
	 * Creates an animation loop that will use the specified renderer. The animation
	 * loop will not actually run until {@link #start()} is called.
	 * @throws IllegalStateException if the renderer is already active.
	 */
	public AnimationLoop(Renderer renderer, Scene initialScene) {
		if (renderer.isActive()) {
			throw new IllegalStateException("Renderer is already active");
		}
		
		this.renderer = renderer;
		this.resourceLoader = new ResourceLoader(renderer);
		this.sceneAnimator = new SceneAnimator();
		this.activeScene = initialScene;
	}
	
	/**
	 * Starts the animation loop in a separate thread.
	 * @throws IllegalStateException if the animation loop is already active.
	 */
	public void start() {
		if (renderer.isActive()) {
			throw new IllegalStateException("Animation loop is already active");
		}
		renderer.registerCallback(this);
		renderer.initialize();
	}
	
	/**
	 * Stops the animation loop. If it was not running anyway this method does
	 * nothing.
	 */
	public void stop() {
		sceneAnimator.stop();
		renderer.unregisterCallback(this);
		if (renderer.isActive()) {
			renderer.stop();
		}
	}
	
	public void onInitialized() {
		sceneAnimator.start();
		sceneAnimator.setActiveScene(activeScene);
		
		activeScene.onSceneStart(this);
	}

	public void onFrame(float deltaTime) {
		activeScene.onFrame(this, deltaTime);
		sceneAnimator.onFrame(deltaTime);
		
		DisplayList displayList = activeScene.onRender(this);
		displayList.draw(renderer);
	}

	public void onStopped() {
		activeScene.onSceneEnd(this);
	}
	
	public boolean isActive() {
		return true;
	}
	
	public void changeScene(Scene newScene) {
		if (newScene == activeScene) {
			return;
		}
		
		Scene oldScene = activeScene;
		activeScene = newScene;
		
		if (oldScene != null) {
			oldScene.onSceneEnd(this);
		}
		newScene.onSceneStart(this);
		
		sceneAnimator.setActiveScene(newScene);
	}
	
	public Scene getActiveScene() {
		return activeScene;
	}
	
	public Renderer getRenderer() {
		return renderer;
	}
	
	public InputDevice getInputDevice() {
		return renderer.getInputDevice();
	}
	
	public AudioQueue getAudioQueue() {
		return renderer.getAudioQueue();
	}
	
	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}
	
	public SceneAnimator getSceneAnimator() {
		return sceneAnimator;
	}
	
	public RenderStatistics getRenderStats() {
		return renderer.getStats();
	}
}
