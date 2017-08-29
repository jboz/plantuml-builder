/*-
 * Plantuml builder
 *
 * Copyright (C) 2017 Focus IT
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package ch.ifocusit.plantuml.classdiagram;

import ch.ifocusit.plantuml.PlantUmlBuilder;
import ch.ifocusit.plantuml.classdiagram.model.Association;
import ch.ifocusit.plantuml.classdiagram.model.Cardinality;
import ch.ifocusit.plantuml.classdiagram.model.DiagramMember;
import ch.ifocusit.plantuml.classdiagram.model.Method.ClassMethod;
import ch.ifocusit.plantuml.classdiagram.model.Method.Method;
import ch.ifocusit.plantuml.classdiagram.model.Package;
import ch.ifocusit.plantuml.classdiagram.model.attribute.Attribute;
import ch.ifocusit.plantuml.classdiagram.model.attribute.ClassAttribute;
import ch.ifocusit.plantuml.classdiagram.model.attribute.MethodAttribute;
import ch.ifocusit.plantuml.classdiagram.model.clazz.Clazz;
import ch.ifocusit.plantuml.classdiagram.model.clazz.JavaClazz;
import ch.ifocusit.plantuml.utils.ClassUtils;
import com.google.common.collect.Lists;
import com.google.common.reflect.ClassPath;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ch.ifocusit.plantuml.classdiagram.model.Association.*;
import static ch.ifocusit.plantuml.utils.ClassUtils.DOLLAR;

/**
 * Build class diagram from Class definition.
 *
 * @author Julien Boz
 */
public class ClassDiagramBuilder implements NamesMapper {

    private final Set<java.lang.Package> packages = new LinkedHashSet<>();
    private final Set<Class> classes = new LinkedHashSet<>();
    private Predicate<ClassAttribute> additionalFieldPredicate = a -> true; // always true by default

    private static final List<String> DEFAULT_METHODS_EXCLUDED = Lists.newArrayList("equals", "hashCode", "toString");

    // by default java Object methods and getter/setter will be ignored
    private Predicate<ClassMethod> additionalMethodPredicate = m -> !DEFAULT_METHODS_EXCLUDED.contains(m.getName()) && ClassUtils.isNotGetterSetter(m.getMethod());

    private final PlantUmlBuilder builder = new PlantUmlBuilder();
    // potential association
    private final Set<ClassAttribute> associations = new LinkedHashSet<>();
    // potential use definition
    private final Set<MethodAttribute> uses = new LinkedHashSet<>();
    // effective associations, really add to the diagram
    private final List<Pair<Class, Class>> effectiveAssociations = new ArrayList<>();

    private NamesMapper namesMapper = this;

    private String header;
    private String footer;

    private final Map<Class, JavaClazz> cache = new HashMap<>();

    public ClassDiagramBuilder() {
    }

    public ClassDiagramBuilder setHeader(String header) {
        this.header = header;
        return this;
    }

    public ClassDiagramBuilder setFooter(String footer) {
        this.footer = footer;
        return this;
    }

    public ClassDiagramBuilder excludes(String... excludes) {
        // keep the corresponding fields
        Predicate<ClassAttribute> notMatchField = field -> Stream.of(excludes).noneMatch(excl -> field.toStringAttribute().matches(excl));
        this.additionalFieldPredicate = this.additionalFieldPredicate.and(notMatchField);

        // keep the corresponding fields
        Predicate<ClassMethod> notMatchMethod = field -> Stream.of(excludes).noneMatch(excl -> field.toStringMethod().matches(excl));
        this.additionalMethodPredicate = this.additionalMethodPredicate.and(notMatchMethod);
        return this;
    }

    public ClassDiagramBuilder addFieldPredicate(Predicate<ClassAttribute> predicate) {
        this.additionalFieldPredicate = this.additionalFieldPredicate.and(predicate);
        return this;
    }

    public ClassDiagramBuilder addMethodPredicate(Predicate<ClassMethod> predicate) {
        this.additionalMethodPredicate = this.additionalMethodPredicate.and(predicate);
        return this;
    }

    public ClassDiagramBuilder addClasse(Iterable<Class> classes) {
        classes.forEach(this.classes::add);
        return this;
    }

    public ClassDiagramBuilder addClasse(Class... classes) {
        this.classes.addAll(Arrays.asList(classes));
        return this;
    }

    public ClassDiagramBuilder addPackage(java.lang.Package... packages) {
        this.packages.addAll(Arrays.asList(packages));
        return this;
    }

    public ClassDiagramBuilder withNamesMapper(NamesMapper namesMapper) {
        this.namesMapper = namesMapper;
        return this;
    }

    public String build() {
        associations.clear();
        // generate diagram from configuration
        builder.start();
        builder.appendPart(header);
        addPackages(); // add package definition
        addTypes(); // add types definition
        addAssociations(); // then add their associations
        builder.appendPart(footer);
        builder.end();
        return builder.build();
    }

    protected void addPackages() {
        packages.stream().forEach(pkg -> {
            try {
                ClassPath classPath = ClassPath.from(Thread.currentThread().getContextClassLoader());
                Clazz[] classes = classPath.getTopLevelClasses(pkg.getName()).stream()
                        .map(ClassPath.ClassInfo::load)
                        .map(this::createJavaClass)
                        .sorted()
                        .toArray(Clazz[]::new);
                builder.addPackage(Package.from(pkg), classes);
            } catch (IOException e) {
                throw new IllegalStateException("Cannot load classes from package " + pkg, e);
            }

        });
    }

