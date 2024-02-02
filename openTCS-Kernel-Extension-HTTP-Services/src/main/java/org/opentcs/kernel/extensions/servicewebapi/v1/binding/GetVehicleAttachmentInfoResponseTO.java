/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.binding;

import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.stream.Collectors;
import org.opentcs.drivers.vehicle.management.VehicleAttachmentInformation;

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
      List<String> availableCommAdapters) {
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

  /**
   * Creates a new instance from <code>AttachmentInformation</code>.
   *
   * @param attachmentInformation The <code>AttachmentInformation</code> to create an
   * instance from.
   * @return A new instance containing the data from the given <code>AttachmentInformation</code>.
   */
  public static GetVehicleAttachmentInfoResponseTO fromAttachmentInformation(
      VehicleAttachmentInformation attachmentInformation) {
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
