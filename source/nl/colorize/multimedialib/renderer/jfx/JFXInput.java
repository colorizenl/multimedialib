//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.jfx;

import com.google.common.collect.ImmutableMap;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.KeyCode;
import nl.colorize.multimedialib.renderer.Pointer;
import nl.colorize.util.Subscribable;
import nl.colorize.util.swing.Popups;
import nl.colorize.util.swing.SwingUtils;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Captures the current state for mouse and keyboard events, updated using
 * JavaFX event handlers.
 */
public class JFXInput implements InputDevice {

    protected Queue<MouseEvent> mouseEventQueue;
    protected Queue<KeyEvent> keyEventQueue;

    private Canvas canvas;
    private Pointer mouse;
    private Map<KeyCode, Integer> keyboard;

    private static final Map<Integer, KeyCode> KEY_CODES = new ImmutableMap.Builder<Integer, KeyCode>()
        .put(37, KeyCode.LEFT)
        .put(39, KeyCode.RIGHT)
        .put(38, KeyCode.UP)
        .put(40, KeyCode.DOWN)
        .put(10, KeyCode.ENTER)
        .put(32, KeyCode.SPACEBAR)
        .put(27, KeyCode.ESCAPE)
        .put(16, KeyCode.SHIFT)
        .put(8, KeyCode.BACKSPACE)
        .put(9, KeyCode.TAB)
        .put(65, KeyCode.A)
        .put(66, KeyCode.B)
        .put(67, KeyCode.C)
        .put(68, KeyCode.D)
        .put(69, KeyCode.E)
        .put(70, KeyCode.F)
        .put(71, KeyCode.G)
        .put(72, KeyCode.H)
        .put(73, KeyCode.I)
        .put(74, KeyCode.J)
        .put(75, KeyCode.K)
        .put(76, KeyCode.L)
        .put(77, KeyCode.M)
        .put(78, KeyCode.N)
        .put(79, KeyCode.O)
        .put(80, KeyCode.P)
        .put(81, KeyCode.Q)
        .put(82, KeyCode.R)
        .put(83, KeyCode.S)
        .put(84, KeyCode.T)
        .put(85, KeyCode.U)
        .put(86, KeyCode.V)
        .put(87, KeyCode.W)
        .put(88, KeyCode.X)
        .put(89, KeyCode.Y)
        .put(90, KeyCode.Z)
        .put(49, KeyCode.N1)
        .put(50, KeyCode.N2)
        .put(51, KeyCode.N3)
        .put(52, KeyCode.N4)
        .put(53, KeyCode.N5)
        .put(54, KeyCode.N6)
        .put(55, KeyCode.N7)
        .put(56, KeyCode.N8)
        .put(57, KeyCode.N9)
        .put(48, KeyCode.N0)
        .put(112, KeyCode.F1)
        .put(113, KeyCode.F2)
        .put(114, KeyCode.F3)
        .put(115, KeyCode.F4)
        .put(116, KeyCode.F5)
        .put(117, KeyCode.F6)
        .put(118, KeyCode.F7)
        .put(119, KeyCode.F8)
        .put(120, KeyCode.F9)
        .put(121, KeyCode.F10)
        .put(122, KeyCode.F11)
        .put(123, KeyCode.F12)
        .put(44, KeyCode.COMMA)
        .put(46, KeyCode.PERIOD)
        .put(521, KeyCode.PLUS)
        .put(45, KeyCode.MINUS)
        .put(61, KeyCode.EQUALS)
        .build();

    protected JFXInput(Canvas canvas) {
        this.canvas = canvas;
        this.mouse = new Pointer("mouse");
        this.keyboard = new HashMap<>();

        for (KeyCode key : KeyCode.values()) {
            keyboard.put(key, Pointer.STATE_IDLE);
        }

        this.mouseEventQueue = new LinkedList<>();
        this.keyEventQueue = new LinkedList<>();
    }

    @Override
    public void update(float deltaTime) {
        if (mouse.getState() == Pointer.STATE_RELEASED) {
            mouse.setState(Pointer.STATE_IDLE);
        }

        for (KeyCode key : KeyCode.values()) {
            if (keyboard.get(key) == Pointer.STATE_RELEASED) {
                keyboard.put(key, Pointer.STATE_IDLE);
            }
        }

        mouseEventQueue.forEach(this::processMouseEvent);
        mouseEventQueue.clear();

        keyEventQueue.forEach(this::processKeyEvent);
        keyEventQueue.clear();
    }

    private void processMouseEvent(MouseEvent event) {
        float canvasX = canvas.toCanvasX((int) event.getSceneX());
        float canvasY = canvas.toCanvasY((int) event.getSceneY());
        mouse.setPosition(new Point2D(canvasX, canvasY));

        if (event.getEventType() == MouseEvent.MOUSE_RELEASED) {
            mouse.setState(Pointer.STATE_RELEASED);
        } else if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
            mouse.setState(Pointer.STATE_PRESSED);
        }
    }

    private void processKeyEvent(KeyEvent event) {
        KeyCode keyCode = KEY_CODES.get(event.getCode().getCode());

        if (keyCode != null) {
            if (event.getEventType() == KeyEvent.KEY_RELEASED) {
                keyboard.put(keyCode, Pointer.STATE_RELEASED);
            } else if (event.getEventType() == KeyEvent.KEY_PRESSED) {
                keyboard.put(keyCode, Pointer.STATE_PRESSED);
            }
        }
    }

    @Override
    public Iterable<Pointer> getPointers() {
        return List.of(mouse);
    }

    @Override
    public void clearPointerState() {
        mouse.setState(Pointer.STATE_IDLE);
    }

    @Override
    public boolean isTouchAvailable() {
        return false;
    }

    @Override
    public boolean isKeyboardAvailable() {
        return true;
    }

    @Override
    public boolean isKeyPressed(KeyCode keyCode) {
        return keyboard.get(keyCode) == Pointer.STATE_PRESSED;
    }

    @Override
    public boolean isKeyReleased(KeyCode keyCode) {
        return keyboard.get(keyCode) == Pointer.STATE_RELEASED;
    }

    @Override
    public Subscribable<String> requestTextInput(String labelText, String initialValue) {
        JLabel label = new JLabel(labelText);
        JTextField field = new JTextField(initialValue);

        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        SwingUtils.setPreferredWidth(panel, 300);

        Popups.message(null, "", panel);

        Subscribable<String> subscribable = new Subscribable<>();
        if (field.getText() != null && !field.getText().isEmpty()) {
            subscribable.next(field.getText());
        }
        return subscribable;
    }

    @Override
    public void fillClipboard(String text) {
        SwingUtils.copyToClipboard(text);
    }
}
