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
 * A <code>Scheduler</code> manages resources used by vehicles, preventing
 * both collisions and deadlocks.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface Scheduler
    extends Lifecycle {

  /**
   * Claims a set of resources for a vehicle.
   *
   * @param client The client claiming the resources.
   * @param resourceSequence The sequence of resources claimed.
   * @throws IllegalArgumentException If the given list of resources is empty, or if the client
   * already holds a claim.
   */
  void claim(@Nonnull Client client, @Nonnull List<Set<TCSResource<?>>> resourceSequence)
      throws IllegalArgumentException;

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
   */
  void updateProgressIndex(@Nonnull Client client, int index)
      throws IllegalArgumentException;

  /**
   * Unclaims a set of resources claimed by a vehicle.
   *
   * @param client The client unclaiming the resources.
   * @throws IllegalArgumentException If the given client does not hold a claim.
   */
  void unclaim(@Nonnull Client client)
      throws IllegalArgumentException;

  /**
   * Requests allocation of the given resources.
   * The client will be notified via callback if the allocation was successful or not.
   *
   * @param client The client requesting the resources.
   * @param resources The resources requested.
   * @throws IllegalArgumentException If the given client did not claim any resources, or if the
   * resources to be allocated are not in the set of currently claimed resources, or if the client
   * has already requested resources that have not yet been granted.
   */
  void allocate(@Nonnull Client client, @Nonnull Set<TCSResource<?>> resources)
      throws IllegalArgumentException;

  /**
   * Informs the scheduler that a set of resources are to be allocated for the given client
   * <em>immediately</em>, i.e. without blocking.
   * <p>
   * This method should only be called in urgent/emergency cases, for instance if a vehicle has been
   * moved to a different point manually, which has to be reflected by resource allocation in the
   * scheduler.
   * </p>
   * This method does not block, which means that it's safe to call it synchronously.
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
   * Releases all resources allocation by the given client.
   *
   * @param client The client.
   */
  void freeAll(@Nonnull Client client);

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
  @ScheduledApiChange(when = "5.0", details = "Default implementation will be removed.")
  default void preparationSuccessful(@Nonnull Module module,
                                     @Nonnull Client client,
                                     @Nonnull Set<TCSResource<?>> resources) {
  }

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
     * (any more), freeing them implicitly.
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
     */
    void claim(@Nonnull Client client, @Nonnull List<Set<TCSResource<?>>> claim);

    /**
     * Resets a client's <i>total claim</i>.
     *
     * @param client The client for which to reset the claim.
     */
    void unclaim(@Nonnull Client client);

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
    @ScheduledApiChange(when = "5.0", details = "Default implementation will be removed.")
    default void prepareAllocation(@Nonnull Client client,
                                   @Nonnull Set<TCSResource<?>> resources) {
    }

    /**
     * Checks if this module is done preparing the given resources for a client.
     *
     * @param client The client the resources are being prepared for.
     * @param resources The resources to be checked.
     * @return <code>true</code> if the resoruces are prepared for a client.
     */
    @ScheduledApiChange(when = "5.0", details = "Default implementation will be removed.")
    default boolean hasPreparedAllocation(@Nonnull Client client,
                                          @Nonnull Set<TCSResource<?>> resources) {
      return true;
    }

    /**
     * Informs this module about resources being fully released by a client.
     *
     * @param client The client releasing the resources.
     * @param resources The resources being released.
     */
    @ScheduledApiChange(when = "5.0", details = "Default implementation will be removed.")
    default void allocationReleased(@Nonnull Client client,
                                    @Nonnull Set<TCSResource<?>> resources) {
    }
  }
}
