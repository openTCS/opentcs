/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.workingset;

import java.util.Objects;
import static java.util.Objects.requireNonNull;
import java.util.function.Predicate;
import javax.inject.Inject;
import org.opentcs.customizations.kernel.GlobalSyncObject;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.data.peripherals.PeripheralJob;
import org.opentcs.kernel.OrderPoolConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A task that periodically removes orders, order sequences and peripheral jobs in a final state.
 */
public class WorkingSetCleanupTask
    implements Runnable {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(WorkingSetCleanupTask.class);
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
   * This class's configuration.
   */
  private final OrderPoolConfiguration configuration;
  /**
   * Checks whether an order sequence may be removed.
   */
  private final CompositeOrderSequenceCleanupApproval compositeOrderSequenceCleanupApproval;
  /**
   * Checks whether a transport order may be removed.
   */
  private final CompositeTransportOrderCleanupApproval compositeTransportOrderCleanupApproval;
  /**
   * Checks whether a peripheral job may be removed.
   */
  private final CompositePeripheralJobCleanupApproval compositePeripheralJobCleanupApproval;
  /**
   * Keeps track of the time used to determine whether a working set item should be removed
   * (according to its creation time).
   */
  private final CreationTimeThreshold creationTimeThreshold;

  /**
   * Creates a new instance.
   *
   * @param globalSyncObject The kernel threads' global synchronization object.
   * @param orderPoolManager The order pool manager to be used.
   * @param peripheralJobPoolManager The peripheral job pool manager to be used.
   * @param compositeOrderSequenceCleanupApproval Checks whether an order sequence may be removed.
   * @param compositeTransportOrderCleanupApproval Checks whether a transport order may be removed.
   * @param compositePeripheralJobCleanupApproval Checks whether a peripheral job may be removed.
   * @param creationTimeThreshold Keeps track of the time used to determine whether a working set
   * item should be removed (according to its creation time).
   * @param configuration This class's configuration.
   */
  @Inject
  public WorkingSetCleanupTask(
      @GlobalSyncObject Object globalSyncObject,
      TransportOrderPoolManager orderPoolManager,
      PeripheralJobPoolManager peripheralJobPoolManager,
      OrderPoolConfiguration configuration,
      CompositeOrderSequenceCleanupApproval compositeOrderSequenceCleanupApproval,
      CompositeTransportOrderCleanupApproval compositeTransportOrderCleanupApproval,
      CompositePeripheralJobCleanupApproval compositePeripheralJobCleanupApproval,
      CreationTimeThreshold creationTimeThreshold) {
    this.globalSyncObject = requireNonNull(globalSyncObject, "globalSyncObject");
    this.orderPoolManager = requireNonNull(orderPoolManager, "orderPoolManager");
    this.peripheralJobPoolManager = requireNonNull(peripheralJobPoolManager,
                                                   "peripheralJobPoolManager");
    this.compositeOrderSequenceCleanupApproval
        = requireNonNull(compositeOrderSequenceCleanupApproval);
    this.compositeTransportOrderCleanupApproval
        = requireNonNull(compositeTransportOrderCleanupApproval);
    this.compositePeripheralJobCleanupApproval
        = requireNonNull(compositePeripheralJobCleanupApproval);
    this.creationTimeThreshold = requireNonNull(creationTimeThreshold, "creationTimeThreshold");
    this.configuration = requireNonNull(configuration, "configuration");
  }

  public long getSweepInterval() {
    return configuration.sweepInterval();
  }

  @Override
  public void run() {
    synchronized (globalSyncObject) {
      LOG.debug("Sweeping working set...");

      // Update the creation time threshold for this cleanup run.
      creationTimeThreshold.updateCurrentThreshold(configuration.sweepAge());

      // Remove all peripheral jobs in a final state that do not belong to a transport order and
      // that are older than the threshold.
      Predicate<PeripheralJob> noRelatedTransportOrder = job
          -> job.getRelatedTransportOrder() == null;
      for (PeripheralJob peripheralJob
               : peripheralJobPoolManager.getObjectRepo().getObjects(
              PeripheralJob.class,
              noRelatedTransportOrder.and(compositePeripheralJobCleanupApproval)
          )) {
        peripheralJobPoolManager.removePeripheralJob(peripheralJob.getReference());
      }

      // Remove all transport orders in a final state that do NOT belong to a sequence and that are
      // older than the threshold, including their related peripheral jobs.
      Predicate<TransportOrder> noWrappingSequence = order -> order.getWrappingSequence() == null;
      for (TransportOrder transportOrder
               : orderPoolManager.getObjectRepo().getObjects(
              TransportOrder.class,
              noWrappingSequence.and(compositeTransportOrderCleanupApproval)
          )) {
        removeRelatedPeripheralJobs(transportOrder.getReference());
        orderPoolManager.removeTransportOrder(transportOrder.getReference());
      }

      // Remove all order sequences that have been finished, including their transport orders and
      // the transport orders' related peripheral jobs.
      for (OrderSequence orderSequence
               : orderPoolManager.getObjectRepo().getObjects(
              OrderSequence.class,
              compositeOrderSequenceCleanupApproval
          )) {
        for (TCSObjectReference<TransportOrder> transportOrderRef : orderSequence.getOrders()) {
          removeRelatedPeripheralJobs(transportOrderRef);
        }
        orderPoolManager.removeFinishedOrderSequenceAndOrders(orderSequence.getReference());
      }
    }
  }

  private void removeRelatedPeripheralJobs(TCSObjectReference<TransportOrder> transportOrderRef) {
    for (PeripheralJob peripheralJob
             : peripheralJobPoolManager.getObjectRepo().getObjects(
            PeripheralJob.class,
            job -> Objects.equals(job.getRelatedTransportOrder(), transportOrderRef)
        )) {
      peripheralJobPoolManager.removePeripheralJob(peripheralJob.getReference());
    }
  }
}
