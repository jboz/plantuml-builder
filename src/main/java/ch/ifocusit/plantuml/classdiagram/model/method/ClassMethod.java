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
package ch.ifocusit.plantuml.classdiagram.model.method;

import ch.ifocusit.plantuml.classdiagram.model.ClassMember;
import ch.ifocusit.plantuml.classdiagram.model.Link;
import ch.ifocusit.plantuml.classdiagram.model.attribute.MethodAttribute;
import ch.ifocusit.plantuml.utils.ClassUtils;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static ch.ifocusit.plantuml.utils.ClassUtils.getGenericTypes;
import static ch.ifocusit.plantuml.utils.ClassUtils.getSimpleName;

/**
 * @author Julien Boz
 */
public class ClassMethod implements ch.ifocusit.plantuml.classdiagram.model.method.Method, ClassMember {

    private final Method method;
    private final String methodName;
    private Optional<Link> link;

    public ClassMethod(Method method) {
        this(method, method.getName());
    }

    public ClassMethod(Method method, String methodName) {
        this.method = method;
        this.methodName = methodName;
    }

    @Override
    public Optional<String> getReturnTypeName() {
        return Optional.ofNullable(method.getReturnType().equals(Void.TYPE) ? null : getSimpleName(method.getGenericReturnType()));
    }

    @Override
    public Class getType() {
        return getMethod().getReturnType();
    }

    @Override
    public String getName() {
        return methodName;
    }

    @Override
    public Optional<MethodAttribute[]> getParameters() {
        return Optional.of(Stream.of(method.getParameters()).map(param -> new MethodAttribute(param)).toArray(MethodAttribute[]::new));
    }

    public Method getMethod() {
        return method;
    }

    public Class getDeclaringClass() {
        return method.getDeclaringClass();
    }

    private Class getMethodReturnType() {
        return method.getReturnType();
    }

    public Set<Class> getConcernedTypes() {
        return ClassUtils.getConcernedTypes(this.method);
    }

    public Set<Class> getConcernedReturnedTypes() {
        Set<Class> classes = new HashSet<>();
        // manage returned types
        classes.add(method.getReturnType());
        classes.addAll(getGenericTypes(method));
        return classes;
    }

    public String toStringMethod() {
        return method.getDeclaringClass().getName() + "." + method.getName();
    }

    public Optional<Link> getLink() {
        return link;
    }

    public ClassMethod setLink(Optional<Link> link) {
        this.link = link;
        return this;
    }
}
