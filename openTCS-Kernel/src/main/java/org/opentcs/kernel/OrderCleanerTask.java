/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel;

import com.google.common.collect.Iterables;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.regex.Pattern;
import javax.inject.Inject;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.kernel.workingset.TransportOrderPool;
import org.opentcs.util.CyclicTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A task that periodically removes orders in a final state.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
class OrderCleanerTask
    extends CyclicTask {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(OrderCleanerTask.class);
  /**
   * A global object to be used for synchronization within the kernel.
   */
  private final Object globalSyncObject;
  /**
   * Keeps all the transport orders.
   */
  private final TransportOrderPool orderPool;
  /**
   * This class's configuration.
   */
  private final OrderPoolConfiguration configuration;

  /**
   * Creates a new OrderCleanerTask.
   *
   * @param kernel The kernel.
   * @param configuration This class's configuration.
   */
  @Inject
  public OrderCleanerTask(@GlobalKernelSync Object globalSyncObject,
                          TransportOrderPool orderPool,
                          OrderPoolConfiguration configuration) {
    super(configuration.sweepInterval());
    this.globalSyncObject = requireNonNull(globalSyncObject, "globalSyncObject");
    this.orderPool = requireNonNull(orderPool, "orderPool");
    this.configuration = requireNonNull(configuration, "configuration");
  }

  @Override
  protected void runActualTask() {
    synchronized (globalSyncObject) {
      LOG.debug("Sweeping order pool...");
      // Candidates that are created before this point of time should be removed.
      long creationTimeThreshold = System.currentTimeMillis() - configuration.sweepAge();

      // Remove all transport orders in a final state that do NOT belong to a sequence and that are
      // older than the threshold.
      orderPool.getTransportOrders((Pattern) null).stream()
          .filter(order -> order.getState().isFinalState())
          .filter(order -> order.getWrappingSequence() == null)
          .filter(order -> order.getCreationTime() < creationTimeThreshold)
          .forEach(order -> orderPool.removeTransportOrder(order.getReference()));

      // Remove all order sequences that have been finished, including their transport orders.
      orderPool.getOrderSequences(null).stream()
          .filter(seq -> seq.isFinished())
          .filter(seq -> {
            List<TCSObjectReference<TransportOrder>> orderRefs = seq.getOrders();
            if (orderRefs.isEmpty()) {
              return true;
            }
            TransportOrder lastOrder = orderPool.getTransportOrder(Iterables.getLast(orderRefs));
            return lastOrder.getCreationTime() < creationTimeThreshold;
          })
          .forEach(seq -> orderPool.removeFinishedOrderSequenceAndOrders(seq.getReference()));
    }
  }
}
