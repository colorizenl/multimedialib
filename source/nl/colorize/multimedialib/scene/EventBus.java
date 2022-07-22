//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Broker for communication between different systems, avoiding the need to have
 * direct coupling between systems. Instead, systems can subscribe to events.
 * When another system then sends that type of event, it can be handled without
 * a direct reference to the originating system.
 *
 * @param <T> The common type interface for all objects that can be registered
 *            as event listener.
 */
public class EventBus<T> {

    private List<T> eventListeners;

    public EventBus() {
        this.eventListeners = new ArrayList<>();
    }

    public void send(Consumer<T> handler) {
        eventListeners.forEach(handler);
    }

    public void addListener(T listener) {
        eventListeners.add(listener);
    }

    public void removeListener(T listener) {
        eventListeners.remove(listener);
    }

    public void reset() {
        eventListeners.clear();
    }
}
