//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2019 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import com.google.common.collect.ImmutableMap;
import nl.colorize.multimedialib.math.Point;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.KeyCode;
import nl.colorize.multimedialib.scene.Updatable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TeaInputDevice implements InputDevice, Updatable {

    private Point pointer;
    private boolean pointerDown;
    private boolean pointerUp;
    private Set<Integer> keysDown;
    private Set<Integer> keysUp;

    private static final Map<KeyCode, Integer> BROWSER_KEY_CODE_MAPPING =
        new ImmutableMap.Builder<KeyCode, Integer>()
            .put(KeyCode.LEFT, 37)
            .put(KeyCode.RIGHT, 39)
            .put(KeyCode.UP, 38)
            .put(KeyCode.DOWN, 40)
            .put(KeyCode.ENTER, 13)
            .put(KeyCode.SPACEBAR, 32)
            .put(KeyCode.ESCAPE, 27)
            .put(KeyCode.A, 65)
            .put(KeyCode.B, 66)
            .put(KeyCode.C, 67)
            .put(KeyCode.D, 68)
            .put(KeyCode.E, 69)
            .put(KeyCode.F, 70)
            .put(KeyCode.G, 71)
            .put(KeyCode.H, 72)
            .put(KeyCode.I, 73)
            .put(KeyCode.J, 74)
            .put(KeyCode.K, 75)
            .put(KeyCode.L, 76)
            .put(KeyCode.M, 77)
            .put(KeyCode.N, 78)
            .put(KeyCode.O, 79)
            .put(KeyCode.P, 80)
            .put(KeyCode.Q, 81)
            .put(KeyCode.R, 82)
            .put(KeyCode.S, 83)
            .put(KeyCode.T, 84)
            .put(KeyCode.U, 85)
            .put(KeyCode.V, 86)
            .put(KeyCode.W, 87)
            .put(KeyCode.X, 88)
            .put(KeyCode.Y, 89)
            .put(KeyCode.Z, 90)
            .put(KeyCode.N1, 49)
            .put(KeyCode.N2, 50)
            .put(KeyCode.N3, 51)
            .put(KeyCode.N4, 52)
            .put(KeyCode.N5, 53)
            .put(KeyCode.N6, 54)
            .put(KeyCode.N7, 55)
            .put(KeyCode.N8, 56)
            .put(KeyCode.N9, 57)
            .put(KeyCode.N0, 48)
            .build();

    public TeaInputDevice() {
        this.pointer = new Point(0f, 0f);
        this.pointerDown = false;
        this.pointerUp = false;
        this.keysDown = new HashSet<>();
        this.keysUp = new HashSet<>();
    }

    @Override
    public void update(float deltaTime) {
        updatePointerState();
        updateKeyboardState();
    }

    private void updatePointerState() {
        pointer.setX(Browser.getPointerX());
        pointer.setY(Browser.getPointerY());

        int pointerState = Math.round(Browser.getPointerState());

        if (pointerState == 1) {
            pointerDown = true;
            pointerUp = false;
        } else if (pointerDown) {
            pointerDown = false;
            pointerUp = true;
        } else {
            pointerDown = false;
            pointerUp = false;
        }
    }

    private void updateKeyboardState() {
        for (KeyCode keyCode : KeyCode.values()) {
            Integer browserKeyCode = BROWSER_KEY_CODE_MAPPING.get(keyCode);
            int keyState = Math.round(Browser.getKeyState(browserKeyCode));

            if (keyState == 1) {
                keysDown.add(browserKeyCode);
                keysUp.remove(browserKeyCode);
            } else if (keysDown.contains(browserKeyCode)) {
                keysDown.remove(browserKeyCode);
                keysUp.add(browserKeyCode);
            } else {
                keysDown.remove(browserKeyCode);
                keysUp.remove(browserKeyCode);
            }
        }
    }

    @Override
    public Point getPointer() {
        return pointer.copy();
    }

    @Override
    public boolean isPointerPressed() {
        return pointerDown;
    }

    @Override
    public boolean isPointerReleased() {
        return pointerUp;
    }

    @Override
    public boolean isKeyboardAvailable() {
        return true;
    }

    @Override
    public boolean isKeyPressed(KeyCode keyCode) {
        return keysDown.contains(BROWSER_KEY_CODE_MAPPING.get(keyCode));
    }

    @Override
    public boolean isKeyReleased(KeyCode keyCode) {
        return keysUp.contains(BROWSER_KEY_CODE_MAPPING.get(keyCode));
    }
}
