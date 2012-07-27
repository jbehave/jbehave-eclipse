package org.jbehave.eclipse.util;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ConcurrentMultimap<K,V> {

    private final ConcurrentHashMap<K, ConcurrentLinkedQueue<V>> underlying = New.concurrentHashMap();
    
    public ConcurrentHashMap<K, ConcurrentLinkedQueue<V>> getUnderlying() {
        return underlying;
    }
    
    public void put(K key, V value) {
        ConcurrentLinkedQueue<V> values = underlying.get(key);
        if(values==null) {
            ConcurrentLinkedQueue<V> newValues = New.concurrentLinkedQueue();
            values = underlying.putIfAbsent(key, newValues);
            if(values==null)
                values = newValues;
        }
        values.add(value);
    }

    /**
     * @return
     * @see java.util.concurrent.ConcurrentHashMap#isEmpty()
     */
    public boolean isEmpty() {
        return underlying.isEmpty();
    }

    /**
     * @param key
     * @return
     * @see java.util.concurrent.ConcurrentHashMap#containsKey(java.lang.Object)
     */
    public boolean containsKey(Object key) {
        return underlying.containsKey(key);
    }

    /**
     * @param key
     * @return
     * @see java.util.concurrent.ConcurrentHashMap#remove(java.lang.Object)
     */
    public ConcurrentLinkedQueue<V> remove(Object key) {
        return underlying.remove(key);
    }

    /**
     * @return
     * @see java.util.concurrent.ConcurrentHashMap#keySet()
     */
    public Set<K> keySet() {
        return underlying.keySet();
    }
    
}
