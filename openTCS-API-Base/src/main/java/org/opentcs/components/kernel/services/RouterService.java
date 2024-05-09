/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.components.kernel.services;

import java.util.Map;
import java.util.Set;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.components.kernel.Router;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.Route;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * Provides methods concerning the {@link Router}.
 */
public interface RouterService {

  /**
   * Updates a path's lock state.
   * <p>
   * If called within the kernel application, this method is supposed to be called only on the
   * kernel executor thread.
   * </p>
   *
   * @param ref A reference to the path to be updated.
   * @param locked Indicates whether the path is to be locked ({@code true}) or unlocked
   * ({@code false}).
   * @throws ObjectUnknownException If the referenced path does not exist.
   * @throws KernelRuntimeException In case there is an exception executing this method.
   * @deprecated Use
   * {@link PlantModelService#updatePathLock(org.opentcs.data.TCSObjectReference, boolean)} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "6.0", details = "Will be removed.")
  void updatePathLock(TCSObjectReference<Path> ref, boolean locked)
      throws ObjectUnknownException, KernelRuntimeException;

  /**
   * Notifies the router that the topology has changed in a significant way and needs to be
   * re-evaluated.
   * <p>
   * If called within the kernel application, this method is supposed to be called only on the
   * kernel executor thread.
   * </p>
   *
   * @throws KernelRuntimeException In case there is an exception executing this method.
   * @deprecated Use {@link #updateRoutingTopology(java.util.Set)} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "6.0", details = "Will be removed.")
  void updateRoutingTopology()
      throws KernelRuntimeException;

  /**
   * Notifies the router that the topology has changed with respect to the given paths and needs to
   * be re-evaluated.
   * <p>
   * If called within the kernel application, this method is supposed to be called only on the
   * kernel executor thread.
   * </p>
   *
   * @param refs References to paths that have changed in the routing topology. An empty set of
   * path references results in the router updating the entire routing topology.
   * @throws KernelRuntimeException In case there is an exception executing this method.
   */
  @ScheduledApiChange(when = "6.0", details = "Default implementation will be removed.")
  default void updateRoutingTopology(Set<TCSObjectReference<Path>> refs)
      throws KernelRuntimeException {
    updateRoutingTopology();
  }

  /**
   * Computes routes for the given vehicle from a source point to a set of destination points.
   * <p>
   * If called within the kernel application, this method is supposed to be called only on the
   * kernel executor thread.
   * </p>
   *
   * @param vehicleRef A reference to the vehicle to calculate the routes for.
   * @param sourcePointRef A reference to the source point.
   * @param destinationPointRefs A set of references to the destination points.
   * @return A map of destination points to the corresponding computed routes or {@code null}, if
   * no route could be determined for a specific destination point.
   * @throws KernelRuntimeException In case there is an exception executing this method.
   * @deprecated Use {@link #computeRoutes(org.opentcs.data.TCSObjectReference,
   * org.opentcs.data.TCSObjectReference, java.util.Set, java.util.Set)} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "6.0", details = "Will be removed.")
  Map<TCSObjectReference<Point>, Route> computeRoutes(
      TCSObjectReference<Vehicle> vehicleRef,
      TCSObjectReference<Point> sourcePointRef,
      Set<TCSObjectReference<Point>> destinationPointRefs)
      throws KernelRuntimeException;

  /**
   * Computes routes for the given vehicle from a source point to a set of destination points.
   * <p>
   * If called within the kernel application, this method is supposed to be called only on the
   * kernel executor thread.
   * </p>
   *
   * @param vehicleRef A reference to the vehicle to calculate the routes for.
   * @param sourcePointRef A reference to the source point.
   * @param destinationPointRefs A set of references to the destination points.
   * @param resourcesToAvoid A set of references to resources that are to be avoided.
   * @return A map of destination points to the corresponding computed routes or {@code null}, if
   * no route could be determined for a specific destination point.
   * @throws KernelRuntimeException In case there is an exception executing this method.
   */
  Map<TCSObjectReference<Point>, Route> computeRoutes(
      TCSObjectReference<Vehicle> vehicleRef,
      TCSObjectReference<Point> sourcePointRef,
      Set<TCSObjectReference<Point>> destinationPointRefs,
      Set<TCSResourceReference<?>> resourcesToAvoid)
      throws KernelRuntimeException;
}
