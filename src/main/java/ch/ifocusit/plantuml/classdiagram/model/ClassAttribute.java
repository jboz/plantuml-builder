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
package ch.ifocusit.plantuml.classdiagram.model;

import ch.ifocusit.plantuml.utils.ClassUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ch.ifocusit.plantuml.utils.ClassUtils.getSimpleName;
import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * @author Julien Boz
 */
public class ClassAttribute implements Attribute {

    public static final String GENERICS_OPEN = "<";
    public static final String GENERICS_CLOSE = ">";
    public static final String GENERICS_SEP = ", ";

    private final Field field;
    private final String fieldName;
    private Optional<Link> link;
    private boolean bidirectionnal;

    public ClassAttribute(Field field) {
        this(field, field.getName());
    }

    public ClassAttribute(Field field, String fieldName) {
        this.field = field;
        this.fieldName = fieldName;
    }

    @Override
    public Optional<String> getType() {
        if (field.getDeclaringClass().isEnum()) {
            return Optional.empty();
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
        return Optional.of(fieldClassName);
    }

    @Override
    public String getName() {
        return fieldName;
    }

    public Field getField() {
        return field;
    }

    public Class getDeclaringClass() {
        return field.getDeclaringClass();
    }

    private Class getFieldType() {
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
        return ClassUtils.isCollection(getFieldType());
    }

    public boolean isLeftCollection() {
        Optional<Field> field = ClassUtils.getField(getFieldType(), getDeclaringClass());
        return field.isPresent() && ClassUtils.isCollection(field.get().getType());
    }

    public String toStringAttribute() {
        return field.getDeclaringClass().getName() + "." + field.getName();
    }


    public Optional<Link> getLink() {
        return link;
    }

    public ClassAttribute setLink(Optional<Link> link) {
        this.link = link;
        return this;
    }
}
