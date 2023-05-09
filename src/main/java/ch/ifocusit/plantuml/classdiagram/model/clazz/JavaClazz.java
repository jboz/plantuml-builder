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
package ch.ifocusit.plantuml.classdiagram.model.clazz;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import ch.ifocusit.plantuml.classdiagram.model.Link;
import ch.ifocusit.plantuml.classdiagram.model.attribute.ClassAttribute;
import ch.ifocusit.plantuml.classdiagram.model.method.ClassMethod;
import ch.ifocusit.plantuml.utils.ClassUtils;

/**
 * @author Julien Boz
 */
@SuppressWarnings({"unused", "rawtypes"})
public class JavaClazz implements Clazz {

    private final Class<?> relatedClass;
    private String overridedName;
    private Link link;
    private final List<ClassAttribute> attributes = new ArrayList<>();
    private final List<ClassMethod> methods = new ArrayList<>();
    private String backgroundColor;
    private String borderColor;

    public JavaClazz(Class<?> relatedClass) {
        this.relatedClass = relatedClass;
    }

    public String getName() {
        return Optional.ofNullable(overridedName).orElse(ClassUtils.getSimpleName(relatedClass));
    }

    public Type getType() {
        return parseType(relatedClass);
    }

    public Optional<Link> getLink() {
        return Optional.ofNullable(link);
    }

    public JavaClazz setLink(Link link) {
        this.link = link;
        return this;
    }

    public Class<?> getRelatedClass() {
        return relatedClass;
    }

    public List<ClassAttribute> getAttributes() {
        return attributes;
    }

    public void addAttributes(ClassAttribute... attributes) {
        this.attributes.addAll(Arrays.asList(attributes));
    }

    public List<ClassMethod> getMethods() {
        return methods;
    }

    public void addMethods(ClassMethod... methods) {
        this.methods.addAll(Arrays.asList(methods));
    }

    public JavaClazz setOverridedName(String overridedName) {
        this.overridedName = overridedName;
        return this;
    }

    private Type parseType(Class aClass) {
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

    @Override
    public Optional<String> getBackgroundColor() {
        return Optional.ofNullable(backgroundColor);
    }

    @Override
    public Optional<String> getBorderColor() {
        return Optional.ofNullable(borderColor);
    }

    public JavaClazz setBackgroundColor(String color) {
        this.backgroundColor = color;
        return this;
    }

    public JavaClazz setBorderColor(String color) {
        this.borderColor = color;
        return this;
    }

    public static JavaClazz from(Class aClass, ClassAttribute[] attributes, ClassMethod[] methods) {
        JavaClazz javaClass = new JavaClazz(aClass);
        javaClass.addAttributes(attributes);
        javaClass.addMethods(methods);
        return javaClass;
    }
}
