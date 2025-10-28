// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.services;

import static java.util.Objects.requireNonNull;

import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.components.kernel.Router;
import org.opentcs.components.kernel.services.RouterService;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.customizations.kernel.GlobalSyncObject;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.Route;
import org.opentcs.kernel.workingset.PlantModelManager;

/**
 * This class is the standard implementation of the {@link RouterService} interface.
 */
public class StandardRouterService
    implements
      RouterService {

  /**
   * A global object to be used for synchronization within the kernel.
   */
  private final Object globalSyncObject;
  /**
   * The router.
   */
  private final Router router;
  /**
   * The plant model manager.
   */
  private final PlantModelManager plantModelManager;
  /**
   * The object service.
   */
  private final TCSObjectService objectService;

  /**
   * Creates a new instance.
   *
   * @param globalSyncObject The kernel threads' global synchronization object.
   * @param router The scheduler.
   * @param plantModelManager The plant model manager to be used.
   * @param objectService The object service.
   */
  @Inject
  public StandardRouterService(
      @GlobalSyncObject
      Object globalSyncObject,
      Router router,
      PlantModelManager plantModelManager,
      TCSObjectService objectService
  ) {
    this.globalSyncObject = requireNonNull(globalSyncObject, "globalSyncObject");
    this.router = requireNonNull(router, "router");
    this.plantModelManager = requireNonNull(plantModelManager, "plantModelManager");
    this.objectService = requireNonNull(objectService, "objectService");
  }

  @Override
  public void updateRoutingTopology(Set<TCSObjectReference<Path>> refs)
      throws KernelRuntimeException {
    synchronized (globalSyncObject) {
      router.updateRoutingTopology(
          refs.stream()
              .map(ref -> plantModelManager.getObjectRepo().getObject(Path.class, ref))
              .collect(Collectors.toSet())
      );
    }
  }

  @Override
  public Map<TCSObjectReference<Point>, Set<Route>> computeRoutes(
      TCSObjectReference<Vehicle> vehicleRef,
      TCSObjectReference<Point> sourcePointRef,
      Set<TCSObjectReference<Point>> destinationPointRefs,
      Set<TCSResourceReference<?>> resourcesToAvoid,
      int maxRoutesPerDestinationPoint
  )
      throws KernelRuntimeException {
    requireNonNull(vehicleRef, "vehicleRef");
    requireNonNull(sourcePointRef, "sourcePointRef");
    requireNonNull(destinationPointRefs, "destinationPointRefs");
    requireNonNull(resourcesToAvoid, "resourcesToAvoid");

    synchronized (globalSyncObject) {
      Map<TCSObjectReference<Point>, Set<Route>> result = new HashMap<>();
      Vehicle vehicle = objectService.fetch(Vehicle.class, vehicleRef).orElse(null);
      if (vehicle == null) {
        throw new ObjectUnknownException("Unknown vehicle: " + vehicleRef.getName());
      }
      Point sourcePoint = objectService.fetch(Point.class, sourcePointRef).orElse(null);
      if (sourcePoint == null) {
        throw new ObjectUnknownException("Unknown source point: " + sourcePointRef.getName());
      }
      for (TCSObjectReference<Point> dest : destinationPointRefs) {
        Point destinationPoint = objectService.fetch(Point.class, dest).orElse(null);
        if (destinationPoint == null) {
          throw new ObjectUnknownException("Unknown destination point: " + dest.getName());
        }
        result.put(
            dest,
            router.getRoutes(
                vehicle,
                sourcePoint,
                destinationPoint,
                resourcesToAvoid,
                maxRoutesPerDestinationPoint
            )
        );
      }
      return result;
    }
  }
}
