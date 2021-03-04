/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.drivers.vehicle.management;

import java.io.Serializable;
import java.util.List;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.VehicleCommAdapterDescription;

/**
 *
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class AttachmentInformation
    implements Serializable {

  /**
   * The vehicle this attachment information belongs to.
   */
  private final TCSObjectReference<Vehicle> vehicleReference;
  /**
   * The list of comm adapters available to be attached to the referenced vehicle.
   */
  private final List<VehicleCommAdapterDescription> availableCommAdapters;
  /**
   * The comm adapter attached to the referenced vehicle.
   */
  private final VehicleCommAdapterDescription attachedCommAdapter;

  /**
   * Creates a new instance.
   *
   * @param vehicleReference The vehicle this attachment information belongs to.
   * @param availableCommAdapters The list of comm adapters available to be attached to the
   * referenced vehicle.
   * @param attachedCommAdapter The comm adapter attached to the referenced vehicle.
   */
  public AttachmentInformation(@Nonnull TCSObjectReference<Vehicle> vehicleReference,
                               @Nonnull List<VehicleCommAdapterDescription> availableCommAdapters,
                               @Nonnull VehicleCommAdapterDescription attachedCommAdapter) {
    this.vehicleReference = requireNonNull(vehicleReference, "vehicleReference");
    this.availableCommAdapters = requireNonNull(availableCommAdapters, "availableCommAdapters");
    this.attachedCommAdapter = requireNonNull(attachedCommAdapter, "attachedCommAdapter");
  }

  /**
   * Returns the vehicle this attachment information belongs to.
   *
   * @return The vehicle this attachment information belongs to.
   */
  @Nonnull
  public TCSObjectReference<Vehicle> getVehicleReference() {
    return vehicleReference;
  }

  /**
   * Creates a copy of this object with the given vehicle reference.
   *
   * @param vehicleReference The new vehicle reference.
   * @return A copy of this object, differing in the given vehicle reference.
   */
  public AttachmentInformation withVehicleReference(TCSObjectReference<Vehicle> vehicleReference) {
    return new AttachmentInformation(vehicleReference, availableCommAdapters, attachedCommAdapter);
  }

  /**
   * Returns the list of comm adapters available to be attached to the referenced vehicle.
   *
   * @return The list of comm adapters available to be attached to the referenced vehicle.
   */
  @Nonnull
  public List<VehicleCommAdapterDescription> getAvailableCommAdapters() {
    return availableCommAdapters;
  }

  /**
   * Creates a copy of this object with the given available comm adapters.
   *
   * @param availableCommAdapters The new available comm adapters.
   * @return A copy of this object, differing in the given available comm adapters.
   */
  public AttachmentInformation withAvailableCommAdapters(
      @Nonnull List<VehicleCommAdapterDescription> availableCommAdapters) {
    return new AttachmentInformation(vehicleReference, availableCommAdapters, attachedCommAdapter);
  }

  /**
   * Returns the comm adapter attached to the referenced vehicle.
   *
   * @return The comm adapter attached to the referenced vehicle.
   */
  @Nonnull
  public VehicleCommAdapterDescription getAttachedCommAdapter() {
    return attachedCommAdapter;
  }

  /**
   * Creates a copy of this object with the given attached comm adapter.
   *
   * @param attachedCommAdapter The new attached comm adapter.
   * @return A copy of this object, differing in the given attached comm adapter.
   */
  public AttachmentInformation withAttachedCommAdapter(
      @Nonnull VehicleCommAdapterDescription attachedCommAdapter) {
    return new AttachmentInformation(vehicleReference, availableCommAdapters, attachedCommAdapter);
  }
}
