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
package ch.ifocusit.plantuml.classdiagram.model;

import java.util.Optional;
import org.apache.commons.lang3.Validate;

/**
 * @author Julien Boz
 */
public class Package {

    private String name;
    private Type type = Type.Folder;
    private Optional<String> color = Optional.empty();

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public Optional<String> getColor() {
        return color;
    }

    public void validate() {
        Validate.notNull(getName(), "Package name must be defined !");
        Validate.notNull(getType(),
                String.format("Package '%s' tpye must be defined !", getName()));
    }

    public static Package from(java.lang.Package javaPkg) {
        Package p = new Package();
        p.name = javaPkg.getName();
        return p;
    }

    public static enum Type {
        Node, Rectangle, Folder, Frame, Cloud, Database;
    }

}
