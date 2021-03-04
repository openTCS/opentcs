/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel;

import java.awt.Color;
import java.io.IOException;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.inject.Inject;
import org.opentcs.access.Kernel;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.components.kernel.KernelExtension;
import org.opentcs.customizations.kernel.ActiveInModellingMode;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Block;
import org.opentcs.data.model.Group;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.model.visualization.LayoutElement;
import org.opentcs.data.model.visualization.VisualLayout;
import org.opentcs.kernel.persistence.ModelPersister;
import org.opentcs.kernel.workingset.Model;
import org.opentcs.kernel.workingset.NotificationBuffer;
import org.opentcs.kernel.workingset.TCSObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements the standard openTCS kernel in modelling mode.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
class KernelStateModelling
    extends KernelStateOnline {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(KernelStateModelling.class);
  /**
   * This kernel state's local extensions.
   */
  private final Set<KernelExtension> extensions;
  /**
   * This instance's <em>initialized</em> flag.
   */
  private boolean initialized;

  /**
   * Creates a new instance.
   *
   * @param objectPool The object pool to be used.
   * @param messageBuffer The message buffer to be used.
   * @param modelPersister The model persister to be used.
   * @param configuration This class's configuration.
   */
  @Inject
  KernelStateModelling(@GlobalKernelSync Object globalSyncObject,
                       TCSObjectPool objectPool,
                       Model model,
                       NotificationBuffer messageBuffer,
                       ModelPersister modelPersister,
                       KernelApplicationConfiguration configuration,
                       @ActiveInModellingMode Set<KernelExtension> extensions) {
    super(globalSyncObject,
          objectPool,
          model,
          messageBuffer,
          modelPersister,
          configuration.saveModelOnTerminateModelling());
    this.extensions = requireNonNull(extensions, "extensions");
  }

  @Override
  public void initialize() {
    if (initialized) {
      throw new IllegalStateException("Already initialized");
    }
    LOG.debug("Initializing modelling state...");

    // Start kernel extensions.
    for (KernelExtension extension : extensions) {
      LOG.debug("Initializing kernel extension '{}'...", extension);
      extension.initialize();
    }
    LOG.debug("Finished initializing kernel extensions.");

    initialized = true;

    LOG.debug("Modelling state initialized.");
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void terminate() {
    if (!initialized) {
      throw new IllegalStateException("Not initialized, cannot terminate");
    }
    LOG.debug("Terminating modelling state...");
    super.terminate();

    // Terminate everything that may still use resources.
    for (KernelExtension extension : extensions) {
      LOG.debug("Terminating kernel extension '{}'...", extension);
      extension.terminate();
    }
    LOG.debug("Terminated kernel extensions.");

    initialized = false;

    LOG.debug("Modelling state terminated.");
  }

  @Override
  public Kernel.State getState() {
    return Kernel.State.MODELLING;
  }

  @Override
  @Deprecated
  public void createModel(String modelName) {
    this.createPlantModel(new PlantModelCreationTO(modelName));
  }

  @Override
  @Deprecated
  public void createPlantModel(PlantModelCreationTO to) {
    synchronized (getGlobalSyncObject()) {
      getModel().clear();
      getModel().setName(to.getName());
      getModel().createPlantModelObjects(to);
    }
  }

  @Override
  @Deprecated
  public void loadPlantModel()
      throws IllegalStateException {
    synchronized (getGlobalSyncObject()) {
      getModelPersister().loadModel(getModel());
    }
  }

  @Override
  @Deprecated
  public void loadModel()
      throws IOException {
    this.loadPlantModel();
  }

  @Override
  @Deprecated
  public void removeModel()
      throws IOException {
    getModelPersister().removeModel();
  }

  @Override
  @Deprecated
  public void removeTCSObject(TCSObjectReference<?> ref)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      TCSObject<?> object = getGlobalObjectPool().getObjectOrNull(ref);
      if (object == null) {
        throw new ObjectUnknownException(ref);
      }
      // We only allow removal of model objects in modelling mode.
      if (object instanceof Block) {
        getModel().removeBlock(((Block) object).getReference());
      }
      else if (object instanceof Group) {
        getModel().removeGroup(((Group) object).getReference());
      }
      else if (object instanceof Location) {
        getModel().removeLocation(((Location) object).getReference());
      }
      else if (object instanceof LocationType) {
        getModel().removeLocationType(((LocationType) object).getReference());
      }
      else if (object instanceof Path) {
        getModel().removePath(((Path) object).getReference());
      }
      else if (object instanceof Point) {
        getModel().removePoint(((Point) object).getReference());
      }
      else if (object instanceof org.opentcs.data.model.StaticRoute) {
        getModel().removeStaticRoute(((org.opentcs.data.model.StaticRoute) object).getReference());
      }
      else if (object instanceof Vehicle) {
        getModel().removeVehicle(((Vehicle) object).getReference());
      }
      else {
        super.removeTCSObject(ref);
      }
    }
  }

  @Override
  @Deprecated
  public VisualLayout createVisualLayout() {
    synchronized (getGlobalSyncObject()) {
      return getModel().createVisualLayout((Integer) null).clone();
    }
  }

  @Override
  @Deprecated
  public void setVisualLayoutScaleX(TCSObjectReference<VisualLayout> ref,
                                    double scaleX)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      getModel().setVisualLayoutScaleX(ref, scaleX);
    }
  }

  @Override
  @Deprecated
  public void setVisualLayoutScaleY(TCSObjectReference<VisualLayout> ref,
                                    double scaleY)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      getModel().setVisualLayoutScaleY(ref, scaleY);
    }
  }

  @Override
  @Deprecated
  public void setVisualLayoutColors(TCSObjectReference<VisualLayout> ref,
                                    Map<String, Color> colors)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      getModel().setVisualLayoutColors(ref, colors);
    }
  }

  @Override
  @Deprecated
  public void setVisualLayoutElements(TCSObjectReference<VisualLayout> ref,
                                      Set<LayoutElement> elements)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      getModel().setVisualLayoutElements(ref, elements);
    }
  }

  @Override
  @Deprecated
  public Point createPoint() {
    synchronized (getGlobalSyncObject()) {
      // Return a copy of the point
      return getModel().createPoint((Integer) null).clone();
    }
  }

  @Override
  @Deprecated
  public void setPointPosition(TCSObjectReference<Point> ref,
                               Triple position)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      getModel().setPointPosition(ref, position);
    }
  }

  @Override
  @Deprecated
  public void setPointVehicleOrientationAngle(TCSObjectReference<Point> ref,
                                              double angle)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      getModel().setPointVehicleOrientationAngle(ref, angle);
    }
  }

  @Override
  @Deprecated
  public void setPointType(TCSObjectReference<Point> ref,
                           Point.Type newType)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      getModel().setPointType(ref, newType);
    }
  }

  @Override
  @Deprecated
  public Path createPath(TCSObjectReference<Point> srcRef,
                         TCSObjectReference<Point> destRef)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      return getModel().createPath(null, srcRef, destRef).clone();
    }
  }

  @Override
  @Deprecated
  public void setPathLength(TCSObjectReference<Path> ref, long length)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      getModel().setPathLength(ref, length);
    }
  }

  @Override
  @Deprecated
  public void setPathRoutingCost(TCSObjectReference<Path> ref, long cost)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      getModel().setPathRoutingCost(ref, cost);
    }
  }

  @Override
  @Deprecated
  public void setPathMaxVelocity(TCSObjectReference<Path> ref, int velocity)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      getModel().setPathMaxVelocity(ref, velocity);
    }
  }

  @Override
  @Deprecated
  public void setPathMaxReverseVelocity(TCSObjectReference<Path> ref,
                                        int velocity)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      getModel().setPathMaxReverseVelocity(ref, velocity);
    }
  }

  @Override
  @SuppressWarnings("deprecation")
  public void setPathLocked(TCSObjectReference<Path> ref, boolean locked)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      getModel().setPathLocked(ref, locked);
    }
  }

  @Override
  @Deprecated
  public Vehicle createVehicle() {
    synchronized (getGlobalSyncObject()) {
      return getModel().createVehicle((Integer) null).clone();
    }
  }

  @Override
  @Deprecated
  public void setVehicleLength(TCSObjectReference<Vehicle> ref, int length)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      getModel().setVehicleLength(ref, length);
    }
  }

  @Override
  @Deprecated
  public LocationType createLocationType() {
    synchronized (getGlobalSyncObject()) {
      return getModel().createLocationType((Integer) null).clone();
    }
  }

  @Override
  @Deprecated
  public void addLocationTypeAllowedOperation(
      TCSObjectReference<LocationType> ref,
      String operation)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      getModel().addLocationTypeAllowedOperation(ref, operation);
    }
  }

  @Override
  @Deprecated
  public void removeLocationTypeAllowedOperation(
      TCSObjectReference<LocationType> ref, String operation)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      getModel().removeLocationTypeAllowedOperation(ref, operation);
    }
  }

  @Override
  @Deprecated
  public Location createLocation(TCSObjectReference<LocationType> typeRef)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      return getModel().createLocation(null, typeRef).clone();
    }
  }

  @Override
  @Deprecated
  public void setLocationPosition(TCSObjectReference<Location> ref,
                                  Triple position)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      getModel().setLocationPosition(ref, position);
    }
  }

  @Override
  @Deprecated
  public void setLocationType(TCSObjectReference<Location> ref,
                              TCSObjectReference<LocationType> typeRef)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      getModel().setLocationType(ref, typeRef);
    }
  }

  @Override
  @Deprecated
  public void connectLocationToPoint(TCSObjectReference<Location> locRef,
                                     TCSObjectReference<Point> pointRef)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      getModel().connectLocationToPoint(locRef, pointRef);
    }
  }

  @Override
  @Deprecated
  public void disconnectLocationFromPoint(TCSObjectReference<Location> locRef,
                                          TCSObjectReference<Point> pointRef)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      getModel().disconnectLocationFromPoint(locRef, pointRef);
    }
  }

  @Override
  @Deprecated
  public void addLocationLinkAllowedOperation(
      TCSObjectReference<Location> locRef, TCSObjectReference<Point> pointRef,
      String operation)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      getModel().addLocationLinkAllowedOperation(locRef, pointRef, operation);
    }
  }

  @Override
  @Deprecated
  public void removeLocationLinkAllowedOperation(
      TCSObjectReference<Location> locRef, TCSObjectReference<Point> pointRef,
      String operation)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      getModel().removeLocationLinkAllowedOperation(locRef, pointRef, operation);
    }
  }

  @Override
  @Deprecated
  public void clearLocationLinkAllowedOperations(
      TCSObjectReference<Location> locRef, TCSObjectReference<Point> pointRef)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      getModel().clearLocationLinkAllowedOperations(locRef, pointRef);
    }
  }

  @Override
  @Deprecated
  public Block createBlock() {
    synchronized (getGlobalSyncObject()) {
      // Return a copy of the point
      return getModel().createBlock((Integer) null).clone();
    }
  }

  @Override
  @Deprecated
  public void addBlockMember(TCSObjectReference<Block> ref,
                             TCSResourceReference<?> newMemberRef)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      getModel().addBlockMember(ref, newMemberRef);
    }
  }

  @Override
  @Deprecated
  public void removeBlockMember(TCSObjectReference<Block> ref,
                                TCSResourceReference<?> rmMemberRef)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      getModel().removeBlockMember(ref, rmMemberRef);
    }
  }

  @Override
  @Deprecated
  public org.opentcs.data.model.StaticRoute createStaticRoute() {
    synchronized (getGlobalSyncObject()) {
      // Return a copy of the point
      return getModel().createStaticRoute((Integer) null).clone();
    }
  }

  @Override
  @Deprecated
  public void addStaticRouteHop(TCSObjectReference<org.opentcs.data.model.StaticRoute> ref,
                                TCSObjectReference<Point> newHopRef)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      getModel().addStaticRouteHop(ref, newHopRef);
    }
  }

  @Override
  @Deprecated
  public void clearStaticRouteHops(TCSObjectReference<org.opentcs.data.model.StaticRoute> ref)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      getModel().clearStaticRouteHops(ref);
    }
  }
}
