/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.services;

import java.util.List;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.inject.Provider;
import org.opentcs.access.to.order.OrderSequenceCreationTO;
import org.opentcs.access.to.order.TransportOrderCreationTO;
import org.opentcs.components.kernel.Dispatcher;
import org.opentcs.components.kernel.services.InternalTransportOrderService;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.components.kernel.services.TransportOrderService;
import org.opentcs.data.ObjectExistsException;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.Rejection;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.kernel.GlobalKernelSync;
import org.opentcs.kernel.workingset.Model;
import org.opentcs.kernel.workingset.TCSObjectPool;
import org.opentcs.kernel.workingset.TransportOrderPool;

/**
 * This class is the standard implementation of the {@link TransportOrderService} interface.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class StandardTransportOrderService
    extends AbstractTCSObjectService
    implements InternalTransportOrderService {

  /**
   * A global object to be used for synchronization within the kernel.
   */
  private final Object globalSyncObject;
  /**
   * The container of all course model and transport order objects.
   */
  private final TCSObjectPool globalObjectPool;
  /**
   * The order facade to the object pool.
   */
  private final TransportOrderPool orderPool;
  /**
   * The model facade to the object pool.
   */
  private final Model model;
  /**
   * Provides the dispatcher instance.
   */
  private final Provider<Dispatcher> dispatcherProvider;

  /**
   * Creates a new instance.
   *
   * @param objectService The tcs obejct service.
   * @param globalSyncObject The kernel threads' global synchronization object.
   * @param globalObjectPool The object pool to be used.
   * @param orderPool The oder pool to be used.
   * @param model The model to be used.
   * @param dispatcherProvider Provides the dispatcher instance.
   */
  @Inject
  public StandardTransportOrderService(TCSObjectService objectService,
                                       @GlobalKernelSync Object globalSyncObject,
                                       TCSObjectPool globalObjectPool,
                                       TransportOrderPool orderPool,
                                       Model model,
                                       Provider<Dispatcher> dispatcherProvider) {
    super(objectService);
    this.globalSyncObject = requireNonNull(globalSyncObject, "globalSyncObject");
    this.globalObjectPool = requireNonNull(globalObjectPool, "globalObjectPool");
    this.orderPool = requireNonNull(orderPool, "orderPool");
    this.model = requireNonNull(model, "model");
    this.dispatcherProvider = requireNonNull(dispatcherProvider, "dispatcherProvider");
  }

  @Override
  public void registerTransportOrderRejection(TCSObjectReference<TransportOrder> ref,
                                              Rejection rejection)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      orderPool.addTransportOrderRejection(ref, rejection);
    }
  }

  @Override
  public void markOrderSequenceFinished(TCSObjectReference<OrderSequence> ref)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      OrderSequence seq = globalObjectPool.getObject(OrderSequence.class, ref);
      // Make sure we don't execute this if the sequence is already marked as finished, as that 
      // would make it possible to trigger disposition of a vehicle at any given moment.
      if (seq.isFinished()) {
        return;
      }

      orderPool.setOrderSequenceFinished(ref);
      // If the sequence was being processed by a vehicle, clear its back reference to the sequence 
      // to make it available again and dispatch it.
      if (seq.getProcessingVehicle() != null) {
        Vehicle vehicle = globalObjectPool.getObject(Vehicle.class,
                                                     seq.getProcessingVehicle());
        model.setVehicleOrderSequence(vehicle.getReference(), null);
      }
    }
  }

  @Override
  public void updateOrderSequenceFinishedIndex(TCSObjectReference<OrderSequence> ref, int index)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      orderPool.setOrderSequenceFinishedIndex(ref, index);
    }
  }

  @Override
  public void updateOrderSequenceProcessingVehicle(TCSObjectReference<OrderSequence> seqRef,
                                                   TCSObjectReference<Vehicle> vehicleRef)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      orderPool.setOrderSequenceProcessingVehicle(seqRef, vehicleRef);
    }
  }

  @Override
  public void updateTransportOrderProcessingVehicle(TCSObjectReference<TransportOrder> orderRef,
                                                    TCSObjectReference<Vehicle> vehicleRef,
                                                    List<DriveOrder> driveOrders)
      throws ObjectUnknownException, IllegalArgumentException {
    synchronized (globalSyncObject) {
      orderPool.setTransportOrderProcessingVehicle(orderRef, vehicleRef, driveOrders);
    }
  }

  @Override
  public void updateTransportOrderDriveOrders(TCSObjectReference<TransportOrder> ref,
                                              List<DriveOrder> driveOrders)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      orderPool.setTransportOrderDriveOrders(ref, driveOrders);
    }
  }

  @Override
  public void updateTransportOrderNextDriveOrder(TCSObjectReference<TransportOrder> ref)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      orderPool.setTransportOrderNextDriveOrder(ref);
    }
  }

  @Override
  public void updateTransportOrderState(TCSObjectReference<TransportOrder> ref,
                                        TransportOrder.State state)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      orderPool.setTransportOrderState(ref, state);
    }
  }

  @Override
  @SuppressWarnings("deprecation")
  public OrderSequence createOrderSequence(OrderSequenceCreationTO to) {
    synchronized (globalSyncObject) {
      return orderPool.createOrderSequence(to).clone();
    }
  }

  @Override
  @SuppressWarnings("deprecation")
  public TransportOrder createTransportOrder(TransportOrderCreationTO to)
      throws ObjectUnknownException, ObjectExistsException {
    synchronized (globalSyncObject) {
      return orderPool.createTransportOrder(to).clone();
    }
  }

  @Override
  public void markOrderSequenceComplete(TCSObjectReference<OrderSequence> ref)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      OrderSequence seq = globalObjectPool.getObject(OrderSequence.class, ref);
      // Make sure we don't execute this if the sequence is already marked as finished, as that 
      // would make it possible to trigger disposition of a vehicle at any given moment.
      if (seq.isComplete()) {
        return;
      }
      orderPool.setOrderSequenceComplete(ref);
      // If there aren't any transport orders left to be processed as part of the sequence, mark 
      // it as finished, too.
      if (seq.getNextUnfinishedOrder() == null) {
        orderPool.setOrderSequenceFinished(ref);
        // If the sequence was being processed by a vehicle, clear its back reference to the 
        // sequence to make it available again and dispatch it.
        if (seq.getProcessingVehicle() != null) {
          Vehicle vehicle = globalObjectPool.getObject(Vehicle.class,
                                                       seq.getProcessingVehicle());
          model.setVehicleOrderSequence(vehicle.getReference(), null);
        }
      }
    }
  }

}
