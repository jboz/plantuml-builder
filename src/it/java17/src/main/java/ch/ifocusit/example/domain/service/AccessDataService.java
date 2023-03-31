package ch.ifocusit.example.domain.service;

import java.time.Instant;
import ch.ifocusit.example.domain.model.Driver;
import ch.ifocusit.example.domain.model.material.Car;

public interface AccessDataService {

    Driver whoDrive(Car car, Instant instant);
}
