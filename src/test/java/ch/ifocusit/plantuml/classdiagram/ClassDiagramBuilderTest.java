/*
 * Plantuml builder
 *
 * Copyright (C) 2023 Focus IT
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
package ch.ifocusit.plantuml.classdiagram;

import ch.ifocusit.plantuml.PlantUmlBuilder;
import ch.ifocusit.plantuml.classdiagram.model.Link;
import ch.ifocusit.plantuml.test.helper.domain.Devise;
import ch.ifocusit.plantuml.test.helper.domain.Driver;
import ch.ifocusit.plantuml.test.helper.domain.Price;
import ch.ifocusit.plantuml.test.helper.domain.material.Car;
import ch.ifocusit.plantuml.test.helper.domain.material.Machine;
import ch.ifocusit.plantuml.test.helper.domain.material.Vehicule;
import ch.ifocusit.plantuml.test.helper.domain.material.Wheel;
import ch.ifocusit.plantuml.test.helper.service.AccessDataService;
import ch.ifocusit.plantuml.utils.ClassUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Julien Boz
 */
@SuppressWarnings("rawtypes")
class ClassDiagramBuilderTest {

    private static final String CR = PlantUmlBuilder.NEWLINE;

    @Test
    void buildShouldGenerateDiagram() throws Exception {
        String expected = IOUtils.toString(Objects.requireNonNull(this.getClass().getResourceAsStream("/domain-diagram.plantuml")), Charset.defaultCharset());

        // tag::createSimple[]
        String diagram =
                new ClassDiagramBuilder().<ClassDiagramBuilder>excludes(".*\\.ignored", "Machine")
                        .addPackage(Vehicule.class.getPackage()).addClasses(Vehicule.class,
                                Car.class, Driver.class, Price.class, Wheel.class, Devise.class)
                        .build();
        // end::createSimple[]

        assertThat(diagram).isEqualTo(expected);
    }

    @Test
    public void buildShouldGenerateDiagramFromAggregateMaster() throws Exception {
        String expected = IOUtils.toString(Objects.requireNonNull(this.getClass().getResourceAsStream("/domain-aggregate-diagram.plantuml")), Charset.defaultCharset());

        // tag::createSimple[]
        String diagram = new ClassDiagramBuilder()
                .<ClassDiagramBuilder>excludes(".*\\.ignored")
                .addClasses(Car.class)
                .withDependencies()
                .setStartOptions("!pragma layout smetana")
                .build();
        // end::createSimple[]

        assertThat(diagram).isEqualTo(expected);
    }

    @Test
    public void buildShouldGenerateDiagramWithDepth() throws Exception {
        String expected = IOUtils.toString(Objects.requireNonNull(this.getClass().getResourceAsStream("/service-diagram.plantuml")), Charset.defaultCharset());

        // tag::createFromOneClassWithDependencies[]
        String diagram = new ClassDiagramBuilder().addClasses(AccessDataService.class)
                .withDependencies()
                .setStartOptions("skinparam backgroundColor lightgray")
                .setHeader("class diagram type")
                .setTitle("Service diagram")
                .setFooter("page 1/1")
                .setEndOptions("show methods", "hide fields")
                .build();
        // end::createFromOneClassWithDependencies[]

        assertThat(diagram).isEqualTo(expected);
    }

    @Test
    public void buildShouldExportOnlyAnnotatedClassAndField() {
        String diagram = new ClassDiagramBuilder().excludes(".*\\.ignored")
                // only annotated
                .addFieldPredicate(attribute -> attribute.getField().isAnnotationPresent(Machine.class))
                // no method
                .<ClassDiagramBuilder>addMethodPredicate(classMethod -> false)
                .addClasses(Car.class)
                .build();

        assertThat(diagram).isEqualTo("@startuml" + CR + CR + "class \"Car\" {" + CR
                + "  brand : String" + CR + "  model : String" + CR + "  wheels : Collection<Wheel>"
                + CR + "}" + CR + CR + CR + "@enduml");
    }

    @Test
    public void testOverrideNames() {
        String diagram = new ClassDiagramBuilder().excludes(".*\\.ignored")
                // only annotated
                .addFieldPredicate(attribute -> attribute.getField().isAnnotationPresent(Machine.class))
                // no method
                .<ClassDiagramBuilder>addMethodPredicate(classMethod -> false)
                .addClasses(Car.class)
                .withNamesMapper(new NamesMapper() {
                    @Override
                    public String getClassName(Class aClass) {
                        return "domain." + ClassUtils.getSimpleName(aClass);
                    }

                    @Override
                    public String getFieldName(Field field) {
                        return "attr." + field.getName();
                    }
                })
                .build();

        assertThat(diagram).isEqualTo("@startuml" + CR + CR + "class \"domain.Car\" {" + CR
                + "  attr.brand : String" + CR + "  attr.model : String" + CR
                + "  attr.wheels : Collection<Wheel>" + CR + "}" + CR + CR + CR + "@enduml");
    }

    @Test
    public void testLinkRenderer() throws IOException {
        String expected = IOUtils.toString(Objects.requireNonNull(this.getClass().getResourceAsStream("/links.puml")), Charset.defaultCharset());

        String diagram = new ClassDiagramBuilder()
                .addClasses(Car.class, Driver.class)
                .withLinkMaker(new LinkMaker() {
                    @Override
                    public Optional<Link> getClassLink(Class aClass) {
                        String label = aClass.getSimpleName();
                        Link link = new Link();
                        link.setLabel(label);
                        link.setUrl("https://link.com/" + label.toLowerCase());
                        if (aClass.equals(Driver.class)) {
                            link.setTooltip("Taxi Driver");
                        }
                        return Optional.of(link);
                    }

                    @Override
                    public Optional<Link> getFieldLink(Field field) {
                        if (field.getName().equals("wheels") || field.getDeclaringClass().equals(Driver.class)) {
                            return Optional.empty();
                        }
                        Link link = new Link();
                        link.setUrl("https://link.com/car/" + field.getName().toLowerCase());
                        if (field.getName().equals("brand") ) {
                            link.setLabel("lien");
                        }
                        if (field.getName().equals("price") ) {
                            link.setTooltip("Show details");
                        }
                        if (field.getName().equals("model") ) {
                            link.setLabel("Car models");
                            link.setTooltip("Show all cars' models");
                        }
                        return Optional.of(link);
                    }
                })
                .excludes(".*\\.ignored")
                .build();

        assertThat(diagram).isEqualTo(expected);
    }
}
