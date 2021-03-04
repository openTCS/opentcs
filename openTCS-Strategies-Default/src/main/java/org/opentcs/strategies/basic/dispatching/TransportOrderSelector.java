/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching;

import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import org.opentcs.components.Lifecycle;
import org.opentcs.data.model.Vehicle;
import org.opentcs.strategies.basic.dispatching.orderselection.NoOrderSelectionStrategy;
import org.opentcs.strategies.basic.dispatching.orderselection.ParkingOrderSelectionStrategy;
import org.opentcs.strategies.basic.dispatching.orderselection.RechargeOrderSelectionStrategy;
import org.opentcs.strategies.basic.dispatching.orderselection.ReservedOrderSelectionStrategy;
import org.opentcs.strategies.basic.dispatching.orderselection.TransportOrderSelectionStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Selects transport orders for vehicles.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class TransportOrderSelector
    implements Lifecycle {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(TransportOrderSelector.class);
  /**
   * A strategy for selecting no order.
   */
  private final NoOrderSelectionStrategy noOrderSelectionStrategy;
  /**
   * A strategy for selecting a reserved order.
   */
  private final ReservedOrderSelectionStrategy reservedOrderSelectionStrategy;
  /**
   * A strategy for selecting a transport order.
   */
  private final TransportOrderSelectionStrategy transportOrderSelectionStrategy;
  /**
   * A strategy for selecting a recharge order.
   */
  private final RechargeOrderSelectionStrategy rechargeOrderSelectionStrategy;
  /**
   * A strategy for selecting a parking order.
   */
  private final ParkingOrderSelectionStrategy parkingOrderSelectionStrategy;
  /**
   * Indicates whether this component is initialized.
   */
  private boolean initialized;

  /**
   * Creates a new instance.
   *
   * @param noOrderSelectionStrategy A strategy for selecting no order.
   * @param reservedOrderSelectionStrategy A strategy for selecting a reserved order.
   * @param transportOrderSelectionStrategy A strategy for selecting a transport order.
   * @param rechargeOrderSelectionStrategy A strategy for selecting recharge orders.
   * @param parkingOrderSelectionStrategy A strategy for selecting parking orders.
   */
  @Inject
  public TransportOrderSelector(
      @Nonnull NoOrderSelectionStrategy noOrderSelectionStrategy,
      @Nonnull ReservedOrderSelectionStrategy reservedOrderSelectionStrategy,
      @Nonnull TransportOrderSelectionStrategy transportOrderSelectionStrategy,
      @Nonnull RechargeOrderSelectionStrategy rechargeOrderSelectionStrategy,
      @Nonnull ParkingOrderSelectionStrategy parkingOrderSelectionStrategy) {
    this.noOrderSelectionStrategy = requireNonNull(noOrderSelectionStrategy,
                                                   "noOrderSelectionStrategy");
    this.reservedOrderSelectionStrategy = requireNonNull(reservedOrderSelectionStrategy,
                                                         "reservedOrderSelectionStrategy");
    this.transportOrderSelectionStrategy = requireNonNull(transportOrderSelectionStrategy,
                                                          "transportOrderSelectionStrategy");
    this.rechargeOrderSelectionStrategy = requireNonNull(rechargeOrderSelectionStrategy,
                                                         "rechargeOrderSelectionStrategy");
    this.parkingOrderSelectionStrategy = requireNonNull(parkingOrderSelectionStrategy,
                                                        "parkingOrderSelectionStrategy");
  }

  @Override
  public void initialize() {
    if (initialized) {
      return;
    }
    noOrderSelectionStrategy.initialize();
    reservedOrderSelectionStrategy.initialize();
    transportOrderSelectionStrategy.initialize();
    rechargeOrderSelectionStrategy.initialize();
    parkingOrderSelectionStrategy.initialize();
    initialized = true;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void terminate() {
    if (!initialized) {
      return;
    }
    noOrderSelectionStrategy.terminate();
    reservedOrderSelectionStrategy.terminate();
    transportOrderSelectionStrategy.terminate();
    rechargeOrderSelectionStrategy.terminate();
    parkingOrderSelectionStrategy.terminate();
    initialized = false;
  }

  @Nonnull
  public VehicleOrderSelection selectTransportOrder(@Nonnull Vehicle vehicle) {
    requireNonNull(vehicle, "vehicle");

    VehicleOrderSelection result;

    LOG.debug("{}: Checking if we should leave the vehicle alone...", vehicle.getName());
    result = noOrderSelectionStrategy.selectOrder(vehicle);
    if (result != null) {
      return result;
    }

    LOG.debug("{}: Checking for reserved order...", vehicle.getName());
    result = reservedOrderSelectionStrategy.selectOrder(vehicle);
    if (result != null) {
      return result;
    }

    LOG.debug("{}: Checking for regular processable transport order...", vehicle.getName());
    result = transportOrderSelectionStrategy.selectOrder(vehicle);
    if (result != null) {
      return result;
    }

    LOG.debug("{}: Checking for recharge order...", vehicle.getName());
    result = rechargeOrderSelectionStrategy.selectOrder(vehicle);
    if (result != null) {
      return result;
    }

    LOG.debug("{}: Checking for parking order...", vehicle.getName());
    result = parkingOrderSelectionStrategy.selectOrder(vehicle);
    if (result != null) {
      return result;
    }

    LOG.debug("{}: No order found - vehicle should not do anything.");
    return new VehicleOrderSelection(null, vehicle, null);
  }

}
