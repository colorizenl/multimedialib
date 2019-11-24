//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package org.teavm.classlib.java.util;

import java.util.Collection;
import java.util.Iterator;

public class TSpliterators {

    public static <T> TSpliterator<T> spliterator(Object[] array, int additionalCharacteristics) {
        throw new UnsupportedOperationException();
    }

    public static <T> TSpliterator<T> spliterator(Collection<? extends T> c, int characteristics) {
        throw new UnsupportedOperationException();
    }

    public static TSpliterator.OfInt spliterator(int[] array, int fromIndex, int toIndex,
                                                 int additionalCharacteristics) {
        throw new UnsupportedOperationException();
    }

    public static <T> TSpliterator<T> spliterator(Iterator<? extends T> iterator,
                                                  long size, int characteristics) {
        throw new UnsupportedOperationException();
    }
}
