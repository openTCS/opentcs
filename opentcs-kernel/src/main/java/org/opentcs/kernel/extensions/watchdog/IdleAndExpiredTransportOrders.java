// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.watchdog;

import static java.util.Objects.requireNonNull;

import jakarta.inject.Inject;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.opentcs.components.Lifecycle;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.util.TimeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to find out idle and expired transport orders.
 */
public class IdleAndExpiredTransportOrders
    implements
      Lifecycle {
  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(IdleAndExpiredTransportOrders.class);
  /**
   * Object service to access the model.
   */
  private final TCSObjectService objectService;
  /**
   * Provider to get the current time.
   */
  private final TimeProvider timeProvider;
  /**
   * Map to store the current snapshot for each transport order.
   */
  private final Map<String, TransportOrderSnapshot> currentSnapshots = new HashMap<>();
  /**
   * Map to store the previous snapshot for each transport order.
   */
  private final Map<String, TransportOrderSnapshot> previousSnapshots = new HashMap<>();
  /**
   * Whether this instance is initialized.
   */
  private boolean initialized;

  /**
   * Creates a new instance.
   *
   * @param objectService The object service.
   * @param timeProvider Provider to get the current time.
   */
  @Inject
  public IdleAndExpiredTransportOrders(
      TCSObjectService objectService,
      TimeProvider timeProvider
  ) {
    this.objectService = requireNonNull(objectService, "objectService");
    this.timeProvider = requireNonNull(timeProvider, "timeProvider");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

    long currentTime = timeProvider.getCurrentTimeEpochMillis();
    objectService.fetchObjects(TransportOrder.class).forEach(order -> {
      TransportOrderSnapshot transportOrderSnapshot = new TransportOrderSnapshot(order);
      transportOrderSnapshot.setLastRelevantStateChange(currentTime);
      currentSnapshots.put(order.getName(), transportOrderSnapshot);
    });

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

    currentSnapshots.clear();
    previousSnapshots.clear();

    initialized = false;
  }

  /**
   * Identifies idle and expired transport orders.
   *
   * @param currentTime The current time.
   * @param idleDurationThreshold The duration that a vehicle must be in the same state to
   * actually be considered idle.
   */
  public void identifyIdleOrExpiredTransportOrders(
      long currentTime,
      long idleDurationThreshold
  ) {
    LOG.debug("Identifying idle transport orders...");
    previousSnapshots.clear();
    previousSnapshots.putAll(currentSnapshots);
    currentSnapshots.clear();

    objectService.fetchObjects(TransportOrder.class)
        .stream()
        .forEach(order -> {
          TransportOrderSnapshot previousSnapshot = previousSnapshots.get(order.getName());
          TransportOrderSnapshot currentSnapshot = new TransportOrderSnapshot(order);

          if (order.getState().isFinalState()) {
            currentSnapshot.setIdle(false);
          }
          else {
            if (order.hasState(TransportOrder.State.BEING_PROCESSED)) {
              currentSnapshot.setIdle(false);
            }
            else {
              LOG.debug("Checking if transport order '{}' is idle long enough...", order);
              if (previousSnapshot != null) {
                if (relevantPropertiesChanged(previousSnapshot, currentSnapshot)) {
                  currentSnapshot.setLastRelevantStateChange(currentTime);
                }
                else {
                  currentSnapshot.setLastRelevantStateChange(
                      previousSnapshot.getLastRelevantStateChange()
                  );
                }
              }
              else {
                currentSnapshot.setLastRelevantStateChange(currentTime);
              }

              long idleDuration = currentTime - currentSnapshot.getLastRelevantStateChange();
              currentSnapshot.setIdle(idleDuration >= idleDurationThreshold);
            }
          }
          currentSnapshot.setWasIdle(
              currentSnapshot.isIdle() || (previousSnapshot != null && previousSnapshot.wasIdle())
          );
          currentSnapshot.setDeadlineExpired(
              isDeadlineExpired(previousSnapshot, currentSnapshot, currentTime)
          );
          LOG.debug("Snapshot of transport order '{}': {}", order.getName(), currentSnapshot);
          currentSnapshots.put(order.getName(), currentSnapshot);
        });
  }

  /**
   * Returns transport orders that are currently considered newly idle (i.e., transport orders that
   * were previously not considered idle, but are now considered idle).
   *
   * @return The set of transport orders that are considered newly idle.
   */
  public Set<TransportOrderSnapshot> newlyIdleTransportOrders() {
    return currentSnapshots.values().stream()
        .filter(
            transportOrderSnapshot -> transportOrderSnapshot.isIdle()
                && (!previousSnapshots.containsKey(
                    transportOrderSnapshot.getTransportOrder().getName()
                ) || !previousSnapshots.get(transportOrderSnapshot.getTransportOrder().getName())
                    .isIdle())
        )
        .collect(Collectors.toSet());
  }

  /**
   * Returns transport orders that are currently considered newly expired (i.e., transport orders
   * whose deadline has been expired since the previous check).
   *
   * @return The set of transport orders that are considered newly idle.
   */
  public Set<TransportOrderSnapshot> newlyExpiredTransportOrders() {
    return currentSnapshots.values().stream()
        .filter(
            transportOrderSnapshot -> transportOrderSnapshot.isDeadlineExpired()
                && (!previousSnapshots.containsKey(
                    transportOrderSnapshot.getTransportOrder().getName()
                )
                    || !previousSnapshots.get(transportOrderSnapshot.getTransportOrder().getName())
                        .isDeadlineExpired())
        )
        .collect(Collectors.toSet());
  }

  /**
   * Returns transport orders that are currently in a final state and were previously considered
   * idle or expired.
   *
   * @return The set of transport orders that are no longer considered idle.
   */
  public Set<TransportOrder> newTransportOrdersInFinalState() {
    return currentSnapshots.values().stream()
        .filter(
            transportOrderSnapshot -> transportOrderSnapshot.isDeadlineExpired()
                || transportOrderSnapshot.wasIdle()
        )
        .map(transportOrderSnapshot -> transportOrderSnapshot.getTransportOrder())
        .filter(
            order -> order.getState().isFinalState()
                && (!previousSnapshots.containsKey(order.getName())
                    || !previousSnapshots.get(order.getName()).getLastState().isFinalState())
        )
        .collect(Collectors.toSet());
  }

  private boolean isDeadlineExpired(
      TransportOrderSnapshot previousSnapshot,
      TransportOrderSnapshot currentSnapshot,
      Long currentTime
  ) {
    TransportOrder order = currentSnapshot.getTransportOrder();
    return (order.getDeadline().isBefore(Instant.ofEpochMilli(currentTime))
        && !order.getState().isFinalState())
        || (previousSnapshot != null && previousSnapshot.isDeadlineExpired());
  }

  private boolean relevantPropertiesChanged(
      TransportOrderSnapshot previousSnapshot,
      TransportOrderSnapshot currentSnapshot
  ) {
    return previousSnapshot.getLastState() != currentSnapshot.getLastState()
        || previousSnapshot.getProcessingVehicle() != currentSnapshot.getProcessingVehicle();
  }

  /**
   * A snapshot of a transport order's state with additional information on whether the given
   * transport order is considered idle and when the last state change occurred.
   */
  public static class TransportOrderSnapshot {

    private final TransportOrder transportOrder;
    private long lastRelevantStateChange;
    private boolean deadlineExpired;
    private boolean idle;
    private boolean wasIdle;

    /**
     * Creates a new instance.
     *
     * @param transportOrder Transport order to be stored.
     */
    public TransportOrderSnapshot(TransportOrder transportOrder) {
      this.transportOrder = requireNonNull(transportOrder, "transportOrder");
    }

    public boolean isIdle() {
      return idle;
    }

    public void setIdle(boolean idle) {
      this.idle = idle;
    }

    public boolean wasIdle() {
      return wasIdle;
    }

    public void setWasIdle(boolean wasIdle) {
      this.wasIdle = wasIdle;
    }

    public boolean isDeadlineExpired() {
      return deadlineExpired;
    }

    public void setDeadlineExpired(boolean deadlineExceeded) {
      this.deadlineExpired = deadlineExceeded;
    }

    public TransportOrder getTransportOrder() {
      return transportOrder;
    }

    public long getLastRelevantStateChange() {
      return lastRelevantStateChange;
    }

    public void setLastRelevantStateChange(long lastRelevantStateChange) {
      this.lastRelevantStateChange = lastRelevantStateChange;
    }

    public TCSObjectReference<Vehicle> getProcessingVehicle() {
      return transportOrder.getProcessingVehicle();
    }

    public TransportOrder.State getLastState() {
      return transportOrder.getState();
    }

    @Override
    public String toString() {
      return "TransportOrderSnapshot{"
          + "idle=" + idle
          + ", deadlineExpired=" + deadlineExpired
          + ", wasIdle=" + wasIdle
          + ", lastRelevantStateChange=" + lastRelevantStateChange
          + ", transportOrder=" + transportOrder
          + '}';
    }
  }
}
