/*-
 * Plantuml builder
 *
 * Copyright (C) 2023 Focus IT
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

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.google.common.base.CharMatcher;

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
            // local and anonymous classes are prefixed with number (1,2,3...), anonymous classes
            // are
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
                .map(ClassUtils::getSimpleName).collect(Collectors.joining(GENERICS_SEP));
        return GENERICS_OPEN + subtypes + GENERICS_CLOSE;
    }

    public static boolean isCollection(Class aClass) {
        return Collection.class.isAssignableFrom(aClass);
    }

    public static Optional<Field> getField(Class container, Class aClass) {
        return Stream.of(container.getDeclaredFields()).filter(attr -> getConcernedTypes(attr)
                .stream().anyMatch(fieldType -> fieldType.equals(aClass))).findFirst();
    }

    public static Set<Class> getConcernedTypes(Field field) {
        Set<Class> classes = new HashSet<>();
        classes.add(field.getType());
        classes.addAll(getGenericTypes(field));
        return classes;
    }

    public static Set<Class> getConcernedTypes(Method method) {
        Set<Class> classes = new HashSet<>();
        // manage returned types
        classes.add(method.getReturnType());
        classes.addAll(getGenericTypes(method));
        // manage parameters types
        for (Parameter parameter : method.getParameters()) {
            classes.addAll(getConcernedTypes(parameter));
        }
        return classes;
    }

    public static Set<Class> getConcernedTypes(Parameter parameter) {
        Set<Class> classes = new HashSet<>();
        classes.add(parameter.getType());
        classes.addAll(getGenericTypes(parameter));
        return classes;
    }

    public static Set<Class> getGenericTypes(ParameterizedType type) {
        return Stream.of(type.getActualTypeArguments()).filter(Class.class::isInstance)
                .map(Class.class::cast).collect(Collectors.toSet());
    }

    public static Set<Class> getGenericTypes(Field field) {
        if (field.getGenericType() instanceof ParameterizedType) {
            // manage generics
            return getGenericTypes((ParameterizedType) field.getGenericType());
        }
        return new HashSet<>();
    }

    public static Set<Class> getGenericTypes(Method method) {
        if (method.getGenericReturnType() instanceof ParameterizedType) {
            // manage generics
            return getGenericTypes((ParameterizedType) method.getGenericReturnType());
        }
        return new HashSet<>();
    }

    public static Set<Class> getGenericTypes(Parameter parameter) {
        if (parameter.getParameterizedType() instanceof ParameterizedType) {
            // manage generics
            return getGenericTypes((ParameterizedType) parameter.getParameterizedType());
        }
        return new HashSet<>();
    }

    public static boolean isGetter(Method method) {
        try {
            return Stream
                    .of(Introspector.getBeanInfo(method.getDeclaringClass())
                            .getPropertyDescriptors())
                    .map(desc -> desc.getReadMethod()).filter(Objects::nonNull)
                    .anyMatch(getter -> getter.equals(method));
        } catch (IntrospectionException e) {
            throw new IllegalStateException(e);
        }
    }

    public static boolean isSetter(Method method) {
        try {
            return Stream
                    .of(Introspector.getBeanInfo(method.getDeclaringClass())
                            .getPropertyDescriptors())
                    .map(desc -> desc.getWriteMethod()).filter(Objects::nonNull)
                    .anyMatch(setter -> setter.equals(method));
        } catch (IntrospectionException e) {
            throw new IllegalStateException(e);
        }
    }

    public static boolean isNotGetterSetter(Method method) {
        return !isGetter(method) && !isSetter(method);
    }
}
