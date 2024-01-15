/*-
 * Plantuml builder
 *
 * Copyright (C) 2024 Focus IT
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.Validate;
import ch.ifocusit.plantuml.classdiagram.model.Link;
import ch.ifocusit.plantuml.classdiagram.model.attribute.Attribute;
import ch.ifocusit.plantuml.classdiagram.model.method.Method;

/**
 * @author Julien Boz
 */
@SuppressWarnings("unused")
public interface Clazz extends Comparable<Clazz> {

    String getName();

    Type getType();

    default Optional<Link> getLink() {
        return Optional.empty();
    }

    List<? extends Attribute> getAttributes();

    default List<? extends Method> getMethods() {
        return new ArrayList<>();
    }

    default Optional<List<String>> getStereotypes() {
        return Optional.empty();
    }

    default Optional<String> getBackgroundColor() {
        return Optional.empty();
    }

    default Optional<String> getBorderColor() {
        return Optional.empty();
    }

    default void validate() {
        Validate.notNull(getName(), "Class name must be defined !");
        Validate.notNull(getType(), String.format("Class '%s' type must be defined !", getName()));
    }

    @Override
    default int compareTo(Clazz clazz) {
        return getName().compareTo(clazz.getName());
    }

    default boolean hasContent() {
        return !getAttributes().isEmpty() || !getMethods().isEmpty();
    }

    enum Type {
        INTERFACE("interface"), ENUM("enum"), CLASS("class"), ABSTRACT("abstract class");

        private final String name;

        Type(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
