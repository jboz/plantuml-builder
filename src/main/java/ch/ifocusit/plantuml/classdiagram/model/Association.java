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
package ch.ifocusit.plantuml.classdiagram.model;

import static ch.ifocusit.plantuml.classdiagram.model.Association.AssociationType.DIRECTION;
import static ch.ifocusit.plantuml.classdiagram.model.Cardinality.NONE;

/**
 * @author Julien Boz
 */
public class Association {

    protected String aName;
    protected String bName;
    protected AssociationType type;
    protected String label;
    protected Cardinality aCardinality;
    protected Cardinality bCardinality;

    public static Association from(final String aName, final String bName) {
        return from(aName, bName, DIRECTION, null, NONE, NONE);
    }

    public static Association from(final String aName, final String bName, final AssociationType assoc, final String label, final Cardinality aCardinality, final Cardinality bCardinality) {
        Association association = new Association();
        association.aName = aName;
        association.bName = bName;
        association.type = assoc;
        association.label = label;
        association.aCardinality = aCardinality;
        association.bCardinality = bCardinality;
        return association;
    }

    public String getbName() {
        return bName;
    }

    public void setbName(final String bName) {
        this.bName = bName;
    }

    public AssociationType getType() {
        return type;
    }

    public void setType(final AssociationType type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(final String label) {
        this.label = label;
    }

    public Cardinality getaCardinality() {
        return aCardinality;
    }

    public void setaCardinality(final Cardinality aCardinality) {
        this.aCardinality = aCardinality;
    }

    public Cardinality getbCardinality() {
        return bCardinality;
    }

    public void setbCardinality(final Cardinality bCardinality) {
        this.bCardinality = bCardinality;
    }

    public String getaName() {
        return aName;
    }

    public void setaName(final String aName) {
        this.aName = aName;
    }

    public enum AssociationType {

        LINK("-"),
        DIRECTION("-->"),
        BI_DIRECTION("<->"),
        INHERITANCE("<|--");

        private String symbol;

        AssociationType(String symbol) {
            this.symbol = symbol;
        }

        @Override
        public String toString() {
            return symbol;
        }
    }
}
