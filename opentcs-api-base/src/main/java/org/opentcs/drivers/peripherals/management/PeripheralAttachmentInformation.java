// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.drivers.peripherals.management;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import java.io.Serializable;
import java.util.List;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.drivers.peripherals.PeripheralCommAdapterDescription;

/**
 * Describes which communication adapter a location is currently associated with.
 */
public class PeripheralAttachmentInformation
    implements
      Serializable {

  /**
   * The location this attachment information belongs to.
   */
  private final TCSResourceReference<Location> locationReference;
  /**
   * The list of comm adapters available to be attached to the referenced location.
   */
  private final List<PeripheralCommAdapterDescription> availableCommAdapters;
  /**
   * The comm adapter attached to the referenced location.
   */
  private final PeripheralCommAdapterDescription attachedCommAdapter;

  /**
   * Creates a new instance.
   *
   * @param locationReference The location this attachment information belongs to.
   * @param availableCommAdapters The list of comm adapters available to be attached to the
   * referenced location.
   * @param attachedCommAdapter The comm adapter attached to the referenced location.
   */
  public PeripheralAttachmentInformation(
      @Nonnull
      TCSResourceReference<Location> locationReference,
      @Nonnull
      List<PeripheralCommAdapterDescription> availableCommAdapters,
      @Nonnull
      PeripheralCommAdapterDescription attachedCommAdapter
  ) {
    this.locationReference = requireNonNull(locationReference, "locationReference");
    this.availableCommAdapters = requireNonNull(availableCommAdapters, "availableCommAdapters");
    this.attachedCommAdapter = requireNonNull(attachedCommAdapter, "attachedCommAdapter");
  }

  /**
   * Returns the location this attachment information belongs to.
   *
   * @return The location this attachment information belongs to.
   */
  @Nonnull
  public TCSResourceReference<Location> getLocationReference() {
    return locationReference;
  }

  /**
   * Creates a copy of this object with the given location reference.
   *
   * @param locationReference The new location reference.
   * @return A copy of this object, differing in the given location reference.
   */
  public PeripheralAttachmentInformation withLocationReference(
      TCSResourceReference<Location> locationReference
  ) {
    return new PeripheralAttachmentInformation(
        locationReference,
        availableCommAdapters,
        attachedCommAdapter
    );
  }

  /**
   * Returns the list of comm adapters available to be attached to the referenced location.
   *
   * @return The list of comm adapters available to be attached to the referenced location.
   */
  @Nonnull
  public List<PeripheralCommAdapterDescription> getAvailableCommAdapters() {
    return availableCommAdapters;
  }

  /**
   * Creates a copy of this object with the given available comm adapters.
   *
   * @param availableCommAdapters The new available comm adapters.
   * @return A copy of this object, differing in the given available comm adapters.
   */
  public PeripheralAttachmentInformation withAvailableCommAdapters(
      @Nonnull
      List<PeripheralCommAdapterDescription> availableCommAdapters
  ) {
    return new PeripheralAttachmentInformation(
        locationReference,
        availableCommAdapters,
        attachedCommAdapter
    );
  }

  /**
   * Returns the comm adapter attached to the referenced location.
   *
   * @return The comm adapter attached to the referenced location.
   */
  @Nonnull
  public PeripheralCommAdapterDescription getAttachedCommAdapter() {
    return attachedCommAdapter;
  }

  /**
   * Creates a copy of this object with the given attached comm adapter.
   *
   * @param attachedCommAdapter The new attached comm adapter.
   * @return A copy of this object, differing in the given attached comm adapter.
   */
  public PeripheralAttachmentInformation withAttachedCommAdapter(
      @Nonnull
      PeripheralCommAdapterDescription attachedCommAdapter
  ) {
    return new PeripheralAttachmentInformation(
        locationReference,
        availableCommAdapters,
        attachedCommAdapter
    );
  }

  @Override
  public String toString() {
    return "PeripheralAttachmentInformation{"
        + "locationReference=" + locationReference
        + ", availableCommAdapters=" + availableCommAdapters
        + ", attachedCommAdapter=" + attachedCommAdapter
        + '}';
  }
}
