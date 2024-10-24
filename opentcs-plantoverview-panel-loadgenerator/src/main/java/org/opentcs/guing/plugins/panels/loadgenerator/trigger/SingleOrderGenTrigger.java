// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.plugins.panels.loadgenerator.trigger;

import java.util.Objects;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.guing.plugins.panels.loadgenerator.batchcreator.OrderBatchCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Triggers creation of transport orders only once.
 */
public class SingleOrderGenTrigger
    implements
      OrderGenerationTrigger {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(SingleOrderGenTrigger.class);
  /**
   * The instance actually creating the new orders.
   */
  private final OrderBatchCreator orderBatchCreator;

  /**
   * Creates a new SingleOrderGenTrigger.
   *
   * @param orderBatchCreator The order batch creator
   */
  public SingleOrderGenTrigger(final OrderBatchCreator orderBatchCreator) {
    this.orderBatchCreator = Objects.requireNonNull(
        orderBatchCreator,
        "orderBatchCreator is null"
    );
  }

  @Override
  public void setTriggeringEnabled(boolean enabled) {
    if (enabled) {
      triggerOrderGeneration();
    }
  }

  @Override
  public void triggerOrderGeneration()
      throws KernelRuntimeException {
    try {
      if (orderBatchCreator != null) {
        orderBatchCreator.createOrderBatch();
      }
    }
    catch (KernelRuntimeException exc) {
      LOG.warn("Exception triggering order generation, terminating triggering", exc);
    }
  }
}
