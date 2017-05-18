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
import ch.ifocusit.plantuml.classdiagram.model.Package;
import ch.ifocusit.plantuml.classdiagram.model.attribute.Attribute;
import ch.ifocusit.plantuml.classdiagram.model.attribute.ClassAttribute;
import ch.ifocusit.plantuml.classdiagram.model.clazz.Clazz;
import ch.ifocusit.plantuml.classdiagram.model.clazz.JavaClazz;
import ch.ifocusit.plantuml.utils.ClassUtils;
import com.google.common.reflect.ClassPath;

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
 * Build class diagram from JavaClazz definition.
 *
 * @author Julien Boz
 */
public class ClassDiagramBuilder implements NamesMapper {

    private final Set<java.lang.Package> packages = new LinkedHashSet<>();
    private final Set<Class> classes = new LinkedHashSet<>();
    private Predicate<ClassAttribute> additionalFieldPredicate = a -> true; // always true by default

    private final PlantUmlBuilder builder = new PlantUmlBuilder();
    private final Set<ClassAttribute> attributs = new LinkedHashSet<>();
    private NamesMapper namesMapper = this;

    private final Map<Class, JavaClazz> cache = new HashMap<>();

    public ClassDiagramBuilder() {
    }

    public ClassDiagramBuilder excludes(String... excludes) {
        // keep the corresponding fields
        Predicate<ClassAttribute> notMatch = field -> Stream.of(excludes).noneMatch(excl -> field.toStringAttribute().matches(excl));

        // new additionalFieldPredicate base on full path
        this.additionalFieldPredicate = this.additionalFieldPredicate.and(notMatch);
        return this;
    }

    public ClassDiagramBuilder addFieldPredicate(Predicate<ClassAttribute> predicate) {
        this.additionalFieldPredicate = this.additionalFieldPredicate.and(predicate);
        return this;
    }

    public ClassDiagramBuilder addClasse(Iterable<Class> classes) {
        classes.forEach(this.classes::add);
        return this;
    }

    public ClassDiagramBuilder addClasse(Class... classes) {
        Stream.of(classes).forEach(this.classes::add);
        return this;
    }

    public ClassDiagramBuilder addPackage(java.lang.Package... packages) {
        Stream.of(packages).forEach(this.packages::add);
        return this;
    }

    public ClassDiagramBuilder withNamesMapper(NamesMapper namesMapper) {
        this.namesMapper = namesMapper;
        return this;
    }

    public String build() {
        attributs.clear();
        // generate diagram from configuration
        builder.start();
        addPackages(); // add package definition
        addTypes(); // add types definition
        addAssociations(); // then add their associations
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
        return cache.computeIfAbsent(aClass, clazz -> JavaClazz.from(clazz, readFields(clazz))
                .setOverridedName(namesMapper.getClassName(clazz))
                .setLink(namesMapper.getClassLink(clazz))
        );
    }

    protected Predicate<ClassAttribute> filter() {
        return additionalFieldPredicate;
    }

    protected Attribute[] readFields(Class aClass) {
        return Stream.of(aClass.getDeclaredFields())
                // exclude inner class
                .filter(field -> !field.getName().startsWith(DOLLAR))
                // exclude static fields
                .filter(field -> field.getDeclaringClass().isEnum() || !Modifier.isStatic(field.getModifiers()))
                .map(this::createClassAttribute)
                // excludes specific fields
                .filter(filter())
                .toArray(Attribute[]::new);
    }

    protected ClassAttribute createClassAttribute(Field field) {
        ClassAttribute attribut = new ClassAttribute(field, namesMapper.getFieldName(field));
        // look for an existing reverse field definition
        Optional<ClassAttribute> existing = attributs.stream()
                .filter(attr -> attribut.getConcernedTypes().collect(Collectors.toList()).contains(attr.getDeclaringClass())
                        && attr.getConcernedTypes().collect(Collectors.toList()).contains(attribut.getDeclaringClass()))
                .findFirst();
        if (existing.isPresent()) {
            existing.get().setBidirectionnal(true);
        } else if (!field.getDeclaringClass().isEnum()) {
            this.attributs.add(attribut);
        }
        attribut.setLink(namesMapper.getFieldLink(field));
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

        attributs.stream()
                // exclude not managed class
                .filter(field -> field.isManaged(classes))
                // excludes specific fields
                .filter(filter())
                .forEach(attr -> {
                    // class association
                    attr.getConcernedTypes()
                            .filter(classes::contains)
                            .forEach(aClass -> {
                                String aCardinality = attr.isLeftCollection() ? "*" : null;
                                String bCardinality = attr.isRightCollection() ? "*" : null;
                                String name = attr.getName();
                                Association link = attr.isBidirectionnal() ? BI_DIRECTION : DIRECTION;

                                builder.addAssociation(
                                        namesMapper.getClassName(attr.getDeclaringClass()),
                                        namesMapper.getClassName(aClass),
                                        link, name, aCardinality, bCardinality);
                            });

                });
    }
}
