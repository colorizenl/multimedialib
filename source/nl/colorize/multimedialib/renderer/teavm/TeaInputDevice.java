//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import com.google.common.collect.ImmutableMap;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.KeyCode;
import nl.colorize.multimedialib.renderer.Pointer;
import nl.colorize.util.LogHelper;
import nl.colorize.util.Subject;
import org.teavm.jso.browser.Window;
import org.teavm.jso.core.JSArrayReader;
import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.events.KeyboardEvent;
import org.teavm.jso.dom.events.MouseEvent;
import org.teavm.jso.dom.events.Touch;
import org.teavm.jso.dom.events.TouchEvent;
import org.teavm.jso.dom.html.HTMLElement;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Captures browser events for various input methods, and makes them accessible
 * from the animation loop. During each frame, incoming events are added to a
 * buffer. When the frame update takes place, the buffer is processed to determine
 * the current state for each input device.
 */
public class TeaInputDevice implements InputDevice {

    private BrowserBridge bridge;
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
            .put(KeyCode.COMMA, 188)
            .put(KeyCode.PERIOD, 190)
            .put(KeyCode.PLUS, 187)
            .put(KeyCode.MINUS, 189)
            // KeyCode.EQUALS is not supported because JavaScript uses
            // the same keycode for both "+" and "=".
            .build();

    private static final String CONTAINER = "#multimediaLibContainer";
    private static final Logger LOGGER = LogHelper.getLogger(TeaInputDevice.class);

    public TeaInputDevice(Canvas canvas, TeaGraphics graphics) {
        this.bridge = Browser.getBrowserBridge();
        this.canvas = canvas;
        this.graphics = graphics;

        this.pointers = new HashMap<>();
        this.keysDown = new HashSet<>();
        this.keysUp = new HashSet<>();
    }

    public void bindEventHandlers() {
        Window window = Window.current();
        HTMLElement container = window.getDocument().querySelector(CONTAINER);

        container.addEventListener("mousedown", this::onMouseEvent);
        container.addEventListener("mouseup", this::onMouseEvent);
        container.addEventListener("mousemove", this::onMouseEvent);
        container.addEventListener("mouseout",this::onMouseEvent);
        container.addEventListener("touchstart", this::onTouchEvent, true);
        container.addEventListener("touchmove", this::onTouchEvent, true);
        container.addEventListener("touchend", this::onTouchEvent, true);
        container.addEventListener("touchcancel", this::onTouchEvent, true);
        window.addEventListener("keydown", this::onKeyDown);
        window.addEventListener("keyup", this::onKeyUp);
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
            case "mousedown" -> mousePointer.setState(Pointer.STATE_PRESSED);
            case "mouseup" -> mousePointer.setState(Pointer.STATE_RELEASED);
            default -> {}
        }

        event.preventDefault();
        event.stopPropagation();
    }

    private void onKeyDown(Event event) {
        KeyboardEvent keyboardEvent = (KeyboardEvent) event;
        keysDown.add(keyboardEvent.getKeyCode());
        event.stopPropagation();
    }

    private void onKeyUp(Event event) {
        KeyboardEvent keyboardEvent = (KeyboardEvent) event;
        keysUp.add(keyboardEvent.getKeyCode());
        event.stopPropagation();
    }

    private void onTouchEvent(Event event) {
        TouchEvent touchEvent = (TouchEvent) event;
        JSArrayReader<Touch> changedTouches = touchEvent.getChangedTouches();

        for (int i = 0; i < changedTouches.getLength(); i++) {
            onTouchEvent(event.getType(), changedTouches.get(i));
        }

        touchEvent.preventDefault();
        touchEvent.stopPropagation();
    }

    private void onTouchEvent(String eventType, Touch touch) {
        String identifier = String.valueOf(touch.getIdentifier());
        Pointer touchPointer = pointers.get(identifier);

        if (eventType.equals("touchcancel")) {
            pointers.remove(identifier);
            return;
        }

        if (touchPointer == null) {
            touchPointer = new Pointer(identifier);
            pointers.put(identifier, touchPointer);
        }

        float pageX = (float) touch.getClientX();
        float pageY = (float) touch.getClientY();
        touchPointer.setPosition(getPointerCanvasPosition(pageX, pageY));

        switch (eventType) {
            case "touchstart" -> touchPointer.setState(Pointer.STATE_PRESSED);
            case "touchend" -> touchPointer.setState(Pointer.STATE_RELEASED);
            default -> {}
        }
    }

    public void reset() {
        pointers.keySet().removeIf(id -> pointers.get(id).isReleased());
        keysDown.removeAll(keysUp);
        keysUp.clear();
    }

    @Override
    public Iterable<Pointer> getPointers() {
        return List.copyOf(pointers.values());
    }

    private Point2D getPointerCanvasPosition(float pageX, float pageY) {
        int screenX = Math.round(pageX * graphics.getDevicePixelRatio());
        int screenY = Math.round(pageY * graphics.getDevicePixelRatio());

        float canvasX = canvas.toCanvasX(screenX);
        float canvasY = canvas.toCanvasY(screenY);

        return new Point2D(canvasX, canvasY);
    }

    @Override
    public void clearPointerState() {
        pointers.clear();
    }

    @Override
    public boolean isTouchAvailable() {
        return bridge.isTouchSupported();
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
    public Subject<String> requestTextInput(String label, String initialValue) {
        Subject<String> subject = new Subject<>();

        bridge.requestTextInput(label, initialValue, (name, value) -> {
            subject.next(value);
        });

        return subject;
    }

    @Override
    public void fillClipboard(String text) {
        ErrorCallback callback = errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                LOGGER.warning("Failed to write text to clipboard: " + text);
            }
        };

        bridge.writeClipboard(text, callback);
    }

    @Override
    public void update(float deltaTime) {
    }
}
