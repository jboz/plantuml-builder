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
package ch.ifocusit.plantuml;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import ch.ifocusit.plantuml.classdiagram.model.Association.AssociationType;
import ch.ifocusit.plantuml.classdiagram.model.Cardinality;
import ch.ifocusit.plantuml.classdiagram.model.attribute.SimpleAttribute;
import ch.ifocusit.plantuml.classdiagram.model.clazz.Clazz;
import ch.ifocusit.plantuml.classdiagram.model.clazz.SimpleClazz;

public class PlantUmlBuilderTest {

    private static final String CR = PlantUmlBuilder.NEWLINE;

    @Test
    public void buildInterface() {
        final String diagram = new PlantUmlBuilder()
                .addType(SimpleClazz.create("Vehicule", Clazz.Type.INTERFACE))
                .build();
        assertThat(diagram).isEqualTo("interface \"Vehicule\"" + CR + CR);
    }

    @Test
    public void buildClassNoField() {
        final String diagram = new PlantUmlBuilder()
                .addType(SimpleClazz.create("Wheel", Clazz.Type.CLASS)).build();
        assertThat(diagram).isEqualTo("class \"Wheel\"" + CR + CR);
    }

    @Test
    public void buildClassWithManyFields() {
        final String diagram = new PlantUmlBuilder()
                .addType(SimpleClazz.create("Car", Clazz.Type.CLASS,
                        new SimpleAttribute("brand", "String"),
                        new SimpleAttribute("wheels", "Collection<Wheel>")))
                .build();

        assertThat(diagram).isEqualTo("class \"Car\" {" + CR + "  brand : String" + CR
                + "  wheels : Collection<Wheel>" + CR + "}" + CR + CR);
    }

    @Test
    public void buildEnum() {
        final String diagram = new PlantUmlBuilder().addType(SimpleClazz.create("Devise",
                        Clazz.Type.ENUM, new SimpleAttribute("CHF", null),
                        new SimpleAttribute("EUR", null), new SimpleAttribute("USD", null)))
                .build();

        assertThat(diagram).isEqualTo("enum \"Devise\" {" + CR + "  CHF" + CR + "  EUR" + CR
                + "  USD" + CR + "}" + CR + CR);
    }

    @Test
    public void buildAssociations() throws Exception {
        String diagram = new PlantUmlBuilder()
                .addAssociation("Vehicule", "Car", AssociationType.INHERITANCE)
                .addAssociation("Car", "Price", AssociationType.DIRECTION, "price")
                .addAssociation("Car", "Wheel", AssociationType.DIRECTION, "wheels",
                        Cardinality.NONE, Cardinality.MANY)
                .addAssociation("Price", "Devise", AssociationType.DIRECTION)
                .build();

        assertThat(diagram).isEqualTo(
                "\"Vehicule\" <|-- \"Car\"" + CR + "\"Car\" --> \"Price\" : price"
                        + CR + "\"Car\" --> \"*\" \"Wheel\" : wheels" + CR
                        + "\"Price\" --> \"Devise\"" + CR);
    }
}
