/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.peripherals;

import java.util.HashMap;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import org.opentcs.components.Lifecycle;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.TCSResourceReference;
import static org.opentcs.util.Assertions.checkArgument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a pool of {@link PeripheralEntry}s with an entry for every {@link Location} object in
 * the kernel.
 */
public class PeripheralEntryPool
    implements Lifecycle {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(PeripheralEntryPool.class);
  /**
   * The object service.
   */
  private final TCSObjectService objectService;
  /**
   * The peripheral comm adapter registry.
   */
  private final PeripheralCommAdapterRegistry commAdapterRegistry;
  /**
   * The entries of this pool.
   */
  private final Map<TCSResourceReference<Location>, PeripheralEntry> entries = new HashMap<>();
  /**
   * Whether the pool is initialized or not.
   */
  private boolean initialized;

  /**
   * Creates a new instance.
   *
   * @param objectService The object service.
   * @param commAdapterRegistry The peripheral comm adapter registry.
   */
  @Inject
  public PeripheralEntryPool(@Nonnull TCSObjectService objectService,
                             @Nonnull PeripheralCommAdapterRegistry commAdapterRegistry) {
    this.objectService = requireNonNull(objectService, "objectService");
    this.commAdapterRegistry = requireNonNull(commAdapterRegistry, "commAdapterRegistry");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

    for (Location location : objectService.fetchObjects(Location.class)) {
      entries.put(
          location.getReference(),
          new PeripheralEntry(
              location,
              commAdapterRegistry.findFactoriesFor(location).stream()
                  .map(factory -> factory.getDescription())
                  .collect(Collectors.toList())
          )
      );
    }

    LOG.debug("Initialized peripheral entry pool: {}", entries);
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

    entries.clear();
    initialized = false;
  }

  @Nonnull
  public Map<TCSResourceReference<Location>, PeripheralEntry> getEntries() {
    return entries;
  }

  /**
   * Returns the {@link PeripheralEntry} for the given location.
   *
   * @param location The reference to the location.
   * @return The entry for the given location.
   * @throws IllegalArgumentException If no entry is present for the given location.
   */
  @Nonnull
  public PeripheralEntry getEntryFor(@Nonnull TCSResourceReference<Location> location)
      throws IllegalArgumentException {
    requireNonNull(location, "location");
    checkArgument(entries.containsKey(location),
                  "No peripheral entry present for %s",
                  location.getName());
    return entries.get(location);
  }
}
