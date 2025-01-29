//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.mock;

import com.google.common.primitives.Longs;
import nl.colorize.util.Stopwatch;

import java.util.ArrayList;
import java.util.List;

public class MockStopwatch extends Stopwatch {

    private List<Long> values;

    public MockStopwatch(long... values) {
        this.values = new ArrayList<>();
        this.values.addAll(Longs.asList(values));
        tick();
    }

    @Override
    public long value() {
        if (values == null || values.isEmpty()) {
            return 0L;
        }
        return values.remove(0);
    }
}
