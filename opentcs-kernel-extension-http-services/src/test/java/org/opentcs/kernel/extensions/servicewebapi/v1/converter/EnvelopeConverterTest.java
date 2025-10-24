// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.converter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.samePropertyValuesAs;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.access.to.model.CoupleCreationTO;
import org.opentcs.access.to.model.EnvelopeCreationTO;
import org.opentcs.data.model.Couple;
import org.opentcs.data.model.Envelope;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.CoupleTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.EnvelopeTO;

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

  @Test
  void checkEnvelopeTOs() {
    Map<String, Envelope> envelopeMap = Map.of("E1", new Envelope(List.of(new Couple(1, 1))));

    List<EnvelopeTO> result = envelopeConverter.toEnvelopeTOs(envelopeMap);

    assertThat(result, hasSize(1));
    assertThat(result.get(0).getKey(), is("E1"));
    assertThat(result.get(0).getVertices(), hasSize(1));
    assertThat(result.get(0).getVertices().get(0), samePropertyValuesAs(new CoupleTO(1, 1)));
  }
}
