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
import javax.inject.Inject;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.drivers.peripherals.PeripheralCommAdapter;
import org.opentcs.drivers.peripherals.PeripheralController;
import static org.opentcs.util.Assertions.checkArgument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maintains associations of {@link Location}, {@link PeripheralController} and
 * {@link PeripheralCommAdapter}.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class DefaultPeripheralControllerPool
    implements LocalPeripheralControllerPool {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DefaultPeripheralControllerPool.class);
  /**
   * The object service to use.
   */
  private final TCSObjectService objectService;
  /**
   * A factory for peripheral controllers.
   */
  private final PeripheralControllerFactory controllerFactory;
  /**
   * The entries of this pool mapped to the corresponding locations.
   */
  private final Map<TCSResourceReference<Location>, PoolEntry> poolEntries = new HashMap<>();
  /**
   * Indicates whether this component is initialized.
   */
  private boolean initialized;

  /**
   * Creates a new DefaultPeripheralControllerPool.
   *
   * @param objectService The object service to be used.
   * @param controllerFactory The controller factory to be used.
   */
  @Inject
  public DefaultPeripheralControllerPool(TCSObjectService objectService,
                                         PeripheralControllerFactory controllerFactory) {
    this.objectService = requireNonNull(objectService, "objectService");
    this.controllerFactory = requireNonNull(controllerFactory, "controllerFactory");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      LOG.debug("Already initialized, doing nothing.");
      return;
    }

    initialized = true;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void terminate() {
    if (!isInitialized()) {
      LOG.debug("Not initialized, doing nothing.");
      return;
    }
    // Detach all peripherals.
    for (PoolEntry curEntry : poolEntries.values()) {
      curEntry.controller.terminate();
    }
    poolEntries.clear();

    initialized = false;
  }

  @Override
  public PeripheralController getPeripheralController(TCSResourceReference<Location> locationRef)
      throws IllegalArgumentException {
    requireNonNull(locationRef, "locationRef");
    checkArgument(poolEntries.containsKey(locationRef),
                  "No controller present for %s",
                  locationRef.getName());

    return poolEntries.get(locationRef).getController();
  }

  @Override
  public void attachPeripheralController(TCSResourceReference<Location> locationRef,
                                         PeripheralCommAdapter commAdapter)
      throws IllegalArgumentException {
    requireNonNull(locationRef, "locationRef");
    requireNonNull(commAdapter, "commAdapter");

    if (poolEntries.containsKey(locationRef)) {
      LOG.warn("{}: Peripheral controller already attached, doing nothing.", locationRef.getName());
      return;
    }

    Location location = objectService.fetchObject(Location.class, locationRef);
    checkArgument(location != null, "No such location: %s", locationRef.getName());

    LOG.debug("{}: Attaching controller...", locationRef.getName());
    PeripheralController controller = controllerFactory.createVehicleController(locationRef,
                                                                                commAdapter);
    poolEntries.put(locationRef, new PoolEntry(locationRef, controller, commAdapter));
    controller.initialize();
  }

  @Override
  public void detachPeripheralController(TCSResourceReference<Location> locationRef) {
    requireNonNull(locationRef, "locationRef");

    if (!poolEntries.containsKey(locationRef)) {
      LOG.warn("{}: No peripheral controller attached, doing nothing.", locationRef.getName());
      return;
    }

    LOG.debug("{}: Detaching controller...", locationRef.getName());
    poolEntries.remove(locationRef).getController().terminate();
  }

  /**
   * An entry in this controller pool.
   */
  private static class PoolEntry {

    /**
     * The location.
     */
    private final TCSResourceReference<Location> location;
    /**
     * The peripheral controller associated with the location.
     */
    private final PeripheralController controller;
    /**
     * The comm adapter associated with the location.
     */
    private final PeripheralCommAdapter commAdapter;

    /**
     * Creates a new pool entry.
     *
     * @param location The location.
     * @param controller The peripheral controller associated with the location.
     * @param cmmmAdapter The comm adapter associated with the location.
     */
    private PoolEntry(TCSResourceReference<Location> location,
                      PeripheralController controller,
                      PeripheralCommAdapter cmmmAdapter) {
      this.location = requireNonNull(location, "location");
      this.controller = requireNonNull(controller, "controller");
      this.commAdapter = requireNonNull(cmmmAdapter, "cmmmAdapter");
    }

    public TCSResourceReference<Location> getLocation() {
      return location;
    }

    public PeripheralController getController() {
      return controller;
    }

    public PeripheralCommAdapter getCommAdapter() {
      return commAdapter;
    }
  }
}
