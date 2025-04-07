// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.converter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.VehicleCommAdapterDescription;
import org.opentcs.drivers.vehicle.management.VehicleAttachmentInformation;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.GetVehicleAttachmentInfoResponseTO;

/**
 * Tests for {@link VehicleAttachmentInformationConverter}.
 */
public class VehicleAttachmentInformationConverterTest {
  private final VehicleAttachmentInformationConverter converter
      = new VehicleAttachmentInformationConverter();

  @Test
  void checkToGetPeripheralAttachmentInfoResponseTO() {
    VehicleAttachmentInformation information = new VehicleAttachmentInformation(
        new Vehicle("V1").getReference(),
        List.of(new MockVehicleCommAdapterDescription()),
        new MockVehicleCommAdapterDescription()
    );

    GetVehicleAttachmentInfoResponseTO response
        = converter.toGetVehicleAttachmentInfoResponseTO(information);

    assertThat(response.getVehicleName(), is("V1"));
    assertThat(response.getAvailableCommAdapters().size(), is(1));
    assertThat(
        response.getAvailableCommAdapters().getFirst(),
        is(MockVehicleCommAdapterDescription.class.getName())
    );
    assertThat(
        response.getAttachedCommAdapter(),
        is(MockVehicleCommAdapterDescription.class.getName())
    );
  }

  static class MockVehicleCommAdapterDescription
      extends
        VehicleCommAdapterDescription {

    @Override
    public String getDescription() {
      return "some-vehicle-comm-adapter";
    }

    @Override
    public boolean isSimVehicleCommAdapter() {
      return false;
    }
  }
}
