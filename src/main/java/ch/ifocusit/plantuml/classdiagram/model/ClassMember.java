package ch.ifocusit.plantuml.classdiagram.model;

import java.util.Collection;
import java.util.Set;

@SuppressWarnings({"unused", "rawtypes"})
public interface ClassMember extends Comparable<ClassMember> {

    Set<Class> getConcernedTypes();

    String getName();

    Class getDeclaringClass();

    Class getType();

    /**
     * @return true if concerned types are in the <code>classes</code> collection
     */
    default boolean isManaged(Collection<Class> classes) {
        return getConcernedTypes().stream().anyMatch(classes::contains);
    }

    @Override
    default int compareTo(ClassMember o) {
        return getName().compareTo(o.getName());
    }
}
