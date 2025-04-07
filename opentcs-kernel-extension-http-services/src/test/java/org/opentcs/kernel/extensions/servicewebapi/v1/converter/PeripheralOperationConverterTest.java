// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.converter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.access.to.peripherals.PeripheralOperationCreationTO;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.peripherals.PeripheralOperation;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.PeripheralOperationTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.PeripheralOperationDescription;

/**
 * Tests for {@link PeripheralOperationConverter}.
 */
class PeripheralOperationConverterTest {

  private PeripheralOperationConverter peripheralOpConverter;

  @BeforeEach
  void setUp() {
    peripheralOpConverter = new PeripheralOperationConverter();
  }

  @Test
  void checkToPeripheralOperationCreationTOs() {
    PeripheralOperationTO peripheralOp = new PeripheralOperationTO("all", "l1")
        .setExecutionTrigger(PeripheralOperation.ExecutionTrigger.AFTER_ALLOCATION.name())
        .setCompletionRequired(true);

    List<PeripheralOperationCreationTO> result
        = peripheralOpConverter.toPeripheralOperationCreationTOs(List.of(peripheralOp));

    assertThat(result, hasSize(1));
    assertThat(result.get(0).getOperation(), is("all"));
    assertThat(result.get(0).getLocationName(), is("l1"));
    assertThat(
        result.get(0).getExecutionTrigger(),
        is(PeripheralOperation.ExecutionTrigger.AFTER_ALLOCATION)
    );
    assertTrue(result.get(0).isCompletionRequired());
  }

  @Test
  void checkToPeripheralOperationsTOs() {
    PeripheralOperation peripheralOp = new PeripheralOperation(
        new Location("L1", new LocationType("LT1").getReference()).getReference(),
        "operation",
        PeripheralOperation.ExecutionTrigger.AFTER_ALLOCATION,
        true
    );

    List<PeripheralOperationTO> result
        = peripheralOpConverter.toPeripheralOperationsTOs(List.of(peripheralOp));

    assertThat(result, hasSize(1));
    assertThat(result.get(0).getOperation(), is("operation"));
    assertThat(result.get(0).getLocationName(), is("L1"));
    assertThat(
        result.get(0).getExecutionTrigger(),
        is(PeripheralOperation.ExecutionTrigger.AFTER_ALLOCATION.name())
    );
    assertTrue(result.get(0).isCompletionRequired());
  }

  @Test
  void checkToPeripheralOperationDescription() {
    PeripheralOperation peripheralOp = new PeripheralOperation(
        new Location("L1", new LocationType("LT1").getReference()).getReference(),
        "operation",
        PeripheralOperation.ExecutionTrigger.AFTER_ALLOCATION,
        true
    );

    PeripheralOperationDescription description
        = peripheralOpConverter.toPeripheralOperationDescription(peripheralOp);

    assertThat(description.getOperation(), is("operation"));
    assertThat(description.getLocationName(), is("L1"));
    assertThat(
        description.getExecutionTrigger(),
        is(PeripheralOperation.ExecutionTrigger.AFTER_ALLOCATION)
    );
    assertTrue(description.isCompletionRequired());
  }
}
