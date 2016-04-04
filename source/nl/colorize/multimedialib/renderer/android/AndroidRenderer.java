//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.android;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Debug;
import android.view.View;

import com.google.common.io.Closeables;

import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.renderer.AbstractRenderer;
import nl.colorize.multimedialib.renderer.RendererException;
import nl.colorize.multimedialib.renderer.ScaleStrategy;
import nl.colorize.util.Platform;
import nl.colorize.util.ResourceFile;

/**
 * Base class for all Android-based renderers.
 */
public abstract class AndroidRenderer extends AbstractRenderer {
	
	protected Context context;
	protected MultiTouchInput touchInput;
	protected AndroidAudioPlayer audioPlayer;
	
	private Rect screenBounds;
	private boolean orientationLocked;
	private String traceLog;

	public AndroidRenderer(Context context, ScaleStrategy scaleStrategy, int targetFramerate) {
		super(scaleStrategy, targetFramerate);
		
		this.context = context;
		this.touchInput = new MultiTouchInput();
		this.audioPlayer = new AndroidAudioPlayer();
		
		screenBounds = new Rect(0, 0, 0, 0);
		orientationLocked = false;
		
		Platform.registerAccessProvider(new AndroidPlatformAccessProvider(context));
	}
	
	protected void startRenderer() {
		if (traceLog != null) {
			Debug.startMethodTracing(traceLog);
		}
	}
	
	protected void onDisplayChanged() {
		Activity activity = (Activity) context;
		if (scaleStrategy.getCanvasWidth(screenBounds) > scaleStrategy.getCanvasHeight(screenBounds)) {
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		} else {
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
	}

	protected void stopRenderer() {
		audioPlayer.stopAll();
		
		if (traceLog != null) {
			Debug.stopMethodTracing();
		}
	}
	
	public Rect getScreenBounds() {
		return screenBounds;
	}

	public MultiTouchInput getInputDevice() {
		return touchInput;
	}

	public AndroidAudioPlayer getAudioQueue() {
		return audioPlayer;
	}
	
	/**
	 * Returns the view to which the renderer will draw. This view needs to be
	 * attached to an {@code Activity} in order to display the renderer's results. 
	 */
	public abstract View getView();
	
	protected Bitmap loadBitmap(ResourceFile source) {
		InputStream imageStream = null;
		Bitmap bitmap = null;
		
		Options bitmapOptions = new BitmapFactory.Options();
		bitmapOptions.inScaled = false;
			
		try {
			imageStream = source.openStream();
			bitmap = BitmapFactory.decodeStream(imageStream, null, bitmapOptions);
			if (bitmap == null) {
				throw new RendererException("Cannot decode bitmap from " + source.getPath());
			}
		} catch (IOException e) {
			throw new RendererException("Cannot load bitmap from " + source.getPath(), e);
		} finally {
			Closeables.closeQuietly(imageStream);
		}
		
		return bitmap;
	}

	public void setOrientationLocked(boolean orientationLocked) {
		this.orientationLocked = orientationLocked;
		onDisplayChanged();
	}
	
	public boolean isOrientationLocked() {
		return orientationLocked;
	}
	
	/**
	 * Enables the Android debugger's method tracing. Captured data is written
	 * to a log file, and can be used for performance analysis.
	 * @return The file name of the log file that will be created.
	 * @throws IllegalStateException if the renderer has already been started.
	 */
	public String activateTraceLog(String filename) {
		if (isActive()) {
			throw new IllegalStateException("Renderer has already been started");
		}
		
		traceLog = "tracelog_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".trace";
		return traceLog;
	}
}
