//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.android;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

/**
 * Activity that uses a renderer as its main view.
 */
public abstract class MultimediaActivity extends Activity {

	private AndroidRenderer renderer;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		super.onCreate(savedInstanceState);
	
		renderer = initializeRenderer();
		renderer.startRenderer();
		setContentView(renderer.getView());
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if (renderer.getView() instanceof GLSurfaceView) {
			((GLSurfaceView) renderer.getView()).onPause();
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (renderer.getView() instanceof GLSurfaceView) {
			((GLSurfaceView) renderer.getView()).onResume();
		}
	}
	
	@Override
	protected void onDestroy() {
		renderer.stop();
		renderer = null;
		super.onDestroy();
	}
	
	protected abstract AndroidRenderer initializeRenderer();
	
	public AndroidRenderer getRenderer() {
		return renderer;
	}
}
