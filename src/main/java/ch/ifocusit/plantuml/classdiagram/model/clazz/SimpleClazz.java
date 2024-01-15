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

import ch.ifocusit.plantuml.classdiagram.model.attribute.Attribute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Julien Boz
 */
public class SimpleClazz implements Clazz {

    private String name;
    private Type type;
    private final List<Attribute> attributes = new ArrayList<>();

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public List<Attribute> getAttributes() {
        return attributes;
    }

    public static SimpleClazz create(String name, Type type, Attribute... attributes) {
        SimpleClazz c = new SimpleClazz();
        c.name = name;
        c.type = type;
        c.attributes.addAll(Arrays.asList(attributes));
        return c;
    }
}
