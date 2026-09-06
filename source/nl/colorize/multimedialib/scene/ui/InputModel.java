//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene.ui;

import lombok.Getter;
import nl.colorize.util.Signal;

import java.util.function.Consumer;

/**
 * Model class for one of the widgets within a {@link Form}. This consists of
 * the field's value plus additional field state, such as whether the field
 * is enabled.
 *
 * @param <T> The type of value stored by this field.
 */
@Getter
public class InputModel<T> {

    private Signal<T> value;
    private Signal<Boolean> enabled;

    public InputModel(T initialValue) {
        this.value = Signal.of(initialValue);
        this.enabled = Signal.of(true);
    }

    public void subscribe(Consumer<T> subscriber) {
        value.getChanges().subscribe(subscriber);
    }

    public void subscribe(Runnable action) {
        value.getChanges().subscribe(_ -> action.run());
    }

    public void disable() {
        enabled.set(false);
    }
}
