// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.converter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.drivers.peripherals.PeripheralCommAdapterDescription;
import org.opentcs.drivers.peripherals.management.PeripheralAttachmentInformation;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.GetPeripheralAttachmentInfoResponseTO;

/**
 * Tests for {@link PeripheralAttachmentInformationConverter}.
 */
public class PeripheralAttachmentInformationConverterTest {
  private final PeripheralAttachmentInformationConverter converter
      = new PeripheralAttachmentInformationConverter();

  @Test
  void checkToGetPeripheralAttachmentInfoResponseTO() {
    PeripheralAttachmentInformation information = new PeripheralAttachmentInformation(
        new Location("L1", new LocationType("LT1").getReference()).getReference(),
        List.of(new MockPeripheralCommAdapterDescription()),
        new MockPeripheralCommAdapterDescription()
    );

    GetPeripheralAttachmentInfoResponseTO response
        = converter.toGetPeripheralAttachmentInfoResponseTO(information);

    assertThat(response.getLocationName(), is("L1"));
    assertThat(response.getAvailableCommAdapters().size(), is(1));
    assertThat(
        response.getAvailableCommAdapters().getFirst(),
        is(MockPeripheralCommAdapterDescription.class.getName())
    );
    assertThat(
        response.getAttachedCommAdapter(),
        is(MockPeripheralCommAdapterDescription.class.getName())
    );
  }

  static class MockPeripheralCommAdapterDescription
      extends
        PeripheralCommAdapterDescription {

    @Override
    public String getDescription() {
      return "some-peripheral-comm-adapter";
    }
  }
}
