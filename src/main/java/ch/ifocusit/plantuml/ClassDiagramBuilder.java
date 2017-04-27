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
package ch.ifocusit.plantuml;

import com.google.common.base.CharMatcher;
import org.apache.commons.lang3.ClassUtils;

import java.lang.reflect.Modifier;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static ch.ifocusit.plantuml.Association.DIRECTION;
import static ch.ifocusit.plantuml.Association.INHERITANCE;
import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * Build class diagram from Class definition.
 *
 * @author Julien Boz
 */
public class ClassDiagramBuilder {

    public static final String DOLLAR = "$";
    private final Set<Class> classes = new LinkedHashSet<>();

    public ClassDiagramBuilder() {
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
        PlantUmlBuilder builder = new PlantUmlBuilder();
        builder.start();
        addTypes(builder);
        addAssociations(builder);
        builder.end();
        return builder.build();
    }

    private void addAssociations(PlantUmlBuilder builder) {
        classes.forEach(aClass -> {

            Stream.concat(Stream.of(aClass.getSuperclass()), ClassUtils.getAllInterfaces(aClass).stream())
                    .filter(Objects::nonNull).filter(classes::contains)
                    .forEach(parentClass -> builder.addAssociation(getSimpleName(parentClass), getSimpleName(aClass), INHERITANCE));

            if (!aClass.isEnum()) {
                Stream.of(aClass.getDeclaredFields())
                        .filter(field -> classes.contains(field.getType()))
                        .forEach(field -> builder.addAssociation(getSimpleName(aClass), getSimpleName(field.getType()), DIRECTION, field.getName()));
            }
        });
    }

    private void addTypes(PlantUmlBuilder builder) {
        classes.forEach(aClass -> builder.addClass(getSimpleName(aClass), parseType(aClass), readFields(aClass)));
    }

    public Attribut[] readFields(Class aClass) {
        return Stream.of(aClass.getDeclaredFields())
                // exclude some field
                .filter(field -> !field.getName().startsWith(DOLLAR))
                // field name and type if not in enum
                .map(field -> new Attribut(field.getName(),
                        (field.getDeclaringClass().isEnum() ? EMPTY : getSimpleName(field.getType()))))
                .toArray(Attribut[]::new);
    }

    public Type parseType(Class aClass) {
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

    private String getSimpleName(Class aClass) {
        String className = aClass.getSimpleName();
        int lastDollarSign = className.lastIndexOf(DOLLAR);
        if (lastDollarSign != -1) {
            String innerClassName = className.substring(lastDollarSign + 1);
            // local and anonymous classes are prefixed with number (1,2,3...), anonymous classes are
            // entirely numeric whereas local classes have the user supplied name as a suffix
            return CharMatcher.digit().trimLeadingFrom(innerClassName);
        }
        return className;
    }
}
