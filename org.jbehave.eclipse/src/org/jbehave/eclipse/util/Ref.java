package org.jbehave.eclipse.util;


public class Ref<T> {
    private T value;
    
    public Ref() {
    }
    public Ref(T value) {
        this.value = value;
    }
    
    public void set(T value) {
        this.value = value;
    }
    public T get() {
        return value;
    }
    public static <T> Ref<T> create() {
        return new Ref<T>();
    }
    public static <T> Ref<T> create(T initialValue) {
        return new Ref<T>(initialValue);
    }
    public boolean isNull() {
        return value==null;
    }
    public T getOrElse(T defaultValue) {
        if(isNull())
            return defaultValue;
        else
            return value;
    }
}
