/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.converter;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.opentcs.data.model.Couple;
import org.opentcs.data.model.Envelope;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.CoupleTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.EnvelopeTO;

/**
 * Includes the conversion methods for all Envelope classes.
 */
public class EnvelopeConverter {

  public EnvelopeConverter() {
  }

  public Map<String, Envelope> toVehicleEnvelopeMap(List<EnvelopeTO> envelopeEntries) {
    return envelopeEntries.stream()
        .collect(Collectors.toMap(
            EnvelopeTO::getKey,
            entry -> {
              List<Couple> couples = entry.getVertices().stream()
                  .map(coupleTO -> new Couple(coupleTO.getX(), coupleTO.getY()))
                  .collect(Collectors.toList());
              return new Envelope(couples);
            }
        ));
  }

  public List<EnvelopeTO> toEnvelopeTOs(Map<String, Envelope> envelopeMap) {
    return envelopeMap.entrySet().stream()
        .map(entry -> new EnvelopeTO(
        entry.getKey(),
        entry.getValue().getVertices().stream()
            .map(couple -> new CoupleTO(couple.getX(), couple.getY()))
            .collect(Collectors.toList()))
        )
        .sorted(Comparator.comparing(EnvelopeTO::getKey))
        .collect(Collectors.toList());
  }
}
