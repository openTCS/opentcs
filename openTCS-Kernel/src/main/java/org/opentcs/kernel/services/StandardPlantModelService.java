/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.services;

import static com.google.common.base.Strings.isNullOrEmpty;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.inject.Inject;
import org.opentcs.access.Kernel;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.LocalKernel;
import org.opentcs.access.ModelTransitionEvent;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.components.kernel.services.InternalPlantModelService;
import org.opentcs.components.kernel.services.NotificationService;
import org.opentcs.components.kernel.services.PlantModelService;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.customizations.kernel.GlobalSyncObject;
import org.opentcs.data.ObjectExistsException;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Block;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.PlantModel;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.TCSResource;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.model.visualization.VisualLayout;
import org.opentcs.data.notification.UserNotification;
import org.opentcs.kernel.persistence.ModelPersister;
import org.opentcs.kernel.workingset.PlantModelManager;
import org.opentcs.util.event.EventHandler;

/**
 * This class is the standard implementation of the {@link PlantModelService} interface.
 */
public class StandardPlantModelService
    extends AbstractTCSObjectService
    implements InternalPlantModelService {

  /**
   * The kernel.
   */
  private final Kernel kernel;
  /**
   * A global object to be used for synchronization within the kernel.
   */
  private final Object globalSyncObject;
  /**
   * The plant model manager.
   */
  private final PlantModelManager plantModelManager;
  /**
   * The persister loading and storing model data.
   */
  private final ModelPersister modelPersister;
  /**
   * Where we send events to.
   */
  private final EventHandler eventHandler;
  /**
   * The notification service.
   */
  private final NotificationService notificationService;

  /**
   * Creates a new instance.
   *
   * @param kernel The kernel.
   * @param objectService The tcs object service.
   * @param globalSyncObject The kernel threads' global synchronization object.
   * @param plantModelManager The plant model manager to be used.
   * @param modelPersister The model persister to be used.
   * @param eventHandler Where this instance sends events to.
   * @param notificationService The notification service.
   */
  @Inject
  public StandardPlantModelService(LocalKernel kernel,
                                   TCSObjectService objectService,
                                   @GlobalSyncObject Object globalSyncObject,
                                   PlantModelManager plantModelManager,
                                   ModelPersister modelPersister,
                                   @ApplicationEventBus EventHandler eventHandler,
                                   NotificationService notificationService) {
    super(objectService);
    this.kernel = requireNonNull(kernel, "kernel");
    this.globalSyncObject = requireNonNull(globalSyncObject, "globalSyncObject");
    this.plantModelManager = requireNonNull(plantModelManager, "plantModelManager");
    this.modelPersister = requireNonNull(modelPersister, "modelPersister");
    this.eventHandler = requireNonNull(eventHandler, "eventHandler");
    this.notificationService = requireNonNull(notificationService, "notificationService");
  }

  @Override
  public Set<TCSResource<?>> expandResources(Set<TCSResourceReference<?>> resources)
      throws ObjectUnknownException {
    requireNonNull(resources, "resources");
    
    synchronized (globalSyncObject) {
      return plantModelManager.expandResources(resources);
    }
  }

  @Override
  public void loadPlantModel()
      throws IllegalStateException {
    synchronized (globalSyncObject) {
      if (!modelPersister.hasSavedModel()) {
        createPlantModel(new PlantModelCreationTO(Kernel.DEFAULT_MODEL_NAME));
        return;
      }

      final String oldModelName = getModelName();
      // Load the new model
      PlantModelCreationTO modelCreationTO = modelPersister.readModel();
      final String newModelName = isNullOrEmpty(modelCreationTO.getName())
          ? ""
          : modelCreationTO.getName();
      // Let listeners know we're in transition.
      emitModelEvent(oldModelName, newModelName, true, false);
      plantModelManager.createPlantModelObjects(modelCreationTO);
      // Let listeners know we're done with the transition.
      emitModelEvent(oldModelName, newModelName, true, true);
      notificationService.publishUserNotification(
          new UserNotification("Kernel loaded model " + newModelName,
                               UserNotification.Level.INFORMATIONAL));
    }
  }

  @Override
  public void savePlantModel()
      throws IllegalStateException {
    synchronized (globalSyncObject) {
      modelPersister.saveModel(plantModelManager.createPlantModelCreationTO());
    }
  }

  @Override
  public PlantModel getPlantModel() {
    synchronized (globalSyncObject) {
      return new PlantModel(plantModelManager.getName())
          .withProperties(getModelProperties())
          .withPoints(fetchObjects(Point.class))
          .withPaths(fetchObjects(Path.class))
          .withLocationTypes(fetchObjects(LocationType.class))
          .withLocations(fetchObjects(Location.class))
          .withBlocks(fetchObjects(Block.class))
          .withVehicles(fetchObjects(Vehicle.class))
          .withVisualLayouts(fetchObjects(VisualLayout.class));
    }
  }

  @Override
  public void createPlantModel(PlantModelCreationTO to)
      throws ObjectUnknownException, ObjectExistsException, IllegalStateException {
    requireNonNull(to, "to");

    boolean kernelInOperating = kernel.getState() == Kernel.State.OPERATING;
    // If we are in state operating, change the kernel state before creating the plant model
    if (kernelInOperating) {
      kernel.setState(Kernel.State.MODELLING);
    }

    String oldModelName = getModelName();
    emitModelEvent(oldModelName, to.getName(), true, false);

    // Create the plant model
    synchronized (globalSyncObject) {
      plantModelManager.createPlantModelObjects(to);
    }

    savePlantModel();

    // If we were in state operating before, change the kernel state back to operating
    if (kernelInOperating) {
      kernel.setState(Kernel.State.OPERATING);
    }

    emitModelEvent(oldModelName, to.getName(), true, true);
    notificationService.publishUserNotification(
        new UserNotification("Kernel created model " + to.getName(),
                             UserNotification.Level.INFORMATIONAL));
  }

  @Override
  public String getModelName() {
    synchronized (globalSyncObject) {
      return plantModelManager.getName();
    }
  }

  @Override
  public Map<String, String> getModelProperties()
      throws KernelRuntimeException {
    synchronized (globalSyncObject) {
      return plantModelManager.getProperties();
    }
  }

  @Override
  public void updateLocationLock(TCSObjectReference<Location> ref, boolean locked)
      throws ObjectUnknownException {
    requireNonNull(ref, "ref");
    
    synchronized (globalSyncObject) {
      plantModelManager.setLocationLocked(ref, locked);
    }
  }

  @Deprecated
  @Override
  public void updateLocationReservationToken(TCSObjectReference<Location> ref, String token)
      throws ObjectUnknownException, KernelRuntimeException {
    requireNonNull(ref, "ref");
    
    synchronized (globalSyncObject) {
      plantModelManager.setLocationReservationToken(ref, token);
    }
  }

  @Override
  public void updatePathLock(TCSObjectReference<Path> ref, boolean locked)
      throws ObjectUnknownException, KernelRuntimeException {
    synchronized (globalSyncObject) {
      plantModelManager.setPathLocked(ref, locked);
    }
  }

  /**
   * Generates an event for a Model change.
   *
   * @param oldModelName The old model name.
   * @param newModelName The new model name.
   * @param modelContentChanged Whether the model's content actually changed.
   * @param transitionFinished Whether the transition is finished or not.
   */
  private void emitModelEvent(String oldModelName,
                              String newModelName,
                              boolean modelContentChanged,
                              boolean transitionFinished) {
    requireNonNull(newModelName, "newModelName");

    eventHandler.onEvent(new ModelTransitionEvent(oldModelName,
                                                  newModelName,
                                                  modelContentChanged,
                                                  transitionFinished));
  }
}
