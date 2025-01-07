// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.services;

import static java.util.Objects.requireNonNull;

import jakarta.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.components.kernel.services.InternalVehicleService;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.components.kernel.services.VehicleService;
import org.opentcs.customizations.kernel.GlobalSyncObject;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.AcceptableOrderType;
import org.opentcs.data.model.BoundingBox;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Pose;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.model.Vehicle.EnergyLevelThresholdSet;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.drivers.vehicle.AdapterCommand;
import org.opentcs.drivers.vehicle.LoadHandlingDevice;
import org.opentcs.drivers.vehicle.VehicleCommAdapterDescription;
import org.opentcs.drivers.vehicle.management.VehicleAttachmentInformation;
import org.opentcs.drivers.vehicle.management.VehicleProcessModelTO;
import org.opentcs.kernel.extensions.controlcenter.vehicles.AttachmentManager;
import org.opentcs.kernel.extensions.controlcenter.vehicles.VehicleEntry;
import org.opentcs.kernel.extensions.controlcenter.vehicles.VehicleEntryPool;
import org.opentcs.kernel.vehicles.LocalVehicleControllerPool;
import org.opentcs.kernel.vehicles.VehicleCommAdapterRegistry;
import org.opentcs.kernel.workingset.PlantModelManager;
import org.opentcs.util.annotations.ScheduledApiChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is the standard implementation of the {@link VehicleService} interface.
 */
