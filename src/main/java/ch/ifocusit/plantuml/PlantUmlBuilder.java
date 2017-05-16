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

import ch.ifocusit.plantuml.classdiagram.model.Association;
import ch.ifocusit.plantuml.classdiagram.model.Attribute;
import ch.ifocusit.plantuml.classdiagram.model.Clazz;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.util.stream.Collectors;

import static ch.ifocusit.plantuml.classdiagram.model.Association.DIRECTION;
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
    public static final String STEREOTYPE_OPEN = "<<";
    public static final String STEREOTYPE_CLOSE = ">>";
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

    public PlantUmlBuilder addType(Clazz javaClass) {
        content.append(javaClass.getType()).append(SPACE).append(javaClass.getName());
        if (!javaClass.getAttributes().isEmpty()) {
            content.append(SPACE);
            // class link
            javaClass.getLink().ifPresent(link -> content.append(link.toString()).append(SPACE));
            // stereotype
            javaClass.getStereotypes().ifPresent(stereotypes -> content
                    .append(STEREOTYPE_OPEN)
                    .append(stereotypes.stream().collect(Collectors.joining(", ")))
                    .append(STEREOTYPE_CLOSE)
                    .append(SPACE));
            // class color
            javaClass.getBackgroundColor().ifPresent(color -> content.append("#").append(color).append(SPACE));
            content.append(BRACE_OPEN).append(NEWLINE);
            for (Attribute attribute : javaClass.getAttributes()) {
                // name
                content.append(TAB).append(attribute.getName());
                // type
                attribute.getType().ifPresent(type -> content.append(SPACE).append(SEMICOLON).append(SPACE).append(type));
                // field link
                attribute.getLink().ifPresent(link -> content.append(SPACE).append(link.toString()));
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
        Validate.notBlank(aName, "JavaClass a name is mandatory");
        Validate.notBlank(bName, "JavaClass b name is mandatory");
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
