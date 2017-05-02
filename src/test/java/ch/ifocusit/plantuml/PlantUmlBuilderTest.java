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

import static org.fest.assertions.Assertions.assertThat;

public class PlantUmlBuilderTest {
    @Test
    public void build() throws Exception {

        String expected = "interface Vehicule\r\n" +
                "\r\n" +
                "class Car {\r\n" +
                "  brand : String\r\n" +
                "  wheels : Collection<Wheel>\r\n" +
                "}\r\n" +
                "\r\n" +
                "class Price {\r\n" +
                "  amount : BigDecimal\r\n" +
                "  devise : Devise\r\n" +
                "}\r\n" +
                "\r\n" +
                "class Wheel\r\n" +
                "\r\n" +
                "enum Devise {\r\n" +
                "  CHF\r\n" +
                "  EUR\r\n" +
                "  USD\r\n" +
                "}\r\n" +
                "\r\n" +
                "Vehicule <|-- Car\r\n" +
                "Car --> Price : price\r\n" +
                "Car --> \"*\" Wheel : wheels\r\n" +
                "Price --> Devise\r\n";

        // tag::createSimple[]
        String diagram = new PlantUmlBuilder()
                // classes
                .addType("Vehicule", Type.INTERFACE)
                .addType("Car", Type.CLASS,
                        new SimpleAttribut("brand", "String"),
                        new SimpleAttribut("wheels", "Collection<Wheel>")
                )
                .addType("Price", Type.CLASS, new SimpleAttribut("amount", "BigDecimal"), new SimpleAttribut("devise", "Devise"))
                .addType("Wheel", Type.CLASS)
                .addType("Devise", Type.ENUM,
                        new SimpleAttribut("CHF", null),
                        new SimpleAttribut("EUR", null),
                        new SimpleAttribut("USD", null)
                )
                // associations
                .addAssociation("Vehicule", "Car", Association.INHERITANCE)
                .addAssociation("Car", "Price", Association.DIRECTION, "price")
                .addAssociation("Car", "Wheel", Association.DIRECTION, "wheels", null, "*")
                .addAssociation("Price", "Devise", Association.DIRECTION)
                .build();
        // end::createSimple[]

        assertThat(diagram).isEqualTo(expected);
    }

}