//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Actors, also referred to as entities, are objects that exist within the scene.
 * Actors do not contain any data or logic of their own. An actor is effectively
 * an ID that is connected to a number of {@link Component}s. The set of
 * components and their contents is then used by {@link ActorSystem}s to
 * implement scene logic.
 */
public class Actor implements Updatable {

    private UUID id;
    private Map<Class<? extends Component>, Component> components;

    public Actor(UUID id, List<Component> components) {
        this.id = id;
        this.components = ImmutableMap.copyOf(components.stream()
            .collect(Collectors.toMap(c -> c.getClass(), c -> c)));
    }

    public Actor(UUID id, Component... components) {
        this(id, ImmutableList.copyOf(components));
    }

    public Actor(List<Component> components) {
        this(UUID.randomUUID(), components);
    }

    public Actor(Component... components) {
        this(ImmutableList.copyOf(components));
    }

    public UUID getId() {
        return id;
    }

    public boolean hasComponent(Class<? extends Component> componentClass) {
        return components.containsKey(componentClass);
    }

    public <T extends Component> T getComponent(Class<T> componentClass) {
        Component component = components.get(componentClass);
        Preconditions.checkState(component != null,
            "Entity does not have component " + componentClass);
        return (T) component;
    }

    public Iterable<Component> getComponents() {
        return components.values();
    }

    @Override
    public void update(float deltaTime) {
        for (Component component : components.values()) {
            component.update(deltaTime);
        }
    }
}
