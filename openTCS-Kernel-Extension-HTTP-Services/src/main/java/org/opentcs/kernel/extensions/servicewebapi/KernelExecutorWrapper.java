/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi;

import static java.util.Objects.requireNonNull;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.customizations.kernel.KernelExecutor;

/**
 * Calls callables/runnables via the kernel executor and waits for the outcome.
 */
public class KernelExecutorWrapper {

  private final ExecutorService kernelExecutor;

  /**
   * Creates a new instance.
   *
   * @param kernelExecutor The kernel executor.
   */
  @Inject
  public KernelExecutorWrapper(@KernelExecutor ExecutorService kernelExecutor) {
    this.kernelExecutor = requireNonNull(kernelExecutor, "kernelExecutor");
  }

  /**
   * Calls the given callable via the kernel executor and waits for the outcome.
   *
   * @param <T> The callable's return type.
   * @param callable The callable.
   * @return The result of the call.
   * @throws IllegalStateException In case the call via the kernel executor was unexpectedly
   * interrupted.
   * @throws RuntimeException In case an exception was thrown from the callable. If the exception
   * thrown is a {@code RuntimeException}, it is forwarded directly; if it is not a
   * {@code RuntimeException}, it is wrapped in a {@link KernelRuntimeException}.
   */
  public <T> T callAndWait(Callable<T> callable)
      throws IllegalStateException, RuntimeException {
    requireNonNull(callable, "callable");

    try {
      return kernelExecutor.submit(callable).get();
    }
    catch (InterruptedException exc) {
      throw new IllegalStateException("Unexpectedly interrupted");
    }
    catch (ExecutionException exc) {
      if (exc.getCause() instanceof RuntimeException) {
        throw (RuntimeException) exc.getCause();
      }
      throw new KernelRuntimeException(exc.getCause());
    }
  }

  /**
   * Calls the given runnable via the kernel executor and waits for the outcome.
   *
   * @param runnable The runnable.
   * @throws IllegalStateException In case the call via the kernel executor was unexpectedly
   * interrupted.
   * @throws RuntimeException In case an exception was thrown from the runnable. If the exception
   * thrown is a {@code RuntimeException}, it is forwarded directly; if it is not a
   * {@code RuntimeException}, it is wrapped in a {@link KernelRuntimeException}.
   */
  public void callAndWait(Runnable runnable)
      throws IllegalStateException, RuntimeException {
    requireNonNull(runnable, "runnable");

    callAndWait(Executors.callable(runnable));
  }
}
