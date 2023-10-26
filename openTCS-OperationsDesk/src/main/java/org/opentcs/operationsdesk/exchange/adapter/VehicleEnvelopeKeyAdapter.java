/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.exchange.adapter;

import java.util.Objects;
import static java.util.Objects.requireNonNull;
import org.opentcs.access.Kernel;
import org.opentcs.access.KernelServicePortal;
import org.opentcs.access.SharedKernelServicePortal;
import org.opentcs.access.SharedKernelServicePortalProvider;
import org.opentcs.components.kernel.services.ServiceUnavailableException;
import org.opentcs.data.model.Vehicle;
import org.opentcs.guing.base.components.properties.event.AttributesChangeEvent;
import org.opentcs.guing.base.components.properties.event.AttributesChangeListener;
import org.opentcs.guing.base.model.elements.VehicleModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Updates a vehicle's envelope key with the kernel when it changes.
 */
public class VehicleEnvelopeKeyAdapter
    implements AttributesChangeListener {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(VehicleEnvelopeKeyAdapter.class);
  /**
   * The vehicle model.
   */
  private final VehicleModel model;
  /**
   * Provides access to a portal.
   */
  private final SharedKernelServicePortalProvider portalProvider;
  /**
   * The vehicle's envelope key the last time we checked it.
   */
  private String previousEnvelopeKey;

  /**
   * Creates a new instance.
   *
   * @param portalProvider A kernel provider.
   * @param model The vehicle model.
   */
  public VehicleEnvelopeKeyAdapter(SharedKernelServicePortalProvider portalProvider,
                                   VehicleModel model) {
    this.portalProvider = requireNonNull(portalProvider, "portalProvider");
    this.model = requireNonNull(model, "model");
    this.previousEnvelopeKey = getEnvelopeKey();
  }

  @Override
  public void propertiesChanged(AttributesChangeEvent e) {
    if (e.getModel() != model) {
      return;
    }

    String envelopeKey = getEnvelopeKey();
    if (Objects.equals(previousEnvelopeKey, envelopeKey)) {
      LOG.debug("Ignoring vehicle properties update as the envelope key did not change.");
      return;
    }

    previousEnvelopeKey = envelopeKey;
    new Thread(() -> updateEnvelopeKeyInKernel(envelopeKey)).start();
  }

  private String getEnvelopeKey() {
    return model.getPropertyEnvelopeKey().getText();
  }

  private void updateEnvelopeKeyInKernel(String envelopeKey) {
    try (SharedKernelServicePortal sharedPortal = portalProvider.register()) {
      KernelServicePortal portal = sharedPortal.getPortal();
      // Check if the kernel is in operating mode, too.
      if (portal.getState() == Kernel.State.OPERATING) {
        Vehicle vehicle = portal.getVehicleService().fetchObject(Vehicle.class, model.getName());
        if (Objects.equals(vehicle.getEnvelopeKey(), envelopeKey)) {
          LOG.debug("Ignoring vehicle properties update. Already up do date.");
          return;
        }
        portal.getVehicleService().updateVehicleEnvelopeKey(vehicle.getReference(),
                                                            envelopeKey);
      }

    }
    catch (ServiceUnavailableException exc) {
      LOG.warn("Could not connect to kernel", exc);
    }
  }
}
