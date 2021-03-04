/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.phase.recharging;

import java.util.ArrayList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.Optional;
import java.util.UUID;
import javax.inject.Inject;
import org.opentcs.access.to.order.DestinationCreationTO;
import org.opentcs.access.to.order.TransportOrderCreationTO;
import org.opentcs.components.kernel.Router;
import org.opentcs.components.kernel.services.InternalTransportOrderService;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.dispatching.DefaultDispatcherConfiguration;
import org.opentcs.strategies.basic.dispatching.Phase;
import org.opentcs.strategies.basic.dispatching.ProcessabilityChecker;
import org.opentcs.strategies.basic.dispatching.TransportOrderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates recharging orders for any vehicles with a degraded energy level.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
@SuppressWarnings("deprecation")
public class RechargeIdleVehiclesPhase
    implements Phase {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(RechargeIdleVehiclesPhase.class);
  /**
   * The transport order service.
   */
  private final InternalTransportOrderService orderService;
  /**
   * The strategy used for finding suitable recharge locations.
   */
  private final org.opentcs.components.kernel.RechargePositionSupplier rechargePosSupplier;
  /**
   * The Router instance calculating route costs.
   */
  private final Router router;
  /**
   * Checks processability of transport orders for vehicles.
   */
  private final ProcessabilityChecker processabilityChecker;

  private final TransportOrderUtil transportOrderUtil;
  /**
   * The dispatcher configuration.
   */
  private final DefaultDispatcherConfiguration configuration;
  /**
   * Indicates whether this component is initialized.
   */
  private boolean initialized;

  @Inject
  public RechargeIdleVehiclesPhase(
      InternalTransportOrderService orderService,
      org.opentcs.components.kernel.RechargePositionSupplier rechargePosSupplier,
      Router router,
      ProcessabilityChecker processabilityChecker,
      TransportOrderUtil transportOrderUtil,
      DefaultDispatcherConfiguration configuration) {
    this.router = requireNonNull(router, "router");
    this.orderService = requireNonNull(orderService, "orderService");
    this.rechargePosSupplier = requireNonNull(rechargePosSupplier, "rechargePosSupplier");
    this.processabilityChecker = requireNonNull(processabilityChecker, "processabilityChecker");
    this.transportOrderUtil = requireNonNull(transportOrderUtil, "transportOrderUtil");
    this.configuration = requireNonNull(configuration, "configuration");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

    rechargePosSupplier.initialize();

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

    rechargePosSupplier.terminate();

    initialized = false;
  }

  @Override
  public void run() {
    if (!configuration.rechargeIdleVehicles()) {
      return;
    }

    for (Vehicle vehicle : orderService.fetchObjects(Vehicle.class, this::idleAndDegraded)) {
      createRechargeOrder(vehicle);
    }
  }

  private void createRechargeOrder(Vehicle vehicle) {
    List<DriveOrder.Destination> rechargeDests = rechargePosSupplier.findRechargeSequence(vehicle);
    LOG.debug("Recharge sequence for {}: {}", vehicle, rechargeDests);

    if (rechargeDests.isEmpty()) {
      LOG.info("{}: Did not find a suitable recharge sequence.", vehicle.getName());
      return;
    }

    List<DestinationCreationTO> chargeDests = new ArrayList<>(rechargeDests.size());
    for (DriveOrder.Destination dest : rechargeDests) {
      chargeDests.add(
          new DestinationCreationTO(dest.getDestination().getName(), dest.getOperation())
              .withProperties(dest.getProperties())
      );
    }
    // Create a transport order for recharging and verify its processability.
    // The recharge order may be withdrawn unless its energy level is critical.
    TransportOrder rechargeOrder = orderService.createTransportOrder(
        new TransportOrderCreationTO("Recharge-" + UUID.randomUUID(), chargeDests)
            .withIntendedVehicleName(vehicle.getName())
            .withDispensable(!vehicle.isEnergyLevelCritical())
    );

    Point vehiclePosition = orderService.fetchObject(Point.class, vehicle.getCurrentPosition());
    Optional<List<DriveOrder>> driveOrders
        = router.getRoute(vehicle, vehiclePosition, rechargeOrder);
    if (processabilityChecker.checkProcessability(vehicle, rechargeOrder)
        && driveOrders.isPresent()) {
      transportOrderUtil.assignTransportOrder(vehicle, rechargeOrder, driveOrders.get());
    }
    else {
      // Mark the order as failed, since the vehicle cannot execute it.
      orderService.updateTransportOrderState(rechargeOrder.getReference(),
                                             TransportOrder.State.FAILED);
    }
  }

  private boolean idleAndDegraded(Vehicle vehicle) {
    return vehicle.getIntegrationLevel() == Vehicle.IntegrationLevel.TO_BE_UTILIZED
        && vehicle.hasProcState(Vehicle.ProcState.IDLE)
        && vehicle.hasState(Vehicle.State.IDLE)
        && vehicle.getCurrentPosition() != null
        && vehicle.getOrderSequence() == null
        && vehicle.isEnergyLevelDegraded();
  }

}
