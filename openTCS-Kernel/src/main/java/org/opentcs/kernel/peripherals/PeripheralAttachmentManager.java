/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.peripherals;

import java.util.List;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import org.opentcs.components.Lifecycle;
import org.opentcs.components.kernel.services.InternalPeripheralService;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.drivers.peripherals.PeripheralCommAdapter;
import org.opentcs.drivers.peripherals.PeripheralCommAdapterDescription;
import org.opentcs.drivers.peripherals.PeripheralCommAdapterFactory;
import org.opentcs.drivers.peripherals.PeripheralProcessModel;
import org.opentcs.drivers.peripherals.management.PeripheralAttachmentEvent;
import org.opentcs.drivers.peripherals.management.PeripheralAttachmentInformation;
import org.opentcs.drivers.peripherals.management.PeripheralProcessModelEvent;
import org.opentcs.kernel.KernelApplicationConfiguration;
import org.opentcs.util.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages attachment and detachment of peripheral communication adapters to location.
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class PeripheralAttachmentManager
    implements Lifecycle {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(PeripheralAttachmentManager.class);
  /**
   * This class's configuration.
   */
  private final KernelApplicationConfiguration configuration;
  /**
   * The peripheral service.
   */
  private final InternalPeripheralService peripheralService;
  /**
   * The peripheral controller pool.
   */
  private final LocalPeripheralControllerPool controllerPool;
  /**
   * The peripheral comm adapter registry.
   */
  private final PeripheralCommAdapterRegistry commAdapterRegistry;
  /**
   * The pool of peripheral entries.
   */
  private final PeripheralEntryPool peripheralEntryPool;
  /**
   * The handler to send events to.
   */
  private final EventHandler eventHandler;
  /**
   * Whether the attachment manager is initialized or not.
   */
  private boolean initialized;

  /**
   * Creates a new instance.
   *
   * @param peripheralService The peripheral service.
   * @param controllerPool The peripheral controller pool.
   * @param commAdapterRegistry The peripheral comm adapter registry.
   * @param peripheralEntryPool The pool of peripheral entries.
   * @param eventHandler The handler to send events to.
   * @param configuration This class's configuration.
   */
  @Inject
  public PeripheralAttachmentManager(@Nonnull InternalPeripheralService peripheralService,
                                     @Nonnull LocalPeripheralControllerPool controllerPool,
                                     @Nonnull PeripheralCommAdapterRegistry commAdapterRegistry,
                                     @Nonnull PeripheralEntryPool peripheralEntryPool,
                                     @Nonnull @ApplicationEventBus EventHandler eventHandler,
                                     @Nonnull KernelApplicationConfiguration configuration) {
    this.peripheralService = requireNonNull(peripheralService, "peripheralService");
    this.controllerPool = requireNonNull(controllerPool, "controllerPool");
    this.commAdapterRegistry = requireNonNull(commAdapterRegistry, "commAdapterRegistry");
    this.peripheralEntryPool = requireNonNull(peripheralEntryPool, "peripheralEntryPool");
    this.eventHandler = requireNonNull(eventHandler, "eventHandler");
    this.configuration = requireNonNull(configuration, "configuration");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

    commAdapterRegistry.initialize();
    peripheralEntryPool.initialize();

    autoAttachAllAdapters();
    LOG.debug("Locations attached: {}", peripheralEntryPool.getEntries());

    if (configuration.autoEnablePeripheralDriversOnStartup()) {
      autoEnableAllAdapters();
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
      LOG.debug("Not initialized.");
      return;
    }

    // Disable and terminate all attached drivers to clean up.
    disableAndTerminateAllAdapters();
    peripheralEntryPool.terminate();
    commAdapterRegistry.terminate();

    initialized = false;
  }

  /**
   * Attaches a peripheral comm adapter to a location.
   *
   * @param location The location to attach to.
   * @param description The description of the comm adapter to attach.
   */
  public void attachAdapterToLocation(@Nonnull TCSResourceReference<Location> location,
                                      @Nonnull PeripheralCommAdapterDescription description) {
    requireNonNull(location, "location");
    requireNonNull(description, "description");

    attachAdapterToLocation(peripheralEntryPool.getEntryFor(location),
                            commAdapterRegistry.findFactoryFor(description));
  }

  /**
   * Returns the attachment information for a location.
   *
   * @param location The location to get attachment information about.
   * @return The attachment information for a location.
   */
  @Nonnull
  public PeripheralAttachmentInformation getAttachmentInformation(
      @Nonnull TCSResourceReference<Location> location) {
    requireNonNull(location, "location");

    PeripheralEntry entry = peripheralEntryPool.getEntryFor(location);
    return new PeripheralAttachmentInformation(entry.getLocation(),
                                               entry.getCommAdapterFactory().getDescription());
  }

  private void attachAdapterToLocation(PeripheralEntry entry,
                                       PeripheralCommAdapterFactory factory) {
    Location location = peripheralService.fetchObject(Location.class, entry.getLocation());
    PeripheralCommAdapter commAdapter = factory.getAdapterFor(location);
    if (commAdapter == null) {
      LOG.warn("Factory {} did not provide adapter for location {}, ignoring.",
               factory,
               entry.getLocation().getName());
      return;
    }

    // Perform a cleanup for the old adapter.
    disableAndTerminateAdapter(entry);
    controllerPool.detachPeripheralController(entry.getLocation());

    commAdapter.initialize();
    controllerPool.attachPeripheralController(entry.getLocation(), commAdapter);

    entry.setCommAdapterFactory(factory);
    entry.setCommAdapter(commAdapter);

    // Publish events about the new attached adapter.
    eventHandler.onEvent(new PeripheralAttachmentEvent(
        entry.getLocation(),
        new PeripheralAttachmentInformation(entry.getLocation(),
                                            entry.getCommAdapterFactory().getDescription()))
    );
    eventHandler.onEvent(new PeripheralProcessModelEvent(
        entry.getLocation(),
        PeripheralProcessModel.Attribute.LOCATION.name(),
        entry.getProcessModel()
    ));
  }

  private void autoAttachAdapterToLocation(PeripheralEntry peripheralEntry) {
    // Do not auto-attach if there is already a (real) comm adapter attached to the location.
    if (!(peripheralEntry.getCommAdapter() instanceof NullPeripheralCommAdapter)) {
      return;
    }

    Location location = peripheralService.fetchObject(Location.class,
                                                      peripheralEntry.getLocation());
    List<PeripheralCommAdapterFactory> factories = commAdapterRegistry.findFactoriesFor(location);
    if (!factories.isEmpty()) {
      LOG.debug("Attaching {} to first available adapter: {}.",
                peripheralEntry.getLocation().getName(),
                factories.get(0).getDescription().getDescription());
      attachAdapterToLocation(peripheralEntry, factories.get(0));
    }
  }

  private void autoAttachAllAdapters() {
    peripheralEntryPool.getEntries().forEach((location, entry) -> {
      autoAttachAdapterToLocation(entry);
    });
  }

  private void disableAndTerminateAdapter(PeripheralEntry peripheralEntry) {
    peripheralEntry.getCommAdapter().disable();
    peripheralEntry.getCommAdapter().terminate();
  }

  private void autoEnableAllAdapters() {
    peripheralEntryPool.getEntries().values().stream()
        .map(entry -> entry.getCommAdapter())
        .filter(adapter -> !adapter.isEnabled())
        .forEach(adapter -> adapter.enable());
  }

  private void disableAndTerminateAllAdapters() {
    LOG.debug("Detaching peripheral communication adapters...");
    peripheralEntryPool.getEntries().forEach((location, entry) -> {
      disableAndTerminateAdapter(entry);
    });
    LOG.debug("Detached peripheral communication adapters");
  }
}
