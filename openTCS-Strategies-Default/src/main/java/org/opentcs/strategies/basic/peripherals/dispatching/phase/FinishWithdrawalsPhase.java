/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.peripherals.dispatching.phase;

import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import org.opentcs.components.kernel.services.InternalPeripheralJobService;
import org.opentcs.components.kernel.services.InternalPeripheralService;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.PeripheralInformation;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.data.peripherals.PeripheralJob;
import org.opentcs.drivers.peripherals.PeripheralControllerPool;
import org.opentcs.strategies.basic.peripherals.dispatching.PeripheralDispatcherPhase;

/**
 * Finishes withdrawals of peripheral jobs after their related transport order has failed.
 */
public class FinishWithdrawalsPhase
    implements PeripheralDispatcherPhase {

  /**
   * The object service.
   */
  private final TCSObjectService objectService;

  private final InternalPeripheralService peripheralService;

  private final InternalPeripheralJobService peripheralJobService;
  /**
   * The controller pool.
   */
  private final PeripheralControllerPool controllerPool;
  /**
   * Indicates whether this component is initialized.
   */
  private boolean initialized;

  @Inject
  public FinishWithdrawalsPhase(@Nonnull TCSObjectService objectService,
                                @Nonnull InternalPeripheralService peripheralService,
                                @Nonnull InternalPeripheralJobService peripheralJobService,
                                @Nonnull PeripheralControllerPool controllerPool) {
    this.objectService = requireNonNull(objectService, "objectService");
    this.peripheralService = requireNonNull(peripheralService, "peripheralService");
    this.peripheralJobService = requireNonNull(peripheralJobService, "peripheralJobService");
    this.controllerPool = requireNonNull(controllerPool, "controllerPool");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }
    initialized = true;
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
    initialized = false;
  }

  @Override
  public void run() {
    // Get all non-final peripheral jobs that are related to a transport order, and if their
    // transport order is marked as FAILED, abort them.
    Set<PeripheralJob> jobs
        = objectService.fetchObjects(PeripheralJob.class,
                                     this::isRelatedToTransportOrderAndNotInFinalState);

    Set<TCSObjectReference<TransportOrder>> failedOrderRefs
        = jobs.stream()
            .map(PeripheralJob::getRelatedTransportOrder)
            .distinct()
            .map(orderRef -> objectService.fetchObject(TransportOrder.class, orderRef))
            .filter(order -> order.hasState(TransportOrder.State.FAILED))
            .map(TransportOrder::getReference)
            .collect(Collectors.toSet());

    jobs.stream()
        .filter(job -> failedOrderRefs.contains(job.getRelatedTransportOrder()))
        .forEach(job -> abortJob(job));
  }

  private boolean isRelatedToTransportOrderAndNotInFinalState(PeripheralJob job) {
    return job.getRelatedTransportOrder() != null && !job.getState().isFinalState();
  }

  private void abortJob(PeripheralJob job) {
    if (job.getState() == PeripheralJob.State.BEING_PROCESSED) {
      controllerPool.getPeripheralController(job.getPeripheralOperation().getLocation()).abortJob();
      peripheralService.updatePeripheralProcState(job.getPeripheralOperation().getLocation(),
                                                  PeripheralInformation.ProcState.IDLE);
      peripheralService.updatePeripheralJob(job.getPeripheralOperation().getLocation(), null);
    }

    peripheralJobService.updatePeripheralJobState(job.getReference(), PeripheralJob.State.FAILED);
  }
}
