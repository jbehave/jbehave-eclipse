package org.jbehave.eclipse.util;

public interface Transform<R,T> {
    T transform(R elem);
}
