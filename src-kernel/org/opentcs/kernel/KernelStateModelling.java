/*
 * openTCS copyright information:
 * Copyright (c) 2007 Fraunhofer IML
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
import java.util.logging.Logger;
import javax.inject.Inject;
import org.opentcs.access.Kernel;
import org.opentcs.algorithms.KernelExtension;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Block;
import org.opentcs.data.model.Group;
import org.opentcs.data.model.Layout;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.StaticRoute;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.model.visualization.LayoutElement;
import org.opentcs.data.model.visualization.VisualLayout;
import org.opentcs.kernel.workingset.MessageBuffer;
import org.opentcs.kernel.workingset.Model;
import org.opentcs.kernel.workingset.TCSObjectPool;

/**
 * This class implements the standard openTCS kernel in modelling mode.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
final class KernelStateModelling
    extends KernelState {

  /**
   * This class's Logger.
   */
  private static final Logger log
      = Logger.getLogger(KernelStateModelling.class.getName());
  /**
   * This kernel state's local extensions.
   */
  private final Set<KernelExtension> extensions;
  /**
   * This instance's <em>initialized</em> flag.
   */
  private boolean initialized;

  /**
   * Creates a new kernel.
   *
   * @param kernel The kernel.
   * @param objectPool The object pool to be used.
   * @param messageBuffer The message buffer to be used.
   */
  @Inject
  KernelStateModelling(StandardKernel kernel,
                       @GlobalKernelSync Object globalSyncObject,
                       TCSObjectPool objectPool,
                       Model model,
                       MessageBuffer messageBuffer,
                       @KernelExtension.Modelling Set<KernelExtension> extensions) {
    super(kernel,
          globalSyncObject,
          objectPool,
          model,
          messageBuffer);
    this.extensions = requireNonNull(extensions, "extensions");
  }

  // Implementation of abstract class StandardKernelState starts here.
  @Override
  public void initialize() {
    if (initialized) {
      throw new IllegalStateException("Already initialized");
    }
    log.fine("Initializing operating state...");
    // Start kernel extensions.
    for (KernelExtension extension : extensions) {
      extension.plugIn();
    }

    initialized = true;
    
    log.fine("Modelling state initialized.");
  }

  @Override
  public void terminate() {
    if (!initialized) {
      throw new IllegalStateException("Not initialized, cannot terminate");
    }
    log.fine("Terminating modelling state...");
    // Terminate everything that may still use resources.
    for (KernelExtension extension : extensions) {
      extension.plugOut();
    }

    initialized = false;

    log.fine("Modelling state terminated.");
  }

  @Override
  public Kernel.State getState() {
    return Kernel.State.MODELLING;
  }

  @Override
  public void createModel(String modelName) {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      // Clear the model and set its name.
      model.clear();
      model.setName(modelName);
    }
  }

  @Override
  public void loadModel(String modelName)
      throws IOException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      kernel.modelPersister.loadModel(modelName, model);
    }
  }

  @Override
  public void saveModel(String modelName, boolean overwrite)
      throws IOException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      if (modelName != null) {
        verifyModelNameCaseMatch(modelName);
        model.setName(modelName);
      }
      kernel.modelPersister.saveModel(model, model.getName(), overwrite);
    }
  }

  @Override
  public void removeModel(String rmName)
      throws IOException {
    log.finer("method entry");
    kernel.modelPersister.removeModel(rmName);
  }

  @Override
  public void removeTCSObject(TCSObjectReference<?> ref)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      TCSObject<?> object = globalObjectPool.getObject(ref);
      if (object == null) {
        throw new ObjectUnknownException(ref);
      }
      // We only allow removal of model objects in modelling mode.
      if (object instanceof Block) {
        model.removeBlock(((Block) object).getReference());
      }
      else if (object instanceof Group) {
        model.removeGroup(((Group) object).getReference());
      }
      else if (object instanceof Layout) {
        model.removeLayout(((Layout) object).getReference());
      }
      else if (object instanceof Location) {
        model.removeLocation(((Location) object).getReference());
      }
      else if (object instanceof LocationType) {
        model.removeLocationType(((LocationType) object).getReference());
      }
      else if (object instanceof Path) {
        model.removePath(((Path) object).getReference());
      }
      else if (object instanceof Point) {
        model.removePoint(((Point) object).getReference());
      }
      else if (object instanceof StaticRoute) {
        model.removeStaticRoute(((StaticRoute) object).getReference());
      }
      else if (object instanceof Vehicle) {
        model.removeVehicle(((Vehicle) object).getReference());
      }
      else {
        super.removeTCSObject(ref);
      }
    }
  }

  @Override
  public Layout createLayout(byte[] layoutData) {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      return model.createLayout(null, layoutData).clone();
    }
  }

  @Override
  public void setLayoutData(
      TCSObjectReference<Layout> ref, byte[] newData)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      model.setLayoutData(ref, newData);
    }
  }

  @Override
  public VisualLayout createVisualLayout() {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      return model.createVisualLayout(null).clone();
    }
  }

  @Override
  public void setVisualLayoutScaleX(TCSObjectReference<VisualLayout> ref,
                                    double scaleX)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      model.setVisualLayoutScaleX(ref, scaleX);
    }
  }

  @Override
  public void setVisualLayoutScaleY(TCSObjectReference<VisualLayout> ref,
                                    double scaleY)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      model.setVisualLayoutScaleY(ref, scaleY);
    }
  }

  @Override
  public void setVisualLayoutColors(TCSObjectReference<VisualLayout> ref,
                                    Map<String, Color> colors)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      model.setVisualLayoutColors(ref, colors);
    }
  }

  @Override
  public void setVisualLayoutElements(TCSObjectReference<VisualLayout> ref,
                                      Set<LayoutElement> elements)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      model.setVisualLayoutElements(ref, elements);
    }
  }

  @Override
  public Point createPoint() {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      // Return a copy of the point
      return model.createPoint(null).clone();
    }
  }

  @Override
  public void setPointPosition(TCSObjectReference<Point> ref,
                               Triple position)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      model.setPointPosition(ref, position);
    }
  }

  @Override
  public void setPointVehicleOrientationAngle(TCSObjectReference<Point> ref,
                                              double angle)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      model.setPointVehicleOrientationAngle(ref, angle);
    }
  }

  @Override
  public void setPointType(TCSObjectReference<Point> ref,
                           Point.Type newType)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      model.setPointType(ref, newType);
    }
  }

  @Override
  public Path createPath(TCSObjectReference<Point> srcRef,
                         TCSObjectReference<Point> destRef)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      return model.createPath(null, srcRef, destRef).clone();
    }
  }

  @Override
  public void setPathLength(TCSObjectReference<Path> ref,
                            long length)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      model.setPathLength(ref, length);
    }
  }

  @Override
  public void setPathRoutingCost(TCSObjectReference<Path> ref,
                                 long cost)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      model.setPathRoutingCost(ref, cost);
    }
  }

  @Override
  public void setPathMaxVelocity(TCSObjectReference<Path> ref,
                                 int velocity)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      model.setPathMaxVelocity(ref, velocity);
    }
  }

  @Override
  public void setPathMaxReverseVelocity(TCSObjectReference<Path> ref,
                                        int velocity)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      model.setPathMaxReverseVelocity(ref, velocity);
    }
  }

  @Override
  public void setPathLocked(TCSObjectReference<Path> ref,
                            boolean locked)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      model.setPathLocked(ref, locked);
    }
  }

  @Override
  public Vehicle createVehicle() {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      return model.createVehicle(null).clone();
    }
  }

  @Override
  public void setVehicleLength(TCSObjectReference<Vehicle> ref, int length)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      model.setVehicleLength(ref, length);
    }
  }

  @Override
  public LocationType createLocationType() {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      return model.createLocationType(null).clone();
    }
  }

  @Override
  public void addLocationTypeAllowedOperation(
      TCSObjectReference<LocationType> ref,
      String operation)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      model.addLocationTypeAllowedOperation(ref, operation);
    }
  }

  @Override
  public void removeLocationTypeAllowedOperation(
      TCSObjectReference<LocationType> ref, String operation)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      model.removeLocationTypeAllowedOperation(ref, operation);
    }
  }

  @Override
  public Location createLocation(TCSObjectReference<LocationType> typeRef)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      return model.createLocation(null, typeRef).clone();
    }
  }

  @Override
  public void setLocationPosition(TCSObjectReference<Location> ref,
                                  Triple position)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      model.setLocationPosition(ref, position);
    }
  }

  @Override
  public void setLocationType(TCSObjectReference<Location> ref,
                              TCSObjectReference<LocationType> typeRef)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      model.setLocationType(ref, typeRef);
    }
  }

  @Override
  public void connectLocationToPoint(TCSObjectReference<Location> locRef,
                                     TCSObjectReference<Point> pointRef)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      model.connectLocationToPoint(locRef, pointRef);
    }
  }

  @Override
  public void disconnectLocationFromPoint(TCSObjectReference<Location> locRef,
                                          TCSObjectReference<Point> pointRef)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      model.disconnectLocationFromPoint(locRef, pointRef);
    }
  }

  @Override
  public void addLocationLinkAllowedOperation(
      TCSObjectReference<Location> locRef, TCSObjectReference<Point> pointRef,
      String operation)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      model.addLocationLinkAllowedOperation(locRef, pointRef, operation);
    }
  }

  @Override
  public void removeLocationLinkAllowedOperation(
      TCSObjectReference<Location> locRef, TCSObjectReference<Point> pointRef,
      String operation)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      model.removeLocationLinkAllowedOperation(locRef, pointRef, operation);
    }
  }

  @Override
  public void clearLocationLinkAllowedOperations(
      TCSObjectReference<Location> locRef, TCSObjectReference<Point> pointRef)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      model.clearLocationLinkAllowedOperations(locRef, pointRef);
    }
  }

  @Override
  public Block createBlock() {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      // Return a copy of the point
      return model.createBlock(null).clone();
    }
  }

  @Override
  public void addBlockMember(TCSObjectReference<Block> ref,
                             TCSResourceReference<?> newMemberRef)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      model.addBlockMember(ref, newMemberRef);
    }
  }

  @Override
  public void removeBlockMember(TCSObjectReference<Block> ref,
                                TCSResourceReference<?> rmMemberRef)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      model.removeBlockMember(ref, rmMemberRef);
    }
  }

  @Override
  public StaticRoute createStaticRoute() {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      // Return a copy of the point
      return model.createStaticRoute(null).clone();
    }
  }

  @Override
  public void addStaticRouteHop(TCSObjectReference<StaticRoute> ref,
                                TCSObjectReference<Point> newHopRef)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      model.addStaticRouteHop(ref, newHopRef);
    }
  }

  @Override
  public void clearStaticRouteHops(TCSObjectReference<StaticRoute> ref)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      model.clearStaticRouteHops(ref);
    }
  }

  @Override
  public void attachResource(TCSResourceReference<?> resource,
                             TCSResourceReference<?> newResource)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      model.attachResource(resource, newResource).clone();
    }
  }

  @Override
  public void detachResource(TCSResourceReference<?> resource,
                             TCSResourceReference<?> rmResource)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      model.detachResource(resource, rmResource).clone();
    }
  }
}
