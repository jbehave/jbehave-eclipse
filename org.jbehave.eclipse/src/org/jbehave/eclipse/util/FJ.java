package org.jbehave.eclipse.util;

import fj.F;
import fj.F2;
import fj.data.Array;
import fj.data.List;
import fj.data.Option;

public class FJ {

    public static <T> int count(Iterable<T> values, final F<T, Boolean> filter) {
        return Array.iterableArray(values).foldLeft(new F2<Integer, T, Integer>() {
            public Integer f(Integer acc, T value) {
                if (filter.f(value))
                    return acc + 1;
                else
                    return acc;
            };
        }, 0);
    }

    public static <T, R> Array<R> mapAndFilter(Iterable<T> values, final F<T, R> map, final F<R, Boolean> filter) {
        return Array.iterableArray(values).map(map).filter(filter);
    }

    public static <T> F<T, Boolean> alwaysTrue() {
        return new F<T, Boolean>() {
            @Override
            public Boolean f(T arg) {
                return Boolean.TRUE;
            }
        };
    }

    public static <T> F<T, Boolean> and(final F<T, Boolean> one, final F<T, Boolean> two) {
        return new F<T, Boolean>() {
            public Boolean f(T value) {
                return one.f(value) && two.f(value);
            }
        };
    }

    public static <T> F<T, Option<T>> identityOption() {
        return Option.some_();
    }
    
    public static <T> F2<List<T>, T[], List<T>> listCollector(Class<T> type) {
        return new F2<List<T>, T[], List<T>>() {
            public fj.data.List<T> f(fj.data.List<T> list, T[] dataToAppend) {
                return list.append(List.list(dataToAppend));
            };
        };
    }

}
