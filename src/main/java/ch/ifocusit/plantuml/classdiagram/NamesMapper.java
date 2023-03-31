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

import static org.apache.commons.lang3.ClassUtils.getSimpleName;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author Julien Boz
 */
public interface NamesMapper {

    /**
     * @return the class name as shown in the diagram
     */
    default String getClassName(Class aClass) {
        return getSimpleName(aClass);
    }


    /**
     * @return the attribute name as shown in the diagram
     */
    default String getFieldName(Field field) {
        return field.getName();
    }


    /**
     * @return the method name as shown in the diagram
     */
    default String getMethodName(Method method) {
        return method.getName();
    }

}
