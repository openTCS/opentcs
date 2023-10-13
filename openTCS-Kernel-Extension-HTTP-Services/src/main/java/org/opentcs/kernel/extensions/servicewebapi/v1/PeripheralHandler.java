/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1;

import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.opentcs.components.kernel.services.PeripheralService;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.model.Location;
import org.opentcs.drivers.peripherals.PeripheralCommAdapterDescription;
import org.opentcs.drivers.peripherals.management.PeripheralAttachmentInformation;
import org.opentcs.kernel.extensions.servicewebapi.KernelExecutorWrapper;

/**
 * Handles requests related to peripherals.
 */
public class PeripheralHandler {

  private final PeripheralService peripheralService;
  private final KernelExecutorWrapper executorWrapper;

  /**
   * Creates a new instance.
   *
   * @param peripheralService The service used to manage peripherals.
   * @param executorWrapper Executes calls via the kernel executor and waits for the outcome.
   */
  @Inject
  public PeripheralHandler(PeripheralService peripheralService,
                           KernelExecutorWrapper executorWrapper) {
    this.peripheralService = requireNonNull(peripheralService, "peripheralService");
    this.executorWrapper = requireNonNull(executorWrapper, "executorWrapper");
  }

  public void putPeripheralCommAdapter(String name, String value)
      throws ObjectUnknownException {
    requireNonNull(name, "name");
    requireNonNull(value, "value");

    executorWrapper.callAndWait(() -> {
      Location location = peripheralService.fetchObject(Location.class, name);
      if (location == null) {
        throw new ObjectUnknownException("Unknown location: " + name);
      }

      PeripheralCommAdapterDescription newAdapter
          = peripheralService.fetchAttachmentInformation(location.getReference())
              .getAvailableCommAdapters()
              .stream()
              .filter(description -> description.getClass().getName().equals(value))
              .findAny()
              .orElseThrow(
                  () -> new IllegalArgumentException(
                      "Unknown peripheral driver class name: " + value
                  )
              );

      peripheralService.attachCommAdapter(location.getReference(), newAdapter);
    });
  }

  public void putPeripheralCommAdapterEnabled(String name, String value)
      throws ObjectUnknownException, IllegalArgumentException {
    requireNonNull(name, "name");
    requireNonNull(value, "value");

    executorWrapper.callAndWait(() -> {
      Location location = peripheralService.fetchObject(Location.class, name);
      if (location == null) {
        throw new ObjectUnknownException("Unknown location: " + name);
      }

      if (Boolean.parseBoolean(value)) {
        peripheralService.enableCommAdapter(location.getReference());
      }
      else {
        peripheralService.disableCommAdapter(location.getReference());
      }

    });
  }

  public PeripheralAttachmentInformation getPeripheralCommAdapterAttachmentInformation(String name)
      throws ObjectUnknownException {
    requireNonNull(name, "name");

    return executorWrapper.callAndWait(() -> {
      Location location = peripheralService.fetchObject(Location.class, name);
      if (location == null) {
        throw new ObjectUnknownException("Unknown location: " + name);
      }

      return peripheralService.fetchAttachmentInformation(location.getReference());
    });
  }

}
