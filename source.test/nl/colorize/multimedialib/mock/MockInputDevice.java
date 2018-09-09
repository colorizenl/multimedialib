//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2018 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.mock;

import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.KeyCode;

import java.util.HashMap;
import java.util.Map;

/**
 * Mock implementation of the {@code InputDevice} interface.
 */
public class MockInputDevice implements InputDevice {

    private Point2D pointer;
    private boolean pointerPressed;
    private Map<KeyCode, Boolean> keysPressed;

    public MockInputDevice() {
        pointer = new Point2D(0, 0);
        pointerPressed = false;
        keysPressed = new HashMap<>();
    }

    @Override
    public boolean isPointerAvailable() {
        return true;
    }

    public void setPointer(float x, float y) {
        pointer.set(x, y);
    }

    @Override
    public Point2D getPointer() {
        return pointer;
    }

    public void setPointerPressed(boolean pointerPressed) {
        this.pointerPressed = pointerPressed;
    }

    @Override
    public boolean isPointerPressed() {
        return pointerPressed;
    }

    @Override
    public boolean isPointerReleased() {
        return false;
    }

    @Override
    public boolean isKeyboardAvailable() {
        return true;
    }

    public void setKeyPressed(KeyCode keyCode, boolean pressed) {
        keysPressed.put(keyCode, pressed);
    }

    @Override
    public boolean isKeyPressed(KeyCode keyCode) {
        return keysPressed.getOrDefault(keyCode, Boolean.FALSE);
    }

    @Override
    public boolean isKeyReleased(KeyCode keyCode) {
        return keysPressed.getOrDefault(keyCode, Boolean.FALSE);
    }
}
