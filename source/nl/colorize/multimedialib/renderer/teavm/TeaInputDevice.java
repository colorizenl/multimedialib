//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.KeyCode;
import nl.colorize.multimedialib.renderer.Updatable;
import nl.colorize.util.CSVRecord;
import nl.colorize.util.PlatformFamily;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Captures browser events for various input methods, and makes them accessible
 * from the animation loop. During each frame, incoming events are added to a
 * buffer. When the frame update takes place, the buffer is processed to determine
 * the current state for each input device.
 */
public class TeaInputDevice implements InputDevice, Updatable {

    private Canvas canvas;
    private PlatformFamily platform;

    private Map<String, Pointer> pointers;
    private Set<Integer> keysDown;
    private Set<Integer> keysUp;

    private static final Map<String, Integer> POINTER_EVENTS = new ImmutableMap.Builder<String, Integer>()
        .put("mousedown", 1)
        .put("mouseup", 2)
        .put("mousemove", 0)
        .put("mouseout", 2)
        .put("touchstart", 1)
        .put("touchend", 2)
        .put("touchmove", 1)
        .put("touchcancel", 2)
        .build();

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

    public TeaInputDevice(Canvas canvas, PlatformFamily platform) {
        this.canvas = canvas;
        this.platform = platform;

        this.pointers = new HashMap<>();
        this.keysDown = new HashSet<>();
        this.keysUp = new HashSet<>();
    }

    @Override
    public void update(float deltaTime) {
        updatePointerState();
        updateKeyboardState();
    }

    private void updatePointerState() {
        List<Pointer> snapshot = ImmutableList.copyOf(pointers.values());

        for (Pointer pointer : snapshot) {
            if (pointer.state == 2) {
                pointers.remove(pointer.identifier);
            }
        }

        for (String pointerEvent : Browser.flushPointerEventBuffer()) {
            CSVRecord data = CSVRecord.parseRecord(pointerEvent, ";");

            Pointer pointer = getPointer(data.get(1));
            pointer.location.set(data.getFloat(2), data.getFloat(3));

            String eventType = data.get(0);
            int eventState = getPointerEventState(eventType);
            pointer.state = Math.max(pointer.state, eventState);
        }
    }

    private Pointer getPointer(String identifier) {
        Pointer pointer = pointers.get(identifier);
        if (pointer == null) {
            pointer = new Pointer(identifier);
            pointers.put(identifier, pointer);
        }
        return pointer;
    }

    private int getPointerEventState(String eventType) {
        Integer eventState = POINTER_EVENTS.get(eventType);
        Preconditions.checkArgument(eventState != null, "Unknown pointer event type: " + eventType);
        return eventState;
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
    public List<Point2D> getPointers() {
        return pointers.values().stream()
            .map(pointer -> pointer.getCanvasPosition(canvas))
            .collect(Collectors.toList());
    }

    @Override
    public boolean isPointerPressed(Rect area) {
        return pointers.values().stream()
            .filter(pointer -> pointer.state == 1)
            .anyMatch(pointer -> area.contains(pointer.getCanvasPosition(canvas)));
    }

    @Override
    public boolean isPointerReleased(Rect area) {
        return pointers.values().stream()
            .filter(pointer -> pointer.state == 2)
            .anyMatch(pointer -> area.contains(pointer.getCanvasPosition(canvas)));
    }

    @Override
    public boolean isTouchAvailable() {
        return platform.isMobile();
    }

    @Override
    public boolean isKeyboardAvailable() {
        return !platform.isMobile();
    }

    @Override
    public boolean isKeyPressed(KeyCode keyCode) {
        return keysDown.contains(BROWSER_KEY_CODE_MAPPING.get(keyCode));
    }

    @Override
    public boolean isKeyReleased(KeyCode keyCode) {
        return keysUp.contains(BROWSER_KEY_CODE_MAPPING.get(keyCode));
    }

    @Override
    public String requestTextInput(String label, String initialValue) {
        return Browser.prompt(label, initialValue);
    }

    /**
     * Groups information relates to the state of a pointer. There can be multiple
     * pointers actively simultaneously in the case of multi-touch events.
     */
    private static class Pointer {

        private String identifier;
        private Point2D location;
        private int state;

        public Pointer(String identifier) {
            this.identifier = identifier;
            this.location = new Point2D(0f, 0f);
            this.state = 0;
        }

        public Point2D getCanvasPosition(Canvas canvas) {
            float devicePixelRatio = Browser.getDevicePixelRatio();
            float canvasX = canvas.toCanvasX(Math.round(location.getX() * devicePixelRatio));
            float canvasY = canvas.toCanvasY(Math.round(location.getY() * devicePixelRatio));
            return new Point2D(canvasX, canvasY);
        }
    }
}
