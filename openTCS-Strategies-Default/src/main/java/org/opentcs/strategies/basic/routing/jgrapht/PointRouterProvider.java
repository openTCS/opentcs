/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.routing.jgrapht;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import org.opentcs.components.kernel.routing.GroupMapper;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.routing.PointRouter;
import org.opentcs.strategies.basic.routing.PointRouterFactory;
import org.opentcs.strategies.basic.routing.ResourceAvoidanceExtractor;
import org.opentcs.strategies.basic.routing.ResourceAvoidanceExtractor.ResourcesToAvoid;

/**
 * Provides point routers for vehicles (more specifically for routing groups of vehicles).
 * <p>
 * This provider caches constructed point routers until it is {@link #invalidate() invalidated}.
 * </p>
 */
public class PointRouterProvider {

  private final TCSObjectService objectService;
  private final ResourceAvoidanceExtractor resourceAvoidanceExtractor;
  private final GroupMapper routingGroupMapper;
  private final PointRouterFactory pointRouterFactory;
  private final GraphProvider graphProvider;
  /**
   * The point routers by vehicle routing group.
   */
  private final Map<String, PointRouter> pointRoutersByVehicleGroup = new ConcurrentHashMap<>();

  /**
   * Creates a new instance.
   *
   * @param objectService The object service providing the model data.
   * @param resourceAvoidanceExtractor Extracts resources to be avoided from transport orders.
   * @param routingGroupMapper Used to map vehicles to their routing groups.
   * @param pointRouterFactory A builder for constructing point routers (i.e., the routing tables).
   * @param graphProvider Provides routing graphs for vehicles.
   */
  @Inject
  public PointRouterProvider(TCSObjectService objectService,
                             ResourceAvoidanceExtractor resourceAvoidanceExtractor,
                             GroupMapper routingGroupMapper,
                             PointRouterFactory pointRouterFactory,
                             GraphProvider graphProvider) {
    this.objectService = requireNonNull(objectService, "objectService");
    this.resourceAvoidanceExtractor = requireNonNull(resourceAvoidanceExtractor,
                                                     "resourceAvoidanceExtractor");
    this.routingGroupMapper = requireNonNull(routingGroupMapper, "routingGroupMapper");
    this.pointRouterFactory = requireNonNull(pointRouterFactory, "pointRouterFactory");
    this.graphProvider = requireNonNull(graphProvider, "graphProvider");
  }

  /**
   * Invalidates any point routers that have already been constructed.
   */
  public void invalidate() {
    pointRoutersByVehicleGroup.clear();
    graphProvider.invalidate();
  }

  /**
   * Updates the routing topology with respect to the given paths.
   *
   * @param paths The paths to update in the routing topology. An empty set of paths results in any
   * constructed point routers to be invalidated.
   */
  public void updateRoutingTopology(@Nonnull Set<Path> paths) {
    requireNonNull(paths, "paths");

    pointRoutersByVehicleGroup.clear();

    if (paths.isEmpty()) {
      graphProvider.invalidate();
    }
    else {
      graphProvider.updateGraphResults(paths);
    }
  }

  /**
   * Returns the {@link PointRouter} for the given vehicle considering the vehicle's routing group
   * and the given transport order.
   *
   * @param vehicle The vehicle to get the point router for.
   * @param order The transport order to be processed by the vehicle.
   * @return The point router.
   */
  public PointRouter getPointRouterForVehicle(@Nonnull Vehicle vehicle,
                                              @Nullable TransportOrder order) {
    requireNonNull(vehicle, "vehicle");

    return getPointRouterForVehicle(
        vehicle,
        resourceAvoidanceExtractor
            .extractResourcesToAvoid(order)
    );
  }

  /**
   * Returns the {@link PointRouter} for the given vehicle considering the vehicle's routing group
   * and the given set of resources to avoid.
   *
   * @param vehicle The vehicle to get the point router for.
   * @param resourcesToAvoid The resources to avoid when computing the route.
   * @return The point router.
   */
  public PointRouter getPointRouterForVehicle(
      @Nonnull Vehicle vehicle,
      @Nonnull Set<TCSResourceReference<?>> resourcesToAvoid
  ) {
    requireNonNull(vehicle, "vehicle");
    requireNonNull(resourcesToAvoid, "resourcesToAvoid");

    return getPointRouterForVehicle(
        vehicle,
        resourceAvoidanceExtractor
            .extractResourcesToAvoid(resourcesToAvoid)
    );
  }

  /**
   * Returns all point routers mapped to the vehicle routing group they belong to.
   *
   * @return All point routers mapped to the vehicle routing group they belong to.
   */
  public Map<String, PointRouter> getPointRoutersByVehicleGroup() {
    // Since point routers get reset on topology changes, make sure there are point routers for
    // all routing groups.
    createMissingPointRouters();

    return Collections.unmodifiableMap(pointRoutersByVehicleGroup);
  }

  private void createMissingPointRouters() {
    Map<String, Vehicle> distinctRoutingGroups = new HashMap<>();
    for (Vehicle vehicle : objectService.fetchObjects(Vehicle.class)) {
      distinctRoutingGroups.putIfAbsent(routingGroupMapper.apply(vehicle), vehicle);
    }

    // Lazily create point routers if they don't exist.
    distinctRoutingGroups.forEach(
        (routingGroup, vehicle) -> getPointRouterForVehicle(vehicle, (TransportOrder) null)
    );
  }

  private PointRouter getPointRouterForVehicle(Vehicle vehicle, ResourcesToAvoid resourcesToAvoid) {
    if (!resourcesToAvoid.isEmpty()) {
      return pointRouterFactory.createPointRouter(vehicle,
                                                  resourcesToAvoid.getPoints(),
                                                  resourcesToAvoid.getPaths());
    }

    // In all other cases, create a point router if it does not yet exist for the vehicle's routing
    // group.
    return pointRoutersByVehicleGroup.computeIfAbsent(
        routingGroupMapper.apply(vehicle),
        routingGroup -> pointRouterFactory.createPointRouter(vehicle, Set.of(), Set.of())
    );
  }
}
