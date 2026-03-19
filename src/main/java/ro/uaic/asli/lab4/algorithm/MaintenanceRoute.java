package ro.uaic.asli.lab4.algorithm;

import ro.uaic.asli.lab4.model.Intersection;

import java.util.List;

public record MaintenanceRoute(List<Intersection> tour, double totalLength) {
}
