// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.notification.UserNotification;
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
      @Nullable
      TCSObjectReference<Vehicle> vehicleRef
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
      @Nullable
      TCSObjectReference<Vehicle> vehicleRef
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
      @Nullable
      TCSObjectReference<Vehicle> vehicleRef
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
      @Nullable
      TCSObjectReference<TransportOrder> orderRef
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
  public static Predicate<Vehicle> vehicleWithProcState(
      @Nullable
      Vehicle.ProcState procState
  ) {
    return procState == null
        ? vehicle -> true
        : vehicle -> Objects.equals(procState, vehicle.getProcState());
  }

  /**
   * Returns a predicate that is true only for user notification whose creation timestamp is after
   * the given time threshold.
   *
   * @param timeThreshold The time threshold.
   * @return A predicate that is true only for user notification whose creation timestamp is after
   * the given time threshold.
   */
  public static Predicate<UserNotification> userNotificationCreatedAfter(
      @Nonnull
      Instant timeThreshold
  ) {
    return notification -> notification.getTimestamp().isAfter(timeThreshold);
  }

  /**
   * Returns a predicate that is true only for {@link TCSObject}s whose name is contained in the
   * given list of names.
   * In case the given list if names is null or empty, all {@link TCSObject}s are accepted.
   *
   * @param names The list of names.
   * @return A predicate that is true only for {@link TCSObject}s whose name is contained in the
   * given list of names.
   */
  public static Predicate<TCSObject<?>> objectNameMatchesOneOf(
      @Nullable
      List<String> names
  ) {
    return names == null || names.isEmpty()
        ? tcsObject -> true
        : tcsObject -> names.contains(tcsObject.getName());
  }
}
