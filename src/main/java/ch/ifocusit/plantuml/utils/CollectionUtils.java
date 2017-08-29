package ch.ifocusit.plantuml.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class CollectionUtils {

    public static <E> Collection<E> intersection(Collection<E> a, Collection<E> b) {
        BiFunction<Collection<E>, Collection<E>, Collection<E>> intersector =
                (smallest, biggest) ->
                        smallest.parallelStream()
                                .filter((e) -> biggest.contains(e))
                                .collect(Collectors.toList());

        if (null == a || null == b) {
            return Collections.emptyList();
        } else {
            return a.size() < b.size()
                    ? intersector.apply(a, b)
                    : intersector.apply(b, a);
        }
    }
}
