// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT

package org.opentcs.strategies.basic.scheduling;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import org.opentcs.components.Lifecycle;
import org.opentcs.components.kernel.Scheduler;

/**
 * Manages pending allocations and allocation futures scheduled on the kernel executor.
 * <p>
 * This implementation assumes single-threaded access and is not thread-safe.
 */
public class PendingAllocationManager
    implements
      Lifecycle {

  /**
   * Allocations deferred because they couldn't be granted, yet.
   */
  private final Queue<AllocatorCommand.Allocate> deferredAllocations = new ArrayDeque<>();
  /**
   * Allocations that are scheduled for execution on the kernel executor.
   */
  private final Map<Scheduler.Client, List<Future<?>>> allocationFutures = new HashMap<>();
  /**
   * This instance's initialized flag.
   */
  private boolean initialized;

  /**
   * Creates an instance.
   */
  public PendingAllocationManager() {

  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

    clear();
    this.initialized = true;
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

    clear();
    this.initialized = false;
  }

  /**
   * Clears deferred allocations and all allocation futures.
   */
  private void clear() {
    deferredAllocations.clear();

    cancelAllPendingAllocateFutures();
    allocationFutures.clear();
  }

  /**
   * Clears pending allocations (deferred + futures) for the given client.
   *
   * @param client The scheduler's client.
   */
  public void clearPendingAllocations(
      @Nonnull
      Scheduler.Client client
  ) {
    requireNonNull(client, "client");

    clearDeferredAllocations(client);
    cancelPendingAllocateFutures(client);
    removeCompletedAllocateFutures(client);
  }

  private void clearDeferredAllocations(Scheduler.Client client) {
    deferredAllocations.removeIf(allocate -> client.equals(allocate.getClient()));
  }

  /**
   * Adds a deferred allocation to the queue.
   *
   * @param allocate The allocate command.
   */
  public void addDeferredAllocation(
      @Nonnull
      AllocatorCommand.Allocate allocate
  ) {
    requireNonNull(allocate, "allocate");

    deferredAllocations.add(allocate);
  }

  /**
   * Drains all deferred allocations.
   * <p>
   * Retrieves all deferred allocations and clears the internal queue.
   *
   * @return All deferred allocations that were pending.
   */
  public List<AllocatorCommand.Allocate> drainDeferredAllocations() {
    List<AllocatorCommand.Allocate> allocations = new ArrayList<>(deferredAllocations);
    deferredAllocations.clear();
    return allocations;
  }

  /**
   * Returns the number of pending allocation futures for each client.
   *
   * @return A map from each scheduler client to the number of its pending allocation futures.
   */
  public Map<Scheduler.Client, Integer> countPendingAllocationFutures() {
    return allocationFutures.entrySet()
        .stream()
        .map(entry -> Map.entry(entry.getKey(), countUndoneFutures(entry.getValue())))
        .filter(entry -> entry.getValue() > 0)
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private int countUndoneFutures(List<Future<?>> futures) {
    return (int) futures.stream()
        .filter(f -> !f.isDone())
        .count();
  }

  /**
   * Adds an allocation future for the given client.
   * <p>
   * This method also removes any previously completed futures for the client.
   *
   * @param client The scheduler's client.
   * @param allocationFuture The allocate future.
   */
  public void addAllocationFuture(
      @Nonnull
      Scheduler.Client client,
      @Nonnull
      Future<?> allocationFuture
  ) {
    requireNonNull(client, "client");
    requireNonNull(allocationFuture, "allocationFuture");

    // Clean up the collection of allocate futures and remove futures that have already been
    // completed. This could also be done in other places, but doing it for every new allocation
    // should be sufficient.
    removeCompletedAllocateFutures(client);

    allocationFutures
        .computeIfAbsent(client, v -> new ArrayList<>())
        .add(allocationFuture);
  }

  private void removeCompletedAllocateFutures(Scheduler.Client client) {
    List<Future<?>> futures = allocationFutures.get(client);
    if (futures != null) {
      futures.removeIf(Future::isDone);
    }
  }

  private void cancelPendingAllocateFutures(Scheduler.Client client) {
    if (!allocationFutures.containsKey(client)) {
      return;
    }

    allocationFutures.get(client).stream()
        .filter(future -> !future.isDone())
        .forEach(future -> future.cancel(false));
  }

  private void cancelAllPendingAllocateFutures() {
    allocationFutures.values()
        .forEach(futures -> futures.forEach(future -> {
          if (!future.isDone()) {
            future.cancel(false);
          }
        })
        );
  }

}
