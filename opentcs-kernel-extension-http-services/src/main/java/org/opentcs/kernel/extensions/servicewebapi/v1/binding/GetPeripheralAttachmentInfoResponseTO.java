// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.binding;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import java.util.List;

/**
 */
public class GetPeripheralAttachmentInfoResponseTO {

  @Nonnull
  private String locationName;

  @Nonnull
  private List<String> availableCommAdapters;

  @Nonnull
  private String attachedCommAdapter;

  public GetPeripheralAttachmentInfoResponseTO(
      @Nonnull
      String locationName,
      @Nonnull
      String attachedCommAdapter,
      @Nonnull
      List<String> availableCommAdapters
  ) {
    this.locationName = requireNonNull(locationName, "locationName");
    this.attachedCommAdapter = requireNonNull(attachedCommAdapter, "attachedCommAdapter");
    this.availableCommAdapters = requireNonNull(availableCommAdapters, "availableCommAdapters");
  }

  @Nonnull
  public String getLocationName() {
    return locationName;
  }

  public GetPeripheralAttachmentInfoResponseTO setLocationName(
      @Nonnull
      String locationName
  ) {
    this.locationName = requireNonNull(locationName, "locationName");
    return this;
  }

  @Nonnull
  public List<String> getAvailableCommAdapters() {
    return availableCommAdapters;
  }

  public GetPeripheralAttachmentInfoResponseTO setAvailableCommAdapters(
      @Nonnull
      List<String> availableCommAdapters
  ) {
    this.availableCommAdapters = requireNonNull(availableCommAdapters, "availableCommAdapters");
    return this;
  }

  @Nonnull
  public String getAttachedCommAdapter() {
    return attachedCommAdapter;
  }

  public GetPeripheralAttachmentInfoResponseTO setAttachedCommAdapter(
      @Nonnull
      String attachedCommAdapter
  ) {
    this.attachedCommAdapter = requireNonNull(attachedCommAdapter, "attachedCommAdapter");
    return this;
  }
}
