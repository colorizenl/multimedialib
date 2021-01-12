//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.mock;

import com.google.common.collect.ImmutableList;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.KeyCode;

import java.util.HashMap;
import java.util.List;
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

    public void setPointer(float x, float y) {
        pointer.set(x, y);
    }

    @Override
    public List<Point2D> getPointers() {
        return ImmutableList.of(pointer);
    }

    public void setPointerPressed(boolean pointerPressed) {
        this.pointerPressed = pointerPressed;
    }

    @Override
    public boolean isPointerPressed(Rect area) {
        return pointerPressed && area.contains(pointer);
    }

    @Override
    public boolean isPointerReleased(Rect area) {
        return false;
    }

    @Override
    public boolean isTouchAvailable() {
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
