/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.drivers.vehicle.management;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import java.io.Serializable;

/**
 * Instances of this class represent events emitted by/for attaching comm adapters.
 */
public class VehicleAttachmentEvent
    extends
      CommAdapterEvent
    implements
      Serializable {

  /**
   * The vehicle's name a comm adapter has been attached to.
   */
  private final String vehicleName;
  /**
   * The {@link VehicleAttachmentInformation} to the actual attachment.
   */
  private final VehicleAttachmentInformation attachmentInformation;

  /**
   * Creates a new instance.
   *
   * @param vehicleName The vehicle's name a comm adapter has been attached to.
   * @param attachmentInformation The information to the actual attachment.
   */
  public VehicleAttachmentEvent(
      @Nonnull
      String vehicleName,
      @Nonnull
      VehicleAttachmentInformation attachmentInformation
  ) {
    this.vehicleName = requireNonNull(vehicleName, "vehicleName");
    this.attachmentInformation = requireNonNull(attachmentInformation, "attachmentInformation");
  }

  /**
   * Returns the vehicle's name a comm adapter has been attached to.
   *
   * @return The vehicle's name a comm adapter has been attached to.
   */
  public String getVehicleName() {
    return vehicleName;
  }

  /**
   * Returns the {@link VehicleAttachmentInformation} to the actual attachment.
   *
   * @return The {@link VehicleAttachmentInformation} to the actual attachment.
   */
  public VehicleAttachmentInformation getAttachmentInformation() {
    return attachmentInformation;
  }
}
