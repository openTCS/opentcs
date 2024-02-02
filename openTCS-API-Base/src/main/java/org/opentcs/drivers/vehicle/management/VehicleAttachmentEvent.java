/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.drivers.vehicle.management;

import javax.annotation.Nonnull;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * Instances of this class represent events emitted by/for attaching comm adapters.
 */
@SuppressWarnings("deprecation")
public class VehicleAttachmentEvent
    extends AttachmentEvent {

  /**
   * Creates a new instance.
   *
   * @param vehicleName The vehicle's name a comm adapter has been attached to.
   * @param attachmentInformation The information to the actual attachment.
   */
  public VehicleAttachmentEvent(@Nonnull String vehicleName,
                                @Nonnull VehicleAttachmentInformation attachmentInformation) {
    super(vehicleName, attachmentInformation);
  }

  /**
   * {@inheritDoc}
   *
   * @deprecated Use {@link #getAttachmentInformation()} instead.
   */
  @Override
  @Deprecated
  @ScheduledApiChange(details = "Will be removed.", when = "6.0")
  public VehicleAttachmentInformation getUpdatedAttachmentInformation() {
    return (VehicleAttachmentInformation) super.getUpdatedAttachmentInformation();
  }

  /**
   * Returns the {@link VehicleAttachmentInformation} to the actual attachment.
   *
   * @return The {@link VehicleAttachmentInformation} to the actual attachment.
   */
  public VehicleAttachmentInformation getAttachmentInformation() {
    return getUpdatedAttachmentInformation();
  }
}
