// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT

package org.opentcs.kernel.extensions.watchdog;

import static java.util.Objects.requireNonNull;

import jakarta.inject.Inject;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.opentcs.components.Lifecycle;
import org.opentcs.customizations.kernel.KernelExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KernelHeartbeatLogger
    implements
      Runnable,
      Lifecycle {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(KernelHeartbeatLogger.class);
  /**
   * The kernel executor.
   */
  private final ScheduledExecutorService kernelExecutor;
  /**
   * The configuration.
   */
  private final WatchdogConfiguration configuration;
  /**
   * Whether this check is initialized.
   */
  private boolean initialized;
  /**
   * The Future created for the kernel heartbeat logger.
   */
  private ScheduledFuture<?> scheduledFuture;
  /**
   * The heartbeat counter.
   */
  private long counter;

  /**
   * Creates a new instance.
   *
   * @param kernelExecutor The kernel executor.
   * @param configuration The watchdog configuration.
   */
  @Inject
  public KernelHeartbeatLogger(
      @KernelExecutor
      ScheduledExecutorService kernelExecutor,
      WatchdogConfiguration configuration
  ) {
    this.kernelExecutor = requireNonNull(kernelExecutor, "kernelExecutor");
    this.configuration = requireNonNull(configuration, "configuration");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

    scheduledFuture = kernelExecutor.scheduleAtFixedRate(
        this,
        configuration.heartbeatLogInterval(),
        configuration.heartbeatLogInterval(),
        TimeUnit.MILLISECONDS
    );

    initialized = true;
    counter = 0;
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

    if (scheduledFuture != null) {
      scheduledFuture.cancel(true);
      scheduledFuture = null;
    }

    initialized = false;
  }

  @Override
  public void run() {
    counter++;
    LOG.debug("Kernel heartbeat logger running (counter value: {})", counter);
  }
}
