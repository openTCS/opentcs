// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.converter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.access.to.peripherals.PeripheralOperationCreationTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.PeripheralOperationTO;

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
        .setExecutionTrigger(PeripheralOperationTO.ExecutionTrigger.AFTER_ALLOCATION)
        .setCompletionRequired(true);

    List<PeripheralOperationCreationTO> result
        = peripheralOpConverter.toPeripheralOperationCreationTOs(List.of(peripheralOp));

    assertThat(result, hasSize(1));
    assertThat(result.get(0).getOperation(), is("all"));
    assertThat(result.get(0).getLocationName(), is("l1"));
    assertThat(
        result.get(0).getExecutionTrigger(),
        is(PeripheralOperationCreationTO.ExecutionTrigger.AFTER_ALLOCATION)
    );
    assertTrue(result.get(0).isCompletionRequired());
  }
}
