/*
 * Plantuml builder
 *
 * Copyright (C) 2024 Focus ITCopyright (C) 2024 Focus IT
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package ch.ifocusit.plantuml;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.Objects;

import ch.ifocusit.example.domain.model.Driver;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import ch.ifocusit.example.domain.model.Devise;
import ch.ifocusit.example.domain.model.Price;
import ch.ifocusit.example.domain.model.material.Car;
import ch.ifocusit.example.domain.model.material.Machine;
import ch.ifocusit.example.domain.model.material.Vehicule;
import ch.ifocusit.example.domain.model.material.Wheel;
import ch.ifocusit.example.domain.service.AccessDataService;
import ch.ifocusit.plantuml.classdiagram.ClassDiagramBuilder;
import ch.ifocusit.plantuml.classdiagram.NamesMapper;
import ch.ifocusit.plantuml.utils.ClassUtils;

/**
 * @author Julien Boz
 */
public class ClassDiagramBuilderTest {

        private static final String CR = PlantUmlBuilder.NEWLINE;

        @Test
        public void buildShouldGenerateDiagram() throws Exception {
                String expected = IOUtils.toString(
                                Objects.requireNonNull(this.getClass()
                                                .getResourceAsStream("/domain-diagram.plantuml")),
                                Charset.defaultCharset());

                String diagram = new ClassDiagramBuilder()
                                .<ClassDiagramBuilder>excludes(".*\\.ignored", "Machine")
                                .addPackage(Vehicule.class.getPackage())
                                .addClasses(Vehicule.class, Car.class, Driver.class, Price.class, Wheel.class,
                                                Devise.class)
                                .build();

                assertThat(diagram).isEqualTo(expected);
        }

        @Test
        public void buildShouldGenerateDiagramFromAggregateMaster() throws Exception {
                String expected = IOUtils.toString(
                                Objects.requireNonNull(this.getClass().getResourceAsStream(
                                                "/domain-aggregate-diagram.plantuml")),
                                Charset.defaultCharset());

                String diagram = new ClassDiagramBuilder()
                                .<ClassDiagramBuilder>excludes(".*\\.ignored").addClasses(Car.class)
                                .withDependencies().build();

                assertThat(diagram).isEqualTo(expected);
        }

        @Test
        public void buildShouldGenerateDiagramWithDepth() throws Exception {
                String expected = IOUtils.toString(
                                Objects.requireNonNull(this.getClass()
                                                .getResourceAsStream("/service-diagram.plantuml")),
                                Charset.defaultCharset());

                String diagram = new ClassDiagramBuilder().addClasses(AccessDataService.class)
                                .withDependencies().setHeader("Service diagram").build();

                assertThat(diagram).isEqualTo(expected);
        }

        @Test
        public void buildShouldExportOnlyAnnotatedClassAndField() {
                String diagram = new ClassDiagramBuilder().excludes(".*\\.ignored")
                                // only annotated
                                .addFieldPredicate(attribute -> attribute.getField()
                                                .isAnnotationPresent(Machine.class))
                                // no method
                                .<ClassDiagramBuilder>addMethodPredicate(classMethod -> false)
                                .addClasses(Car.class).build();

                assertThat(diagram).isEqualTo("@startuml" + CR + CR + "class \"Car\" {" + CR
                                + "  brand : String" + CR + "  model : String" + CR
                                + "  wheels : Collection<Wheel>" + CR + "}" + CR + CR + CR
                                + "@enduml");
        }

        @Test
        public void testOverrideNames() {
                String diagram = new ClassDiagramBuilder().excludes(".*\\.ignored")
                                // only annotated
                                .addFieldPredicate(attribute -> attribute.getField()
                                                .isAnnotationPresent(Machine.class))
                                // no method
                                .<ClassDiagramBuilder>addMethodPredicate(classMethod -> false)
                                .addClasses(Car.class).withNamesMapper(new NamesMapper() {
                                        @Override
                                        public String getClassName(Class aClass) {
                                                return "domain." + ClassUtils.getSimpleName(aClass);
                                        }

                                        @Override
                                        public String getFieldName(Field field) {
                                                return "attr." + field.getName();
                                        }
                                }).build();

                assertThat(diagram).isEqualTo("@startuml" + CR + CR + "class \"domain.Car\" {" + CR
                                + "  attr.brand : String" + CR + "  attr.model : String" + CR
                                + "  attr.wheels : Collection<Wheel>" + CR + "}" + CR + CR + CR
                                + "@enduml");
        }
}
