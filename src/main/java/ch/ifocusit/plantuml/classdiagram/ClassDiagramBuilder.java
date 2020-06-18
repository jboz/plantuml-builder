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

import ch.ifocusit.plantuml.classdiagram.model.Association;
import ch.ifocusit.plantuml.classdiagram.model.ClassMember;
import ch.ifocusit.plantuml.classdiagram.model.Package;
import ch.ifocusit.plantuml.classdiagram.model.attribute.ClassAttribute;
import ch.ifocusit.plantuml.classdiagram.model.attribute.MethodAttribute;
import ch.ifocusit.plantuml.classdiagram.model.clazz.Clazz;
import ch.ifocusit.plantuml.classdiagram.model.clazz.JavaClazz;
import ch.ifocusit.plantuml.classdiagram.model.method.ClassMethod;
import ch.ifocusit.plantuml.utils.ClassUtils;
import com.google.common.collect.Sets;
import com.google.common.reflect.ClassPath;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
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
public class ClassDiagramBuilder extends AbstractClassDiagramBuilder implements NamesMapper {

    private final Set<java.lang.Package> packages = new LinkedHashSet<>();
    private final Set<Class> classesRepository = new LinkedHashSet<>();
    private Predicate<ClassAttribute> additionalFieldPredicate = a -> true; // always true by default

    private NamesMapper namesMapper = this;

    public static void writeDiagramToFile(String filename, Class<?>... classes) throws IOException {
        writeDiagramToFile(new File(filename), classes);
    }

