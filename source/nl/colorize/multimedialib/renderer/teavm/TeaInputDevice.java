//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.KeyCode;
import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.events.KeyboardEvent;
import org.teavm.jso.dom.events.MouseEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Captures browser events for various input methods, and makes them accessible
 * from the animation loop. During each frame, incoming events are added to a
 * buffer. When the frame update takes place, the buffer is processed to determine
 * the current state for each input device.
 */
public class TeaInputDevice implements InputDevice {

    private Canvas canvas;
    private TeaGraphics graphics;

    private Multimap<String, MouseEvent> mouseEvents;
    private Multimap<Integer, KeyboardEvent> keyEvents;

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

        this.mouseEvents = ArrayListMultimap.create();
        this.keyEvents = ArrayListMultimap.create();
    }

    public void bindEventHandlers() {
        Window window = Window.current();
        window.addEventListener("mousedown", this::onMouseEvent);
        window.addEventListener("mouseup", this::onMouseEvent);
        window.addEventListener("mousemove", this::onMouseEvent);
        window.addEventListener("mouseout",this::onMouseEvent);
        window.addEventListener("keydown", this::onKeyEvent);
        window.addEventListener("keyup", this::onKeyEvent);
    }

    private void onMouseEvent(Event event) {
        MouseEvent mouseEvent = (MouseEvent) event;
        mouseEvents.put("mouse", mouseEvent);

        event.preventDefault();
        event.stopPropagation();
    }

    private void onKeyEvent(Event event) {
        KeyboardEvent keyEvent = (KeyboardEvent) event;
        keyEvents.put(keyEvent.getKeyCode(), keyEvent);

        event.preventDefault();
        event.stopPropagation();
    }

    public void reset() {
        mouseEvents.clear();
        keyEvents.clear();
    }

    @Override
    public List<Point2D> getPointers() {
        List<Point2D> positions = new ArrayList<>();

        for (String id : mouseEvents.keySet()) {
            List<MouseEvent> events = (List<MouseEvent>) mouseEvents.get(id);
            MouseEvent last = events.get(events.size() - 1);
            positions.add(getPointerCanvasPosition(last));
        }

        return positions;
    }

    private Point2D getPointerCanvasPosition(MouseEvent event) {
        int screenX = Math.round(event.getPageX() * graphics.getDevicePixelRatio());
        int screenY = Math.round(event.getPageY() * graphics.getDevicePixelRatio());

        float canvasX = canvas.toCanvasX(screenX);
        float canvasY = canvas.toCanvasY(screenY);

        return new Point2D(canvasX, canvasY);
    }

    @Override
    public boolean isPointerPressed(Rect area) {
        return mouseEvents.values().stream()
            .filter(event -> event.getType().equals("mousedown"))
            .map(this::getPointerCanvasPosition)
            .anyMatch(canvasPosition -> area.contains(canvasPosition));
    }

    @Override
    public boolean isPointerReleased(Rect area) {
        return mouseEvents.values().stream()
            .filter(event -> event.getType().equals("mouseup"))
            .map(this::getPointerCanvasPosition)
            .anyMatch(canvasPosition -> area.contains(canvasPosition));
    }

    @Override
    public boolean isPointerReleased() {
        return mouseEvents.values().stream()
            .anyMatch(event -> event.getType().equals("mouseup"));
    }

    @Override
    public void clearPointerReleased() {
        mouseEvents.clear();
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
        return keyEvents.get(BROWSER_KEY_CODE_MAPPING.get(keyCode)).stream()
            .anyMatch(event -> event.getType().equals("keydown"));
    }

    @Override
    public boolean isKeyReleased(KeyCode keyCode) {
        return keyEvents.get(BROWSER_KEY_CODE_MAPPING.get(keyCode)).stream()
            .anyMatch(event -> event.getType().equals("keyup"));
    }

    @Override
    public String requestTextInput(String label, String initialValue) {
        return Browser.prompt(label, initialValue);
    }
}
