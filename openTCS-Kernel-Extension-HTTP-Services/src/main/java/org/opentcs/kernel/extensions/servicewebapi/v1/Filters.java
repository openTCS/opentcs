/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1;

import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.data.peripherals.PeripheralJob;

/**
 * Provides some commonly used object filters.
 */
public class Filters {

  /**
   * Prevents instantiation.
   */
  private Filters() {
  }

  /**
   * Returns a predicate that is true only for transport orders whose intended vehicle is the given
   * one.
   * In case the given vehicle reference is null, all transport orders are accepted.
   *
   * @param vehicleRef The vehicle reference.
   * @return A predicate that is true only for transport orders whose intended vehicle is the given
   * one.
   */
  public static Predicate<TransportOrder> transportOrderWithIntendedVehicle(
      @Nullable TCSObjectReference<Vehicle> vehicleRef
  ) {
    return vehicleRef == null
        ? order -> true
        : order -> Objects.equals(vehicleRef, order.getIntendedVehicle());
  }

  /**
   * Returns a predicate that is true only for order sequences whose intended vehicle is the given
   * one.
   * In case the given vehicle reference is null, all order sequences are accepted.
   *
   * @param vehicleRef The vehicle reference.
   * @return A predicate that is true only for order sequences whose intended vehicle is the given
   * one.
   */
  public static Predicate<OrderSequence> orderSequenceWithIntendedVehicle(
      @Nullable TCSObjectReference<Vehicle> vehicleRef
  ) {
    return vehicleRef == null
        ? sequence -> true
        : sequence -> Objects.equals(vehicleRef, sequence.getIntendedVehicle());
  }

  /**
   * Returns a predicate that is true only for peripheral jobs whose related vehicle is the given
   * one.
   * In case the given vehicle reference is null, all peripheral jobs are accepted.
   *
   * @param vehicleRef The vehicle reference.
   * @return A predicate that is true only for peripheral jobs whose related vehicle is the given
   * one.
   */
  public static Predicate<PeripheralJob> peripheralJobWithRelatedVehicle(
      @Nullable TCSObjectReference<Vehicle> vehicleRef
  ) {
    return vehicleRef == null
        ? job -> true
        : job -> Objects.equals(vehicleRef, job.getRelatedVehicle());
  }

  /**
   * Returns a predicate that is true only for peripheral jobs whose related transport order is the
   * given one.
   * In case the given vehicle reference is null, all peripheral jobs are accepted.
   *
   * @param orderRef The transport order reference.
   * @return A predicate that is true only for peripheral jobs whose related transport order is the
   * given one.
   */
  public static Predicate<PeripheralJob> peripheralJobWithRelatedTransportOrder(
      @Nullable TCSObjectReference<TransportOrder> orderRef
  ) {
    return orderRef == null
        ? job -> true
        : job -> Objects.equals(orderRef, job.getRelatedTransportOrder());
  }

  /**
   * Returns a predicate that is true only for vehicles whose processing state is the given one.
   * In case the given procState is null, all vehicles are accepted.
   *
   * @param procState The processing state.
   * @return A predicate that is true only for vehicles whose processing state is the given one.
   */
  public static Predicate<Vehicle> vehicleWithProcState(@Nullable Vehicle.ProcState procState) {
    return procState == null
        ? vehicle -> true
        : vehicle -> Objects.equals(procState, vehicle.getProcState());
  }
}