public class StandardVehicleService
    extends
      AbstractTCSObjectService
    implements
      InternalVehicleService {

  /**
   * This class' logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(StandardVehicleService.class);
  /**
   * A global object to be used for synchronization within the kernel.
   */
  private final Object globalSyncObject;
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
   * The plant model manager.
   */
  private final PlantModelManager plantModelManager;

  /**
   * Creates a new instance.
   *
   * @param objectService The tcs object service.
   * @param globalSyncObject The kernel threads' global synchronization object.
   * @param vehicleControllerPool The controller pool to be used.
   * @param vehicleEntryPool The pool of vehicle entries to be used.
   * @param attachmentManager The attachment manager.
   * @param commAdapterRegistry The registry for all communication adapters.
   * @param plantModelManager The plant model manager to be used.
   */
  @Inject
  public StandardVehicleService(
      TCSObjectService objectService,
      @GlobalSyncObject
      Object globalSyncObject,
      LocalVehicleControllerPool vehicleControllerPool,
      VehicleEntryPool vehicleEntryPool,
      AttachmentManager attachmentManager,
      VehicleCommAdapterRegistry commAdapterRegistry,
      PlantModelManager plantModelManager
  ) {
    super(objectService);
    this.globalSyncObject = requireNonNull(globalSyncObject, "globalSyncObject");
    this.vehicleControllerPool = requireNonNull(vehicleControllerPool, "vehicleControllerPool");
    this.vehicleEntryPool = requireNonNull(vehicleEntryPool, "vehicleEntryPool");
    this.attachmentManager = requireNonNull(attachmentManager, "attachmentManager");
    this.commAdapterRegistry = requireNonNull(commAdapterRegistry, "commAdapterRegistry");
    this.plantModelManager = requireNonNull(plantModelManager, "plantModelManager");
  }

  @Override
  public void updateVehicleEnergyLevel(TCSObjectReference<Vehicle> ref, int energyLevel)
      throws ObjectUnknownException {
    requireNonNull(ref, "ref");

    synchronized (globalSyncObject) {
      plantModelManager.setVehicleEnergyLevel(ref, energyLevel);
    }
  }

  @Override
  public void updateVehicleLoadHandlingDevices(
      TCSObjectReference<Vehicle> ref,
      List<LoadHandlingDevice> devices
  )
      throws ObjectUnknownException {
    requireNonNull(ref, "ref");
    requireNonNull(devices, "devices");

    synchronized (globalSyncObject) {
      plantModelManager.setVehicleLoadHandlingDevices(ref, devices);
    }
  }

  @Override
  public void updateVehicleNextPosition(
      TCSObjectReference<Vehicle> vehicleRef,
      TCSObjectReference<Point> pointRef
  )
      throws ObjectUnknownException {
    requireNonNull(vehicleRef, "vehicleRef");

    synchronized (globalSyncObject) {
      plantModelManager.setVehicleNextPosition(vehicleRef, pointRef);
    }
  }

  @Override
  public void updateVehicleOrderSequence(
      TCSObjectReference<Vehicle> vehicleRef,
      TCSObjectReference<OrderSequence> sequenceRef
  )
      throws ObjectUnknownException {
    requireNonNull(vehicleRef, "vehicleRef");

    synchronized (globalSyncObject) {
      plantModelManager.setVehicleOrderSequence(vehicleRef, sequenceRef);
    }
  }

  @Deprecated
  @Override
  public void updateVehicleOrientationAngle(TCSObjectReference<Vehicle> ref, double angle)
      throws ObjectUnknownException {
    requireNonNull(ref, "ref");

    synchronized (globalSyncObject) {
      Vehicle previousState = plantModelManager.getObjectRepo().getObject(Vehicle.class, ref);
      plantModelManager.setVehiclePose(ref, previousState.getPose().withOrientationAngle(angle));
    }
  }

  @Override
  public void updateVehiclePosition(
      TCSObjectReference<Vehicle> vehicleRef,
      TCSObjectReference<Point> pointRef
  )
      throws ObjectUnknownException {
    requireNonNull(vehicleRef, "vehicleRef");

    synchronized (globalSyncObject) {
      LOG.debug("Vehicle {} has reached point {}.", vehicleRef, pointRef);
      plantModelManager.setVehiclePosition(vehicleRef, pointRef);
    }
  }

  @Deprecated
  @Override
  public void updateVehiclePrecisePosition(TCSObjectReference<Vehicle> ref, Triple position)
      throws ObjectUnknownException {
    requireNonNull(ref, "ref");

    synchronized (globalSyncObject) {
      Vehicle previousState = plantModelManager.getObjectRepo().getObject(Vehicle.class, ref);
      plantModelManager.setVehiclePose(ref, previousState.getPose().withPosition(position));
    }
  }

  @Override
  public void updateVehiclePose(TCSObjectReference<Vehicle> ref, Pose pose)
      throws ObjectUnknownException {
    requireNonNull(ref, "ref");
    requireNonNull(pose, "pose");

    synchronized (globalSyncObject) {
      plantModelManager.setVehiclePose(ref, pose);
    }
  }

  @Override
  public void updateVehicleProcState(TCSObjectReference<Vehicle> ref, Vehicle.ProcState state)
      throws ObjectUnknownException {
    requireNonNull(ref, "ref");
    requireNonNull(state, "state");

    synchronized (globalSyncObject) {
      LOG.debug("Updating procState of vehicle {} to {}...", ref.getName(), state);
      plantModelManager.setVehicleProcState(ref, state);
    }
  }

  @Override
  public void updateVehicleRechargeOperation(
      TCSObjectReference<Vehicle> ref,
      String rechargeOperation
  )
      throws ObjectUnknownException {
    requireNonNull(ref, "ref");
    requireNonNull(rechargeOperation, "rechargeOperation");

    synchronized (globalSyncObject) {
      plantModelManager.setVehicleRechargeOperation(ref, rechargeOperation);
    }
  }

  @Override
  public void updateVehicleClaimedResources(
      TCSObjectReference<Vehicle> ref,
      List<Set<TCSResourceReference<?>>> resources
  )
      throws ObjectUnknownException {
    requireNonNull(ref, "ref");
    requireNonNull(resources, "resources");

    synchronized (globalSyncObject) {
      plantModelManager.setVehicleClaimedResources(ref, resources);
    }
  }

  @Override
  public void updateVehicleAllocatedResources(
      TCSObjectReference<Vehicle> ref,
      List<Set<TCSResourceReference<?>>> resources
  )
      throws ObjectUnknownException {
    requireNonNull(ref, "ref");
    requireNonNull(resources, "resources");

    synchronized (globalSyncObject) {
      plantModelManager.setVehicleAllocatedResources(ref, resources);
    }
  }

  @Override
  public void updateVehicleState(TCSObjectReference<Vehicle> ref, Vehicle.State state)
      throws ObjectUnknownException {
    requireNonNull(ref, "ref");
    requireNonNull(state, "state");

    synchronized (globalSyncObject) {
      plantModelManager.setVehicleState(ref, state);
    }
  }

  @Override
  @Deprecated
  public void updateVehicleLength(TCSObjectReference<Vehicle> ref, int length)
      throws ObjectUnknownException {
    requireNonNull(ref, "ref");

    synchronized (globalSyncObject) {
      plantModelManager.setVehicleBoundingBox(
          ref,
          plantModelManager.getObjectRepo().getObject(Vehicle.class, ref)
              .getBoundingBox()
              .withLength(length)
      );
    }
  }

  @Override
  public void updateVehicleTransportOrder(
      TCSObjectReference<Vehicle> vehicleRef,
      TCSObjectReference<TransportOrder> orderRef
  )
      throws ObjectUnknownException {
    requireNonNull(vehicleRef, "vehicleRef");

    synchronized (globalSyncObject) {
      plantModelManager.setVehicleTransportOrder(vehicleRef, orderRef);
    }
  }

  @Override
  public void attachCommAdapter(
      TCSObjectReference<Vehicle> ref,
      VehicleCommAdapterDescription description
  )
      throws ObjectUnknownException {
    requireNonNull(ref, "ref");
    requireNonNull(description, "description");

    synchronized (globalSyncObject) {
      attachmentManager.attachAdapterToVehicle(
          ref.getName(),
          commAdapterRegistry.findFactoryFor(description)
      );
    }
  }

  @Override
  public void disableCommAdapter(TCSObjectReference<Vehicle> ref)
      throws ObjectUnknownException {
    requireNonNull(ref, "ref");

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
    requireNonNull(ref, "ref");

    synchronized (globalSyncObject) {
      VehicleEntry entry = vehicleEntryPool.getEntryFor(ref.getName());
      if (entry == null) {
        throw new IllegalArgumentException("No vehicle entry found for " + ref.getName());
      }

      entry.getCommAdapter().enable();
    }
  }

  @Override
  public VehicleAttachmentInformation fetchAttachmentInformation(TCSObjectReference<Vehicle> ref)
      throws ObjectUnknownException {
    requireNonNull(ref, "ref");

    synchronized (globalSyncObject) {
      return attachmentManager.getAttachmentInformation(ref.getName());
    }
  }

  @Override
  public VehicleProcessModelTO fetchProcessModel(TCSObjectReference<Vehicle> ref)
      throws ObjectUnknownException {
    requireNonNull(ref, "ref");

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
    requireNonNull(ref, "ref");
    requireNonNull(command, "command");

    synchronized (globalSyncObject) {
      vehicleControllerPool
          .getVehicleController(ref.getName())
          .sendCommAdapterCommand(command);
    }
  }

  @Override
  public void sendCommAdapterMessage(TCSObjectReference<Vehicle> ref, Object message)
      throws ObjectUnknownException {
    requireNonNull(ref, "ref");

    synchronized (globalSyncObject) {
      vehicleControllerPool
          .getVehicleController(ref.getName())
          .sendCommAdapterMessage(message);
    }
  }

  @Override
  public void updateVehicleIntegrationLevel(
      TCSObjectReference<Vehicle> ref,
      Vehicle.IntegrationLevel integrationLevel
  )
      throws ObjectUnknownException,
        KernelRuntimeException {
    requireNonNull(ref, "ref");
    requireNonNull(integrationLevel, "integrationLevel");

    synchronized (globalSyncObject) {
      Vehicle vehicle = fetchObject(Vehicle.class, ref);

      if (vehicle.isProcessingOrder()
          && (integrationLevel == Vehicle.IntegrationLevel.TO_BE_IGNORED
              || integrationLevel == Vehicle.IntegrationLevel.TO_BE_NOTICED)) {
        throw new IllegalArgumentException(
            String.format(
                "%s: Cannot change integration level to %s while processing orders.",
                vehicle.getName(),
                integrationLevel.name()
            )
        );
      }

      plantModelManager.setVehicleIntegrationLevel(ref, integrationLevel);
    }
  }

  @Override
  public void updateVehiclePaused(TCSObjectReference<Vehicle> ref, boolean paused)
      throws ObjectUnknownException,
        KernelRuntimeException {
    requireNonNull(ref, "ref");

    synchronized (globalSyncObject) {
      plantModelManager.setVehiclePaused(ref, paused);

      vehicleControllerPool.getVehicleController(ref.getName()).onVehiclePaused(paused);
    }
  }

  @Override
  public void updateVehicleEnergyLevelThresholdSet(
      TCSObjectReference<Vehicle> ref,
      EnergyLevelThresholdSet energyLevelThresholdSet
  )
      throws ObjectUnknownException {
    requireNonNull(ref, "ref");
    requireNonNull(energyLevelThresholdSet, "energyLevelThresholdSet");

    synchronized (globalSyncObject) {
      plantModelManager.setVehicleEnergyLevelThresholdSet(ref, energyLevelThresholdSet);
    }
  }

  @Override
  @Deprecated
  public void updateVehicleAllowedOrderTypes(
      TCSObjectReference<Vehicle> ref,
      Set<String> allowedOrderTypes
  )
      throws ObjectUnknownException {
    requireNonNull(ref, "ref");
    requireNonNull(allowedOrderTypes, "allowedOrderTypes");

    synchronized (globalSyncObject) {
      plantModelManager.setVehicleAcceptableOrderTypes(
          ref,
          allowedOrderTypes.stream()
              .map(orderType -> new AcceptableOrderType(orderType, 0))
              .collect(Collectors.toSet())
      );
    }
  }

  @Override
  public void updateVehicleAcceptableOrderTypes(
      TCSObjectReference<Vehicle> ref,
      Set<AcceptableOrderType> acceptableOrderTypes
  )
      throws ObjectUnknownException {
    requireNonNull(ref, "ref");
    requireNonNull(acceptableOrderTypes, "acceptableOrderTypes");

    synchronized (globalSyncObject) {
      plantModelManager.setVehicleAcceptableOrderTypes(ref, acceptableOrderTypes);
    }
  }

  @Override
  @ScheduledApiChange(when = "7.0", details = "Envelope key will become non-null.")
  public void updateVehicleEnvelopeKey(TCSObjectReference<Vehicle> ref, String envelopeKey)
      throws ObjectUnknownException,
        IllegalArgumentException,
        KernelRuntimeException {
    requireNonNull(ref, "ref");

    synchronized (globalSyncObject) {
      Vehicle vehicle = fetchObject(Vehicle.class, ref);
      if (vehicle.isProcessingOrder()
          || !vehicle.getClaimedResources().isEmpty()
          || !vehicle.getAllocatedResources().isEmpty()) {
        throw new IllegalArgumentException(
            "Updating a vehicle's envelope key while the vehicle is processing an order or "
                + "claiming/allocating resources is currently not supported."
        );
      }

      plantModelManager.setVehicleEnvelopeKey(ref, envelopeKey);
    }
  }

  @Override
  public void updateVehicleBoundingBox(TCSObjectReference<Vehicle> ref, BoundingBox boundingBox)
      throws ObjectUnknownException,
        KernelRuntimeException {
    requireNonNull(ref, "ref");
    requireNonNull(boundingBox, "boundingBox");

    synchronized (globalSyncObject) {
      plantModelManager.setVehicleBoundingBox(ref, boundingBox);
    }
  }
}
