package org.jbehave.eclipse.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

public class New {

    public static <T> ArrayList<T> arrayList () {
        return new ArrayList<T>();
    }
    
    public static <T> ArrayList<T> arrayList (Collection<? extends T> elements) {
        return new ArrayList<T>(elements);
    }

    
    public static <T> Vector<T> vector() {
        return new Vector<T>();
    }
    
    public static <K,V> HashMap<K, V> hashMap() {
        return new HashMap<K, V>();
    }

    public static <T> LinkedList<T> linkedList() {
        return new LinkedList<T>();
    }

    public static <T> ConcurrentLinkedQueue<T> concurrentLinkedQueue() {
        return new ConcurrentLinkedQueue<T>();
    }
    
    public static <K,V> ConcurrentHashMap<K,V> concurrentHashMap() {
        return new ConcurrentHashMap<K,V>();
    }
    

    public static <K,V> ConcurrentMultimap<K,V> concurrentMultimap() {
        return new ConcurrentMultimap<K,V>();
    }

    public static <T> CopyOnWriteArrayList<T> copyOnWriteArrayList() {
        return new CopyOnWriteArrayList<T>();
    }

}
