// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.converter;

import java.util.List;
import java.util.stream.Collectors;
import org.opentcs.access.to.peripherals.PeripheralOperationCreationTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.PeripheralOperationTO;

/**
 * Includes the conversion methods for all PeripheralOperation classes.
 */
public class PeripheralOperationConverter {

  public PeripheralOperationConverter() {
  }

  public List<PeripheralOperationCreationTO> toPeripheralOperationCreationTOs(
      List<PeripheralOperationTO> perOps
  ) {
    return perOps.stream()
        .map(
            perOp -> new PeripheralOperationCreationTO(
                perOp.getOperation(),
                perOp.getLocationName()
            )
                .withCompletionRequired(perOp.isCompletionRequired())
                .withExecutionTrigger(toExecutionTrigger(perOp.getExecutionTrigger()))
        )
        .collect(Collectors.toList());
  }

  private PeripheralOperationCreationTO.ExecutionTrigger toExecutionTrigger(
      PeripheralOperationTO.ExecutionTrigger trigger
  ) {
    return switch (trigger) {
      case IMMEDIATE -> PeripheralOperationCreationTO.ExecutionTrigger.IMMEDIATE;
      case AFTER_ALLOCATION -> PeripheralOperationCreationTO.ExecutionTrigger.AFTER_ALLOCATION;
      case AFTER_MOVEMENT -> PeripheralOperationCreationTO.ExecutionTrigger.AFTER_MOVEMENT;
    };
  }
}
