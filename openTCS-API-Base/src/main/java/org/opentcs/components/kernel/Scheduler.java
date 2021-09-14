/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.components.kernel;

import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opentcs.components.Lifecycle;
import org.opentcs.data.model.TCSResource;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * Manages resources used by clients (vehicles) to help prevent both collisions and deadlocks.
 * <p>
 * Every client usually interacts with the <code>Scheduler</code> according to the following
 * workflow:
 * </p>
 * <ol>
 * <li>
 * Initially, the client calls
 * {@link #allocateNow(org.opentcs.components.kernel.Scheduler.Client, java.util.Set) allocateNow()}
 * when a vehicle pops up somewhere in the driving course.
 * This usually happens either upon kernel startup or when a vehicle communicates its current
 * position to the kernel for the first time.
 * </li>
 * <li>
 * Once a transport order is assigned to a vehicle, the client calls
 * {@link #claim(org.opentcs.components.kernel.Scheduler.Client, java.util.List) claim()} with the
 * complete sequence of resource sets the vehicle needs to process the transport order - usually
 * each containing a point and the path leading to it.
 * </li>
 * <li>
 * As the vehicle processes the transport order, the client subsequently calls
 * {@link #allocate(org.opentcs.components.kernel.Scheduler.Client, java.util.Set) allocate()} for
 * resources needed next (for reaching the next point on the route).
 * The <code>Scheduler</code> asynchronously calls back either
 * {@link Client#allocationSuccessful(java.util.Set)} or
 * {@link Client#allocationFailed(java.util.Set)}, informing the client about the result.
 * Upon allocating the resources for the client, it also implicitly removes them from the head of
 * the client's claim sequence.
 * </li>
 * <li>
 * As the vehicle passes points (and paths) on the route, the client calls
 * {@link #free(org.opentcs.components.kernel.Scheduler.Client, java.util.Set) free()} for resources
 * it does not need any more, allowing these resources to be allocated by other clients.
 * </li>
 * </ol>
 * <p>
 * At the end of this process, the client's claim sequence is empty, and only the most recently
 * allocated resources are still assigned to it, reflecting the vehicle's current position.
 * (If the vehicle has disappeared from the driving course after processing the transport order, the
 * client would call {@link #freeAll(org.opentcs.components.kernel.Scheduler.Client) freeAll()} to
 * inform the <code>Scheduler</code> about this.)
 * </p>
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface Scheduler
    extends Lifecycle {

  /**
   * The key of a path property defining the direction in which a vehicle is entering a block when
   * it's taking the path.
   */
  String PROPKEY_BLOCK_ENTRY_DIRECTION = "tcs:blockEntryDirection";

  /**
   * Sets/Updates the resource claim for a vehicle.
   * <p>
   * <em>Claimed</em> resources are resources that a vehicle will eventually require for executing
   * its movements in the future, but for which it does not request allocation, yet.
   * Claiming resources provides information to the scheduler about future allocations <em>and their
   * intended order</em>, allowing the scheduler to consider these information for its resource
   * planning.
   * </p>
   * <p>
   * Resources can be claimed by multiple vehicles at the same time.
   * This is different from allocations:
   * Only a single vehicle can allocate a resource at the same time.
   * </p>
   *
   * @param client The client claiming the resources.
   * @param resourceSequence The sequence of resources claimed. May be empty to clear the client's
   * claim.
   */
  void claim(@Nonnull Client client, @Nonnull List<Set<TCSResource<?>>> resourceSequence);

  /**
   * Notifies the scheduler that the given client has now reached the given index in its claimed
   * resource sequence, and that the client does not need the resources preceding the index in the
   * sequence, any more.
   *
   * @param client The client.
   * @param index The new index in the client's claimed resource sequence.
   * @throws IllegalArgumentException If the client does not hold a claim, or if the new index is
   * larger than a valid index in its claim's resource sequence, or if the new index is not larger
   * than the current index.
   * @deprecated Stick to
   * {@link #claim(org.opentcs.components.kernel.Scheduler.Client, java.util.List) claim()} and
   * {@link #allocate(org.opentcs.components.kernel.Scheduler.Client, java.util.Set) allocate()}.
   * They implicitly update both the set of claimed and the set of allocated resources.
   */
  @Deprecated
  @ScheduledApiChange(when = "6.0", details = "Will be removed.")
  default void updateProgressIndex(@Nonnull Client client, int index)
      throws IllegalArgumentException {
  }

  /**
   * Unclaims a set of resources claimed by a vehicle.
   *
   * @param client The client unclaiming the resources.
   * @throws IllegalArgumentException If the given client does not hold a claim.
   * @deprecated Use {@link #claim(org.opentcs.components.kernel.Scheduler.Client, java.util.List)}
   * with an empty list, instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "6.0", details = "Will be removed.")
  default void unclaim(@Nonnull Client client)
      throws IllegalArgumentException {
    claim(client, List.of());
  }

  /**
   * Requests allocation of the given resources.
   * The client will be informed via a callback to
   * {@link Client#allocationSuccessful(java.util.Set)} or
   * {@link Client#allocationFailed(java.util.Set)} whether the allocation was successful or not.
   * <ul>
   * <li>
   * Clients may only allocate resources in the order they have previously
   * {@link #claim(org.opentcs.components.kernel.Scheduler.Client, java.util.List) claim()}ed them.
   * </li>
   * <li>
   * Upon allocation, the scheduler will implicitly remove the set of allocated resources from (the
   * head of) the client's claim sequence.
   * </li>
   * <li>
   * As a result, a client may only allocate the set of resources at the head of its claim sequence.
   * </li>
   * </ul>
   *
   * @param client The client requesting the resources.
   * @param resources The resources to be allocated.
   * @throws IllegalArgumentException If the set of resources to be allocated is not equal to the
   * <em>next</em> set in the sequence of currently claimed resources, or if the client has already
   * requested resources that have not yet been granted.
   * @see #claim(org.opentcs.components.kernel.Scheduler.Client, java.util.List)
   */
  void allocate(@Nonnull Client client, @Nonnull Set<TCSResource<?>> resources)
      throws IllegalArgumentException;

  /**
   * Informs the scheduler that a set of resources are to be allocated for the given client
   * <em>immediately</em>.
   * <p>
   * Note the following:
   * </p>
   * <ul>
   * <li>
   * This method should only be called in urgent/emergency cases, for instance if a vehicle has been
   * moved to a different point manually, which has to be reflected by resource allocation in the
   * scheduler.
   * </li>
   * <li>
   * Unlike
   * {@link #allocate(org.opentcs.components.kernel.Scheduler.Client, java.util.Set) allocate()},
   * this method does not block, i.e. the operation happens synchronously.
   * </li>
   * <li>
   * This method does <em>not</em> implicitly deallocate or unclaim any other resources for the
   * client.
   * </li>
   * </ul>
   *
   * @param client The client requesting the resources.
   * @param resources The resources requested.
   * @throws ResourceAllocationException If it's impossible to allocate the given set of resources
   * for the given client.
   */
  void allocateNow(@Nonnull Client client, @Nonnull Set<TCSResource<?>> resources)
      throws ResourceAllocationException;

  /**
   * Releases a set of resources allocated by a client.
   *
   * @param client The client releasing the resources.
   * @param resources The resources released. Any resources in the given set not allocated by the
   * given client are ignored.
   */
  void free(@Nonnull Client client, @Nonnull Set<TCSResource<?>> resources);

  /**
   * Releases all resources allocated by the given client.
   *
   * @param client The client.
   */
  void freeAll(@Nonnull Client client);

  /**
   * Releases all pending resource allocations for the given client.
   *
   * @param client The client.
   */
  @ScheduledApiChange(when = "6.0", details = "Default implementation will be removed.")
  default void clearPendingAllocations(@Nonnull Client client) {
  }

  /**
   * Explicitly triggers a rescheduling run during which the scheduler tries to allocate resources
   * for all waiting clients.
   */
  @ScheduledApiChange(when = "6.0", details = "Default implementation will be removed.")
  default void reschedule() {
  }

  /**
   * Returns all resource allocations as a map of client IDs to resources.
   *
   * @return All resource allocations as a map of client IDs to resources.
   */
  @Nonnull
  Map<String, Set<TCSResource<?>>> getAllocations();

  /**
   * Informs the scheduler that a set of resources was successfully prepared in order of allocating
   * them to a client.
   *
   * @param module The module a preparation was necessary for.
   * @param client The client that requested the preparation/allocation.
   * @param resources The resources that are now prepared for the client.
   */
  void preparationSuccessful(@Nonnull Module module,
                             @Nonnull Client client,
                             @Nonnull Set<TCSResource<?>> resources);

  /**
   * Defines callback methods for clients of the resource scheduler.
   */
  interface Client {

    /**
     * Returns an ID string for this client.
     * The returned string should be unique among all clients in the system.
     *
     * @return An unique ID string for this client.
     */
    @Nonnull
    String getId();

    /**
     * Called when resources have been reserved for this client.
     *
     * @param resources The resources reserved.
     * @return <code>true</code> if, and only if, this client accepts the resources allocated. A
     * return value of <code>false</code> indicates this client does not need the given resources
     * (any more), freeing them implicitly, but not restoring any previous claim.
     */
    boolean allocationSuccessful(@Nonnull Set<TCSResource<?>> resources);

    /**
     * Called if it was impossible to allocate a requested set of resources for this client.
     *
     * @param resources The resources which could not be reserved.
     */
    void allocationFailed(@Nonnull Set<TCSResource<?>> resources);
  }

  /**
   * A scheduler module.
   */
  interface Module
      extends Lifecycle {

    /**
     * Sets a client's <i>total claim</i>.
     * With vehicles, this is equivalent to the route a vehicle plans to take.
     *
     * @param client The client the resource sequence is claimed by.
     * @param claim The resource sequence, i.e. total claim.
     * @deprecated Redundant - use
     * {@link #setAllocationState(org.opentcs.components.kernel.Scheduler.Client, java.util.Set, java.util.List)}
     * instead/exclusively.
     */
    @Deprecated
    @ScheduledApiChange(when = "6.0", details = "Will be removed.")
    default void claim(@Nonnull Client client, @Nonnull List<Set<TCSResource<?>>> claim) {
    }

    /**
     * Resets a client's <i>total claim</i>.
     *
     * @param client The client for which to reset the claim.
     * @deprecated Redundant - use
     * {@link #setAllocationState(org.opentcs.components.kernel.Scheduler.Client, java.util.Set, java.util.List)}
     * instead/exclusively.
     */
    @Deprecated
    @ScheduledApiChange(when = "6.0", details = "Will be removed.")
    default void unclaim(@Nonnull Client client) {
    }

    /**
     * Informs this module about a client's current allocation state.
     *
     * @param client The client.
     * @param alloc The client's currently allocated resources.
     * @param remainingClaim The client's remaining claim.
     */
    void setAllocationState(@Nonnull Client client,
                            @Nonnull Set<TCSResource<?>> alloc,
                            @Nonnull List<Set<TCSResource<?>>> remainingClaim);

    /**
     * Checks if the resulting system state is safe if the given set of resources
     * would be allocated by the given resource user.
     *
     * @param client The <code>ResourceUser</code> requesting resources set.
     * @param resources The requested resources.
     * @return <code>true</code> if this module thinks the given resources may be allocated for the
     * given client.
     */
    boolean mayAllocate(@Nonnull Client client, @Nonnull Set<TCSResource<?>> resources);

    /**
     * Lets this module prepare the given resources so they can be allocated to a client.
     *
     * @param client The client the resources are being prepared for.
     * @param resources The resources to be prepared.
     */
    void prepareAllocation(@Nonnull Client client, @Nonnull Set<TCSResource<?>> resources);

    /**
     * Checks if this module is done preparing the given resources for a client.
     *
     * @param client The client the resources are being prepared for.
     * @param resources The resources to be checked.
     * @return <code>true</code> if the resoruces are prepared for a client.
     */
    boolean hasPreparedAllocation(@Nonnull Client client, @Nonnull Set<TCSResource<?>> resources);

    /**
     * Informs this module about resources being fully released by a client.
     *
     * @param client The client releasing the resources.
     * @param resources The resources being released.
     */
    void allocationReleased(@Nonnull Client client, @Nonnull Set<TCSResource<?>> resources);
  }
}
