// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.converter;

import java.util.List;
import java.util.stream.Collectors;
import org.opentcs.access.to.peripherals.PeripheralOperationCreationTO;
import org.opentcs.data.peripherals.PeripheralOperation;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.PeripheralOperationTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.PeripheralOperationDescription;

/**
 * Includes the conversion methods for all PeripheralOperation classes.
 */
public class PeripheralOperationConverter {

  public PeripheralOperationConverter() {
  }

  public List<PeripheralOperationTO> toPeripheralOperationsTOs(
      List<PeripheralOperation> peripheralOperations
  ) {
    return peripheralOperations.stream()
        .map(
            perOp -> new PeripheralOperationTO(
                perOp.getOperation(),
                perOp.getLocation().getName()
            )
                .setCompletionRequired(perOp.isCompletionRequired())
                .setExecutionTrigger(toExecutionTriggerTO(perOp.getExecutionTrigger()))
        )
        .collect(Collectors.toList());
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

  public PeripheralOperationDescription toPeripheralOperationDescription(
      PeripheralOperation operation
  ) {
    return new PeripheralOperationDescription()
        .setOperation(operation.getOperation())
        .setLocationName(operation.getLocation().getName())
        .setExecutionTrigger(toExecutionTriggerTO(operation.getExecutionTrigger()))
        .setCompletionRequired(operation.isCompletionRequired());
  }

  private PeripheralOperationTO.ExecutionTrigger toExecutionTriggerTO(
      PeripheralOperation.ExecutionTrigger trigger
  ) {
    return switch (trigger) {
      case IMMEDIATE -> PeripheralOperationTO.ExecutionTrigger.IMMEDIATE;
      case AFTER_ALLOCATION -> PeripheralOperationTO.ExecutionTrigger.AFTER_ALLOCATION;
      case AFTER_MOVEMENT -> PeripheralOperationTO.ExecutionTrigger.AFTER_MOVEMENT;
    };
  }

  private PeripheralOperation.ExecutionTrigger toExecutionTrigger(
      PeripheralOperationTO.ExecutionTrigger trigger
  ) {
    return switch (trigger) {
      case IMMEDIATE -> PeripheralOperation.ExecutionTrigger.IMMEDIATE;
      case AFTER_ALLOCATION -> PeripheralOperation.ExecutionTrigger.AFTER_ALLOCATION;
      case AFTER_MOVEMENT -> PeripheralOperation.ExecutionTrigger.AFTER_MOVEMENT;
    };
  }
}
