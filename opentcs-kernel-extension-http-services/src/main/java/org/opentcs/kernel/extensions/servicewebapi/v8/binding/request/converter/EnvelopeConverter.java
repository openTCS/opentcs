// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.converter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.opentcs.access.to.model.CoupleCreationTO;
import org.opentcs.access.to.model.EnvelopeCreationTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.shared.EnvelopeTO;

/**
 * Includes the conversion methods for all Envelope classes.
 */
public class EnvelopeConverter {

  public EnvelopeConverter() {
  }

  public Map<String, EnvelopeCreationTO> toVehicleEnvelopeMap(List<EnvelopeTO> envelopeEntries) {
    return envelopeEntries.stream()
        .collect(
            Collectors.toMap(
                EnvelopeTO::getKey,
                entry -> {
                  List<CoupleCreationTO> couples = entry.getVertices().stream()
                      .map(coupleTO -> new CoupleCreationTO(coupleTO.getX(), coupleTO.getY()))
                      .collect(Collectors.toList());
                  return new EnvelopeCreationTO(couples);
                }
            )
        );
  }
}
