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
import org.opentcs.access.to.order.OrderSequenceCreationTO;
import org.opentcs.access.to.order.TransportOrderCreationTO;
import org.opentcs.components.kernel.services.InternalTransportOrderService;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.components.kernel.services.TransportOrderService;
import org.opentcs.customizations.kernel.GlobalSyncObject;
import org.opentcs.data.ObjectExistsException;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.kernel.workingset.PlantModelManager;
import org.opentcs.kernel.workingset.TCSObjectRepository;
import org.opentcs.kernel.workingset.TransportOrderPoolManager;

/**
 * This class is the standard implementation of the {@link TransportOrderService} interface.
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
  private final TCSObjectRepository globalObjectPool;
  /**
   * The order pool manager.
   */
  private final TransportOrderPoolManager orderPoolManager;
  /**
   * The plant model manager.
   */
  private final PlantModelManager plantModelManager;

  /**
   * Creates a new instance.
   *
   * @param objectService The tcs obejct service.
   * @param globalSyncObject The kernel threads' global synchronization object.
   * @param globalObjectPool The object pool to be used.
   * @param orderPoolManager The order pool manager to be used.
   * @param plantModelManager The plant model manager to be used.
   */
  @Inject
  public StandardTransportOrderService(TCSObjectService objectService,
                                       @GlobalSyncObject Object globalSyncObject,
                                       TCSObjectRepository globalObjectPool,
                                       TransportOrderPoolManager orderPoolManager,
                                       PlantModelManager plantModelManager) {
    super(objectService);
    this.globalSyncObject = requireNonNull(globalSyncObject, "globalSyncObject");
    this.globalObjectPool = requireNonNull(globalObjectPool, "globalObjectPool");
    this.orderPoolManager = requireNonNull(orderPoolManager, "orderPoolManager");
    this.plantModelManager = requireNonNull(plantModelManager, "plantModelManager");
  }

  @Override
  public void markOrderSequenceFinished(TCSObjectReference<OrderSequence> ref)
      throws ObjectUnknownException {
    requireNonNull(ref, "ref");

    synchronized (globalSyncObject) {
      OrderSequence seq = globalObjectPool.getObject(OrderSequence.class, ref);
      // Make sure we don't execute this if the sequence is already marked as finished, as that
      // would make it possible to trigger disposition of a vehicle at any given moment.
      if (seq.isFinished()) {
        return;
      }

      orderPoolManager.setOrderSequenceFinished(ref);
      // If the sequence was being processed by a vehicle, clear its back reference to the sequence
      // to make it available again and dispatch it.
      if (seq.getProcessingVehicle() != null) {
        Vehicle vehicle = globalObjectPool.getObject(Vehicle.class,
                                                     seq.getProcessingVehicle());
        plantModelManager.setVehicleOrderSequence(vehicle.getReference(), null);
      }
    }
  }

  @Override
  public void updateOrderSequenceFinishedIndex(TCSObjectReference<OrderSequence> ref, int index)
      throws ObjectUnknownException {
    requireNonNull(ref, "ref");

    synchronized (globalSyncObject) {
      orderPoolManager.setOrderSequenceFinishedIndex(ref, index);
    }
  }

  @Override
  public void updateOrderSequenceProcessingVehicle(TCSObjectReference<OrderSequence> seqRef,
                                                   TCSObjectReference<Vehicle> vehicleRef)
      throws ObjectUnknownException {
    requireNonNull(seqRef, "seqRef");

    synchronized (globalSyncObject) {
      orderPoolManager.setOrderSequenceProcessingVehicle(seqRef, vehicleRef);
    }
  }

  @Override
  public void updateTransportOrderProcessingVehicle(TCSObjectReference<TransportOrder> orderRef,
                                                    TCSObjectReference<Vehicle> vehicleRef,
                                                    List<DriveOrder> driveOrders)
      throws ObjectUnknownException, IllegalArgumentException {
    requireNonNull(orderRef, "orderRef");
    requireNonNull(driveOrders, "driveOrders");

    synchronized (globalSyncObject) {
      orderPoolManager.setTransportOrderProcessingVehicle(orderRef, vehicleRef, driveOrders);
    }
  }

  @Override
  public void updateTransportOrderDriveOrders(TCSObjectReference<TransportOrder> ref,
                                              List<DriveOrder> driveOrders)
      throws ObjectUnknownException {
    requireNonNull(ref, "ref");
    requireNonNull(driveOrders, "driveOrders");

    synchronized (globalSyncObject) {
      orderPoolManager.setTransportOrderDriveOrders(ref, driveOrders);
    }
  }

  @Override
  public void updateTransportOrderNextDriveOrder(TCSObjectReference<TransportOrder> ref)
      throws ObjectUnknownException {
    requireNonNull(ref, "ref");

    synchronized (globalSyncObject) {
      orderPoolManager.setTransportOrderNextDriveOrder(ref);
    }
  }

  @Override
  public void updateTransportOrderCurrentRouteStepIndex(TCSObjectReference<TransportOrder> ref,
                                                        int index)
      throws ObjectUnknownException {
    requireNonNull(ref, "ref");

    synchronized (globalSyncObject) {
      orderPoolManager.setTransportOrderCurrentRouteStepIndex(ref, index);
    }
  }

  @Override
  public void updateTransportOrderState(TCSObjectReference<TransportOrder> ref,
                                        TransportOrder.State state)
      throws ObjectUnknownException {
    requireNonNull(ref, "ref");
    requireNonNull(state, "state");

    synchronized (globalSyncObject) {
      orderPoolManager.setTransportOrderState(ref, state);
    }
  }

  @Override
  public OrderSequence createOrderSequence(OrderSequenceCreationTO to) {
    requireNonNull(to, "to");

    synchronized (globalSyncObject) {
      return orderPoolManager.createOrderSequence(to);
    }
  }

  @Override
  public TransportOrder createTransportOrder(TransportOrderCreationTO to)
      throws ObjectUnknownException, ObjectExistsException {
    requireNonNull(to, "to");

    synchronized (globalSyncObject) {
      return orderPoolManager.createTransportOrder(to);
    }
  }

  @Override
  public void markOrderSequenceComplete(TCSObjectReference<OrderSequence> ref)
      throws ObjectUnknownException {
    requireNonNull(ref, "ref");

    synchronized (globalSyncObject) {
      OrderSequence seq = globalObjectPool.getObject(OrderSequence.class, ref);
      // Make sure we don't execute this if the sequence is already marked as finished, as that
      // would make it possible to trigger disposition of a vehicle at any given moment.
      if (seq.isComplete()) {
        return;
      }
      orderPoolManager.setOrderSequenceComplete(ref);
      // If there aren't any transport orders left to be processed as part of the sequence, mark
      // it as finished, too.
      if (seq.getNextUnfinishedOrder() == null) {
        orderPoolManager.setOrderSequenceFinished(ref);
        // If the sequence was being processed by a vehicle, clear its back reference to the
        // sequence to make it available again and dispatch it.
        if (seq.getProcessingVehicle() != null) {
          Vehicle vehicle = globalObjectPool.getObject(Vehicle.class,
                                                       seq.getProcessingVehicle());
          plantModelManager.setVehicleOrderSequence(vehicle.getReference(), null);
        }
      }
    }
  }

  @Override
  public void updateTransportOrderIntendedVehicle(TCSObjectReference<TransportOrder> orderRef,
                                                  TCSObjectReference<Vehicle> vehicleRef)
      throws ObjectUnknownException, IllegalArgumentException {
    requireNonNull(orderRef, "orderRef");

    synchronized (globalSyncObject) {
      orderPoolManager.setTransportOrderIntendedVehicle(orderRef, vehicleRef);
    }
  }

}
