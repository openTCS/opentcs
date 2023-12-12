/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.converter;

import java.util.List;
import java.util.stream.Collectors;
import org.opentcs.access.to.peripherals.PeripheralOperationCreationTO;
import org.opentcs.data.peripherals.PeripheralOperation;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.PeripheralOperationTO;

/**
 * Includes the conversion methods for all PeripheralOperation classes.
 */
public class PeripheralOperationConverter {

  public PeripheralOperationConverter() {
  }

  public List<PeripheralOperationTO> toPeripheralOperationsTOs(
      List<PeripheralOperation> peripheralOperations) {
    return peripheralOperations.stream()
        .map(perOp -> new PeripheralOperationTO(perOp.getOperation(),
                                                perOp.getLocation().getName())
        .setCompletionRequired(perOp.isCompletionRequired())
        .setExecutionTrigger(
            perOp.getExecutionTrigger().name())
        )
        .collect(Collectors.toList());
  }

  public List<PeripheralOperationCreationTO> toPeripheralOperationCreationTOs(
      List<PeripheralOperationTO> perOps) {
    return perOps.stream()
        .map(
            perOp -> new PeripheralOperationCreationTO(perOp.getOperation(),
                                                       perOp.getLocationName())
                .withCompletionRequired(perOp.isCompletionRequired())
                .withExecutionTrigger(PeripheralOperation.ExecutionTrigger.valueOf(
                    perOp.getExecutionTrigger())))
        .collect(Collectors.toList());
  }
}
