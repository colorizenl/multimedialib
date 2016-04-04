//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import nl.colorize.multimedialib.math.TimeSeries;
import nl.colorize.util.Platform;
import nl.colorize.util.animation.Animatable;

/**
 * Collects statistics while the renderer is running. Reported statistics are
 * averaged over a time period to limit the effect of peaks in the data.
 */
public class RenderStatistics implements Animatable {
	
	private long timeRunning;
	private TimeSeries frameTimes;
	private long lastUsedMemory;
	private float timeSinceGC;

	private static final int FRAME_TIME_SMOOTHING_PERIOD = 1000;
	private static final int MAX_FRAME_TIME_DATA_POINTS = 300;
	private static final float MEMORY_GC_FACTOR = 0.75f;
	
	public RenderStatistics() {
		timeRunning = 0L;
		frameTimes = new TimeSeries();
		frameTimes.setSmoothingPeriod(FRAME_TIME_SMOOTHING_PERIOD);
		frameTimes.setMaxDataPoints(MAX_FRAME_TIME_DATA_POINTS);
	}

	public void onFrame(float deltaTime) {
		timeRunning += Math.round(deltaTime * 1000f);
		frameTimes.addDataPoint(timeRunning, deltaTime);
		
		long usedMemory = Platform.getUsedHeapMemory();
		if (usedMemory < Math.round(MEMORY_GC_FACTOR * lastUsedMemory)) {
			timeSinceGC = 0f;
		}
		timeSinceGC += deltaTime;
		lastUsedMemory = usedMemory;
	}
	
	public float getTimeRunning() {
		return timeRunning / 1000f;
	}
	
	public TimeSeries getFrameTimeSeries() {
		return frameTimes;
	}
	
	public float getFramerate() {
		return 1f / frameTimes.getAverage();
	}
	
	public long getMemoryUsage() {
		return lastUsedMemory;
	}
	
	public float getTimeSinceGC() {
		return timeSinceGC;
	}
}
