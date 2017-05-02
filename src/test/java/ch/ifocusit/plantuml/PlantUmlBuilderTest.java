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

import ch.ifocusit.plantuml.classdiagram.SimpleAttribut;
import org.junit.Test;

import static org.fest.assertions.Assertions.*;

public class PlantUmlBuilderTest {

    private static final String CR = PlantUmlBuilder.NEWLINE;

    @Test
    public void buildInterface() {
        final String diagram = new PlantUmlBuilder().addType("Vehicule", Type.INTERFACE).build();
        assertThat(diagram).isEqualTo("interface Vehicule" + CR + CR);
    }

    @Test
    public void buildClassNoField() {
        final String diagram = new PlantUmlBuilder().addType("Wheel", Type.CLASS).build();
        assertThat(diagram).isEqualTo("class Wheel" + CR + CR);
    }

    @Test
    public void buildClassWithManyFields() {
        final String diagram = new PlantUmlBuilder()
                .addType("Car", Type.CLASS,
                        new SimpleAttribut("brand", "String"),
                        new SimpleAttribut("wheels", "Collection<Wheel>")
                ).build();

        assertThat(diagram).isEqualTo("class Car {" + CR +
                "  brand : String" + CR +
                "  wheels : Collection<Wheel>" + CR +
                "}" + CR + CR);
    }

    @Test
    public void buildEnum() {
        final String diagram = new PlantUmlBuilder()
                .addType("Devise", Type.ENUM,
                        new SimpleAttribut("CHF", null),
                        new SimpleAttribut("EUR", null),
                        new SimpleAttribut("USD", null)
                ).build();

        assertThat(diagram).isEqualTo("enum Devise {" + CR +
                "  CHF" + CR +
                "  EUR" + CR +
                "  USD" + CR +
                "}" + CR + CR);
    }

    @Test
    public void buildAssociations() throws Exception {
        String diagram = new PlantUmlBuilder()
                .addAssociation("Vehicule", "Car", Association.INHERITANCE)
                .addAssociation("Car", "Price", Association.DIRECTION, "price")
                .addAssociation("Car", "Wheel", Association.DIRECTION, "wheels", null, "*")
                .addAssociation("Price", "Devise", Association.DIRECTION)
                .build();

        assertThat(diagram).isEqualTo("Vehicule <|-- Car\r\n" +
                "Car --> Price : price\r\n" +
                "Car --> \"*\" Wheel : wheels\r\n" +
                "Price --> Devise" + CR);
    }
}