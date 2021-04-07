package ch.ifocusit.plantuml.classdiagram.model.attribute;

import ch.ifocusit.plantuml.classdiagram.model.ClassMember;
import ch.ifocusit.plantuml.classdiagram.model.clazz.Clazz.Visibilty;
import ch.ifocusit.plantuml.utils.ClassUtils;

import java.lang.reflect.Parameter;
import java.util.Optional;
import java.util.Set;

public class MethodAttribute implements Attribute, ClassMember {

    private final Parameter methodParameter;

    public MethodAttribute(final Parameter methodParameter) {
        this.methodParameter = methodParameter;
    }

    @Override
    public Set<Class> getConcernedTypes() {
        return ClassUtils.getConcernedTypes(methodParameter);
    }

    @Override
    public Visibilty getVisibilty() {
        return Visibilty.NONE;
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
