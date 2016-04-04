//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import java.util.logging.Logger;

import nl.colorize.multimedialib.math.MathUtils;
import nl.colorize.util.LogHelper;
import nl.colorize.util.Stopwatch;

/**
 * Utility methods for running an animation loop at a fixed or variable framerate.
 * Depending on the chosen strategy the animation loop might run in a separate
 * thread. Callback methods are then invoked from that thread to notify the
 * renderer of frame updates.
 * <p> 
 * These methods can be used by renderers, and are supposed to only be used
 * internally within MultimediaLib.
 */
public final class TimingUtils {
	
	private static final long MIN_FRAME_SLEEP = 2;
	private static final long MAX_FRAME_SLEEP = 100;
	private static final long MAX_OVERSLEEP = 10;
	private static final Logger LOGGER = LogHelper.getLogger(TimingUtils.class);

	private TimingUtils() {
	}
	
	/**
	 * Runs the animation loop, sending notifications to callbacks and keeping
	 * statistics along the way. The animation loop will remain active until
	 * {@code animationLoop} has completed.
	 * @param animationLoop Performs the actual animation loop. This task is not
	 *        required to send "initialized" or "stopped" events to callbacks, or
	 *        to catch exceptions. All of these are already handled by this method.
	 *        However, the task <i>is</i> expected to send "frame" events to
	 *        callbacks, and to update the provided statistics object.
	 * @param useRenderingThread If true, runs the animation loop in a separate 
	 *        rendering thread. If false, runs it from the calling thread.
	 */
	private static void runAnimationLoop(final AnimationLoopTask animationLoop, 
			final RenderCallback callback, boolean useRenderingThread) {
		startDaemonThread();
		
		Runnable wrappedAnimationLoop = new Runnable() {
			public void run() {
				Exception thrown = null;
				try {
					callback.onInitialized();
					animationLoop.run(callback);
				} catch (Exception e) {
					thrown = e;
				} finally {
					callback.onStopped();
				}
				
				if (thrown != null) {
					throw new RendererException("Exception during animation loop", thrown);
				}
			}
		};
		
		if (useRenderingThread) {
			Thread renderingThread = new Thread(wrappedAnimationLoop, "RenderingThread");
			renderingThread.start();
		} else {
			wrappedAnimationLoop.run();
		}
	}
	
	/**
	 * Ensures the system timer operates at the highest precision possible, by
	 * creating a daemon thread that will sleep forever. On some platforms this
	 * will force the platform to switch to a high precision timer.
	 */
	private static void startDaemonThread() {
		Runnable sleepTask = new Runnable() {
			public void run() {
				try {
					Thread.sleep(Long.MAX_VALUE);
				} catch (InterruptedException e) {
					// Not a problem, continue without the daemon thread.
				}
			}
		};
		
		Thread daemonThread = new Thread(sleepTask, "RenderingThread-Daemon");
		daemonThread.setDaemon(true);
		daemonThread.start();
	}
	
	/**
	 * Runs an animation loop with a variable framerate.
	 */
	public static void runVariableFramerateAnimationLoop(RenderCallback callback, 
			final RenderStatistics renderStats) {
		AnimationLoopTask animationLoop = new AnimationLoopTask() {
			public void run(RenderCallback callback) {
				Stopwatch timer = new Stopwatch();
				while (callback.isActive()) {
					float deltaTime = timer.tick() / 1000f;
					callback.onFrame(deltaTime);
					renderStats.onFrame(deltaTime);
				}
			}
		};
		
		runAnimationLoop(animationLoop, callback, false);
	}
	
	/**
	 * Runs an animation loop as close as possible to the target framerate. If
	 * frames take too long the animation loop will run slower than intended, but
	 * no frames will be skipped.
	 */
	public static void runFixedTimestepAnimationLoop(final int framerate, RenderCallback callback,
			final RenderStatistics renderStats) {
		AnimationLoopTask animationLoop = new AnimationLoopTask() {
			public void run(RenderCallback callback) {
				Stopwatch timer = new Stopwatch();
				long oversleep = 0L;
				
				while (callback.isActive()) {
					oversleep = syncFrame(framerate, timer, oversleep, callback, renderStats);;
				}
			}
		};
		
		runAnimationLoop(animationLoop, callback, false);
	}
	
	/**
	 * Performs one frame of the animation loop, then makes the calling thread
	 * wait until the targeted frame time has elapsed.
	 * @param framerate The targeted framerate, from which the framerate will be
	 *                  derived.
	 * @param timer Timer with mark set at the last frame update. A new mark will
	 *              be set at the end of this frame.
	 * @param oversleep The last known oversleep time, in milliseconds. Used to
	 *                  compensate for timer inaccuracy. A value of 0 will disable
	 *                  this compensation behavior.
	 * @return The updated oversleep time, in milliseconds.
	 */
	public static long syncFrame(int framerate, Stopwatch timer, long oversleep,
			RenderCallback callback, RenderStatistics renderStats) {
		long targetFrameTime = 1000L / framerate;
		float deltaTime = timer.tick() / 1000f;
		
		callback.onFrame(deltaTime);
		renderStats.onFrame(deltaTime);
		
		long frameTime = timer.tock();
		long sleepTime = Math.min(targetFrameTime - frameTime - oversleep, MAX_FRAME_SLEEP);
		
		try {
			if (sleepTime >= MIN_FRAME_SLEEP) {
				Thread.sleep(sleepTime);
				
				oversleep = timer.tock() - frameTime - sleepTime;
				oversleep = MathUtils.clamp(oversleep, 0, MAX_OVERSLEEP);
			}
		} catch (InterruptedException e) {
			LOGGER.warning("Frame sync interrupted");
		}
		
		return oversleep;
	}
	
	/**
	 * Task interface that performs an animation loop.
	 */
	private static interface AnimationLoopTask {
		
		public void run(RenderCallback callback);
	}
}