    public static void writeDiagramToFile(File file, Class<?>... classes) throws IOException {
        String diagram = new ClassDiagramBuilder().addClasses(classes).build();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(diagram);
        }
    }

    public static void writeDiagramToFile(Path path, Class<?>... classes) throws IOException {
        String diagram = new ClassDiagramBuilder().addClasses(classes).build();
        Files.write(path, diagram.getBytes());
    }

    public ClassDiagramBuilder() {
    }

    public ClassDiagramBuilder addClasses(Iterable<Class> classes) {
        classes.forEach(this.classesRepository::add);
        return this;
    }

    public ClassDiagramBuilder addClasses(Class... classes) {
        this.classesRepository.addAll(Arrays.asList(classes));
        return this;
    }

    public ClassDiagramBuilder addPackage(java.lang.Package... packages) {
        this.packages.addAll(Arrays.asList(packages));
        return this;
    }

    public ClassDiagramBuilder withNamesMapper(NamesMapper namesMapper) {
        this.namesMapper = namesMapper;
        return this;
    }

    public void addPackages() {
        packages.stream().forEach(pkg -> {
            try {
                ClassPath classPath = ClassPath.from(Thread.currentThread().getContextClassLoader());
                Clazz[] classes = classPath.getTopLevelClasses(pkg.getName()).stream()
                        .map(ClassPath.ClassInfo::load)
                        .map(this::createJavaClass)
                        .sorted()
                        .toArray(Clazz[]::new);
                builder.addPackage(Package.from(pkg), classes);
            } catch (IOException e) {
                throw new IllegalStateException("Cannot load classesRepository from package " + pkg, e);
            }

        });
    }

    public boolean canAppearsInDiagram(Class aClass) {
        return !"void".equals(aClass.getName()) && !aClass.getName().startsWith("java.") && (withDependencies || classesRepository.contains(aClass));
    }

    public void detectAssociations() {
        // browse each defined classesRepository
        clazzes.forEach(javaClazz -> {
            // add inheritance associations
            Stream.concat(Stream.of(javaClazz.getRelatedClass().getSuperclass()), getAllInterfaces(javaClazz.getRelatedClass()).stream())
                    .filter(Objects::nonNull)
                    // exclude class if not in repository
                    .filter(this::canAppearsInDiagram)
                    // create an association between current class and it's parent
                    .forEach(hierarchicalClass -> {
                        ClassAssociation assoc = new ClassAssociation();
                        assoc.classB = javaClazz.getRelatedClass();
                        assoc.classA = hierarchicalClass;
                        assoc.setbName(namesMapper.getClassName(assoc.classB));
                        assoc.setbCardinality(NONE);
                        assoc.setaName(namesMapper.getClassName(assoc.classA));
                        assoc.setaCardinality(NONE);
                        assoc.setLabel(EMPTY);
                        assoc.setType(INHERITANCE);

                        detectedAssociations.add(assoc);
                    });

            // no field association if fields are hidden
            if (!hideFields(javaClazz)) {
                javaClazz.getAttributes().stream()
                        .filter(attribute -> !attribute.getField().isEnumConstant())
                        .forEach(classAttribute -> {
                            classAttribute.getConcernedTypes().stream()
                                    .filter(this::canAppearsInDiagram)
                                    .forEach(classToLinkWith -> {
                                        addOrUpdateAssociation(javaClazz.getRelatedClass(), classToLinkWith, classAttribute);
                                    });
                        });
            }

            // no method association if methods are hidden
            if (!hideMethods(javaClazz)) {
                javaClazz.getMethods().forEach(classMethod -> {
                    classMethod.getParameters().ifPresent(methodAttributes -> {
                        Stream.of(methodAttributes)
                                .forEach(methodAttribute -> {
                                    methodAttribute.getConcernedTypes().stream()
                                            .filter(this::canAppearsInDiagram)
                                            .forEach(classToLinkWith -> {
                                                addOrUpdateAssociation(javaClazz.getRelatedClass(), classToLinkWith, methodAttribute);
                                            });
                                });
                    });
                    classMethod.getConcernedReturnedTypes().stream()
                            .filter(this::canAppearsInDiagram)
                            .forEach(classToLinkWith -> {
                                addOrUpdateAssociation(javaClazz.getRelatedClass(), classToLinkWith, classMethod);
                            });
                });
            }
        });
    }

    private void addOrUpdateAssociation(Class originClass, Class classToLinkWith, ClassMember classMember) {

        // hide inner link
        if (hideSelfLink && originClass.equals(classToLinkWith)) {
            return; // do not add this link
        }

        // look for an existing association
        Optional<Association> existing = detectedAssociations.stream()
                .filter(assoc -> ((ClassAssociation) assoc).concern(originClass, classToLinkWith))
                .findFirst();

        Class typeWithGeneric = classMember.getType();

        String label = "use";
        if (classMember instanceof MethodAttribute) {
            label += classMember.getName().startsWith("arg") ? EMPTY : " as " + classMember.getName();
        } else if (classMember instanceof ClassAttribute) {
            label = classMember.getName();
        }

        if (existing.isPresent()) {
            if (((ClassAssociation) existing.get()).isNoSameOrigin(originClass))
                // do not add the second attribut to the association collection
                // mark attribute as bidirectional
                ((ClassAssociation) existing.get()).setBidirectional();
            if (classMember instanceof ClassAttribute) {
                // update cardinality
                existing.get().setaCardinality(ClassUtils.isCollection(typeWithGeneric) ? MANY : NONE);
                // change name
                existing.get().setLabel(existing.get().getLabel() + "/" + label);
            }
        } else {
            // add association with this class
            ClassAssociation assoc = new ClassAssociation();
            assoc.classA = originClass;
            assoc.setaName(namesMapper.getClassName(assoc.classA));
            assoc.setaCardinality(ClassUtils.isCollection(assoc.classA) ? MANY : NONE);
            assoc.classB = classToLinkWith;
            assoc.setbName(namesMapper.getClassName(assoc.classB));
            assoc.setbCardinality(ClassUtils.isCollection(typeWithGeneric) ? MANY : NONE);
            assoc.setLabel(label);
            assoc.setType(DIRECTION);
            detectedAssociations.add(assoc);
        }
    }

    public void readClasses() {
        // add all classesRepository definition
        // readFields will manage field type definition, exclusions, ...
        classesRepository.forEach(clazz -> clazzes.add(createJavaClass(clazz)));
    }

    public void addTypes() {
        clazzes.forEach(builder::addType);
    }

    public JavaClazz createJavaClass(Class clazz) {
        return JavaClazz.from(clazz, readFields(clazz), readMethods(clazz))
                .setOverridedName(namesMapper.getClassName(clazz))
                .setLink(linkMaker.getClassLink(clazz));
    }

    public ClassMethod[] readMethods(Class aClass) {
        return Stream.of(aClass.getDeclaredMethods())
                // only public and non static methods
                .filter(method -> !Modifier.isStatic(method.getModifiers()) && Modifier.isPublic(method.getModifiers()))
                .map(this::createClassMethod)
                // excludes specific fields
                .filter(filterMethods())
                .sorted()
                .toArray(ClassMethod[]::new);
    }

    public ClassMethod createClassMethod(java.lang.reflect.Method method) {
        ClassMethod classMethod = new ClassMethod(method, namesMapper.getMethodName(method));
        classMethod.setLink(linkMaker.getMethodLink(method));

        return classMethod;
    }

    public ClassAttribute[] readFields(Class aClass) {
        return Stream.of(aClass.getDeclaredFields())
                // exclude inner class
                .filter(field -> !field.getName().startsWith(DOLLAR))
                // exclude static fields
                .filter(field -> field.getDeclaringClass().isEnum() || !Modifier.isStatic(field.getModifiers()))
                .map(this::createClassAttribute)
                // excludes specific fields
                .filter(filterFields())
                .toArray(ClassAttribute[]::new);
    }

    public ClassAttribute createClassAttribute(Field field) {
        ClassAttribute attribute = new ClassAttribute(field, namesMapper.getFieldName(field));
        attribute.setLink(linkMaker.getFieldLink(field));
        return attribute;
    }

    private static class ClassAssociation extends Association implements Comparable<ClassAssociation> {
        private Class classA;
        private Class classB;

        public void setBidirectional() {
            type = BI_DIRECTION;
        }

        public boolean concern(Class otherA, Class otherB) {
            return Sets.intersection(Sets.newHashSet(classA, classB), Sets.newHashSet(otherA, otherB)).size() == 2;
        }

        @Override
        public int compareTo(final ClassAssociation o) {
            return getKey().compareTo(o.getKey());
        }

        private String getKey() {
            return aName + bName;
        }

        public boolean isNoSameOrigin(final Class initialClass) {
            return !classA.equals(initialClass);
        }
    }
}
