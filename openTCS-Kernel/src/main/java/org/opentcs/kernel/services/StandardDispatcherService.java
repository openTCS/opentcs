/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.services;

import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.opentcs.components.kernel.Dispatcher;
import org.opentcs.components.kernel.services.DispatcherService;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.kernel.GlobalKernelSync;
import org.opentcs.kernel.workingset.TCSObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is the standard implementation of the {@link DispatcherService} interface.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class StandardDispatcherService
    implements DispatcherService {

  /**
   * This class' logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(StandardDispatcherService.class);
  /**
   * A global object to be used for synchronization within the kernel.
   */
  private final Object globalSyncObject;
  /**
   * The container of all course model and transport order objects.
   */
  private final TCSObjectPool globalObjectPool;
  /**
   * The dispatcher.
   */
  private final Dispatcher dispatcher;

  /**
   * Creates a new instance.
   *
   * @param globalSyncObject The kernel threads' global synchronization object.
   * @param globalObjectPool The object pool to be used.
   * @param dispatcher The dispatcher.
   */
  @Inject
  public StandardDispatcherService(@GlobalKernelSync Object globalSyncObject,
                                   TCSObjectPool globalObjectPool,
                                   Dispatcher dispatcher) {
    this.globalSyncObject = requireNonNull(globalSyncObject, "globalSyncObject");
    this.globalObjectPool = requireNonNull(globalObjectPool, "globalObjectPool");
    this.dispatcher = requireNonNull(dispatcher, "dispatcher");
  }

  @Override
  public void dispatch() {
    synchronized (globalSyncObject) {
      dispatcher.dispatch();
    }
  }

  @Override
  @Deprecated
  public void releaseVehicle(TCSObjectReference<Vehicle> ref)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      dispatcher.releaseVehicle(globalObjectPool.getObject(Vehicle.class, ref));
    }
  }

  @Override
  @Deprecated
  public void withdrawByVehicle(TCSObjectReference<Vehicle> ref,
                                boolean immediateAbort,
                                boolean disableVehicle)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      dispatcher.withdrawOrder(globalObjectPool.getObject(Vehicle.class, ref),
                               immediateAbort,
                               disableVehicle);
    }
  }

  @Override
  @Deprecated
  public void withdrawByTransportOrder(TCSObjectReference<TransportOrder> ref,
                                       boolean immediateAbort,
                                       boolean disableVehicle)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      dispatcher.withdrawOrder(globalObjectPool.getObject(TransportOrder.class, ref),
                               immediateAbort,
                               disableVehicle);
    }
  }

  @Override
  public void withdrawByVehicle(TCSObjectReference<Vehicle> ref, boolean immediateAbort)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      dispatcher.withdrawOrder(globalObjectPool.getObject(Vehicle.class, ref), immediateAbort);
    }
  }

  @Override
  public void withdrawByTransportOrder(TCSObjectReference<TransportOrder> ref,
                                       boolean immediateAbort)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      dispatcher.withdrawOrder(globalObjectPool.getObject(TransportOrder.class, ref),
                               immediateAbort);
    }
  }
}
