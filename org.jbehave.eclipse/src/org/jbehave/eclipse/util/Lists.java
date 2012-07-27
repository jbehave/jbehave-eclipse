package org.jbehave.eclipse.util;

import java.util.List;

public class Lists {
    
    public static <T> List<T> toList(Iterable<T> values) {
        List<T> list = New.arrayList();
        for(T value : values)
            list.add(value);
        return list;
    }

    public static <T> List<T> filter(List<T> elems, Filter<T> filter) {
        List<T> list = New.arrayList();
        for(T elem : elems)
            if(filter.isAccepted(elem))
                list.add(elem);
        return list;
    }
    
    public static <T, R> List<T> filterTransformed(List<T> elems, Transform<T,R> transform, Filter<R> filter) {
        List<T> list = New.arrayList();
        for(T elem : elems)
            if(filter.isAccepted(transform.transform(elem)))
                list.add(elem);
        return list;
    }
    
    public static <T, R> List<R> transformAndFilter(List<T> elems, Transform<T,R> transform, Filter<R> filter) {
        List<R> list = New.arrayList();
        for(T elem : elems) {
            R t = transform.transform(elem);
            if(filter.isAccepted(t))
                list.add(t);
        }
        return list;
    }
    
    public static <T, R> List<R> transform(List<T> elems, Transform<T,R> transform) {
        List<R> list = New.arrayList();
        for(T elem : elems) {
            R t = transform.transform(elem);
            list.add(t);
        }
        return list;
    }
}
