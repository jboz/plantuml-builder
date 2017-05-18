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
package ch.ifocusit.plantuml.classdiagram.model.clazz;

import ch.ifocusit.plantuml.classdiagram.model.Link;
import ch.ifocusit.plantuml.classdiagram.model.attribute.Attribute;
import ch.ifocusit.plantuml.utils.ClassUtils;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Julien Boz
 */
public class JavaClazz implements Clazz {

    private final Class<?> relatedClass;
    private Optional<String> overridedName;
    private Optional<Link> link;
    private List<Attribute> attributes = new ArrayList<>();
    private String backgroundColor;
    private String borderColor;

    public JavaClazz(Class<?> relatedClass) {
        this.relatedClass = relatedClass;
    }

    public String getName() {
        return overridedName.orElse(ClassUtils.getSimpleName(relatedClass));
    }

    public Type getType() {
        return parseType(relatedClass);
    }

    public Optional<Link> getLink() {
        return link;
    }

    public JavaClazz setLink(Optional<Link> link) {
        this.link = link;
        return this;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public void addAttributes(Attribute... attributes) {
        for (Attribute attribute : attributes) {
            this.attributes.add(attribute);
        }
    }

    public JavaClazz setOverridedName(String overridedName) {
        this.overridedName = Optional.ofNullable(overridedName);
        return this;
    }

    @Override
    public Optional<List<String>> getStereotypes() {
        return Optional.empty();
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

    public static JavaClazz from(Class aClass, Attribute... attributes) {
        JavaClazz javaClass = new JavaClazz(aClass);
        javaClass.addAttributes(attributes);
        return javaClass;
    }
}
