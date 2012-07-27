package org.jbehave.eclipse.util;

public interface Filter<T> {
    boolean isAccepted(T value);
}
