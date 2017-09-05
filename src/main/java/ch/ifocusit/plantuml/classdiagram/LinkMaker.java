package ch.ifocusit.plantuml.classdiagram;

import ch.ifocusit.plantuml.classdiagram.model.Link;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

public interface LinkMaker {

    /**
     * @return the class link to use in the diagram
     */
    default Optional<Link> getClassLink(Class aClass) {
        return Optional.empty();
    }

    /**
     * @return the field link to use in the diagram
     */
    default Optional<Link> getFieldLink(Field field) {
        return Optional.empty();
    }

    /**
     * @return the method link to use in the diagram
     */
    default Optional<Link> getMethodLink(Method method) {
        return Optional.empty();
    }
}
