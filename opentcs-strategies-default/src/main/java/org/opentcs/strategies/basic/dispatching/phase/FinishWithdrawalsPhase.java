// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.dispatching.phase;

import static java.util.Objects.requireNonNull;

import jakarta.inject.Inject;
import org.opentcs.components.kernel.services.InternalTCSObjectService;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.dispatching.Phase;
import org.opentcs.strategies.basic.dispatching.TransportOrderUtil;

/**
 * Finishes withdrawals of transport orders after the vehicle has come to a halt.
 */
public class FinishWithdrawalsPhase
    implements
      Phase {

  /**
   * The object service
   */
  private final InternalTCSObjectService objectService;
  private final TransportOrderUtil transportOrderUtil;
  /**
   * Indicates whether this component is initialized.
   */
  private boolean initialized;

  @Inject
  public FinishWithdrawalsPhase(
      InternalTCSObjectService objectService,
      TransportOrderUtil transportOrderUtil
  ) {
    this.objectService = requireNonNull(objectService, "objectService");
    this.transportOrderUtil = requireNonNull(transportOrderUtil, "transportOrderUtil");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
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
      return;
    }
    initialized = false;
  }

  @Override
  public void run() {
    objectService.fetch(Vehicle.class).stream()
        .filter(vehicle -> vehicle.hasProcState(Vehicle.ProcState.AWAITING_ORDER))
        .filter(vehicle -> hasWithdrawnTransportOrder(vehicle))
        .forEach(vehicle -> transportOrderUtil.finishAbortion(vehicle));
  }

  private boolean hasWithdrawnTransportOrder(Vehicle vehicle) {
    return objectService.fetch(TransportOrder.class, vehicle.getTransportOrder())
        .orElseThrow()
        .hasState(TransportOrder.State.WITHDRAWN);
  }

}
