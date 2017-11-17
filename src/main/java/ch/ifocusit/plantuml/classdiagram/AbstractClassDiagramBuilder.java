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
package ch.ifocusit.plantuml.classdiagram;

import ch.ifocusit.plantuml.PlantUmlBuilder;
import ch.ifocusit.plantuml.classdiagram.model.Association;
import ch.ifocusit.plantuml.classdiagram.model.ClassMember;
import ch.ifocusit.plantuml.classdiagram.model.Package;
import ch.ifocusit.plantuml.classdiagram.model.attribute.ClassAttribute;
import ch.ifocusit.plantuml.classdiagram.model.attribute.MethodAttribute;
import ch.ifocusit.plantuml.classdiagram.model.clazz.Clazz;
import ch.ifocusit.plantuml.classdiagram.model.clazz.JavaClazz;
import ch.ifocusit.plantuml.classdiagram.model.method.ClassMethod;
import ch.ifocusit.plantuml.utils.ClassUtils;
import ch.ifocusit.plantuml.utils.PlantUmlUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.reflect.ClassPath;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static ch.ifocusit.plantuml.classdiagram.model.Association.AssociationType.*;
import static ch.ifocusit.plantuml.classdiagram.model.Cardinality.MANY;
import static ch.ifocusit.plantuml.classdiagram.model.Cardinality.NONE;
import static ch.ifocusit.plantuml.utils.ClassUtils.DOLLAR;
import static org.apache.commons.lang3.ClassUtils.getAllInterfaces;
import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * Build class diagram from Class definition.
 *
 * @author Julien Boz
 */
public abstract class AbstractClassDiagramBuilder implements LinkMaker {

    private Predicate<ClassAttribute> additionalFieldPredicate = a -> true; // always true by default

    private static final List<String> DEFAULT_METHODS_EXCLUDED = Lists.newArrayList("equals", "hashCode", "toString");

    // by default java Object methods and getter/setter will be ignored
    private Predicate<ClassMethod> additionalMethodPredicate = m -> !DEFAULT_METHODS_EXCLUDED.contains(m.getName()) && ClassUtils.isNotGetterSetter(m.getMethod());

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

    public AbstractClassDiagramBuilder() {
    }

    public AbstractClassDiagramBuilder setHeader(String header) {
        this.header = header;
        return this;
    }

    public AbstractClassDiagramBuilder setFooter(String footer) {
        this.footer = footer;
        return this;
    }

    public AbstractClassDiagramBuilder excludes(String... excludes) {
        // keep the corresponding fields
        Predicate<ClassAttribute> notMatchField = field -> Stream.of(excludes).noneMatch(excl -> field.toStringAttribute().matches(excl));
        this.additionalFieldPredicate = this.additionalFieldPredicate.and(notMatchField);

        // keep the corresponding fields
        Predicate<ClassMethod> notMatchMethod = field -> Stream.of(excludes).noneMatch(excl -> field.toStringMethod().matches(excl));
        this.additionalMethodPredicate = this.additionalMethodPredicate.and(notMatchMethod);
        return this;
    }

    public AbstractClassDiagramBuilder addFieldPredicate(Predicate<ClassAttribute> predicate) {
        this.additionalFieldPredicate = this.additionalFieldPredicate.and(predicate);
        return this;
    }

    public AbstractClassDiagramBuilder addMethodPredicate(Predicate<ClassMethod> predicate) {
        this.additionalMethodPredicate = this.additionalMethodPredicate.and(predicate);
        return this;
    }

    public AbstractClassDiagramBuilder withLinkMaker(LinkMaker linkMaker) {
        this.linkMaker = linkMaker;
        return this;
    }

    public String build() {
        // parse classes repository
        // extract java classes definitions
        readClasses();
        // from java classes, detect associations
        detectAssociations();
        // generate diagram from configuration
        builder.start();
        builder.appendPart(header);
        addPackages(); // add package definition
        addTypes(); // add types definition
        addAssociations(); // then add their associations
        builder.appendPart(footer);
        builder.end();
        return builder.build();
    }

    protected abstract void addPackages();

    protected abstract void detectAssociations();

    protected boolean hideFields(JavaClazz javaClazz) {
        return PlantUmlUtils.hideFields(javaClazz, header) || PlantUmlUtils.hideFields(javaClazz, footer);
    }

    protected boolean hideMethods(JavaClazz javaClazz) {
        return PlantUmlUtils.hideMethods(javaClazz, header) || PlantUmlUtils.hideMethods(javaClazz, footer);
    }

    protected abstract void readClasses();

    protected void addTypes() {
        clazzes.forEach(builder::addType);
    }

    protected Predicate<ClassAttribute> filterFields() {
        return additionalFieldPredicate;
    }

    protected Predicate<ClassMethod> filterMethods() {
        return additionalMethodPredicate;
    }

    protected void addAssociations() {
        detectedAssociations.stream().sorted().forEach(builder::addAssociation);
    }

    public AbstractClassDiagramBuilder withDependencies(boolean flag) {
        withDependencies = flag;
        return this;
    }

    public AbstractClassDiagramBuilder hideSelfLink() {
        hideSelfLink = true;
        return this;
    }

    public AbstractClassDiagramBuilder showSelfLink() {
        hideSelfLink = false;
        return this;
    }

    public AbstractClassDiagramBuilder withDependencies() {
        return withDependencies(true);
    }

}
