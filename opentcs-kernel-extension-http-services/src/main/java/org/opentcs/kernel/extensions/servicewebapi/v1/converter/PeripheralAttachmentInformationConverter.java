// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.converter;

import jakarta.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;
import org.opentcs.drivers.peripherals.management.PeripheralAttachmentInformation;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.GetPeripheralAttachmentInfoResponseTO;

/**
 * Includes the conversion methods for all PeripheralAttachmentInformation classes.
 */
public class PeripheralAttachmentInformationConverter {
  public PeripheralAttachmentInformationConverter() {
  }

  public GetPeripheralAttachmentInfoResponseTO toGetPeripheralAttachmentInfoResponseTO(
      @Nullable
      PeripheralAttachmentInformation peripheralAttachmentInfo
  ) {
    if (peripheralAttachmentInfo == null) {
      return null;
    }

    List<String> availableAdapters = peripheralAttachmentInfo.getAvailableCommAdapters()
        .stream()
        .map(description -> description.getClass().getName())
        .collect(Collectors.toList());

    return new GetPeripheralAttachmentInfoResponseTO(
        peripheralAttachmentInfo.getLocationReference().getName(),
        peripheralAttachmentInfo.getAttachedCommAdapter().getClass().getName(),
        availableAdapters
    );
  }
}
