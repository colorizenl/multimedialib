//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.graphics;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 3D polygon mesh that consists of vertex, edge, and face data; texture
 * information, and animations. Meshes are typically loaded from model files
 * created using 3D modeling software, though it is also possible to generate
 * simple meshes programmatically.
 * <p>
 * A mesh cannot directly be added to the stage, it only contains the data.
 * However, the mesh can be used to create models, which are instances that
 * use the mesh data and can be displayed.
 * <p>
 * A mesh can be identified by ID or by name. The ID is unique and used by the
 * renderer to identify the mesh. The name is the mesh's human readable name,
 * and does not necessarily have to be unique.
 */
public final class PolygonMesh {

    private UUID id;
    private String name;
    private Map<String, AnimationInfo> animations;

    public PolygonMesh(UUID id, String name, List<AnimationInfo> animations) {
        Preconditions.checkArgument(name.length() >= 1, "Mesh name required");

        this.id = id;
        this.name = name;
        this.animations = new HashMap<>();

        for (AnimationInfo animation : animations) {
            addAnimation(animation);
        }
    }

    public PolygonMesh(String name, List<AnimationInfo> animations) {
        this(UUID.randomUUID(), name, animations);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void addAnimation(AnimationInfo anim) {
        Preconditions.checkArgument(!animations.containsKey(anim.getName()),
            "Animation with the same name already exists: " + anim.getName());
        animations.put(anim.getName(), anim);
    }

    public AnimationInfo getAnimation(String name) {
        AnimationInfo animation = animations.get(name);
        Preconditions.checkArgument(animation != null, "No such animation: " + name);
        return animation;
    }

    public Set<AnimationInfo> getAnimations() {
        return ImmutableSet.copyOf(animations.values());
    }

    public PolygonModel createModel() {
        return new PolygonModel(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof PolygonMesh) {
            PolygonMesh other = (PolygonMesh) o;
            return id.equals(other.id);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
