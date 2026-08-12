// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v2_0;

import static java.util.Objects.requireNonNull;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_VEHICLE_DEVIATION_EXTENSION_TRIGGER;

import com.google.inject.assistedinject.Assisted;
import jakarta.inject.Inject;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.MovementCommand;

/**
 * Determines whether the deviation (of the very first node in an order) should be extended to
 * include the vehicle's position.
 */
public class DeviationExtensionTrigger {

  /**
   * The trigger that determines whether the deviation should be extended.
   */
  private final Trigger trigger;
  /**
   * Whether the deviation should be extended as a result of a manual request.
   */
  private boolean extendByManualRequest = false;
  /**
   * Whether the deviation should be extended as a result of a "cancelOrder" instant action.
   */
  private boolean extendByCancelOrder = false;

  /**
   * Creates a new instance.
   *
   * @param vehicle The vehicle to create the trigger for.
   */
  @Inject
  public DeviationExtensionTrigger(
      @Assisted
      Vehicle vehicle
  ) {
    this.trigger = Trigger.fromVehicle(requireNonNull(vehicle, "vehicle"));
  }

  /**
   * Determines whether the deviation for the given movement command should be extended to include
   * the vehicle's position.
   *
   * @param command The movement command to check.
   * @return {@code true} if it's the movement command for the very first node of an order and the
   * deviation should be extended, {@code false} otherwise.
   */
  public boolean extendDeviation(MovementCommand command) {
    if (!isFirstMovementCommandInRoute(command)) {
      return false;
    }

    return switch (trigger) {
      case ALWAYS -> true;
      case NEVER -> false;
      case AUTO -> extendByCancelOrder;
      case MANUALLY -> extendByManualRequest;
      case AUTO_AND_MANUALLY -> extendByCancelOrder || extendByManualRequest;
    };
  }

  /**
   * Disables deviation extension that has been enabled via {@link #onCancelOrderEnqueued()} or
   * {@link #onExtensionRequestedManually()}.
   */
  public void reset() {
    extendByCancelOrder = false;
    extendByManualRequest = false;
  }

  /**
   * Called when a "cancelOrder" instant action has been enqueued.
   * <p>
   * After calling this method, deviation extension is enabled (for movement commands for the very
   * first node of orders) until {@link #reset()} is invoked.
   * This only takes effect if the vehicle is configured for the {@link Trigger#AUTO} or
   * {@link Trigger#AUTO_AND_MANUALLY} trigger.
   * </p>
   */
  public void onCancelOrderEnqueued() {
    if (trigger == Trigger.AUTO || trigger == Trigger.AUTO_AND_MANUALLY) {
      extendByCancelOrder = true;
    }
  }

  /**
   * Called when an extension of the deviation has been requested manually.
   * <p>
   * After calling this method, deviation extension is enabled (for movement commands for the very
   * first node of orders) until {@link #reset()} is invoked.
   * This only takes effect if the vehicle is configured for the {@link Trigger#MANUALLY} or
   * {@link Trigger#AUTO_AND_MANUALLY} trigger.
   * </p>
   */
  public void onExtensionRequestedManually() {
    if (trigger == Trigger.MANUALLY || trigger == Trigger.AUTO_AND_MANUALLY) {
      extendByManualRequest = true;
    }
  }

  private boolean isFirstMovementCommandInRoute(MovementCommand command) {
    return command.getStep().getRouteIndex() == 0;
  }

  /**
   * The trigger that determines whether the deviation should be extended (for the very first node
   * of an order).
   */
  private enum Trigger {

    /**
     * The deviation should always be extended.
     */
    ALWAYS,
    /**
     * The deviation should never be extended.
     */
    NEVER,
    /**
     * The deviation should be extended (once) after a "cancelOrder" instant action.
     */
    AUTO,
    /**
     * The deviation should be extended (once) after a manual request.
     */
    MANUALLY,
    /**
     * The combination of {@link #AUTO} and {@link #MANUALLY}.
     */
    AUTO_AND_MANUALLY;

    public static Trigger fromVehicle(Vehicle vehicle) {
      return switch (vehicle.getProperty(PROPKEY_VEHICLE_DEVIATION_EXTENSION_TRIGGER)) {
        case "never" -> NEVER;
        case "auto" -> AUTO;
        case "manually" -> MANUALLY;
        case "auto+manually" -> AUTO_AND_MANUALLY;
        case null, default -> ALWAYS;
      };
    }
  }
}
