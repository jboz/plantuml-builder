package ch.ifocusit.plantuml.classdiagram.model.attribute;

import java.lang.reflect.Parameter;
import java.util.Optional;
import java.util.Set;
import ch.ifocusit.plantuml.classdiagram.model.ClassMember;
import ch.ifocusit.plantuml.utils.ClassUtils;

public class MethodAttribute implements Attribute, ClassMember {

    private final Parameter methodParameter;

    public MethodAttribute(final Parameter methodParameter) {
        this.methodParameter = methodParameter;
    }

    @Override
    public Set<Class> getConcernedTypes() {
        return ClassUtils.getConcernedTypes(methodParameter);
    }

    public Class getParameterType() {
        return methodParameter.getType();
    }

    @Override
    public Class getType() {
        return getParameterType();
    }

    @Override
    public Class getDeclaringClass() {
        return methodParameter.getDeclaringExecutable().getDeclaringClass();
    }

    @Override
    public Optional<String> getTypeName() {
        return Optional.of(ClassUtils.getSimpleName(methodParameter.getParameterizedType()));
    }

    @Override
    public String getName() {
        return methodParameter.getName();
    }
}
