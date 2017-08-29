package ch.ifocusit.plantuml.utils;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StreamUtils {

    public static Collection intersection(final Stream<Class> a, final Stream<Class> b) {
        return CollectionUtils.intersection(a.collect(Collectors.toList()), b.collect(Collectors.toList()));
    }

    public static boolean isIntersectionEmpty(final Stream<Class> a, final Stream<Class> b) {
        return intersection(a, b).isEmpty();
    }

    public static boolean isIntersectionNotEmpty(final Stream<Class> a, final Stream<Class> b) {
        return !isIntersectionEmpty(a, b);
    }
}
