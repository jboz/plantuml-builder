package ch.ifocusit.plantuml.classdiagram.model.attribute;

import ch.ifocusit.plantuml.classdiagram.model.DiagramMember;
import ch.ifocusit.plantuml.utils.ClassUtils;

import java.lang.reflect.Parameter;
import java.util.Optional;
import java.util.stream.Stream;

public class MethodAttribute implements Attribute, DiagramMember {

    private final Parameter methodParameter;

    public MethodAttribute(final Parameter methodParameter) {
        this.methodParameter = methodParameter;
    }

    public boolean isParameterNotTheSameAsItsOwner() {
        return !getDeclaringClass().equals(methodParameter.getType());
    }

    @Override
    public Stream<Class> getConcernedTypes() {
        return ClassUtils.getConcernedTypes(methodParameter);
    }

    @Override
    public boolean isLeftCollection() {
        return false;
    }

    @Override
    public boolean isRightCollection() {
        return false;
    }

    @Override
    public boolean isBidirectional() {
        return false;
    }

    @Override
    public Class getDeclaringClass() {
        return methodParameter.getDeclaringExecutable().getDeclaringClass();
    }

    @Override
    public Optional<String> getType() {
        return Optional.of(ClassUtils.getSimpleName(methodParameter.getParameterizedType()));
    }

    @Override
    public String getName() {
        return methodParameter.getName();
    }
}
