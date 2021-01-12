//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;
import nl.colorize.multimedialib.renderer.Updatable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Records information during a scene, which can then be used to replay the
 * scene at a later time. Multiple objects can be recorded, which are
 * identified by strings acting as object IDs.
 * <p>
 * Recording many objects can lead to significant memory consumption. For this
 * reason, recording is limited by both interval and capacity. The interval
 * defines how frequently data is recorded. For example, an interval of 0.1
 * seconds leads to a recording rate of 10 data points per second. The capacity
 * then controls the maximum data points then be recorded. When this is
 * exceeded, the oldest data points will be discarded. So a 0.1 second interval
 * combined with a 50 capacity would lead to a recording period of 5 seconds.
 */
public class Recorder implements Updatable {

    private float interval;
    private int capacity;

    private Map<String, Supplier<Float>> objects;
    private ListMultimap<String, Float> data;
    private float time;

    public Recorder(float interval, int capacity) {
        Preconditions.checkArgument(interval > 0f, "Invalid interval: " + interval);
        Preconditions.checkArgument(capacity > 0, "Invalid capacity: " + capacity);

        this.interval = interval;
        this.capacity = capacity;

        this.objects = new HashMap<>();
        this.data = ArrayListMultimap.create();
        this.time = 0f;
    }

    /**
     * Registers an object that should be recorded. After registration, the actual
     * recording will occur automatically during frame updates in
     * {@link #update(float)} at the requested recording interval.
     */
    public void record(String objectId, Supplier<Float> object) {
        Preconditions.checkArgument(!objects.containsKey(objectId),
            "Object ID is already in use: " + objectId);
        objects.put(objectId, object);
    }

    @Override
    public void update(float deltaTime) {
        time += deltaTime;

        while (time >= interval) {
            time = Math.max(time - interval, 0f);

            for (String objectId : objects.keySet()) {
                Float value = objects.get(objectId).get();
                data.put(objectId, value);
            }
        }

        trimCapacity();
    }

    private void trimCapacity() {
        List<String> trim = data.keySet().stream()
            .filter(objectId -> data.get(objectId).size() > capacity)
            .collect(Collectors.toList());

        for (String objectId : trim) {
            List<Float> values = data.get(objectId);

            if (values.size() > capacity) {
                values = ImmutableList.copyOf(values.subList(values.size() - capacity, values.size()));
                data.replaceValues(objectId, values);
            }
        }
    }

    public Set<String> getRecordedObjects() {
        return data.keySet();
    }

    public List<Float> getRecordedValues(String objectId) {
        Preconditions.checkArgument(objects.containsKey(objectId),
            "Object has not been recorded: " + objectId);
        return data.get(objectId);
    }

    public float getInterval() {
        return interval;
    }

    public int getCapacity() {
        return capacity;
    }
}
