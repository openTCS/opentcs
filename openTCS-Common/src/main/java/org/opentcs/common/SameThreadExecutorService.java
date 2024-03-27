/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.common;

import java.util.Collection;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import static org.opentcs.util.Assertions.checkArgument;
import static org.opentcs.util.Assertions.checkState;

/**
 * An executor service that executes all task directly on the same thread.
 */
public class SameThreadExecutorService
    implements ExecutorService {

  private boolean shutdown;

  public SameThreadExecutorService() {
    shutdown = false;
  }

  @Override
  public void shutdown() {
    shutdown = true;
  }

  @Override
  public List<Runnable> shutdownNow() {
    shutdown = true;
    return List.of();
  }

  @Override
  public boolean isShutdown() {
    return shutdown;
  }

  @Override
  public boolean isTerminated() {
    return shutdown;
  }

  @Override
  public boolean awaitTermination(long timeout, TimeUnit unit)
      throws InterruptedException {
    // Calling `awaitTermination` before calling shutdown is not a valid use for an executor
    // service and therefore should throw an exeception.
    checkState(shutdown, "Awaiting termination before shutdown was called");
    return true;
  }

  @Override
  public <T> Future<T> submit(Callable<T> task) {
    CompletableFuture<T> future = new CompletableFuture<>();
    try {
      future.complete(task.call());
    }
    catch (Exception e) {
      future.completeExceptionally(e);
    }
    return future;
  }

  @Override
  public <T> Future<T> submit(Runnable task, T result) {
    return this.submit(() -> {
      task.run();
      return result;
    });
  }

  @Override
  public Future<?> submit(Runnable task) {
    return this.submit(task, null);
  }

  @Override
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
      throws InterruptedException {
    return tasks.stream()
        .map(task -> submit(task))
        .collect(Collectors.toList());
  }

  @Override
  public <T> List<Future<T>> invokeAll(
      Collection<? extends Callable<T>> tasks,
      long timeout,
      TimeUnit unit
  )
      throws InterruptedException {
    return invokeAll(tasks);
  }

  @Override
  public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
      throws InterruptedException, ExecutionException {
    requireNonNull(tasks, "tasks");
    checkArgument(tasks.isEmpty(), "tasks is empty");
    if (tasks.stream().anyMatch(task -> task == null)) {
      throw new NullPointerException("At least one task given is null");
    }

    // This implementation interprets the method documentation so that all tasks are started
    // before the result of the first successful one is returned.
    // Since all task are executed directly on the same thread, all task will finish before this
    // method returns the value of any successful task.
    List<Future<T>> futures = invokeAll(tasks);

    for (Future<T> future : futures) {
      try {
        return future.get();
      }
      catch (InterruptedException e) {
        throw e;
      }
      catch (Exception e) {
        // any other exception thrown by the future is ignored
      }
    }

    throw new ExecutionException(new Exception("None of the provided task sucessfully terminated"));
  }

  @Override
  public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
      throws InterruptedException, ExecutionException, TimeoutException {
    return invokeAny(tasks);
  }

  @Override
  public void execute(Runnable command) {
    command.run();
  }

}
