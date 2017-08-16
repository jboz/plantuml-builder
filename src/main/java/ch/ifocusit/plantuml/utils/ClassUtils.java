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
package ch.ifocusit.plantuml.utils;

import com.google.common.base.CharMatcher;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Stream.concat;

/**
 * @author Julien Boz
 */
public class ClassUtils extends org.apache.commons.lang3.ClassUtils {

    public static final String GENERICS_OPEN = "<";
    public static final String GENERICS_CLOSE = ">";
    public static final String GENERICS_SEP = ", ";
    public static final String DOLLAR = "$";

    public static String getSimpleName(Class aClass) {
        return _getSimpleName(aClass);
    }

    public static String _getSimpleName(Class aClass) {
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

    public static String getSimpleName(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType genericType = (ParameterizedType) type;
            return getSimpleName(genericType.getRawType()) + getParameterizedTypeName(genericType);
        }
        if (type instanceof Class) {
            return _getSimpleName((Class) type);
        }
        return type.getTypeName();
    }

    public static String getParameterizedTypeName(ParameterizedType genericType) {
        // manage generics
        String subtypes = Stream.of(genericType.getActualTypeArguments())
                .map(ClassUtils::getSimpleName)
                .collect(Collectors.joining(GENERICS_SEP));
        return GENERICS_OPEN + subtypes + GENERICS_CLOSE;
    }

    public static boolean isCollection(Class aClass) {
        return Collection.class.isAssignableFrom(aClass);
    }

    public static Optional<Field> getField(Class container, Class aClass) {
        return Stream.of(container.getDeclaredFields())
                .filter(attr -> getConcernedTypes(attr).anyMatch(fieldType -> fieldType.equals(aClass)))
                .findFirst();
    }

    public static Stream<Class> getConcernedTypes(Field field) {
        return concat(Stream.of(field.getType()), getGenericTypes(field));
    }

    public static Stream<Class> getGenericTypes(Field field) {
        if (field.getGenericType() instanceof ParameterizedType) {
            // manage generics
            ParameterizedType genericType = (ParameterizedType) field.getGenericType();
            return Stream.of(genericType.getActualTypeArguments()).filter(Class.class::isInstance).map(Class.class::cast);
        }
        return Stream.empty();
    }

    public static Stream<Class> getConcernedTypes(Method method) {
        return concat(Stream.of(method.getReturnType()), getGenericTypes(method));
    }

    public static Stream<Class> getGenericTypes(Method method) {
        if (method.getGenericReturnType() instanceof ParameterizedType) {
            // manage generics
            ParameterizedType genericType = (ParameterizedType) method.getGenericReturnType();
            return Stream.of(genericType.getActualTypeArguments()).filter(Class.class::isInstance).map(Class.class::cast);
        }
        return Stream.empty();
    }
}
