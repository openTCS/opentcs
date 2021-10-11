/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.vehicles;

import com.google.inject.assistedinject.Assisted;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import org.opentcs.components.Lifecycle;
import org.opentcs.components.kernel.services.PeripheralDispatcherService;
import org.opentcs.components.kernel.services.PeripheralJobService;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.data.TCSObjectEvent;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.data.peripherals.PeripheralJob;
import org.opentcs.data.peripherals.PeripheralOperation;
import org.opentcs.drivers.vehicle.MovementCommand;
import org.opentcs.util.event.EventHandler;
import org.opentcs.util.event.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages interactions with peripheral devices that are to be performed before or after the
 * execution of movement commands.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class PeripheralInteractor
    implements EventHandler,
               Lifecycle {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(PeripheralInteractor.class);
  /**
   * The reference to the vehicle that's interacting with peripheral devices.
   */
  private final TCSObjectReference<Vehicle> vehicleRef;
  /**
   * The peripheral job service to use.
   */
  private final PeripheralJobService peripheralJobService;
  /**
   * The peripheral dispatcher service to use.
   */
  private final PeripheralDispatcherService peripheralDispatcherService;
  /**
   * The event source to register with.
   */
  private final EventSource eventSource;
  /**
   * The peripheral interactions to be performed BEFORE the exexution of a movement command mapped
   * to the corresponding movement command.
   */
  private final Map<MovementCommand, PeripheralInteraction> preMovementInteractions
      = new HashMap<>();
  /**
   * The peripheral interactions to be performed AFTER the exexution of a movement command mapped
   * to the corresponding movement command.
   */
  private final Map<MovementCommand, PeripheralInteraction> postMovementInteractions
      = new HashMap<>();
  /**
   * Indicates whether this instance is initialized.
   */
  private boolean initialized;

  /**
   * Creates a new instance.
   *
   * @param vehicleRef The reference to the vehicle that's interacting with peripheral devices.
   * @param peripheralJobService The peripheral job service to use.
   * @param peripheralDispatcherService The peripheral dispatcher service to use.
   * @param eventSource The event source to register with.
   */
  @Inject
  public PeripheralInteractor(@Assisted @Nonnull TCSObjectReference<Vehicle> vehicleRef,
                              @Nonnull PeripheralJobService peripheralJobService,
                              @Nonnull PeripheralDispatcherService peripheralDispatcherService,
                              @Nonnull @ApplicationEventBus EventSource eventSource) {
    this.vehicleRef = requireNonNull(vehicleRef, "vehicleRef");
    this.peripheralJobService = requireNonNull(peripheralJobService, "peripheralJobService");
    this.peripheralDispatcherService = requireNonNull(peripheralDispatcherService,
                                                      "peripheralDispatcherService");
    this.eventSource = requireNonNull(eventSource, "eventSource");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

    eventSource.subscribe(this);

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

    eventSource.unsubscribe(this);

    initialized = false;
  }

  @Override
  public void onEvent(Object event) {
    if (!(event instanceof TCSObjectEvent)) {
      return;
    }

    TCSObjectEvent objectEvent = (TCSObjectEvent) event;
    if (objectEvent.getType() != TCSObjectEvent.Type.OBJECT_MODIFIED) {
      return;
    }

    if (objectEvent.getCurrentOrPreviousObjectState() instanceof PeripheralJob) {
      onPeripheralJobChange(objectEvent);
    }
  }

  /**
   * Prepares for peripheral interactions in the context of the given movement command by
   * determining the interactions that have to be performed before and after the movement command
   * is executed.
   *
   * @param movementCommand The movement command to prepare peripheral interactions for.
   */
  public void prepareInteractions(MovementCommand movementCommand) {
    Path path = movementCommand.getStep().getPath();
    if (path == null) {
      return;
    }

    Map<PeripheralOperation.ExecutionTrigger, List<PeripheralOperation>> operations
        = path.getPeripheralOperations().stream()
            .collect(Collectors.groupingBy(t -> t.getExecutionTrigger()));
    operations.computeIfAbsent(PeripheralOperation.ExecutionTrigger.BEFORE_MOVEMENT,
                               executionTrigger -> new ArrayList<>());
    operations.computeIfAbsent(PeripheralOperation.ExecutionTrigger.AFTER_MOVEMENT,
                               executionTrigger -> new ArrayList<>());
    String reservationToken = determineReservationToken();

    List<PeripheralOperation> preMovementOperations
        = operations.get(PeripheralOperation.ExecutionTrigger.BEFORE_MOVEMENT);
    if (!preMovementOperations.isEmpty()) {
      preMovementInteractions.put(movementCommand,
                                  new PeripheralInteraction(vehicleRef,
                                                            movementCommand,
                                                            preMovementOperations,
                                                            peripheralJobService,
                                                            reservationToken));
    }

    List<PeripheralOperation> postMovementOperations
        = operations.get(PeripheralOperation.ExecutionTrigger.AFTER_MOVEMENT);
    if (!postMovementOperations.isEmpty()) {
      postMovementInteractions.put(movementCommand,
                                   new PeripheralInteraction(vehicleRef,
                                                             movementCommand,
                                                             postMovementOperations,
                                                             peripheralJobService,
                                                             reservationToken));
    }
  }

  /**
   * Starts the peripheral interactions that have to be performed before the given movement command
   * is executed.
   *
   * @param movementCommand The movement command.
   * @param succeededCallback The callback that is executed if the interactions succeeds (i.e. once
   * all required interactions are finished).
   * @param failedCallback The callback that is executed if the interactions fails (i.e. if a
   * single interaction failed).
   */
  public void startPreMovementInteractions(@Nonnull MovementCommand movementCommand,
                                           @Nonnull Runnable succeededCallback,
                                           @Nonnull Runnable failedCallback) {
    requireNonNull(movementCommand, "movementCommand");
    requireNonNull(succeededCallback, "succeededCallback");
    requireNonNull(failedCallback, "failedCallback");
    if (!preMovementInteractions.containsKey(movementCommand)) {
      LOG.debug("{}: No interactions to be performed before movement to {}...",
                vehicleRef.getName(),
                movementCommand.getStep().getDestinationPoint().getName());
      succeededCallback.run();
      return;
    }

    LOG.debug("{}: There are interactions to be performed before movement to {}...",
              vehicleRef.getName(),
              movementCommand.getStep().getDestinationPoint().getName());
    preMovementInteractions.get(movementCommand).start(succeededCallback, failedCallback);

    // In case there are only operations with the completion required flag not set, the interaction
    // is immediately finished and we can remove it right away.
    if (preMovementInteractions.get(movementCommand).isFinished()) {
      preMovementInteractions.remove(movementCommand);
    }

    // Peripheral jobs have been created. Disptach them.
    peripheralDispatcherService.dispatch();
  }

  /**
   * Starts the peripheral interactions that have to be performed after the given movement command
   * is executed.
   *
   * @param movementCommand The movement command.
   * @param succeededCallback The callback that is executed if the interactions succeeds (i.e. once
   * all required interactions are finished).
   * @param failedCallback The callback that is executed if the interactions fails (i.e. if a
   * single interaction failed).
   */
  public void startPostMovementInteractions(@Nonnull MovementCommand movementCommand,
                                            @Nonnull Runnable succeededCallback,
                                            @Nonnull Runnable failedCallback) {
    requireNonNull(movementCommand, "movementCommand");
    requireNonNull(succeededCallback, "succeededCallback");
    requireNonNull(failedCallback, "failedCallback");
    if (!postMovementInteractions.containsKey(movementCommand)) {
      LOG.debug("{}: No interactions to be performed after movement to {}...",
                vehicleRef.getName(),
                movementCommand.getStep().getDestinationPoint().getName());
      succeededCallback.run();
      return;
    }

    LOG.debug("{}: There are interactions to be performed after movement to {}...",
              vehicleRef.getName(),
              movementCommand.getStep().getDestinationPoint().getName());
    postMovementInteractions.get(movementCommand).start(succeededCallback, failedCallback);

    // In case there are only operations with the completion required flag not set, the interaction
    // is immediately finished and we can remove it right away.
    if (postMovementInteractions.get(movementCommand).isFinished()) {
      postMovementInteractions.remove(movementCommand);
    }

    // Peripheral jobs have been created. Disptach them.
    peripheralDispatcherService.dispatch();
  }

  /**
   * Returns whether there are any required (pre or post movement) interactions that have not been
   * finished yet.
   * In case there's a required interaction that has failed (and therefore not finished), this
   * method returns {@code true}.
   *
   * @return Whether there are any required interactions that have not been finished yet.
   */
  public boolean isWaitingForMovementInteractionsToFinish() {
    return isWaitingForPreMovementInteractionsToFinish()
        || isWaitingForPostMovementInteractionsToFinish();
  }

  /**
   * Returns whether there are any required (pre movement) interactions that have not been finished
   * yet.
   * In case there's a required interaction that has failed (and therefore not finished), this
   * method returns {@code true}.
   *
   * @return Whether there are any required (pre movement) interactions that have not been finished
   * yet.
   */
  public boolean isWaitingForPreMovementInteractionsToFinish() {
    return !preMovementInteractions.values().stream()
        .filter(PeripheralInteraction::hasRequiredOperations)
        .allMatch(PeripheralInteraction::isFinished);
  }

  /**
   * Returns whether there are any required (post movement) interactions that have not been finished
   * yet.
   * In case there's a required interaction that has failed (and therefore not finished), this
   * method returns {@code true}.
   *
   * @return Whether there are any required (post movement) interactions that have not been finished
   * yet.
   */
  public boolean isWaitingForPostMovementInteractionsToFinish() {
    return !postMovementInteractions.values().stream()
        .filter(PeripheralInteraction::hasRequiredOperations)
        .allMatch(PeripheralInteraction::isFinished);
  }

  /**
   * Returns a list of required operations that are still to be completed mapped to the associated
   * movement command's destination point.
   *
   * @return A list of required operations that are still to be completed mapped to the associated
   * movement command's destination point.
   */
  public Map<String, List<PeripheralOperation>> pendingRequiredInteractionsByDestination() {
    return Stream.concat(preMovementInteractions.entrySet().stream(),
                         postMovementInteractions.entrySet().stream())
        .map(entry -> entry.getValue())
        .filter(interaction -> interaction.hasRequiredOperations())
        // We're working with two streams from two maps which can each contain the same keys.
        // Therefore we have to use the groupingBy collector and need to flat map each interaction's
        // pending required operations.
        .collect(
            Collectors.groupingBy(
                interact -> interact.getMovementCommand().getStep().getDestinationPoint().getName(),
                Collectors.flatMapping(
                    interaction -> interaction.getPendingRequiredOperations().stream(),
                    Collectors.toList()
                )
            )
        );
  }

  private void onPeripheralJobChange(TCSObjectEvent event) {
    PeripheralJob prevJobState = (PeripheralJob) event.getPreviousObjectState();
    PeripheralJob currJobState = (PeripheralJob) event.getCurrentObjectState();

    if (prevJobState.getState() != currJobState.getState()) {
      switch (currJobState.getState()) {
        case FINISHED:
          onPeripheralJobFinished(currJobState);
          break;
        case FAILED:
          onPeripheralJobFailed(currJobState);
          break;
        default: // Do nothing
      }
    }
  }

  private void onPeripheralJobFinished(PeripheralJob job) {
    Stream.concat(preMovementInteractions.values().stream(),
                  postMovementInteractions.values().stream())
        .forEach(interaction -> interaction.onPeripheralJobFinished(job));

    Set<MovementCommand> preMovementsPrepared = preMovementInteractions.entrySet().stream()
        .filter(entry -> entry.getValue().isFinished())
        .map(entry -> entry.getKey())
        .collect(Collectors.toSet());
    Set<MovementCommand> postMovementsPrepared = postMovementInteractions.entrySet().stream()
        .filter(entry -> entry.getValue().isFinished())
        .map(entry -> entry.getKey())
        .collect(Collectors.toSet());

    preMovementsPrepared.forEach(
        movementCommand -> preMovementInteractions.remove(movementCommand)
    );
    postMovementsPrepared.forEach(
        movementCommand -> postMovementInteractions.remove(movementCommand)
    );
  }

  private void onPeripheralJobFailed(PeripheralJob job) {
    Stream.concat(preMovementInteractions.values().stream(),
                  postMovementInteractions.values().stream())
        .forEach(interaction -> interaction.onPeripheralJobFailed(job));
  }

  /**
   * Clears the interactions.
   */
  public void clear() {
    preMovementInteractions.clear();
    postMovementInteractions.clear();
  }

  private String determineReservationToken() {
    Vehicle vehicle = peripheralJobService.fetchObject(Vehicle.class, vehicleRef);
    if (vehicle.getTransportOrder() != null) {
      TransportOrder transportOrder = peripheralJobService.fetchObject(TransportOrder.class,
                                                                       vehicle.getTransportOrder());
      if (transportOrder.getPeripheralReservationToken() != null) {
        return transportOrder.getPeripheralReservationToken();
      }
    }

    return vehicle.getName();
  }
}
