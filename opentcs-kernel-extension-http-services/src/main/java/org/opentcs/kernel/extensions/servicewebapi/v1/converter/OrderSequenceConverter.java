// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.converter;

import jakarta.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.GetOrderSequenceResponseTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.Property;

/**
 * Includes the conversion methods for all OrderSequence classes.
 */
public class OrderSequenceConverter {
  public OrderSequenceConverter() {
  }

  @SuppressWarnings("deprecation")
  public GetOrderSequenceResponseTO toGetOrderSequenceResponseTO(OrderSequence orderSequence) {
    return new GetOrderSequenceResponseTO(orderSequence.getName())
        .setComplete(orderSequence.isComplete())
        .setFailureFatal(orderSequence.isFailureFatal())
        .setFinished(orderSequence.isFinished())
        .setFinishedIndex(orderSequence.getFinishedIndex())
        .setCreationTime(orderSequence.getCreationTime())
        .setFinishedTime(orderSequence.getFinishedTime())
        .setType(orderSequence.getType())
        .setOrderTypes(orderSequence.getOrderTypes().stream().toList())
        .setOrders(
            orderSequence.getOrders()
                .stream()
                .map(TCSObjectReference::getName)
                .collect(Collectors.toList())
        )
        .setProcessingVehicle(nameOfNullableReference(orderSequence.getProcessingVehicle()))
        .setIntendedVehicle(nameOfNullableReference(orderSequence.getIntendedVehicle()))
        .setProperties(convertProperties(orderSequence.getProperties()));
  }

  private String nameOfNullableReference(
      @Nullable
      TCSObjectReference<?> reference
  ) {
    return reference == null ? null : reference.getName();
  }

  private List<Property> convertProperties(Map<String, String> properties) {
    return properties.entrySet().stream()
        .map(property -> new Property(property.getKey(), property.getValue()))
        .collect(Collectors.toList());
  }
}
