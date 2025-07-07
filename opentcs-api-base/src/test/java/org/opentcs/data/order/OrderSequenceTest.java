// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.data.order;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.opentcs.data.model.Vehicle;

/**
 * Unit tests for {@link OrderSequence}.
 */
class OrderSequenceTest {

  @Test
  void addHistoryEntryForCreation() {
    OrderSequence seq = new OrderSequence("some-sequence");

    assertThat(seq.getHistory().getEntries()).hasSize(1);
  }

  @Test
  void addHistoryEntryWhenChangingProcessingVehicle() {
    OrderSequence seq = new OrderSequence("some-sequence");
    Vehicle vehicle1 = new Vehicle("some-vehicle-1");
    Vehicle vehicle2 = new Vehicle("some-vehicle-2");

    seq = seq.withProcessingVehicle(vehicle1.getReference());
    assertThat(seq.getHistory().getEntries()).hasSize(2);

    seq = seq.withProcessingVehicle(vehicle2.getReference());
    assertThat(seq.getHistory().getEntries()).hasSize(3);

    seq = seq.withProcessingVehicle(vehicle1.getReference());
    assertThat(seq.getHistory().getEntries()).hasSize(4);
  }

  @Test
  void skipRedundantHistoryEntriesForSameProcessingVehicle() {
    OrderSequence seq = new OrderSequence("some-sequence");
    Vehicle vehicle = new Vehicle("some-vehicle");

    seq = seq.withProcessingVehicle(vehicle.getReference());
    assertThat(seq.getHistory().getEntries()).hasSize(2);

    seq = seq.withProcessingVehicle(vehicle.getReference());
    assertThat(seq.getHistory().getEntries()).hasSize(2);
  }
}
