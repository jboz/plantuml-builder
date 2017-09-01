package ch.ifocusit.plantuml.test.helper.service;

import ch.ifocusit.plantuml.test.helper.domain.Driver;
import ch.ifocusit.plantuml.test.helper.domain.material.Car;

import java.time.Instant;

public interface AccessDataService {

    Driver whoDrive(Car car, Instant instant);
}
