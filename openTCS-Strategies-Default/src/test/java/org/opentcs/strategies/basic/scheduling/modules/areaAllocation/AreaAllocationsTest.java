/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.scheduling.modules.areaAllocation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.opentcs.data.model.Vehicle;

/**
 * Tests for {@link AreaAllocations}.
 */
class AreaAllocationsTest {

  private AreaAllocations areaAllocations;
  private Vehicle vehicle = new Vehicle("some-vehicle");

  @BeforeEach
  void setUp() {
    areaAllocations = new AreaAllocations();
    areaAllocations.clearAreaAllocations();
  }

  @Test
  void allowAreaAllocationWhenNoOtherAllocationsPresent() {
    GeometryCollection requestedArea = createCollectionWithOneGeometry(new Coordinate(0, 0),
                                                                       new Coordinate(0, 10),
                                                                       new Coordinate(10, 10),
                                                                       new Coordinate(10, 0),
                                                                       new Coordinate(0, 0));

    assertTrue(areaAllocations.isAreaAllocationAllowed(vehicle.getReference(), requestedArea));
  }

  @Test
  void prohibitAreaAllocationWhenAreaIsAllocatedByAnotherVehicle() {
    // Arrange
    GeometryCollection requestedArea = createCollectionWithOneGeometry(new Coordinate(0, 0),
                                                                       new Coordinate(0, 10),
                                                                       new Coordinate(10, 10),
                                                                       new Coordinate(10, 0),
                                                                       new Coordinate(0, 0));
    Vehicle vehicle2 = new Vehicle("some-other-vehicle");
    // Allocate area for another vehicle.
    assertTrue(areaAllocations.isAreaAllocationAllowed(vehicle2.getReference(), requestedArea));
    areaAllocations.setAreaAllocation(vehicle2.getReference(), requestedArea);

    // Act & Assert
    assertFalse(areaAllocations.isAreaAllocationAllowed(vehicle.getReference(), requestedArea));
  }

  @Test
  void prohibitAreaAllocationWhenAreaIsIntersectingAreaAllocatedByAnotherVehicle() {
    // Arrange
    GeometryCollection allocatedArea = createCollectionWithOneGeometry(new Coordinate(0, 0),
                                                                       new Coordinate(0, 10),
                                                                       new Coordinate(10, 10),
                                                                       new Coordinate(10, 0),
                                                                       new Coordinate(0, 0));
    GeometryCollection requestedArea = createCollectionWithOneGeometry(new Coordinate(5, 0),
                                                                       new Coordinate(5, 10),
                                                                       new Coordinate(15, 10),
                                                                       new Coordinate(15, 0),
                                                                       new Coordinate(5, 0));
    Vehicle vehicle2 = new Vehicle("some-other-vehicle");
    areaAllocations.setAreaAllocation(vehicle2.getReference(), allocatedArea);

    // Act & Assert
    assertFalse(areaAllocations.isAreaAllocationAllowed(vehicle.getReference(), requestedArea));
  }

  @Test
  void allowAreaAllocationWhenAreaIsIntersectingAreaAllocatedBySameVehicle() {
    // Arrange
    GeometryCollection allocatedArea = createCollectionWithOneGeometry(new Coordinate(0, 0),
                                                                       new Coordinate(0, 10),
                                                                       new Coordinate(10, 10),
                                                                       new Coordinate(10, 0),
                                                                       new Coordinate(0, 0));
    GeometryCollection requestedArea = createCollectionWithOneGeometry(new Coordinate(5, 0),
                                                                       new Coordinate(5, 10),
                                                                       new Coordinate(15, 10),
                                                                       new Coordinate(15, 0),
                                                                       new Coordinate(5, 0));
    areaAllocations.setAreaAllocation(vehicle.getReference(), allocatedArea);

    // Act & Assert
    assertTrue(areaAllocations.isAreaAllocationAllowed(vehicle.getReference(), requestedArea));
  }

  private GeometryCollection createCollectionWithOneGeometry(Coordinate... coordinates) {
    GeometryFactory geometryFactory = new GeometryFactory();
    return geometryFactory.createGeometryCollection(
        new Geometry[]{geometryFactory.createPolygon(coordinates)}
    );
  }
}
