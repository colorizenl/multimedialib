//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2019 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.libgdx;

import com.badlogic.gdx.Gdx;
import nl.colorize.multimedialib.math.Point;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.KeyCode;

/**
 * Access to libGDX's user input state.
 */
public class GDXInput implements InputDevice {

    private Point pointer;
    private boolean pointerPressed;
    private boolean pointerReleased;

    protected GDXInput() {
        pointer = new Point(0f, 0f);
        pointerPressed = false;
        pointerReleased = false;
    }

    public void update() {
        updatePointerStatus();
    }

    private void updatePointerStatus() {
        boolean currentState = Gdx.input.isTouched();

        if (currentState) {
            pointerPressed = true;
            pointerReleased = false;
        } else if (pointerPressed) {
            pointerPressed = false;
            pointerReleased = true;
        } else {
            pointerPressed = false;
            pointerReleased = false;
        }
    }

    @Override
    public Point getPointer() {
        pointer.set(Gdx.input.getX(), Gdx.input.getY());
        return pointer;
    }

    @Override
    public boolean isPointerPressed() {
        return pointerPressed;
    }

    @Override
    public boolean isPointerReleased() {
        return pointerReleased;
    }

    @Override
    public boolean isKeyboardAvailable() {
        return false;
    }

    @Override
    public boolean isKeyPressed(KeyCode keyCode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isKeyReleased(KeyCode keyCode) {
        throw new UnsupportedOperationException();
    }
}
