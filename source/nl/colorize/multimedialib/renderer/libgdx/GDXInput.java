//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.libgdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.google.common.collect.ImmutableMap;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.KeyCode;
import nl.colorize.multimedialib.renderer.Pointer;
import nl.colorize.multimedialib.renderer.RenderConfig;
import nl.colorize.util.Platform;
import nl.colorize.util.Subject;
import nl.colorize.util.swing.Popups;
import nl.colorize.util.swing.SwingUtils;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GDXInput implements InputDevice {

    private RenderConfig config;
    private Canvas canvas;
    private Pointer pointer;
    private Set<KeyCode> keysDown;
    private Set<KeyCode> keysUp;

    private static final Map<KeyCode, Integer> KEY_MAPPING = new ImmutableMap.Builder<KeyCode, Integer>()
        .put(KeyCode.LEFT, Input.Keys.LEFT)
        .put(KeyCode.RIGHT, Input.Keys.RIGHT)
        .put(KeyCode.UP, Input.Keys.UP)
        .put(KeyCode.DOWN, Input.Keys.DOWN)
        .put(KeyCode.ENTER, Input.Keys.ENTER)
        .put(KeyCode.SPACEBAR, Input.Keys.SPACE)
        .put(KeyCode.ESCAPE, Input.Keys.ESCAPE)
        .put(KeyCode.SHIFT, Input.Keys.SHIFT_LEFT)
        .put(KeyCode.BACKSPACE, Input.Keys.BACKSPACE)
        .put(KeyCode.TAB, Input.Keys.TAB)
        .put(KeyCode.A, Input.Keys.A)
        .put(KeyCode.B, Input.Keys.B)
        .put(KeyCode.C, Input.Keys.C)
        .put(KeyCode.D, Input.Keys.D)
        .put(KeyCode.E, Input.Keys.E)
        .put(KeyCode.F, Input.Keys.F)
        .put(KeyCode.G, Input.Keys.G)
        .put(KeyCode.H, Input.Keys.H)
        .put(KeyCode.I, Input.Keys.I)
        .put(KeyCode.J, Input.Keys.J)
        .put(KeyCode.K, Input.Keys.K)
        .put(KeyCode.L, Input.Keys.L)
        .put(KeyCode.M, Input.Keys.M)
        .put(KeyCode.N, Input.Keys.N)
        .put(KeyCode.O, Input.Keys.O)
        .put(KeyCode.P, Input.Keys.P)
        .put(KeyCode.Q, Input.Keys.Q)
        .put(KeyCode.R, Input.Keys.R)
        .put(KeyCode.S, Input.Keys.S)
        .put(KeyCode.T, Input.Keys.T)
        .put(KeyCode.U, Input.Keys.U)
        .put(KeyCode.V, Input.Keys.V)
        .put(KeyCode.W, Input.Keys.W)
        .put(KeyCode.X, Input.Keys.X)
        .put(KeyCode.Y, Input.Keys.Y)
        .put(KeyCode.Z, Input.Keys.Z)
        .put(KeyCode.N1, Input.Keys.NUM_1)
        .put(KeyCode.N2, Input.Keys.NUM_2)
        .put(KeyCode.N3, Input.Keys.NUM_3)
        .put(KeyCode.N4, Input.Keys.NUM_4)
        .put(KeyCode.N5, Input.Keys.NUM_5)
        .put(KeyCode.N6, Input.Keys.NUM_6)
        .put(KeyCode.N7, Input.Keys.NUM_7)
        .put(KeyCode.N8, Input.Keys.NUM_8)
        .put(KeyCode.N9, Input.Keys.NUM_9)
        .put(KeyCode.N0, Input.Keys.NUM_0)
        .put(KeyCode.F1, Input.Keys.F1)
        .put(KeyCode.F2, Input.Keys.F2)
        .put(KeyCode.F3, Input.Keys.F3)
        .put(KeyCode.F4, Input.Keys.F4)
        .put(KeyCode.F5, Input.Keys.F5)
        .put(KeyCode.F6, Input.Keys.F6)
        .put(KeyCode.F7, Input.Keys.F7)
        .put(KeyCode.F8, Input.Keys.F8)
        .put(KeyCode.F9, Input.Keys.F9)
        .put(KeyCode.F10, Input.Keys.F10)
        .put(KeyCode.F11, Input.Keys.F11)
        .put(KeyCode.F12, Input.Keys.F12)
        .put(KeyCode.COMMA, Input.Keys.COMMA)
        .put(KeyCode.PERIOD, Input.Keys.PERIOD)
        .put(KeyCode.PLUS, Input.Keys.PLUS)
        .put(KeyCode.MINUS, Input.Keys.MINUS)
        .put(KeyCode.EQUALS, Input.Keys.EQUALS)
        .build();

    protected GDXInput(RenderConfig config) {
        this.config = config;
        this.canvas = config.getCanvas();
        this.pointer = new Pointer("mouse");
        this.keysUp = new HashSet<>();
        this.keysDown = new HashSet<>();
    }

    @Override
    public void update(float deltaTime) {
        updatePointer();
        updateKeyboard();
    }

    private void updatePointer() {
        float pointerX = canvas.toCanvasX(Gdx.input.getX());
        float pointerY = canvas.toCanvasY(Gdx.input.getY());
        pointer.setPosition(new Point2D(pointerX, pointerY));

        if (Gdx.input.isTouched()) {
            pointer.setState(Pointer.STATE_PRESSED);
        } else if (pointer.isPressed()) {
            pointer.setState(Pointer.STATE_RELEASED);
        } else {
            pointer.setState(Pointer.STATE_IDLE);
        }
    }

    private void updateKeyboard() {
        for (KeyCode keyCode : KeyCode.values()) {
            Integer gdxKeyCode = KEY_MAPPING.get(keyCode);

            if (Gdx.input.isKeyPressed(gdxKeyCode)) {
                keysDown.add(keyCode);
                keysUp.remove(keyCode);
            } else if (keysDown.contains(keyCode)) {
                keysDown.remove(keyCode);
                keysUp.add(keyCode);
            } else {
                keysDown.remove(keyCode);
                keysUp.remove(keyCode);
            }
        }
    }

    @Override
    public List<Pointer> getPointers() {
        return List.of(pointer);
    }

    @Override
    public void clearPointerState() {
        pointer.setState(Pointer.STATE_IDLE);
    }

    @Override
    public boolean isTouchAvailable() {
        Platform platform = Platform.getPlatform();
        return config.isSimulationMode() || platform == Platform.IOS || platform == Platform.ANDROID;
    }

    @Override
    public boolean isKeyboardAvailable() {
        return !isTouchAvailable();
    }

    @Override
    public boolean isKeyPressed(KeyCode keyCode) {
        return keysDown.contains(keyCode);
    }

    @Override
    public boolean isKeyReleased(KeyCode keyCode) {
        return keysUp.contains(keyCode);
    }

    @Override
    public Subject<String> requestTextInput(String labelText, String initialValue) {
        JLabel label = new JLabel(labelText);
        JTextField field = new JTextField(initialValue);

        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        SwingUtils.setPreferredWidth(panel, 300);

        Popups.message(null, "", panel);

        Subject<String> subject = new Subject<>();
        if (field.getText() != null && !field.getText().isEmpty()) {
            subject.next(field.getText());
        }
        return subject;
    }

    @Override
    public void fillClipboard(String text) {
        SwingUtils.copyToClipboard(text);
    }
}
