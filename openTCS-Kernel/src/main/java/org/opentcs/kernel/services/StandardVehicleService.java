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
import java.util.Set;
import javax.inject.Inject;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.components.kernel.services.InternalVehicleService;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.components.kernel.services.VehicleService;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.drivers.vehicle.AdapterCommand;
import org.opentcs.drivers.vehicle.LoadHandlingDevice;
import org.opentcs.drivers.vehicle.VehicleCommAdapterDescription;
import org.opentcs.drivers.vehicle.management.AttachmentInformation;
import org.opentcs.drivers.vehicle.management.VehicleProcessModelTO;
import org.opentcs.kernel.GlobalKernelSync;
import org.opentcs.kernel.extensions.controlcenter.vehicles.AttachmentManager;
import org.opentcs.kernel.extensions.controlcenter.vehicles.VehicleEntry;
import org.opentcs.kernel.extensions.controlcenter.vehicles.VehicleEntryPool;
import org.opentcs.kernel.vehicles.LocalVehicleControllerPool;
import org.opentcs.kernel.vehicles.VehicleCommAdapterRegistry;
import org.opentcs.kernel.workingset.Model;
import org.opentcs.kernel.workingset.TCSObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is the standard implementation of the {@link VehicleService} interface.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class StandardVehicleService
    extends AbstractTCSObjectService
    implements InternalVehicleService {

  /**
   * This class' logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(StandardVehicleService.class);
  /**
   * A global object to be used for synchronization within the kernel.
   */
  private final Object globalSyncObject;
  /**
   * The container of all course model and transport order objects.
   */
  private final TCSObjectPool globalObjectPool;
  /**
   * A pool of vehicle controllers.
   */
  private final LocalVehicleControllerPool vehicleControllerPool;
  /**
   * A pool of vehicle entries.
   */
  private final VehicleEntryPool vehicleEntryPool;
  /**
   * The attachment manager.
   */
  private final AttachmentManager attachmentManager;
  /**
   * The registry for all communication adapters.
   */
  private final VehicleCommAdapterRegistry commAdapterRegistry;
  /**
   * The model facade to the object pool.
   */
  private final Model model;

  /**
   * Creates a new instance.
   *
   * @param objectService The tcs object service.
   * @param globalSyncObject The kernel threads' global synchronization object.
   * @param globalObjectPool The object pool to be used.
   * @param vehicleControllerPool The controller pool to be used.
   * @param vehicleEntryPool The pool of vehicle entries to be used.
   * @param attachmentManager The attachment manager.
   * @param commAdapterRegistry The registry for all communication adapters.
   * @param model The model to be used.
   */
  @Inject
  public StandardVehicleService(TCSObjectService objectService,
                                @GlobalKernelSync Object globalSyncObject,
                                TCSObjectPool globalObjectPool,
                                LocalVehicleControllerPool vehicleControllerPool,
                                VehicleEntryPool vehicleEntryPool,
                                AttachmentManager attachmentManager,
                                VehicleCommAdapterRegistry commAdapterRegistry,
                                Model model) {
    super(objectService);
    this.globalSyncObject = requireNonNull(globalSyncObject, "globalSyncObject");
    this.globalObjectPool = requireNonNull(globalObjectPool, "globalObjectPool");
    this.vehicleControllerPool = requireNonNull(vehicleControllerPool, "vehicleControllerPool");
    this.vehicleEntryPool = requireNonNull(vehicleEntryPool, "vehicleEntryPool");
    this.attachmentManager = requireNonNull(attachmentManager, "attachmentManager");
    this.commAdapterRegistry = requireNonNull(commAdapterRegistry, "commAdapterRegistry");
    this.model = requireNonNull(model, "model");
  }

  @Override
  public void updateVehicleEnergyLevel(TCSObjectReference<Vehicle> ref, int energyLevel)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      model.setVehicleEnergyLevel(ref, energyLevel);
    }
  }

  @Override
  public void updateVehicleLoadHandlingDevices(TCSObjectReference<Vehicle> ref,
                                               List<LoadHandlingDevice> devices)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      model.setVehicleLoadHandlingDevices(ref, devices);
    }
  }

  @Override
  public void updateVehicleNextPosition(TCSObjectReference<Vehicle> vehicleRef,
                                        TCSObjectReference<Point> pointRef)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      model.setVehicleNextPosition(vehicleRef, pointRef);
    }
  }

  @Override
  public void updateVehicleOrderSequence(TCSObjectReference<Vehicle> vehicleRef,
                                         TCSObjectReference<OrderSequence> sequenceRef)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      model.setVehicleOrderSequence(vehicleRef, sequenceRef);
    }
  }

  @Override
  public void updateVehicleOrientationAngle(TCSObjectReference<Vehicle> ref, double angle)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      model.setVehicleOrientationAngle(ref, angle);
    }
  }

  @Override
  public void updateVehiclePosition(TCSObjectReference<Vehicle> vehicleRef,
                                    TCSObjectReference<Point> pointRef)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      LOG.debug("Vehicle {} has reached point {}.", vehicleRef, pointRef);
      model.setVehiclePosition(vehicleRef, pointRef);
    }
  }

  @Override
  public void updateVehiclePrecisePosition(TCSObjectReference<Vehicle> ref, Triple position)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      model.setVehiclePrecisePosition(ref, position);
    }
  }

  @Override
  public void updateVehicleProcState(TCSObjectReference<Vehicle> ref, Vehicle.ProcState state)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      LOG.debug("Updating procState of vehicle {} to {}...", ref.getName(), state);
      model.setVehicleProcState(ref, state);
    }
  }

  @Override
  public void updateVehicleRechargeOperation(TCSObjectReference<Vehicle> ref,
                                             String rechargeOperation)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      model.setVehicleRechargeOperation(ref, rechargeOperation);
    }
  }

  @Override
  public void updateVehicleRouteProgressIndex(TCSObjectReference<Vehicle> ref, int index)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      model.setVehicleRouteProgressIndex(ref, index);
    }
  }

  @Override
  public void updateVehicleState(TCSObjectReference<Vehicle> ref, Vehicle.State state)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      model.setVehicleState(ref, state);
    }
  }

  @Override
  public void updateVehicleTransportOrder(TCSObjectReference<Vehicle> vehicleRef,
                                          TCSObjectReference<TransportOrder> orderRef)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      model.setVehicleTransportOrder(vehicleRef, orderRef);
    }
  }

  @Override
  public void attachCommAdapter(TCSObjectReference<Vehicle> ref,
                                VehicleCommAdapterDescription description)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      attachmentManager.attachAdapterToVehicle(ref.getName(),
                                               commAdapterRegistry.findFactoryFor(description));
    }
  }

  @Override
  public void disableCommAdapter(TCSObjectReference<Vehicle> ref)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      VehicleEntry entry = vehicleEntryPool.getEntryFor(ref.getName());
      if (entry == null) {
        throw new IllegalArgumentException("No vehicle entry found for" + ref.getName());
      }

      entry.getCommAdapter().disable();
    }
  }

  @Override
  public void enableCommAdapter(TCSObjectReference<Vehicle> ref)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      VehicleEntry entry = vehicleEntryPool.getEntryFor(ref.getName());
      if (entry == null) {
        throw new IllegalArgumentException("No vehicle entry found for " + ref.getName());
      }

      entry.getCommAdapter().enable();
    }
  }

  @Override
  public AttachmentInformation fetchAttachmentInformation(TCSObjectReference<Vehicle> ref)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      return attachmentManager.getAttachmentInformation(ref.getName());
    }
  }

  @Override
  public VehicleProcessModelTO fetchProcessModel(TCSObjectReference<Vehicle> ref)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      VehicleEntry entry = vehicleEntryPool.getEntryFor(ref.getName());
      if (entry == null) {
        throw new IllegalArgumentException("No vehicle entry found for " + ref.getName());
      }

      return entry.getCommAdapter().createTransferableProcessModel();
    }
  }

  @Override
  public void sendCommAdapterCommand(TCSObjectReference<Vehicle> ref, AdapterCommand command)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      vehicleControllerPool
          .getVehicleController(ref.getName())
          .sendCommAdapterCommand(command);
    }
  }

  @Override
  public void sendCommAdapterMessage(TCSObjectReference<Vehicle> ref, Object message)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      vehicleControllerPool
          .getVehicleController(ref.getName())
          .sendCommAdapterMessage(message);
    }
  }

  @Override
  public void updateVehicleIntegrationLevel(TCSObjectReference<Vehicle> ref,
                                            Vehicle.IntegrationLevel integrationLevel)
      throws ObjectUnknownException, KernelRuntimeException {
    synchronized (globalSyncObject) {
      Vehicle vehicle = fetchObject(Vehicle.class, ref);

      if (vehicle.isProcessingOrder()
          && (integrationLevel == Vehicle.IntegrationLevel.TO_BE_IGNORED
              || integrationLevel == Vehicle.IntegrationLevel.TO_BE_NOTICED)) {
        throw new IllegalArgumentException(
            String.format("%s: Cannot change integration level to %s while processing orders.",
                          vehicle.getName(),
                          integrationLevel.name())
        );
      }

      model.setVehicleIntegrationLevel(ref, integrationLevel);
    }
  }

  @Override
  public void updateVehicleProcessableCategories(TCSObjectReference<Vehicle> ref,
                                                 Set<String> processableCategories)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      model.setVehicleProcessableCategories(ref, processableCategories);
    }
  }
}
