// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v1_1;

import static java.util.Objects.requireNonNull;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ErrorTypes.NO_ROUTE_ERROR;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ErrorTypes.ORDER_ERROR;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ErrorTypes.ORDER_UPDATE_ERROR;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ErrorTypes.VALIDATION_ERROR;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.state.ActionStatus;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.state.ErrorEntry;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.state.ErrorLevel;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.state.InfoLevel;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.state.Load;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.state.OperatingMode;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.state.State;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.LoadHandlingDevice;

/**
 * Utility methods for mapping vehicle state information.
 */
public class StateMappings {

  /**
   * Prevents instantiation.
   */
  private StateMappings() {
  }

  /**
   * Returns a vehicle state derived from the given state message.
   *
   * @param state The state message.
   * @return A vehicle state derived from the given state message.
   */
  public static Vehicle.State toVehicleState(State state) {
    requireNonNull(state, "state");

    for (ErrorEntry error : state.getErrors()) {
      if (error.getErrorLevel() == ErrorLevel.FATAL) {
        return Vehicle.State.ERROR;
      }
    }

    if (state.getOperatingMode() != OperatingMode.AUTOMATIC
        && state.getOperatingMode() != OperatingMode.SEMIAUTOMATIC) {
      return Vehicle.State.UNAVAILABLE;
    }

    if (state.getBatteryState().isCharging()) {
      return Vehicle.State.CHARGING;
    }

    if (state.isDriving()
        || hasPendingMovement(state)
        || hasPendingAction(state)) {
      return Vehicle.State.EXECUTING;
    }

    return Vehicle.State.IDLE;
  }

  /**
   * Returns a list of load handling devices derived from the given state message.
   *
   * @param state The state message.
   * @return A list of load handling devices derived from the given state message.
   */
  public static List<LoadHandlingDevice> toLoadHandlingDevices(State state) {
    requireNonNull(state, "state");

    List<LoadHandlingDevice> lhds = new ArrayList<>();
    if (state.getLoads() != null) {
      int index = 0;
      for (Load load : state.getLoads()) {
        String label = "LHD-" + index++;
        if (load.getLoadPosition() != null && !load.getLoadPosition().isBlank()) {
          label = load.getLoadPosition();
        }
        lhds.add(new LoadHandlingDevice(label, true));
      }
    }
    return lhds;
  }

  /**
   * Returns the length of the vehicle based on the loads reported in the given state.
   *
   * @param state The state message.
   * @param lengthUnloaded The length of the vehicle when unloaded.
   * @param lengthLoaded The length of the vehicle when loaded.
   * @return The length of the vehicle based on the loads reported in the given state.
   */
  public static long toVehicleLength(
      @Nonnull
      State state,
      long lengthUnloaded,
      long lengthLoaded
  ) {
    requireNonNull(state, "state");

    if (state.getLoads() == null || state.getLoads().isEmpty()) {
      return lengthUnloaded;
    }
    else {
      return lengthLoaded;
    }
  }

  public static String toErrorPropertyValue(State state, ErrorLevel errorLevel) {
    requireNonNull(state, "state");
    requireNonNull(errorLevel, "errorLevel");

    return String.join(
        ", ",
        state.getErrors().stream()
            .filter(error -> error.getErrorLevel() == errorLevel)
            .map(error -> error.getErrorType())
            .distinct()
            .sorted()
            .collect(Collectors.toList())
    );
  }

  /**
   * Returns a concatenated list of all info types with the specified info level from
   * the given state message.
   *
   * @param state The state message.
   * @param infoLevel The info level of the info types to be returned.
   * @return A concatenated list of info types from the given state message.
   */
  @Nonnull
  public static String toInfoPropertyValue(
      @Nonnull
      State state,
      @Nonnull
      InfoLevel infoLevel
  ) {
    requireNonNull(state, "state");
    requireNonNull(infoLevel, "infoLevel");

    return String.join(
        ", ",
        state.getInformations().stream()
            .filter(info -> info.getInfoLevel() == infoLevel)
            .map(info -> info.getInfoType())
            .distinct()
            .sorted()
            .collect(Collectors.toList())
    );
  }

  /**
   * Returns the paused state from the given state message if present. Otherwise, returns
   * null.
   *
   * @param state The state message.
   * @return The paused state (can be null).
   */
  @Nullable
  public static String toPausedPropertyValue(
      @Nonnull
      State state
  ) {
    requireNonNull(state, "state");

    return Objects.toString(state.isPaused(), null);
  }

  /**
   * Indicates whether the vehicle rejects an order based on the errors reported in the given state
   * message.
   *
   * @param state The state message.
   * @return Whether the vehicle rejects an order.
   */
  public static boolean vehicleRejectsOrder(State state) {
    return state.getErrors().stream().anyMatch(StateMappings::isOrderRejectionWarning);
  }

  private static boolean isOrderRejectionWarning(ErrorEntry error) {
    return switch (error.getErrorType()) {
      case VALIDATION_ERROR, NO_ROUTE_ERROR, ORDER_ERROR, ORDER_UPDATE_ERROR -> true;
      default -> false;
    };
  }

  private static boolean hasPendingMovement(State state) {
    return state.getNodeStates().stream().anyMatch(nodeState -> nodeState.isReleased())
        && state.getEdgeStates().stream().anyMatch(edgeState -> edgeState.isReleased());
  }

  private static boolean hasPendingAction(State state) {
    return state.getActionStates().stream()
        .anyMatch(
            action -> (action.getActionStatus() != ActionStatus.FAILED
                && action.getActionStatus() != ActionStatus.FINISHED)
        );
  }
}
