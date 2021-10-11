/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.vehicles;

import java.util.ArrayList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opentcs.access.to.peripherals.PeripheralJobCreationTO;
import org.opentcs.access.to.peripherals.PeripheralOperationCreationTO;
import org.opentcs.components.kernel.services.PeripheralJobService;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.peripherals.PeripheralJob;
import org.opentcs.data.peripherals.PeripheralOperation;
import org.opentcs.drivers.vehicle.MovementCommand;
import static org.opentcs.util.Assertions.checkState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Describes an interaction with any number of peripheral devices.
 * <p>
 * An interaction is always associated with a {@link MovementCommand} and contains
 * {@link PeripheralOperation}s for which {@link PeripheralJob}s are created once the interaction
 * is started.
 * </p>
 * <p>
 * In case there are no operations with the completion required flag set, the interaction is marked
 * as finished immediately after it was started.
 * In case there are operations with the completion required flag set, the interaction is only
 * marked as finished after all corresponding jobs have been processed by the respective peripheral
 * device.
 * </p>
 * <p>
 * Once the interaction is finished, an interaction-specific callback is executed.
 * </p>
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class PeripheralInteraction {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(PeripheralInteraction.class);
  /**
   * The reference to the vehicle that's interacting with peripheral devices.
   */
  private final TCSObjectReference<Vehicle> vehicleRef;
  /**
   * The movement command this interaction is associated with.
   */
  private final MovementCommand movementCommand;
  /**
   * The operations that jobs have to be created for.
   */
  private final List<PeripheralOperation> operations;
  /**
   * The jobs that are required to be finished in order for this interaction itself to be marked as
   * finished.
   */
  private final List<PeripheralJob> requiredJobs = new ArrayList<>();
  /**
   * The peripheral job service to use for creating jobs.
   */
  private final PeripheralJobService peripheralJobService;
  /**
   * The reservation token to use for creating jobs.
   */
  private final String reservationToken;
  /**
   * The callback that is to be executed if the interaction succeeds.
   */
  private Runnable interactionSucceededCallback;
  /**
   * The callback that is executed if the interaction fails.
   */
  private Runnable interactionFailedCallback;
  /**
   * The state of the interaction.
   */
  private State state = State.PRISTINE;

  /**
   * Creates a new instance.
   *
   * @param vehicleRef The reference to the vehicle that's interacting with peripheral devices.
   * @param movementCommand The movement command this interaction is associated with.
   * @param operations The operations that jobs have to be created for.
   * @param peripheralJobService The peripheral job service to use for creating jobs.
   * @param reservationToken The reservation token to use for creating jobs.
   */
  public PeripheralInteraction(@Nonnull TCSObjectReference<Vehicle> vehicleRef,
                               @Nonnull MovementCommand movementCommand,
                               @Nonnull List<PeripheralOperation> operations,
                               @Nonnull PeripheralJobService peripheralJobService,
                               @Nonnull String reservationToken) {
    this.vehicleRef = requireNonNull(vehicleRef, "vehicleRef");
    this.movementCommand = requireNonNull(movementCommand, "movementCommand");
    this.operations = requireNonNull(operations, "operations");
    this.peripheralJobService = requireNonNull(peripheralJobService, "peripheralJobService");
    this.reservationToken = requireNonNull(reservationToken, "reservationToken");
  }

  /**
   * Starts this peripheral interaction.
   *
   * @param interactionSucceededCallback The callback that is to be executed if the interaction
   * succeeds.
   * @param interactionFailedCallback The callback that is to be executed if the interaction fails.
   */
  public void start(@Nonnull Runnable interactionSucceededCallback,
                    @Nonnull Runnable interactionFailedCallback) {
    this.interactionSucceededCallback = requireNonNull(interactionSucceededCallback,
                                                       "interactionSucceededCallback");
    this.interactionFailedCallback = requireNonNull(interactionFailedCallback,
                                                    "interactionFailedCallback");

    LOG.debug("{}: Starting peripheral interaction for movement to {}",
              vehicleRef.getName(),
              movementCommand.getStep().getDestinationPoint().getName());
    for (PeripheralOperation operation : operations) {
      PeripheralJob job = createPeripheralJob(operation);
      if (operation.isCompletionRequired()) {
        requiredJobs.add(job);
      }
    }

    state = State.STARTED;

    if (requiredJobs.isEmpty()) {
      onInteractionFinished();
    }
  }

  /**
   * Informs this interaction that a peripheral job (that might be of interest for this interaction)
   * has been finished.
   *
   * @param job The peripheral job that has been finished.
   */
  public void onPeripheralJobFinished(@Nonnull PeripheralJob job) {
    requireNonNull(job, "job");
    if (requiredJobs.remove(job) && requiredJobs.isEmpty()) {
      // The last required job has been finished.
      onInteractionFinished();
    }
  }

  /**
   * Informs this interaction that a peripheral job (that might be of interest for this interaction)
   * has failed.
   *
   * @param job The peripheral job that has failed.
   */
  public void onPeripheralJobFailed(@Nonnull PeripheralJob job) {
    requireNonNull(job, "job");
    if (requiredJobs.contains(job)) {
      onInteractionFailed();
    }
  }

  /**
   * Returns the movement command this interaction is associated with.
   *
   * @return The movement command this interaction is associated with.
   */
  public MovementCommand getMovementCommand() {
    return movementCommand;
  }

  /**
   * Returns whether the interaction is finished.
   *
   * @return Whether the interaction is finished.
   */
  public boolean isFinished() {
    return hasState(State.FINSHED);
  }

  /**
   * Returns whether the interaction has failed.
   *
   * @return Whether the interaction has failed.
   */
  public boolean isFailed() {
    return hasState(State.FAILED);
  }

  /**
   * Returns whether the interaction is in the given state.
   *
   * @param state The state.
   * @return Whether the interaction is in the given state.
   */
  public boolean hasState(State state) {
    return this.state == state;
  }

  /**
   * Returns whether this interaction has some operations that are required to be completed.
   *
   * @return Whether this interaction has some operations that are required to be completed.
   */
  public boolean hasRequiredOperations() {
    return operations.stream()
        .anyMatch(PeripheralOperation::isCompletionRequired);
  }

  /**
   * Returns the list of operations that are required to be completed and that haven't been
   * completed yet.
   *
   * @return A list of operations.
   */
  public List<PeripheralOperation> getPendingRequiredOperations() {
    // If we're already done interacting with the peripheral device, there cannot be any pending
    // operations.
    if (hasState(State.FINSHED)) {
      return new ArrayList<>();
    }

    if (!hasRequiredOperations()) {
      return new ArrayList<>();
    }

    if (!requiredJobs.isEmpty()) {
      // The interaction is still ongoing. Jobs are not yet finished or have even failed.
      return requiredJobs.stream()
          .map(job -> job.getPeripheralOperation())
          .collect(Collectors.toList());
    }

    // The interaction is still ongoing but no jobs have been created (yet) for the required
    // operations.
    return operations.stream()
        .filter(PeripheralOperation::isCompletionRequired)
        .collect(Collectors.toList());
  }

  private void onInteractionFinished() {
    checkState(interactionSucceededCallback != null, "The interaction hasn't been started yet.");
    checkState(!hasState(State.FAILED), "The interaction has already been marked as failed.");
    checkState(!hasState(State.FINSHED), "The interaction has already been marked as finished.");
    state = State.FINSHED;

    LOG.debug("{}: Peripheral interaction finished for movement to {}",
              vehicleRef.getName(),
              movementCommand.getStep().getDestinationPoint().getName());
    interactionSucceededCallback.run();
  }

  private PeripheralJob createPeripheralJob(PeripheralOperation operation) {
    return peripheralJobService.createPeripheralJob(
        new PeripheralJobCreationTO("Job-",
                                    reservationToken,
                                    new PeripheralOperationCreationTO(
                                        operation.getOperation(),
                                        operation.getLocation().getName()
                                    ))
            .withIncompleteName(true)
    );
  }

  private void onInteractionFailed() {
    checkState(interactionFailedCallback != null, "The interaction hasn't been started yet.");
    checkState(!hasState(State.FINSHED), "The interaction has already been marked as finished.");
    checkState(!hasState(State.FAILED), "The interaction has already been marked as failed.");
    state = State.FAILED;

    LOG.debug("{}: Peripheral interaction failed for movement to {}",
              vehicleRef.getName(),
              movementCommand.getStep().getDestinationPoint().getName());
    interactionFailedCallback.run();
  }

  public enum State {
    /**
     * The interaction is initialized and yet to be started.
     */
    PRISTINE,
    /**
     * The interaction was started.
     */
    STARTED,
    /**
     * The interaction was finished.
     * All of the required operations (if any) have been finished successfully.
     */
    FINSHED,
    /**
     * The interaction has failed.
     * At least one of the required operations failed.
     */
    FAILED;
  }
}
