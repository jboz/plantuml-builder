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
package ch.ifocusit.plantuml;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import static ch.ifocusit.plantuml.Association.DIRECTION;
import static org.apache.commons.lang3.StringUtils.SPACE;

/**
 * Diagram helper.
 *
 * @author Julien Boz
 */
public class PlantUmlBuilder {
    private static final String STARTUML = "@startuml";
    private static final String ENDUML = "@enduml";
    public static final String SEMICOLON = ":";
    public static final String BRACE_OPEN = "{";
    public static final String BRACE_CLOSE = "}";
    public static final String TAB = SPACE + SPACE;
    public static final String NEWLINE = System.getProperty("line.separator");
    public static final String QUOTE = "\"";

    private final StringBuilder content = new StringBuilder();

    public PlantUmlBuilder start() {
        content.append(STARTUML).append(NEWLINE).append(NEWLINE);
        return this;
    }

    public PlantUmlBuilder end() {
        content.append(NEWLINE).append(ENDUML);
        return this;
    }

    public String build() {
        return content.toString();
    }

    //*********************************************************************************
    // TYPE
    //*********************************************************************************

    public PlantUmlBuilder addType(String name, Type type, Attribute... attributes) {
        content.append(type).append(SPACE).append(name);
        if (attributes.length > 0) {
            content.append(SPACE).append(BRACE_OPEN).append(NEWLINE);
            for (Attribute attribute : attributes) {
                content.append(TAB).append(attribute.getName());
                if (StringUtils.isNotBlank(attribute.getTypeString())) {
                    content.append(SPACE).append(SEMICOLON).append(SPACE).append(attribute.getTypeString());
                }
                content.append(NEWLINE);
            }
            content.append(BRACE_CLOSE);
        }
        content.append(NEWLINE).append(NEWLINE);
        return this;
    }

    //*********************************************************************************
    // TYPE ASSOCIATION
    //*********************************************************************************

    public PlantUmlBuilder addAssociation(String aName, String bName) {
        return addAssociation(aName, bName, DIRECTION, null);
    }

    public PlantUmlBuilder addAssociation(String aName, String bName, Association assoc) {
        return addAssociation(aName, bName, assoc, null);
    }

    public PlantUmlBuilder addAssociation(String aName, String bName, String label) {
        return addAssociation(aName, bName, DIRECTION, label);
    }

    public PlantUmlBuilder addAssociation(String aName, String bName, Association assoc, String label) {
        return addAssociation(aName, bName, assoc, label, null, null);
    }

    public PlantUmlBuilder addAssociation(String aName, String bName, Association assoc, String label, String aCardinality, String bCardinality) {
        Validate.notBlank(aName, "Class a name is mandatory");
        Validate.notBlank(bName, "Class b name is mandatory");
        Validate.notNull(assoc, "Association type is mandatory");

        content.append(aName);
        if (StringUtils.isNotBlank(aCardinality)) {
            content.append(SPACE).append(QUOTE).append(aCardinality).append(QUOTE);
        }
        content.append(SPACE).append(assoc).append(SPACE);
        if (StringUtils.isNotBlank(bCardinality)) {
            content.append(QUOTE).append(bCardinality).append(QUOTE).append(SPACE);
        }
        content.append(bName);
        if (StringUtils.isNotBlank(label)) {
            content.append(SPACE).append(SEMICOLON).append(SPACE).append(label);

        }
        content.append(NEWLINE);
        return this;
    }
}
