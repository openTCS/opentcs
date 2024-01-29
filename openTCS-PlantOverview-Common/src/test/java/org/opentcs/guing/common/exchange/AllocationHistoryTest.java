/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.exchange;

import java.util.List;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;

/**
 * Tests for {@link AllocationHistory}.
 */
class AllocationHistoryTest {

  private AllocationHistory allocationHistory;
  private Vehicle vehicle = new Vehicle("some-vehicle");
  private Point point1 = new Point("point1");
  private Point point2 = new Point("point2");
  private Point point3 = new Point("point3");
  private Path path1 = new Path("path1", point1.getReference(), point2.getReference());
  private Path path2 = new Path("path2", point2.getReference(), point3.getReference());

  @BeforeEach
  void setUp() {
    allocationHistory = new AllocationHistory();
  }

  @Test
  void returnEmptyEntryForVehicleClaimingAndAllocatingNoResources() {
    AllocationHistory.Entry result = allocationHistory.updateHistory(vehicle);

    assertThat(result.getCurrentClaimedResources()).isEmpty();
    assertThat(result.getCurrentAllocatedResourcesAhead()).isEmpty();
    assertThat(result.getCurrentAllocatedResourcesBehind()).isEmpty();
    assertThat(result.getPreviouslyClaimedOrAllocatedResources()).isEmpty();
  }

  @Test
  void returnEmptyPreviousAllocationsOnFirstUpdate() {
    vehicle = vehicle
        .withAllocatedResources(List.of(Set.of(point1.getReference())))
        .withClaimedResources(List.of(Set.of(path1.getReference(), point2.getReference())));

    AllocationHistory.Entry result = allocationHistory.updateHistory(vehicle);

    assertThat(result.getCurrentAllocatedResourcesAhead()).isEmpty();
    assertThat(result.getCurrentAllocatedResourcesBehind())
        .hasSize(1)
        .contains(point1.getReference());
    assertThat(result.getCurrentClaimedResources())
        .hasSize(2)
        .contains(path1.getReference(), point2.getReference());
    assertThat(result.getPreviouslyClaimedOrAllocatedResources()).isEmpty();
  }

  @Test
  void returnPreviousAllocationsOnConsecutiveUpdate() {
    vehicle = vehicle
        .withAllocatedResources(List.of(Set.of(point1.getReference())))
        .withClaimedResources(List.of(Set.of(path1.getReference(), point2.getReference())));

    allocationHistory.updateHistory(vehicle);

    vehicle = vehicle
        .withAllocatedResources(List.of(Set.of(path1.getReference(), point2.getReference())))
        .withClaimedResources(List.of(Set.of(path2.getReference(), point3.getReference())));

    AllocationHistory.Entry result = allocationHistory.updateHistory(vehicle);

    assertThat(result.getCurrentAllocatedResourcesAhead()).isEmpty();
    assertThat(result.getCurrentAllocatedResourcesBehind())
        .hasSize(2)
        .contains(path1.getReference(), point2.getReference());
    assertThat(result.getCurrentClaimedResources())
        .hasSize(2)
        .contains(path2.getReference(), point3.getReference());
    assertThat(result.getPreviouslyClaimedOrAllocatedResources())
        .hasSize(1)
        .contains(point1.getReference());
  }

  @Test
  void returnCorrectlyDividedAllocationsAheadOrBehind() {
    vehicle = vehicle
        .withCurrentPosition(point2.getReference())
        .withNextPosition(point3.getReference())
        .withAllocatedResources(List.of(
            Set.of(point1.getReference()),
            Set.of(point2.getReference(), path1.getReference()),
            Set.of(point3.getReference(), path2.getReference())
        ));

    allocationHistory.updateHistory(vehicle);

    AllocationHistory.Entry result = allocationHistory.updateHistory(vehicle);

    assertThat(result.getCurrentAllocatedResourcesBehind())
        .contains(path1.getReference(), point1.getReference(), point2.getReference());
    assertThat(result.getCurrentAllocatedResourcesAhead())
        .contains(path2.getReference(), point3.getReference());
  }
}
