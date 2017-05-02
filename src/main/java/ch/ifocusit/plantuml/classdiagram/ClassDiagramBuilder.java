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

import ch.ifocusit.plantuml.Association;
import ch.ifocusit.plantuml.Attribut;
import ch.ifocusit.plantuml.PlantUmlBuilder;
import ch.ifocusit.plantuml.Type;
import ch.ifocusit.plantuml.utils.ClassUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ch.ifocusit.plantuml.Association.*;
import static ch.ifocusit.plantuml.utils.ClassUtils.DOLLAR;
import static org.apache.commons.lang3.ClassUtils.getSimpleName;

/**
 * Build class diagram from Class definition.
 *
 * @author Julien Boz
 */
public class ClassDiagramBuilder {

    private final Set<Class> classes = new LinkedHashSet<>();
    private final Set<String> excludes = new HashSet<>();

    private final PlantUmlBuilder builder = new PlantUmlBuilder();
    private final Set<ClassAttribut> attributs = new LinkedHashSet<>();

    public ClassDiagramBuilder() {
    }

    public ClassDiagramBuilder excludes(String... excludes) {
        Stream.of(excludes).forEach(this.excludes::add);
        return this;
    }

    public ClassDiagramBuilder addClasses(Iterable<Class> classes) {
        classes.forEach(this::addClass);
        return this;
    }

    public ClassDiagramBuilder addClasses(Class... classes) {
        Stream.of(classes).forEach(this::addClass);
        return this;
    }

    public ClassDiagramBuilder addClass(Class aClass) {
        classes.add(aClass);
        return this;
    }

    public String build() {
        attributs.clear();
        // generate diagram from configuration
        builder.start();
        addTypes(); // first add types definition
        addAssociations(); // then add their associations
        builder.end();
        return builder.build();
    }

    protected void addTypes() {
        // add all classes definition
        // readFields will manage field type definition, exclusions, ...
        classes.forEach(aClass -> builder.addType(getSimpleName(aClass), parseType(aClass), readFields(aClass)));
    }

    protected Type parseType(Class aClass) {
        if (aClass.isInterface()) {
            return Type.INTERFACE;
        }
        if (aClass.isEnum()) {
            return Type.ENUM;
        }
        if (Modifier.isAbstract(aClass.getModifiers())) {
            return Type.ABSTRACT;
        }
        return Type.CLASS;
    }

    protected Attribut[] readFields(Class aClass) {
        return Stream.of(aClass.getDeclaredFields())
                // exclude inner class
                .filter(field -> !field.getName().startsWith(DOLLAR))
                // exclude static fields
                .filter(field -> field.getDeclaringClass().isEnum() || !Modifier.isStatic(field.getModifiers()))
                // excludes specific fields
                .filter(field -> excludes.stream().noneMatch(excl -> (field.getDeclaringClass().getName() + "." + field.getName()).matches(excl)))
                .map(this::createAttribut).toArray(Attribut[]::new);
    }

    protected ClassAttribut createAttribut(Field field) {
        ClassAttribut attribut = ClassAttribut.of(field);
        // look for an existing reverse field definition
        Optional<ClassAttribut> existing = attributs.stream()
                .filter(attr -> attribut.getConcernedTypes().collect(Collectors.toList()).contains(attr.getDeclaringClass())
                        && attr.getConcernedTypes().collect(Collectors.toList()).contains(attribut.getDeclaringClass()))
                .findFirst();
        if (existing.isPresent()) {
            existing.get().setBidirectionnal(true);
        } else if (!field.getDeclaringClass().isEnum()) {
            this.attributs.add(attribut);
        }
        return attribut;

    }

    protected void addAssociations() {
        // browse each defined classes
        classes.forEach(aClass -> {
            // add inheritance association
            Stream.concat(Stream.of(aClass.getSuperclass()), ClassUtils.getAllInterfaces(aClass).stream())
                    .filter(Objects::nonNull).filter(classes::contains)
                    .forEach(parentClass -> builder.addAssociation(getSimpleName(parentClass), getSimpleName(aClass), INHERITANCE));
        });

        attributs.stream()
                // exclude not managed class
                .filter(field -> field.isManaged(classes))
                // excludes specific fields
                .filter(field -> excludes.stream().noneMatch(excl -> (field.getDeclaringClass().getName() + "." + field.getName()).matches(excl)))
                .forEach(attr -> {
                    // class association
                    attr.getConcernedTypes()
                            .filter(classes::contains)
                            .forEach(aClass -> {
                                String aCardinality = attr.isLeftCollection() ? "*" : null;
                                String bCardinality = attr.isRightCollection() ? "*" : null;
                                String name = /*attr.isBidirectionnal() ? null : */attr.getName();
                                Association link = attr.isBidirectionnal() ? BI_DIRECTION : DIRECTION;

                                builder.addAssociation(getSimpleName(attr.getDeclaringClass()), getSimpleName(aClass),
                                        link, name, aCardinality, bCardinality);
                            });

                });
    }
}
