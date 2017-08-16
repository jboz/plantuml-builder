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
package ch.ifocusit.plantuml.test.helper.domain.material;

import ch.ifocusit.plantuml.test.helper.domain.Driver;
import ch.ifocusit.plantuml.test.helper.domain.Price;

import java.util.Collection;

@Machine
public class Car implements Vehicule {

    @Machine
    // must be ignored
    private Long ignored;
    @Machine
    private String brand;
    @Machine
    private String model;

    private Driver driver;

    private Price price;

    @Machine
    private Collection<Wheel> wheels;

    public Driver buyBy(Driver driver, Price price) {
        this.driver = driver;
        driver.buy(this);
        this.price = price;
        return driver;
    }
}