// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.converter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.samePropertyValuesAs;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.access.to.model.CoupleCreationTO;
import org.opentcs.access.to.model.EnvelopeCreationTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.shared.CoupleTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.shared.EnvelopeTO;

/**
 * Tests for {@link EnvelopeConverter}.
 */
class EnvelopeConverterTest {

  private EnvelopeConverter envelopeConverter;

  @BeforeEach
  void setUp() {
    envelopeConverter = new EnvelopeConverter();
  }

  @Test
  void checkVehicleEnvelopeMap() {
    EnvelopeTO envelopeTo = new EnvelopeTO("E1", List.of(new CoupleTO(1, 1)));

    Map<String, EnvelopeCreationTO> result = envelopeConverter.toVehicleEnvelopeMap(
        List.of(envelopeTo)
    );

    assertThat(result, is(aMapWithSize(1)));
    assertThat(result, hasKey("E1"));
    assertThat(
        result.get("E1").getVertices().get(0),
        samePropertyValuesAs(new CoupleCreationTO(1, 1))
    );
  }
}
