/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.drivers.vehicle.management;

import java.io.Serializable;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;

/**
 * Instances of this class represent events emitted by/for attaching comm adapters.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class AttachmentEvent
    extends CommAdapterEvent
    implements Serializable {

  /**
   * The vehicle's name a comm adapter has been attached to.
   */
  private final String vehicleName;
  /**
   * The {@link AttachmentInformation} to the actual attachment.
   */
  private final AttachmentInformation updatedAttachmentInformation;

  /**
   * Creates a new instance.
   *
   * @param vehicleName The vehicle's name a comm adapter has been attached to.
   * @param updatedAttachmentInformation The information to the actual attachment.
   */
  public AttachmentEvent(@Nonnull String vehicleName,
                         @Nonnull AttachmentInformation updatedAttachmentInformation) {
    this.vehicleName = requireNonNull(vehicleName, "vehicleName");
    this.updatedAttachmentInformation = requireNonNull(updatedAttachmentInformation,
                                                       "updatedAttachmentInformation");
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
   * Returns the {@link AttachmentInformation} to the actual attachment.
   *
   * @return The {@link AttachmentInformation} to the actual attachment.
   */
  public AttachmentInformation getUpdatedAttachmentInformation() {
    return updatedAttachmentInformation;
  }
}