    protected void addTypes() {
        // add all classes definition
        // readFields will manage field type definition, exclusions, ...
        classes.forEach(clazz -> builder.addType(createJavaClass(clazz)));
    }

    protected JavaClazz createJavaClass(Class aClass) {
        return cache.computeIfAbsent(aClass, clazz -> JavaClazz.from(clazz, readFields(clazz), readMethods(clazz))
                .setOverridedName(namesMapper.getClassName(clazz))
                .setLink(namesMapper.getClassLink(clazz))
        );
    }

    protected Predicate<ClassAttribute> filterFields() {
        return additionalFieldPredicate;
    }

    protected Predicate<ClassMethod> filterMethods() {
        return additionalMethodPredicate;
    }

    protected Method[] readMethods(Class aClass) {
        return Stream.of(aClass.getDeclaredMethods())
                // only public and non static methods
                .filter(method -> !Modifier.isStatic(method.getModifiers()) && Modifier.isPublic(method.getModifiers()))
                .map(this::createClassMethod)
                // excludes specific fields
                .filter(filterMethods())
                .map(this::registerUse)
                .sorted()
                .toArray(Method[]::new);
    }

    protected ClassMethod createClassMethod(java.lang.reflect.Method method) {
        ClassMethod classMethod = new ClassMethod(method, namesMapper.getMethodName(method));
        classMethod.setLink(namesMapper.getMethodLink(method));

        return classMethod;
    }

    protected Attribute[] readFields(Class aClass) {
        return Stream.of(aClass.getDeclaredFields())
                // exclude inner class
                .filter(field -> !field.getName().startsWith(DOLLAR))
                // exclude static fields
                .filter(field -> field.getDeclaringClass().isEnum() || !Modifier.isStatic(field.getModifiers()))
                .map(this::createClassAttribute)
                // excludes specific fields
                .filter(filterFields())
                // need an association
                .map(this::registerAssociation)
                .toArray(Attribute[]::new);
    }

    protected ClassAttribute createClassAttribute(Field field) {
        ClassAttribute attribute = new ClassAttribute(field, namesMapper.getFieldName(field));
        attribute.setLink(namesMapper.getFieldLink(field));
        return attribute;
    }

    private ClassMethod registerUse(ClassMethod classMethod) {
        // prepare to mark methods parameters as a uses in diagram
        classMethod.getParameters().ifPresent(params -> uses.addAll(Arrays.asList(params)));
        return classMethod;
    }

    private ClassAttribute registerAssociation(ClassAttribute attribut) {
        // look for an existing reverse field definition
        Optional<ClassAttribute> existing = associations.stream()
                .filter(attr -> attribut.getConcernedTypes().collect(Collectors.toList()).contains(attr.getDeclaringClass())
                        && attr.getConcernedTypes().collect(Collectors.toList()).contains(attribut.getDeclaringClass()))
                .findFirst();

        if (existing.isPresent()) {
            // mark attribute as bidirectional
            existing.get().setBidirectional(true);
            // do not add the second attribut to the association collection
        } else if (!attribut.getField().getDeclaringClass().isEnum()) {
            this.associations.add(attribut);
        }

        return attribut;
    }

    protected void addAssociations() {
        // browse each defined classes
        classes.forEach(aClass -> {
            // add inheritance association
            Stream.concat(Stream.of(aClass.getSuperclass()), ClassUtils.getAllInterfaces(aClass).stream())
                    .filter(Objects::nonNull).filter(classes::contains)
                    .forEach(parentClass -> builder.addAssociation(
                            namesMapper.getClassName(parentClass),
                            namesMapper.getClassName(aClass),
                            INHERITANCE));
        });

        associations.stream()
                // exclude not managed class
                .filter(field -> field.isManaged(classes))
                // excludes specific fields
                .filter(filterFields())
                .forEach(this::addAssociation);

        // after adding objects' associations, mark other objects' dependencies as "use"
        uses.stream()
                // exclude not managed class
                .filter(attribute -> attribute.isManaged(classes))
                // not same as it's declaring class
                .filter(MethodAttribute::isParameterNotTheSameAsItsOwner)
                // already in association
                .filter(methodAttribute -> methodAttribute.getConcernedTypes()
                        .noneMatch(methodParam -> effectiveAssociation(methodParam, methodAttribute.getDeclaringClass())))
                .forEach(this::addAssociation);
    }

    /**
     * @return true if this dependency is already an association
     */
    private boolean effectiveAssociation(Class methodParam, Class classAttribute) {
        // look for 2 ways associations
        return effectiveAssociations.contains(Pair.of(methodParam, classAttribute))
                || effectiveAssociations.contains(Pair.of(classAttribute, methodParam));
    }

    private void addAssociation(DiagramMember member) {
        // class association must take care of generics
        member.getConcernedTypes()
                // maintain only class in the scope of the diagram
                .filter(classes::contains)
                .forEach(aClass -> {
                    Cardinality aCardinality = member.isLeftCollection() ? Cardinality.MANY : Cardinality.NONE;
                    Cardinality bCardinality = member.isRightCollection() ? Cardinality.MANY : Cardinality.NONE;
                    String name = member instanceof MethodAttribute ? "use" : member.getName();
                    Association type = member instanceof ClassMethod ? LINK :
                            member.isBidirectional() ? BI_DIRECTION : DIRECTION;

                    builder.addAssociation(
                            namesMapper.getClassName(member.getDeclaringClass()),
                            namesMapper.getClassName(aClass),
                            type, name, aCardinality, bCardinality);

                    // remember which association have been added
                    effectiveAssociations.add(Pair.of(member.getDeclaringClass(), aClass));
                });
    }
}
