//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.mock;

import nl.colorize.multimedialib.math.Point;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.KeyCode;

import java.util.HashMap;
import java.util.Map;

/**
 * Mock implementation of the {@code InputDevice} interface.
 */
public class MockInputDevice implements InputDevice {

    private Point pointer;
    private boolean pointerPressed;
    private Map<KeyCode, Boolean> keysPressed;

    public MockInputDevice() {
        pointer = new Point(0, 0);
        pointerPressed = false;
        keysPressed = new HashMap<>();
    }

    public void setPointer(float x, float y) {
        pointer.set(x, y);
    }

    @Override
    public Point getPointer() {
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

    @Override
    public String requestTextInput(String label, String initialValue) {
        return null;
    }
}
