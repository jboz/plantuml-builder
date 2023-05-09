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
package ch.ifocusit.plantuml.classdiagram;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Stream;
import ch.ifocusit.plantuml.PlantUmlBuilder;
import ch.ifocusit.plantuml.classdiagram.model.Association;
import ch.ifocusit.plantuml.classdiagram.model.attribute.ClassAttribute;
import ch.ifocusit.plantuml.classdiagram.model.clazz.JavaClazz;
import ch.ifocusit.plantuml.classdiagram.model.method.ClassMethod;
import ch.ifocusit.plantuml.utils.ClassUtils;
import ch.ifocusit.plantuml.utils.PlantUmlUtils;

/**
 * Build class diagram from Class definition.
 *
 * @author Julien Boz
 */
@SuppressWarnings({"rawtypes", "unchecked", "unused"})
public abstract class AbstractClassDiagramBuilder implements LinkMaker {

    private Predicate<ClassAttribute> additionalFieldPredicate = a -> {
        return !a.getName().equals("ENUM$VALUES");
    };

    private static final List<String> DEFAULT_METHODS_EXCLUDED =
            List.of("equals", "hashCode", "toString");

    // by default java Object methods and getter/setter will be ignored
    private Predicate<ClassMethod> additionalMethodPredicate =
            m -> !DEFAULT_METHODS_EXCLUDED.contains(m.getName())
                    && ClassUtils.isNotGetterSetter(m.getMethod());

    protected final PlantUmlBuilder builder = new PlantUmlBuilder();

    protected final Set<JavaClazz> clazzes = new TreeSet<>();
    protected final Set<Association> detectedAssociations = new HashSet<>();


    protected LinkMaker linkMaker = this;

    private String header;
    private String footer;

    private final Map<Class, JavaClazz> cache = new HashMap<>();

    /**
     * Add not specified Object.
     */
    protected boolean withDependencies = false;

    protected boolean hideSelfLink = true;

    public AbstractClassDiagramBuilder() {}

    public <B extends AbstractClassDiagramBuilder> B setHeader(String header) {
        this.header = header;
        return (B) this;
    }

    public <B extends AbstractClassDiagramBuilder> B setFooter(String footer) {
        this.footer = footer;
        return (B) this;
    }

    public <B extends AbstractClassDiagramBuilder> B excludes(String... excludes) {
        // keep the corresponding fields
        Predicate<ClassAttribute> notMatchField = field -> Stream.of(excludes)
                .noneMatch(excl -> field.toStringAttribute().matches(excl));
        this.additionalFieldPredicate = this.additionalFieldPredicate.and(notMatchField);

        // keep the corresponding fields
        Predicate<ClassMethod> notMatchMethod = field -> Stream.of(excludes)
                .noneMatch(excl -> field.toStringMethod().matches(excl));
        this.additionalMethodPredicate = this.additionalMethodPredicate.and(notMatchMethod);
        return (B) this;
    }

    public <B extends AbstractClassDiagramBuilder> B addFieldPredicate(
            Predicate<ClassAttribute> predicate) {
        this.additionalFieldPredicate = this.additionalFieldPredicate.and(predicate);
        return (B) this;
    }

    public <B extends AbstractClassDiagramBuilder> B addMethodPredicate(
            Predicate<ClassMethod> predicate) {
        this.additionalMethodPredicate = this.additionalMethodPredicate.and(predicate);
        return (B) this;
    }

    public <B extends AbstractClassDiagramBuilder> B withLinkMaker(LinkMaker linkMaker) {
        this.linkMaker = linkMaker;
        return (B) this;
    }

    public String build() {
        // parse classes repository
        // extract java classes definitions
        readClasses();
        // from java classes, detect associations
        detectAssociations();
        // generate diagram from configuration
        builder.start();
        builder.appendHeader(header);
        addPackages(); // add package definition
        addTypes(); // add types definition
        addAssociations(); // then add their associations
        builder.appendFooter(footer);
        builder.end();
        return builder.build();
    }

    public abstract void addPackages();

    public abstract void detectAssociations();

    public boolean hideFields(JavaClazz javaClazz) {
        return PlantUmlUtils.hideFields(javaClazz, header)
                || PlantUmlUtils.hideFields(javaClazz, footer);
    }

    public boolean hideMethods(JavaClazz javaClazz) {
        return PlantUmlUtils.hideMethods(javaClazz, header)
                || PlantUmlUtils.hideMethods(javaClazz, footer);
    }

    public abstract void readClasses();

    public void addTypes() {
        clazzes.forEach(builder::addType);
    }

    public Predicate<ClassAttribute> filterFields() {
        return additionalFieldPredicate;
    }

    public Predicate<ClassMethod> filterMethods() {
        return additionalMethodPredicate;
    }

    public void addAssociations() {
        detectedAssociations.stream().sorted().forEach(builder::addAssociation);
    }

    public <B extends AbstractClassDiagramBuilder> B withDependencies(boolean flag) {
        withDependencies = flag;
        return (B) this;
    }

    public <B extends AbstractClassDiagramBuilder> B hideSelfLink() {
        hideSelfLink = true;
        return (B) this;
    }

    public <B extends AbstractClassDiagramBuilder> B showSelfLink() {
        hideSelfLink = false;
        return (B) this;
    }

    public <B extends AbstractClassDiagramBuilder> B withDependencies() {
        return withDependencies(true);
    }

}
