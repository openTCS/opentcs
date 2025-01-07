// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.converter;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.opentcs.data.model.AcceptableOrderType;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.AcceptableOrderTypeTO;

/**
 * Includes the conversion methods for all acceptable order type classes.
 */
public class AcceptableOrderTypeConverter {
  public AcceptableOrderTypeConverter() {
  }

  public List<AcceptableOrderTypeTO> toAcceptableOrderTypeTOs(
      Set<AcceptableOrderType> acceptableOrderTypes
  ) {
    return acceptableOrderTypes.stream()
        .map(orderType -> new AcceptableOrderTypeTO(orderType.getName(), orderType.getPriority()))
        .sorted(
            Comparator.comparing(AcceptableOrderTypeTO::getPriority)
                .thenComparing(AcceptableOrderTypeTO::getName)
        )
        .collect(Collectors.toList());
  }

  public Set<AcceptableOrderType> toAcceptableOrderTypes(
      List<AcceptableOrderTypeTO> acceptableOrderTypeTOs
  ) {
    return acceptableOrderTypeTOs.stream()
        .map(
            orderTypeTO -> new AcceptableOrderType(orderTypeTO.getName(), orderTypeTO.getPriority())
        )
        .collect(Collectors.toSet());
  }
}
