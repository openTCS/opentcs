// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.converter;

import java.util.stream.Collectors;
import org.opentcs.drivers.vehicle.management.VehicleAttachmentInformation;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.GetVehicleAttachmentInfoResponseTO;

/**
 * Includes the conversion methods for all VehicleAttachmentInformation classes.
 */
public class VehicleAttachmentInformationConverter {
  public VehicleAttachmentInformationConverter() {
  }

  /**
   * Creates a new instance from <code>AttachmentInformation</code>.
   *
   * @param attachmentInformation The <code>AttachmentInformation</code> to create an
   * instance from.
   * @return A new instance containing the data from the given <code>AttachmentInformation</code>.
   */
  public GetVehicleAttachmentInfoResponseTO toGetVehicleAttachmentInfoResponseTO(
      VehicleAttachmentInformation attachmentInformation
  ) {
    if (attachmentInformation == null) {
      return null;
    }
    GetVehicleAttachmentInfoResponseTO attachmentInformationTO
        = new GetVehicleAttachmentInfoResponseTO();

    attachmentInformationTO.setVehicleName(
        attachmentInformation.getVehicleReference()
            .getName()
    );
    attachmentInformationTO.setAvailableCommAdapters(
        attachmentInformation.getAvailableCommAdapters()
            .stream()
            .map(description -> description.getClass().getName())
            .collect(Collectors.toList())
    );
    attachmentInformationTO.setAttachedCommAdapter(
        attachmentInformation.getAttachedCommAdapter()
            .getClass()
            .getName()
    );

    return attachmentInformationTO;
  }
}
