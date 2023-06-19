/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel;

import com.google.common.collect.Iterables;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.function.Predicate;
import javax.inject.Inject;
import org.opentcs.components.kernel.OrderSequenceCleanupApproval;
import org.opentcs.components.kernel.PeripheralJobCleanupApproval;
import org.opentcs.components.kernel.TransportOrderCleanupApproval;
import org.opentcs.customizations.kernel.GlobalSyncObject;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.data.peripherals.PeripheralJob;
import org.opentcs.kernel.workingset.PeripheralJobPoolManager;
import org.opentcs.kernel.workingset.TransportOrderPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A task that periodically removes orders in a final state.
 */
public class OrderCleanerTask
    implements Runnable {

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
  private final TransportOrderPoolManager orderPoolManager;
  /**
   * Keeps all peripheral jobs.
   */
  private final PeripheralJobPoolManager peripheralJobPoolManager;
  /**
   * Checks whether transport orders may be removed.
   */
  private final Set<TransportOrderCleanupApproval> orderCleanupApprovals;
  /**
   * Checks whether order sequences may be removed.
   */
  private final Set<OrderSequenceCleanupApproval> sequenceCleanupApprovals;
  /**
   * Checks whether peripheral jobs may be removed.
   */
  private final Set<PeripheralJobCleanupApproval> peripheralJobCleanupApprovals;
  /**
   * This class's configuration.
   */
  private final OrderPoolConfiguration configuration;

  /**
   * Creates a new instance.
   *
   * @param globalSyncObject The kernel threads' global synchronization object.
   * @param orderPoolManager The order pool manager to be used.
   * @param peripheralJobPoolManager The peripheral job pool manager to be used.
   * @param orderCleanupApprovals The set of order cleanup approvals to use.
   * @param sequenceCleanupApprovals The set of sequence cleanup approvals to use.
   * @param peripheralJobCleanupApprovals The set of peripheral job cleanup approvals to use.
   * @param configuration This class's configuration.
   */
  @Inject
  public OrderCleanerTask(@GlobalSyncObject Object globalSyncObject,
                          TransportOrderPoolManager orderPoolManager,
                          PeripheralJobPoolManager peripheralJobPoolManager,
                          Set<TransportOrderCleanupApproval> orderCleanupApprovals,
                          Set<OrderSequenceCleanupApproval> sequenceCleanupApprovals,
                          Set<PeripheralJobCleanupApproval> peripheralJobCleanupApprovals,
                          OrderPoolConfiguration configuration) {
    this.globalSyncObject = requireNonNull(globalSyncObject, "globalSyncObject");
    this.orderPoolManager = requireNonNull(orderPoolManager, "orderPoolManager");
    this.peripheralJobPoolManager = requireNonNull(peripheralJobPoolManager,
                                                   "peripheralJobPoolManager");
    this.orderCleanupApprovals = requireNonNull(orderCleanupApprovals, "orderCleanupApprovals");
    this.sequenceCleanupApprovals = requireNonNull(sequenceCleanupApprovals,
                                                   "sequenceCleanupApprovals");
    this.peripheralJobCleanupApprovals = requireNonNull(peripheralJobCleanupApprovals,
                                                        "peripheralJobCleanupApprovals");
    this.configuration = requireNonNull(configuration, "configuration");
  }

  public long getSweepInterval() {
    return configuration.sweepInterval();
  }

  @Override
  public void run() {
    synchronized (globalSyncObject) {
      LOG.debug("Sweeping order pool...");
      // Candidates that are created before this point of time should be removed.
      Instant creationTimeThreshold = Instant.now().minusMillis(configuration.sweepAge());

      // Remove all peripheral jobs in a final state that do not belong to a transport order and
      // that are older than the threshold.
      for (PeripheralJob peripheralJob
               : peripheralJobPoolManager.getObjectRepo().getObjects(
              PeripheralJob.class,
              new PeripheralJobApproval(creationTimeThreshold))) {
        peripheralJobPoolManager.removePeripheralJob(peripheralJob.getReference());
      }

      // Remove all transport orders in a final state that do NOT belong to a sequence and that are
      // older than the threshold, including their related peripheral jobs.
      for (TransportOrder transportOrder
               : orderPoolManager.getObjectRepo().getObjects(
              TransportOrder.class,
              new OrderApproval(creationTimeThreshold))) {
        removeTransportOrderAndRelatedPeripheralJobs(transportOrder.getReference());
      }

      // Remove all order sequences that have been finished, including their transport orders and
      // the transport orders' related peripheral jobs.
      for (OrderSequence orderSequence
               : orderPoolManager.getObjectRepo().getObjects(
              OrderSequence.class,
              new SequenceApproval(creationTimeThreshold))) {
        for (TCSObjectReference<TransportOrder> transportOrderRef : orderSequence.getOrders()) {
          removeTransportOrderAndRelatedPeripheralJobs(transportOrderRef);
        }
        orderPoolManager.removeFinishedOrderSequenceAndOrders(orderSequence.getReference());
      }
    }
  }

  private void removeTransportOrderAndRelatedPeripheralJobs(
      TCSObjectReference<TransportOrder> transportOrderRef) {
    for (PeripheralJob peripheralJob
             : peripheralJobPoolManager.getObjectRepo().getObjects(
            PeripheralJob.class,
            job -> Objects.equals(job.getRelatedTransportOrder(), transportOrderRef))) {
      peripheralJobPoolManager.removePeripheralJob(peripheralJob.getReference());
    }
    orderPoolManager.removeTransportOrder(transportOrderRef);
  }

  /**
   * Checks whether a transport order may be removed.
   */
  private class OrderApproval
      implements Predicate<TransportOrder> {

    /**
     * Threshold for when a transport order can be removed if it was created before this.
     */
    private final Instant creationTimeThreshold;

    OrderApproval(Instant creationTimeThreshold) {
      this.creationTimeThreshold = creationTimeThreshold;
    }

    @Override
    public boolean test(TransportOrder order) {
      if (!order.getState().isFinalState()) {
        return false;
      }
      if (order.getWrappingSequence() != null) {
        return false;
      }
      if (isRelatedToJobWithNonFinalState(order)) {
        return false;
      }
      if (order.getCreationTime().isAfter(creationTimeThreshold)) {
        return false;
      }
      for (TransportOrderCleanupApproval approval : orderCleanupApprovals) {
        if (!approval.test(order)) {
          return false;
        }
      }
      return true;
    }

    private boolean isRelatedToJobWithNonFinalState(TransportOrder order) {
      return !peripheralJobPoolManager.getObjectRepo().getObjects(
          PeripheralJob.class,
          job -> Objects.equals(job.getRelatedTransportOrder(), order.getReference())
          && !job.getState().isFinalState()
      ).isEmpty();
    }
  }

  /**
   * Checks whether an order sequence may be removed.
   */
  private class SequenceApproval
      implements Predicate<OrderSequence> {

    /**
     * Threshold for when a sequence can be removed if it was created before this.
     */
    private final Instant creationTimeThreshold;

    SequenceApproval(Instant creationTimeThreshold) {
      this.creationTimeThreshold = creationTimeThreshold;
    }

    @Override
    public boolean test(OrderSequence seq) {
      if (!seq.isFinished()) {
        return false;
      }
      List<TCSObjectReference<TransportOrder>> orderRefs = seq.getOrders();
      if (!orderRefs.isEmpty()) {
        TransportOrder lastOrder
            = orderPoolManager.getObjectRepo().getObject(TransportOrder.class,
                                                         Iterables.getLast(orderRefs));
        if (lastOrder.getCreationTime().isAfter(creationTimeThreshold)) {
          return false;
        }
      }
      for (OrderSequenceCleanupApproval approval : sequenceCleanupApprovals) {
        if (!approval.test(seq)) {
          return false;
        }
      }
      return true;
    }
  }

  /**
   * Checks whether a peripheral job may be removed.
   */
  private class PeripheralJobApproval
      implements Predicate<PeripheralJob> {

    /**
     * Threshold for when a peripheral job can be removed if it was created before this.
     */
    private final Instant creationTimeThreshold;

    PeripheralJobApproval(Instant creationTimeThreshold) {
      this.creationTimeThreshold = creationTimeThreshold;
    }

    @Override
    public boolean test(PeripheralJob job) {
      if (!job.getState().isFinalState()) {
        return false;
      }
      if (job.getRelatedTransportOrder() != null) {
        // Peripheral jobs related to a transport order are removed when the related transport order
        // is removed.
        return false;
      }
      if (job.getCreationTime().isAfter(creationTimeThreshold)) {
        return false;
      }
      for (PeripheralJobCleanupApproval approval : peripheralJobCleanupApprovals) {
        if (!approval.test(job)) {
          return false;
        }
      }
      return true;
    }
  }
}
