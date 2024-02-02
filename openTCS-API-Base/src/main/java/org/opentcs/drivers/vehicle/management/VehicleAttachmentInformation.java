/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.drivers.vehicle.management;

import java.util.List;
import javax.annotation.Nonnull;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.VehicleCommAdapterDescription;

/**
 * Describes which communication adapter a vehicle is currently associated with and which adapters
 * are available.
 */
@SuppressWarnings("deprecation")
public class VehicleAttachmentInformation
    extends AttachmentInformation {

  /**
   * Creates a new instance.
   *
   * @param vehicleReference The vehicle this attachment information belongs to.
   * @param availableCommAdapters The list of comm adapters available to be attached to the
   * referenced vehicle.
   * @param attachedCommAdapter The comm adapter attached to the referenced vehicle.
   */
  public VehicleAttachmentInformation(
      @Nonnull TCSObjectReference<Vehicle> vehicleReference,
      @Nonnull List<VehicleCommAdapterDescription> availableCommAdapters,
      @Nonnull VehicleCommAdapterDescription attachedCommAdapter) {
    super(vehicleReference, availableCommAdapters, attachedCommAdapter);
  }

  /**
   * Creates a copy of this object with the given vehicle reference.
   *
   * @param vehicleReference The new vehicle reference.
   * @return A copy of this object, differing in the given vehicle reference.
   */
  @Override
  public VehicleAttachmentInformation withVehicleReference(
      TCSObjectReference<Vehicle> vehicleReference) {
    return new VehicleAttachmentInformation(vehicleReference,
                                            getAvailableCommAdapters(),
                                            getAttachedCommAdapter());
  }

  /**
   * Creates a copy of this object with the given available comm adapters.
   *
   * @param availableCommAdapters The new available comm adapters.
   * @return A copy of this object, differing in the given available comm adapters.
   */
  @Override
  public VehicleAttachmentInformation withAvailableCommAdapters(
      @Nonnull List<VehicleCommAdapterDescription> availableCommAdapters) {
    return new VehicleAttachmentInformation(getVehicleReference(),
                                            availableCommAdapters,
                                            getAttachedCommAdapter());
  }

  /**
   * Creates a copy of this object with the given attached comm adapter.
   *
   * @param attachedCommAdapter The new attached comm adapter.
   * @return A copy of this object, differing in the given attached comm adapter.
   */
  @Override
  public VehicleAttachmentInformation withAttachedCommAdapter(
      @Nonnull VehicleCommAdapterDescription attachedCommAdapter) {
    return new VehicleAttachmentInformation(getVehicleReference(),
                                            getAvailableCommAdapters(),
                                            attachedCommAdapter);
  }
}
