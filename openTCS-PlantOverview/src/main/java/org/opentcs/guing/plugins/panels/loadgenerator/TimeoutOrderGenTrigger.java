/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.plugins.panels.loadgenerator;

import java.util.Objects;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.util.CyclicTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Triggers creation of a batch of orders after a given timeout.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
class TimeoutOrderGenTrigger
    implements OrderGenerationTrigger {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(TimeoutOrderGenTrigger.class);
  /**
   * The timeout after which to trigger (in ms).
   */
  private final int timeout;
  /**
   * The instance actually creating the new orders.
   */
  private final OrderBatchCreator orderBatchCreator;
  /**
   * The actual task triggering order generation.
   */
  private volatile TriggerTask triggerTask;

  /**
   * Creates a new TimeoutOrderGenTrigger.
   *
   * @param timeout The timeout after which to trigger (in ms).
   * @param orderBatchCreator The order batch creator
   */
  public TimeoutOrderGenTrigger(final int timeout,
                                final OrderBatchCreator orderBatchCreator) {
    this.timeout = timeout;
    this.orderBatchCreator = Objects.requireNonNull(orderBatchCreator,
                                                    "orderBatchCreator is null");
  }

  @Override
  public void setTriggeringEnabled(boolean enabled) {
    if (enabled) {
      triggerTask = new TriggerTask(timeout);
      Thread triggerThread = new Thread(triggerTask, "triggerTask");
      triggerThread.start();
    }
    else {
      if (triggerTask != null) {
        triggerTask.terminate();
        triggerTask = null;
      }
    }
  }

  @Override
  public void triggerOrderGeneration()
      throws KernelRuntimeException {
    orderBatchCreator.createOrderBatch();
  }

  /**
   * A task that repeatedly triggers order generation after a given timeout.
   */
  private final class TriggerTask
      extends CyclicTask {

    /**
     * Creates a new instance.
     *
     * @param timeout The timeout after which to trigger order generation.
     */
    private TriggerTask(int timeout) {
      super(timeout);
    }

    @Override
    protected void runActualTask() {
      try {
        triggerOrderGeneration();
      }
      catch (KernelRuntimeException exc) {
        LOG.warn("Exception triggering order generation, terminating trigger task", exc);
        this.terminate();
      }
    }
  }
}
