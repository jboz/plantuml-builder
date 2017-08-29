package ch.ifocusit.plantuml.classdiagram.model;

import java.util.Set;
import java.util.stream.Stream;

public interface DiagramMember {

    Stream<Class> getConcernedTypes();

    boolean isLeftCollection();

    boolean isRightCollection();

    String getName();

    boolean isBidirectional();

    Class getDeclaringClass();

    default boolean isManaged(Set<Class> classes) {
        return getConcernedTypes().anyMatch(classes::contains);
    }
}
