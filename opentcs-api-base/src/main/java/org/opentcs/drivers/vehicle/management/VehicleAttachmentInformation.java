// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.drivers.vehicle.management;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import java.io.Serializable;
import java.util.List;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.VehicleCommAdapterDescription;

/**
 * Describes which communication adapter a vehicle is currently associated with and which adapters
 * are available.
 */
public class VehicleAttachmentInformation
    implements
      Serializable {

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
  public VehicleAttachmentInformation(
      @Nonnull
      TCSObjectReference<Vehicle> vehicleReference,
      @Nonnull
      List<VehicleCommAdapterDescription> availableCommAdapters,
      @Nonnull
      VehicleCommAdapterDescription attachedCommAdapter
  ) {
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
  public VehicleAttachmentInformation withVehicleReference(
      TCSObjectReference<Vehicle> vehicleReference
  ) {
    return new VehicleAttachmentInformation(
        vehicleReference,
        getAvailableCommAdapters(),
        getAttachedCommAdapter()
    );
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
  public VehicleAttachmentInformation withAvailableCommAdapters(
      @Nonnull
      List<VehicleCommAdapterDescription> availableCommAdapters
  ) {
    return new VehicleAttachmentInformation(
        getVehicleReference(),
        availableCommAdapters,
        getAttachedCommAdapter()
    );
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
  public VehicleAttachmentInformation withAttachedCommAdapter(
      @Nonnull
      VehicleCommAdapterDescription attachedCommAdapter
  ) {
    return new VehicleAttachmentInformation(
        getVehicleReference(),
        getAvailableCommAdapters(),
        attachedCommAdapter
    );
  }
}
