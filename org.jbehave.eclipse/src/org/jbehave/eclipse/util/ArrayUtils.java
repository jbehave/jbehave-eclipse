package org.jbehave.eclipse.util;

import java.util.ArrayList;
import java.util.List;

public class ArrayUtils {

    public static <T> T[] toArray(T...items) {
        return items;
    }
    
    public static <T> List<T> copyOf(List<T> values) {
        return new ArrayList<T>(values);
    }
    
}
