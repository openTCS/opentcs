// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.binding;

import static java.util.Objects.requireNonNull;

import java.util.List;

/**
 * Arranges the data from a vehicle's <code>AttachmentInformation</code> for transferring.
 */
public class GetVehicleAttachmentInfoResponseTO {

  /**
   * The vehicle this attachment information belongs to.
   */
  private String vehicleName;
  /**
   * The list of comm adapters available to be attached to the referenced vehicle.
   */
  private List<String> availableCommAdapters;
  /**
   * The comm adapter attached to the referenced vehicle.
   */
  private String attachedCommAdapter;

  public GetVehicleAttachmentInfoResponseTO() {
  }

  public GetVehicleAttachmentInfoResponseTO setVehicleName(String vehicleName) {
    this.vehicleName = requireNonNull(vehicleName, "vehicleName");
    return this;
  }

  public String getVehicleName() {
    return vehicleName;
  }

  public GetVehicleAttachmentInfoResponseTO setAvailableCommAdapters(
      List<String> availableCommAdapters
  ) {
    this.availableCommAdapters = requireNonNull(availableCommAdapters, "availableCommAdapters");
    return this;
  }

  public List<String> getAvailableCommAdapters() {
    return availableCommAdapters;
  }

  public GetVehicleAttachmentInfoResponseTO setAttachedCommAdapter(String attachedCommAdapter) {
    this.attachedCommAdapter = requireNonNull(attachedCommAdapter, "attachedCommAdapter");
    return this;
  }

  public String getAttachedCommAdapter() {
    return attachedCommAdapter;
  }
}
