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
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Stream.concat;

/**
 * @author Julien Boz
 */
public class ClassUtils extends org.apache.commons.lang3.ClassUtils {

    public static final String DOLLAR = "$";

    public static String getSimpleName(java.lang.reflect.Type type) {
        return getSimpleName((Class) type);
    }

    public static String getSimpleName(Class aClass) {
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
            return Stream.of(genericType.getActualTypeArguments()).map(type -> (Class) type);
        }
        return Stream.empty();
    }
}
