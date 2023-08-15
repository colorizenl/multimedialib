//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.KeyCode;
import nl.colorize.multimedialib.renderer.Pointer;
import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.events.KeyboardEvent;
import org.teavm.jso.dom.events.MouseEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Captures browser events for various input methods, and makes them accessible
 * from the animation loop. During each frame, incoming events are added to a
 * buffer. When the frame update takes place, the buffer is processed to determine
 * the current state for each input device.
 */
public class TeaInputDevice implements InputDevice {

    private Canvas canvas;
    private TeaGraphics graphics;

    private Map<String, Pointer> pointers;
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
            .put(KeyCode.SHIFT, 16)
            .put(KeyCode.BACKSPACE, 8)
            .put(KeyCode.TAB, 9)
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
            .put(KeyCode.F1, 112)
            .put(KeyCode.F2, 113)
            .put(KeyCode.F3, 114)
            .put(KeyCode.F4, 115)
            .put(KeyCode.F5, 116)
            .put(KeyCode.F6, 117)
            .put(KeyCode.F7, 118)
            .put(KeyCode.F8, 119)
            .put(KeyCode.F9, 120)
            .put(KeyCode.F10, 121)
            .put(KeyCode.F11, 122)
            .put(KeyCode.F12, 123)
            .build();

    public TeaInputDevice(Canvas canvas, TeaGraphics graphics) {
        this.canvas = canvas;
        this.graphics = graphics;

        this.pointers = new HashMap<>();
        this.keysDown = new HashSet<>();
        this.keysUp = new HashSet<>();
    }

    public void bindEventHandlers() {
        Window window = Window.current();
        window.addEventListener("mousedown", this::onMouseEvent);
        window.addEventListener("mouseup", this::onMouseEvent);
        window.addEventListener("mousemove", this::onMouseEvent);
        window.addEventListener("mouseout",this::onMouseEvent);
        window.addEventListener("keydown", this::onKeyDown);
        window.addEventListener("keyup", this::onKeyUp);
        window.addEventListener("custom:touchstart", this::onCustomTouchEvent, true);
        window.addEventListener("custom:touchmove", this::onCustomTouchEvent, true);
        window.addEventListener("custom:touchend", this::onCustomTouchEvent, true);
        window.addEventListener("custom:touchcancel", this::onCustomTouchEvent, true);
    }

    private void onMouseEvent(Event event) {
        if (event.getType().equals("mouseout")) {
            pointers.remove("mouse");
            return;
        }

        MouseEvent mouseEvent = (MouseEvent) event;
        Pointer mousePointer = pointers.get("mouse");

        if (mousePointer == null) {
            mousePointer = new Pointer("mouse");
            pointers.put("mouse", mousePointer);
        }

        Point2D position = getPointerCanvasPosition(mouseEvent.getPageX(), mouseEvent.getPageY());
        mousePointer.setPosition(position);

        switch (event.getType()) {
            case "mousedown" -> mousePointer.setPressed(true);
            case "mouseup" -> mousePointer.setReleased(true);
            default -> {}
        }

        event.preventDefault();
        event.stopPropagation();
    }

    private void onKeyDown(Event event) {
        KeyboardEvent keyboardEvent = (KeyboardEvent) event;
        keysDown.add(keyboardEvent.getKeyCode());

        event.preventDefault();
        event.stopPropagation();
    }

    private void onKeyUp(Event event) {
        KeyboardEvent keyboardEvent = (KeyboardEvent) event;
        keysUp.add(keyboardEvent.getKeyCode());

        event.preventDefault();
        event.stopPropagation();
    }

    private void onCustomTouchEvent(Event event) {
        CustomTouchEvent customTouchEvent = (CustomTouchEvent) event;
        String identifier = String.valueOf(customTouchEvent.getDetail().getIdentifier());
        Pointer touchPointer = pointers.get(identifier);

        if (event.getType().equals("custom:touchcancel")) {
            pointers.remove(identifier);
            return;
        }

        if (touchPointer == null) {
            touchPointer = new Pointer(identifier);
            pointers.put(identifier, touchPointer);
        }

        Point2D position = getPointerCanvasPosition(customTouchEvent.getDetail().getPageX(),
            customTouchEvent.getDetail().getPageY());
        touchPointer.setPosition(position);

        switch (event.getType()) {
            case "custom:touchstart" -> touchPointer.setPressed(true);
            case "custom:touchend" -> touchPointer.setReleased(true);
            default -> {}
        }

        customTouchEvent.preventDefault();
        customTouchEvent.stopPropagation();
    }

    public void reset() {
        pointers.keySet().removeIf(id -> pointers.get(id).isReleased());
        keysDown.removeAll(keysUp);
        keysUp.clear();
    }

    @Override
    public Optional<Point2D> getPointer() {
        List<Pointer> pointers = getPointers();
        if (pointers.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(pointers.get(0).getPosition());
    }

    @Override
    public List<Pointer> getPointers() {
        //TODO cannot use List.copyOf() because it is not yet
        //     supported by TeaVM.
        return ImmutableList.copyOf(pointers.values());
    }

    private Point2D getPointerCanvasPosition(int pageX, int pageY) {
        int screenX = Math.round(pageX * graphics.getDevicePixelRatio());
        int screenY = Math.round(pageY * graphics.getDevicePixelRatio());

        float canvasX = canvas.toCanvasX(screenX);
        float canvasY = canvas.toCanvasY(screenY);

        return new Point2D(canvasX, canvasY);
    }

    @Override
    public boolean isPointerPressed(Rect area) {
        List<Pointer> pointers = getPointers();
        return !pointers.isEmpty() && pointers.get(0).isPressed(area);
    }

    @Override
    public boolean isPointerPressed() {
        List<Pointer> pointers = getPointers();
        return !pointers.isEmpty() && pointers.get(0).isPressed();
    }

    @Override
    public boolean isPointerReleased(Rect area) {
        List<Pointer> pointers = getPointers();
        return !pointers.isEmpty() && pointers.get(0).isReleased(area);
    }

    @Override
    public boolean isPointerReleased() {
        List<Pointer> pointers = getPointers();
        return !pointers.isEmpty() && pointers.get(0).isReleased();
    }

    @Override
    public void clearPointerReleased() {
        pointers.clear();
    }

    @Override
    public boolean isTouchAvailable() {
        return Browser.isTouchSupported();
    }

    @Override
    public boolean isKeyboardAvailable() {
        return !isTouchAvailable();
    }

    @Override
    public boolean isKeyPressed(KeyCode keyCode) {
        int browserKeyCode = BROWSER_KEY_CODE_MAPPING.get(keyCode);
        return keysDown.contains(browserKeyCode);
    }

    @Override
    public boolean isKeyReleased(KeyCode keyCode) {
        int browserKeyCode = BROWSER_KEY_CODE_MAPPING.get(keyCode);
        return keysUp.contains(browserKeyCode);
    }

    @Override
    public String requestTextInput(String label, String initialValue) {
        return Window.prompt(label, initialValue);
    }

    @Override
    public void update(float deltaTime) {
    }
}
