// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.scheduling.modules.areaAllocation;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import java.util.Set;
import org.opentcs.components.Lifecycle;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.TCSResource;
import org.opentcs.data.model.Vehicle;

/**
 * Responsible for managing allocated areas.
 */
public class AreaAllocator
    implements
      Lifecycle {

  private final AreaProvider areaProvider;
  private final AreaAllocations areaAllocations;
  private final BlockAreaAllocations blockAreaAllocations;
  private boolean initialized;

  /**
   * Creates a new instance.
   *
   * @param areaProvider Provides areas related to resources.
   * @param areaAllocations Keeps track of areas allocated by vehicles.
   * @param blockAreaAllocations Ensures that areas related to blocks are allocated by one vehicle
   * at a time.
   */
  @Inject
  public AreaAllocator(
      AreaProvider areaProvider,
      AreaAllocations areaAllocations,
      BlockAreaAllocations blockAreaAllocations
  ) {
    this.areaProvider = requireNonNull(areaProvider, "areaProvider");
    this.areaAllocations = requireNonNull(areaAllocations, "areaAllocations");
    this.blockAreaAllocations = requireNonNull(blockAreaAllocations, "blockAreaAllocations");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

    areaProvider.initialize();
    areaAllocations.initialize();
    blockAreaAllocations.initialize();

    initialized = true;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void terminate() {
    if (!isInitialized()) {
      return;
    }

    areaProvider.terminate();
    areaAllocations.terminate();
    blockAreaAllocations.terminate();

    initialized = false;
  }

  /**
   * Checks if the given vehicle is allowed to allocate the areas related to the given envelope key
   * and the given set of resources.
   *
   * @param vehicleRef The vehicle reference.
   * @param envelopeKey The envelope key.
   * @param resources The set of resources.
   * @return {@code true}, if the areas related to the given envelope key and the given set of
   * resources are not allocated by any vehicle other than the given one, otherwise {@code false}.
   */
  public boolean mayAllocateAreas(
      @Nonnull
      TCSObjectReference<Vehicle> vehicleRef,
      @Nonnull
      String envelopeKey,
      @Nonnull
      Set<TCSResource<?>> resources
  ) {
    requireNonNull(vehicleRef, "vehicleRef");
    requireNonNull(envelopeKey, "envelopeKey");
    requireNonNull(resources, "resources");

    if (resources.isEmpty()) {
      return true;
    }

    return areaAllocations.isAreaAllocationAllowed(
        vehicleRef,
        areaProvider.getAreas(envelopeKey, resources)
    ) && blockAreaAllocations.isAreaAllocationAllowed(vehicleRef, envelopeKey, resources);
  }

  /**
   * Updates the given vehicle's allocated areas to the areas related to the given envelope key
   * and the given set of resources.
   *
   * @param vehicleRef The vehicle reference.
   * @param envelopeKey The envelope key.
   * @param resources The set of resources.
   */
  public void updateAllocatedAreas(
      @Nonnull
      TCSObjectReference<Vehicle> vehicleRef,
      @Nonnull
      String envelopeKey,
      @Nonnull
      Set<TCSResource<?>> resources
  ) {
    requireNonNull(vehicleRef, "vehicleRef");
    requireNonNull(envelopeKey, "envelopeKey");
    requireNonNull(resources, "resources");

    if (resources.isEmpty()) {
      areaAllocations.clearAreaAllocation(vehicleRef);
      return;
    }

    areaAllocations.setAreaAllocation(vehicleRef, areaProvider.getAreas(envelopeKey, resources));
  }
}
