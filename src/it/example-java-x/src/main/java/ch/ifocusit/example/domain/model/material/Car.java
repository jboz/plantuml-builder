/*-
 * Plantuml builder
 *
 * Copyright (C) 2024 Focus IT
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
package ch.ifocusit.example.domain.model.material;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import ch.ifocusit.example.domain.model.Devise;
import ch.ifocusit.example.domain.model.Driver;
import ch.ifocusit.example.domain.model.Price;

@Machine
public class Car implements Vehicule {

    @Machine
    // must be ignored
    private Long ignored;
    @Machine
    private String brand;
    @Machine
    private String model;

    private Set<Driver> drivers = new HashSet<>();

    private Price price;

    @Machine
    private Collection<Wheel> wheels;

    public Driver buyBy(Driver driver, BigDecimal amount, Devise devise) {
        this.drivers.add(driver);
        driver.buy(this);
        this.price = Price.of(amount, devise);
        return driver;
    }

    public Car addDriver(Driver anotherDriver) {
        drivers.add(anotherDriver);
        anotherDriver.addCar(this);
        return this;
    }

    public Collection<Wheel> getWheels() {
        return wheels;
    }

    public void addWheel(Wheel wheel) {
        wheels.add(wheel);
    }

    @Override
    public String toString() {
        return showMeTheCarInfo();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Car))
            return false;
        Car car = (Car) o;
        return Objects.equals(ignored, car.ignored)
                && Objects.equals(brand, car.brand)
                && Objects.equals(model, car.model)
                && Objects.equals(drivers, car.drivers)
                && Objects.equals(price, car.price)
                && Objects.equals(wheels, car.wheels);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ignored, brand, model, drivers, price, wheels);
    }

    private String showMeTheCarInfo() {
        return brand + "/" + model;
    }
}
