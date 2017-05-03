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

import ch.ifocusit.plantuml.Attribut;
import ch.ifocusit.plantuml.utils.ClassUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ch.ifocusit.plantuml.utils.ClassUtils.getSimpleName;
import static java.util.stream.Stream.concat;
import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * @author Julien Boz
 */
public class ClassAttribut implements Attribut {

    public static final String GENERICS_OPEN = "<";
    public static final String GENERICS_CLOSE = ">";
    public static final String GENERICS_SEP = ", ";

    private Field field;
    private boolean bidirectionnal;

    public static ClassAttribut of(Field field) {
        ClassAttribut attribut = new ClassAttribut();
        attribut.field = field;
        return attribut;
    }

    @Override
    public String getTypeString() {
        if (field.getDeclaringClass().isEnum()) {
            return EMPTY;
        }
        String fieldClassName = getSimpleName(field.getType());
        if (field.getGenericType() instanceof ParameterizedType) {
            // manage generics
            ParameterizedType genericType = (ParameterizedType) field.getGenericType();
            String subtypes = Stream.of(genericType.getActualTypeArguments())
                    .map(ClassUtils::getSimpleName)
                    .collect(Collectors.joining(GENERICS_SEP));
            fieldClassName += GENERICS_OPEN + subtypes + GENERICS_CLOSE;
        }
        return fieldClassName;
    }

    @Override
    public String getName() {
        return field.getName();
    }


    public Class getDeclaringClass() {
        return field.getDeclaringClass();
    }

    public Class getType() {
        return field.getType();
    }

    public Stream<Class> getConcernedTypes() {
        return ClassUtils.getConcernedTypes(this.field);
    }

    public boolean isManaged(Set<Class> classes) {
        return getConcernedTypes().anyMatch(classes::contains);
    }


    public void setBidirectionnal(boolean bidirectionnal) {
        this.bidirectionnal = bidirectionnal;
    }

    public boolean isBidirectionnal() {
        return bidirectionnal;
    }

    public boolean isRightCollection() {
        return ClassUtils.isCollection(getType());
    }

    public boolean isLeftCollection() {
        Optional<Field> field = ClassUtils.getField(getType(), getDeclaringClass());
        return field.isPresent() && ClassUtils.isCollection(field.get().getType());
    }
}
