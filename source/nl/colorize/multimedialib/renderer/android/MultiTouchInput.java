//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.android;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.view.MotionEvent;

import nl.colorize.multimedialib.math.Point;
import nl.colorize.multimedialib.renderer.InputDevice;

/**
 * Captures multi-touch events to use an Android phone or tablet's touch screen
 * as a pointer device. 
 */
public class MultiTouchInput implements InputDevice {
	
	private List<MotionEvent> touchEventBuffer;
	private Point touchLocation;
	private int touchState;
	
	private static final int TOUCH_STATE_NONE = 0;
	private static final int TOUCH_STATE_PRESSED = 1;
	private static final int TOUCH_STATE_RELEASED = 2;
	
	public MultiTouchInput() {
		touchEventBuffer = Collections.synchronizedList(new ArrayList<MotionEvent>());
		touchLocation = new Point(0, 0);
		touchState = 0;
	}
	
	protected void registerTouchEvent(MotionEvent e) {
		touchEventBuffer.add(e);
	}
	
	protected void processTouchEventBuffer() {
		if (touchEventBuffer.size() > 0) {
			MotionEvent lastEvent = touchEventBuffer.get(touchEventBuffer.size() - 1);
			touchEventBuffer.clear();
			
			touchLocation.set(Math.round(lastEvent.getX()), Math.round(lastEvent.getY()));
			touchState = TOUCH_STATE_PRESSED;
		} else {
			if (touchState == TOUCH_STATE_PRESSED) {
				touchState = TOUCH_STATE_RELEASED;
			} else if (touchState == TOUCH_STATE_RELEASED) {
				touchState = TOUCH_STATE_NONE;
			}
		}
	}

	public Point getPointer() {
		return touchLocation;
	}

	public boolean isPointerPressed() {
		return touchState == TOUCH_STATE_PRESSED;
	}

	public boolean isPointerReleased() {
		return touchState == TOUCH_STATE_RELEASED;
	}
}
