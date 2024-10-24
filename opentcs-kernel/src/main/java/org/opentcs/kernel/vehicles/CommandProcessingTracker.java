// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.vehicles;

import static java.util.Objects.requireNonNull;
import static org.opentcs.util.Assertions.checkArgument;
import static org.opentcs.util.Assertions.checkState;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.SequencedCollection;
import java.util.Set;
import java.util.stream.Collectors;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.TCSResource;
import org.opentcs.drivers.vehicle.MovementCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tracks processing of movement commands.
 * <p>
 * After the movement commands for a new or updated drive order have been passed to
 * {@link #driveOrderUpdated(SequencedCollection)}, movement commands and their corresponding
 * resources are expected to be processed in the following order:
 * <ol>
 * <li>{@link #allocationRequested(Set)}</li>
 * <li>{@link #allocationConfirmed(Set)}</li>
 * <li>{@link #commandSent(MovementCommand)}</li>
 * <li>{@link #commandExecuted(MovementCommand)}</li>
 * <li>{@link #allocationReleased(Set)}</li>
 * </ol>
 */
public class CommandProcessingTracker {

  private static final Logger LOG = LoggerFactory.getLogger(CommandProcessingTracker.class);
  /**
   * The queue of commands that still need to be sent to the communication adapter.
   */
  private final Deque<CommandResourcePair> futureCommands = new ArrayDeque<>();
  /**
   * A command (the next one for the current drive order) that has yet to be sent to the
   * communication adapter.
   */
  private CommandResourcePair pendingCommand;
  /**
   * The state the pending command is currently in.
   */
  private PendingCommandState pendingCommandState = PendingCommandState.UNDEFINED;
  /**
   * The queue of commands that have been sent to the communication adapter.
   */
  private final Deque<CommandResourcePair> sentCommands = new ArrayDeque<>();
  /**
   * The command that was executed last.
   */
  private MovementCommand lastCommandExecuted;
  /**
   * The queue of resource sets that the vehicle has already passed.
   * <p>
   * For every movement command that is executed, two elements are added to this queue - one element
   * containing only the path (if any) associated with the respective movement command, and another
   * element containing the point and the location (if any) associated with the respective movement
   * command.
   * </p>
   */
  private final Deque<Set<TCSResource<?>>> passedResources = new ArrayDeque<>();

  /**
   * Creates a new instance.
   */
  public CommandProcessingTracker() {
  }

  /**
   * Clears (i.e. resets) this instance.
   */
  public void clear() {
    futureCommands.clear();
    pendingCommand = null;
    pendingCommandState = PendingCommandState.UNDEFINED;
    sentCommands.clear();
    lastCommandExecuted = null;
    passedResources.clear();
  }

  /**
   * Called when a drive order was updated (either because a new one was assigned to a vehicle or
   * the one being currently processed was updated due to rerouting).
   *
   * @param movementCommands The collection of movement commands that belong to the updated drive
   * order.
   */
  public void driveOrderUpdated(
      @Nonnull
      SequencedCollection<MovementCommand> movementCommands
  ) {
    requireNonNull(movementCommands, "movementCommands");

    if (isDriveOrderFinished()) {
      // The movement commands belong to a new drive order.
      futureCommands.addAll(toCommandResourcePairs(movementCommands));
    }
    else {
      // The movement commands belong to the same drive order we are currently processing.
      futureCommands.clear();
      if (pendingCommandState == PendingCommandState.ALLOCATION_PENDING) {
        // With drive order updates, any pending resource allocation is reset.
        pendingCommand = null;
        pendingCommandState = PendingCommandState.UNDEFINED;
      }

      futureCommands.addAll(toCommandResourcePairs(movementCommands));

      // The current drive order got updated but our queue of future commands now contains commands
      // that have already been processed, so discard these.
      discardProcessedFutureCommands();
    }
  }

  /**
   * Called when a drive order was aborted.
   *
   * @param immediate Indicates whether the drive order was aborted immediately or regularly.
   */
  public void driveOrderAborted(boolean immediate) {
    if (immediate) {
      futureCommands.clear();
      pendingCommand = null;
      pendingCommandState = PendingCommandState.UNDEFINED;
      sentCommands.clear();
    }
    else {
      futureCommands.clear();
      if (pendingCommandState != PendingCommandState.SENDING_PENDING) {
        pendingCommand = null;
        pendingCommandState = PendingCommandState.UNDEFINED;
      }
    }
  }

  /**
   * Checks if there are any movement commands that are yet to be sent to the communication adapter.
   *
   * @return {@code true} if there are commands to be sent, otherwise {@code false}.
   */
  public boolean hasCommandsToBeSent() {
    return !futureCommands.isEmpty()
        || pendingCommandState == PendingCommandState.ALLOCATION_PENDING
        || pendingCommandState == PendingCommandState.SENDING_PENDING;
  }

  /**
   * Checks if the current drive order is considered finished.
   *
   * @return {@code true}, if there are no more commands that need to be sent to the communication
   * adapter and all commands already sent have been reported as executed.
   */
  public boolean isDriveOrderFinished() {
    return futureCommands.isEmpty()
        && pendingCommand == null
        && sentCommands.isEmpty();
  }

  /**
   * Called when a vehicle's allocation was reset.
   *
   * @param resources The (only) set of resources that the vehicle now allocates.
   */
  public void allocationReset(
      @Nonnull
      Set<TCSResource<?>> resources
  ) {
    requireNonNull(resources, "resources");

    // Clear resources that have been passed previously as they are no longer allocated.
    passedResources.clear();

    if (!resources.isEmpty()) {
      // Now, the given resources are allocated and considered as the new passed resources.
      passedResources.add(resources);
    }

    // Discard the pending command since pending allocations are reset and resources that have
    // already been allocated are freed when allocation is reset.
    pendingCommand = null;
    pendingCommandState = PendingCommandState.UNDEFINED;

    // Clear sent commands since we don't expect a vehicle to report these commands as executed
    // after allocation has been reset.
    sentCommands.clear();
  }

  /**
   * Called when a resource allocation was requested.
   *
   * @param resources The resources for which allocation was requested.
   */
  public void allocationRequested(
      @Nonnull
      Set<TCSResource<?>> resources
  ) {
    requireNonNull(resources, "resources");
    checkArgument(
        !futureCommands.isEmpty(),
        "Allocation requested, but there are no future commands: %s",
        resources
    );
    checkArgument(
        Objects.equals(futureCommands.peek().getResources(), resources),
        "Resource set is not head of future commands: %s (futureCommands=%s)",
        resources,
        futureCommands
    );
    checkArgument(
        pendingCommandState == PendingCommandState.UNDEFINED,
        "pendingCommandState is not '%s' but '%s'",
        PendingCommandState.UNDEFINED,
        pendingCommandState
    );

    pendingCommand = futureCommands.remove();
    pendingCommandState = PendingCommandState.ALLOCATION_PENDING;
  }

  /**
   * Called when a resource allocation was confirmed.
   *
   * @param resources The resources for which allocation was confirmed.
   */
  public void allocationConfirmed(
      @Nonnull
      Set<TCSResource<?>> resources
  ) {
    requireNonNull(resources, "resources");
    checkArgument(
        pendingCommandState == PendingCommandState.ALLOCATION_PENDING,
        "pendingCommandState is not '%s' but '%s'",
        PendingCommandState.ALLOCATION_PENDING,
        pendingCommandState
    );
    checkArgument(
        Objects.equals(pendingCommand.getResources(), resources),
        "Resource set does not belong to pending command: %s (pendingCommand=%s)",
        resources,
        pendingCommand
    );

    pendingCommandState = PendingCommandState.SENDING_PENDING;
  }

  /**
   * Called when a resource allocation was revoked.
   *
   * @param resources The resources for which allocation was revoked.
   */
  public void allocationRevoked(
      @Nonnull
      Set<TCSResource<?>> resources
  ) {
    requireNonNull(resources, "resources");
    checkArgument(
        pendingCommandState == PendingCommandState.SENDING_PENDING,
        "pendingCommandState is not '%s' but '%s'",
        PendingCommandState.SENDING_PENDING,
        pendingCommandState
    );
    checkArgument(
        Objects.equals(pendingCommand.getResources(), resources),
        "Resource set does not belong to pending command: %s (pendingCommand=%s)",
        resources,
        pendingCommand
    );

    pendingCommand = null;
    pendingCommandState = PendingCommandState.UNDEFINED;
  }

  /**
   * Called when a movement command won't be sent to the communication adapter.
   *
   * @param movementCommand The movement command.
   */
  public void commandSendingStopped(
      @Nonnull
      MovementCommand movementCommand
  ) {
    requireNonNull(movementCommand, "movementCommand");
    checkArgument(
        pendingCommandState == PendingCommandState.SENDING_PENDING,
        "pendingCommandState is not '%s' but '%s'",
        PendingCommandState.SENDING_PENDING,
        pendingCommandState
    );
    checkArgument(
        Objects.equals(pendingCommand.getMovementCommand(), movementCommand),
        "Movement command does not belong to pending command: %s (pendingCommand=%s)",
        movementCommand,
        pendingCommand
    );

    pendingCommandState = PendingCommandState.WONT_SEND;
  }

  /**
   * Called when a movement command was sent to the communication adapter.
   *
   * @param movementCommand The movement command.
   */
  public void commandSent(
      @Nonnull
      MovementCommand movementCommand
  ) {
    requireNonNull(movementCommand, "movementCommand");
    checkArgument(
        pendingCommandState == PendingCommandState.SENDING_PENDING,
        "pendingCommandState is not '%s' but '%s'",
        PendingCommandState.SENDING_PENDING,
        pendingCommandState
    );
    checkArgument(
        Objects.equals(pendingCommand.getMovementCommand(), movementCommand),
        "Movement command does not belong to pending command: %s (pendingCommand=%s)",
        movementCommand,
        pendingCommand
    );

    sentCommands.add(pendingCommand);
    pendingCommand = null;
    pendingCommandState = PendingCommandState.UNDEFINED;
  }

  /**
   * Called when a movement command was reported as executed.
   *
   * @param movementCommand The movement command.
   */
  public void commandExecuted(
      @Nonnull
      MovementCommand movementCommand
  ) {
    requireNonNull(movementCommand, "movementCommand");
    checkArgument(
        !sentCommands.isEmpty(),
        "Movement command reported as executed, but no commands have been sent: %s",
        movementCommand
    );
    MovementCommand expectedCommand = sentCommands.peek().getMovementCommand();
    checkArgument(
        Objects.equals(expectedCommand, movementCommand),
        "%s: Unexpected movement command executed: %s != %s",
        movementCommand.getTransportOrder().getProcessingVehicle().getName(),
        movementCommand,
        expectedCommand
    );

    CommandResourcePair executedCommand = sentCommands.remove();
    lastCommandExecuted = executedCommand.getMovementCommand();
    passedResources.add(extractPath(executedCommand.getResources()));
    passedResources.add(extractPointAndLocation(executedCommand.getResources()));
  }

  /**
   * Called when a resource allocation was released.
   *
   * @param resources The resources for which allocation was released.
   */
  public void allocationReleased(
      @Nonnull
      Set<TCSResource<?>> resources
  ) {
    requireNonNull(resources, "resources");
    checkArgument(
        Objects.equals(passedResources.peek(), resources),
        "Resource set is not head of passed resources: %s (passedResources=%s)",
        resources,
        passedResources
    );

    passedResources.remove();
  }

  /**
   * Returns the queue of resources claimed by the vehicle.
   * <p>
   * The order of the elements in this queue corresponds to the order in which they will be
   * allocated, with the first element in the queue (i.e. its head) corresponding to the resources
   * that will be allocated next.
   * </p>
   *
   * @return The queue of resources claimed by the vehicle.
   */
  @Nonnull
  public Deque<Set<TCSResource<?>>> getClaimedResources() {
    Deque<Set<TCSResource<?>>> claimedResources = new ArrayDeque<>();

    if (pendingCommandState == PendingCommandState.ALLOCATION_PENDING) {
      claimedResources.add(pendingCommand.getResources());
    }

    futureCommands.stream()
        .map(CommandResourcePair::getResources)
        .forEach(claimedResources::add);

    return claimedResources;
  }

  /**
   * Returns the queue of resources allocated by the vehicle.
   * <p>
   * The order of the elements in this queue corresponds to the order in which they were allocated,
   * with the first element in the queue (i.e. its head) corresponding to the oldest resources.
   * </p>
   *
   * @return The queue of resources allocated by the vehicle.
   */
  @Nonnull
  public Deque<Set<TCSResource<?>>> getAllocatedResources() {
    Deque<Set<TCSResource<?>>> allocatedResources = new ArrayDeque<>();

    allocatedResources.addAll(passedResources);
    allocatedResources.addAll(getAllocatedResourcesAhead());

    return allocatedResources;
  }

  /**
   * Returns the queue of resources allocated by the vehicle that lie in front of it.
   * <p>
   * The order of the elements in this queue corresponds to the order in which they were allocated,
   * with the first element in the queue (i.e. its head) corresponding to the resources right in
   * front of the vehicle.
   * </p>
   *
   * @return The queue of allocated resources in front of the vehicle.
   */
  @Nonnull
  public Deque<Set<TCSResource<?>>> getAllocatedResourcesAhead() {
    Deque<Set<TCSResource<?>>> allocatedResourcesAhead = new ArrayDeque<>();

    sentCommands.stream()
        .map(CommandResourcePair::getResources)
        .forEach(allocatedResourcesAhead::add);

    if (pendingCommandState == PendingCommandState.SENDING_PENDING
        || pendingCommandState == PendingCommandState.WONT_SEND) {
      allocatedResourcesAhead.add(pendingCommand.getResources());
    }

    return allocatedResourcesAhead;
  }

  /**
   * Returns the movement command for which resource allocation is currently pending.
   *
   * @return An optional containing the movement command for which resource allocation is currently
   * pending or {@link Optional#empty()} if there is no such command.
   * @see #getAllocationPendingResources()
   */
  public Optional<MovementCommand> getAllocationPendingCommand() {
    if (pendingCommandState == PendingCommandState.ALLOCATION_PENDING) {
      return Optional.of(pendingCommand.getMovementCommand());
    }

    return Optional.empty();
  }

  /**
   * Returns the resources for which allocation is currently pending.
   *
   * @return An optional containing the resources for which allocation is currently pending or
   * {@link Optional#empty()} if there are no such resources.
   * @see #getAllocationPendingCommand()
   */
  public Optional<Set<TCSResource<?>>> getAllocationPendingResources() {
    if (pendingCommandState == PendingCommandState.ALLOCATION_PENDING) {
      return Optional.of(pendingCommand.getResources());
    }

    return Optional.empty();
  }

  /**
   * Returns the movement command for which resources have already been allocated but which is yet
   * to be sent to the communication adapter.
   *
   * @return An optional containing the movement command for which resources have already been
   * allocated but which is yet to be sent to the communication adapter or {@link Optional#empty()}
   * if there is no such command.
   */
  public Optional<MovementCommand> getSendingPendingCommand() {
    if (pendingCommandState == PendingCommandState.SENDING_PENDING) {
      return Optional.of(pendingCommand.getMovementCommand());
    }

    return Optional.empty();
  }

  /**
   * Returns the queue of movement commands that have been sent to the communication adapter but
   * have not yet been reported as executed.
   *
   * @return The queue of movement commands that have been sent to the communication adapter but
   * have not yet been reported as executed.
   */
  public Deque<MovementCommand> getSentCommands() {
    return sentCommands.stream()
        .map(CommandResourcePair::getMovementCommand)
        .collect(Collectors.toCollection(ArrayDeque::new));
  }

  /**
   * Returns the movement command that was executed last.
   *
   * @return The movement command that was executed last.
   */
  public Optional<MovementCommand> getLastCommandExecuted() {
    return Optional.ofNullable(lastCommandExecuted);
  }

  /**
   * Returns the movement command for which resources are to be allocated next.
   *
   * @return The movement command for which resources are to be allocated next.
   * @see #getNextAllocationResources()
   */
  public Optional<MovementCommand> getNextAllocationCommand() {
    return Optional.ofNullable(futureCommands.peek())
        .map(CommandResourcePair::getMovementCommand);
  }

  /**
   * Returns the resources that are to be allocated next.
   *
   * @return The resources that are to be allocated next.
   * @see #getNextAllocationCommand()
   */
  public Optional<Set<TCSResource<?>>> getNextAllocationResources() {
    return Optional.ofNullable(futureCommands.peek())
        .map(CommandResourcePair::getResources);
  }

  /**
   * Checks if there are resources for which allocation was requested but is yet to be confirmed.
   *
   * @return {@code true} if there are resources for which allocation was requested but is yet to
   * be confirmed, otherwise {@code false}.
   */
  public boolean isWaitingForAllocation() {
    return pendingCommandState == PendingCommandState.ALLOCATION_PENDING;
  }

  private SequencedCollection<CommandResourcePair> toCommandResourcePairs(
      SequencedCollection<MovementCommand> movementCommands
  ) {
    return movementCommands.stream()
        .map(command -> new CommandResourcePair(command, getNeededResources(command)))
        .toList();
  }

  private void discardProcessedFutureCommands() {
    MovementCommand lastCommandProcessed = lastCommandProcessed();
    if (futureCommands.isEmpty()) {
      // There are no commands to be discarded.
      return;
    }

    if (!fromSameDriveOrder(lastCommandProcessed, futureCommands.peek().getMovementCommand())) {
      // If the last processed command is from a different drive order, there is nothing to be
      // discarded. This is the case, for example, if the vehicle didn't yet process the very first
      // movement command of a new drive order.
      return;
    }

    LOG.debug(
        "{}: Discarding future commands up to '{}' (inclusively): {}",
        lastCommandProcessed.getTransportOrder().getProcessingVehicle().getName(),
        lastCommandProcessed,
        futureCommands
    );
    // Discard commands up to lastCommandProcessed...
    while (!futureCommands.isEmpty()
        && !lastCommandProcessed.equalsInMovement(futureCommands.peek().getMovementCommand())) {
      futureCommands.remove();
    }

    checkState(
        !futureCommands.isEmpty(),
        "%s: Future commands should not be empty.",
        lastCommandProcessed.getTransportOrder().getProcessingVehicle().getName(),
        lastCommandProcessed
    );
    checkState(
        lastCommandProcessed.equalsInMovement(futureCommands.peek().getMovementCommand()),
        "%s: Last command processed is not head of future commands: %s (futureCommands=%s)",
        lastCommandProcessed.getTransportOrder().getProcessingVehicle().getName(),
        lastCommandProcessed,
        futureCommands
    );

    // ...and also discard lastCommandProcessed itself.
    futureCommands.remove();
  }

  /**
   * Returns the last movement command that has been processed in a way that is relevant in the
   * context of rerouting.
   * <p>
   * Generally, a movement command is processed in multiple stages. It is:
   * <ol>
   * <li>Added to the <code>futureCommands</code> queue (when a transport order for the vehicle
   * is set or updated).</li>
   * <li>Removed from the <code>futureCommands</code> queue and set as the
   * <code>pendingCommand</code> (when allocation for the resources needed for executing the next
   * command has been requested).</li>
   * <li>Unset as the <code>pendingCommand</code> and added to the
   * <code>commandsSent</code> queue (when the command has been handed over to the vehicle driver).
   * </li>
   * <li>Removed from the <code>commandsSent</code> queue and set as the
   * <code>lastCommandExecuted</code> (when the driver reports that the command has been executed)
   * </li>
   * </ol>
   * </p>
   * <p>
   * The earliest stage a movement command can be in that is relevant in the context of rerouting is
   * when it is set as the <code>pendingCommand</code> with the state
   * {@link PendingCommandState#SENDING_PENDING}. At this stage, the resources for the command have
   * already been (successfully) allocated, and it will either be handed over to the vehicle driver
   * or discarded. Rerouting should therefore take place from this command (or rather the respective
   * step) at the earliest.
   * </p>
   * <p>
   * <code>pendingCommand</code> with the state {@link PendingCommandState#ALLOCATION_PENDING}
   * (as well as everything prior to that) is not relevant here, as the allocation for corresponding
   * resources is still pending at this stage, and all pending allocations are cleared upon
   * rerouting.
   * </p>
   *
   * @return A movement command or {@code null} if there is no movement command that has been
   * processed by this vehicle controller in a way that is relevant in the context of rerouting.
   */
  @Nullable
  private MovementCommand lastCommandProcessed() {
    return Optional.ofNullable(pendingCommand)
        .or(() -> Optional.ofNullable(sentCommands.peekLast()))
        .map(CommandResourcePair::getMovementCommand)
        .orElse(lastCommandExecuted);
  }

  private boolean fromSameDriveOrder(
      @Nullable
      MovementCommand commandA,
      @Nullable
      MovementCommand commandB
  ) {
    return commandA != null
        && commandB != null
        && Objects.equals(commandA.getDriveOrder(), commandB.getDriveOrder());
  }

  /**
   * Returns a set of resources needed for executing the given command.
   *
   * @param cmd The command for which to return the needed resources.
   * @return A set of resources needed for executing the given command.
   */
  @Nonnull
  private Set<TCSResource<?>> getNeededResources(MovementCommand cmd) {
    requireNonNull(cmd, "cmd");

    Set<TCSResource<?>> result = new HashSet<>();
    result.add(cmd.getStep().getDestinationPoint());
    if (cmd.getStep().getPath() != null) {
      result.add(cmd.getStep().getPath());
    }
    if (cmd.getOpLocation() != null) {
      result.add(cmd.getOpLocation());
    }

    return result;
  }

  private Set<TCSResource<?>> extractPointAndLocation(Set<TCSResource<?>> resources) {
    return resources.stream()
        .filter(resource -> resource instanceof Point || resource instanceof Location)
        .collect(Collectors.toSet());
  }

  private Set<TCSResource<?>> extractPath(Set<TCSResource<?>> resources) {
    return resources.stream()
        .filter(resource -> resource instanceof Path)
        .collect(Collectors.toSet());
  }

  /**
   * Defines the states the pending command can be in.
   */
  private enum PendingCommandState {
    /**
     * The state is undefined. This is the case when the pending command is {@code null}.
     */
    UNDEFINED,
    /**
     * Allocation of the resources for the pending command was requested but is yet to be confirmed.
     */
    ALLOCATION_PENDING,
    /**
     * Allocation of the resources for the pending command was confirmed but the command is yet to
     * be sent to the communication adapter.
     */
    SENDING_PENDING,
    /**
     * The pending command won't be sent to the communication adapter but the corresponding
     * resources are still allocated.
     */
    WONT_SEND;
  }

  /**
   * A wrapper for a {@link MovementCommand} and the {@link TCSResource}s that are associated with
   * it.
   */
  private static class CommandResourcePair {

    private final MovementCommand movementCommand;
    private final Set<TCSResource<?>> resources;

    /**
     * Creates a new instance.
     *
     * @param movementCommand The movement command.
     * @param resources The set of resources associated with the movement command.
     */
    CommandResourcePair(MovementCommand movementCommand, Set<TCSResource<?>> resources) {
      this.movementCommand = requireNonNull(movementCommand, "movementCommand");
      this.resources = requireNonNull(resources, "resources");
    }

    /**
     * Returns the movement command.
     *
     * @return The movement command.
     */
    public MovementCommand getMovementCommand() {
      return movementCommand;
    }

    /**
     * Returns the set of resources associated with the movement command.
     *
     * @return The set of resources associated with the movement command.
     */
    public Set<TCSResource<?>> getResources() {
      return resources;
    }

    @Override
    public String toString() {
      return "CommandResourcePair{" +
          "movementCommand=" + movementCommand +
          ", resources=" + resources +
          '}';
    }
  }
}
