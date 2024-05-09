/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.rmi.services;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.Set;
import org.opentcs.access.rmi.ClientID;
import org.opentcs.components.kernel.services.RouterService;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.Route;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * Declares the methods provided by the {@link RouterService} via RMI.
 *
 * <p>
 * The majority of the methods declared here have signatures analogous to their counterparts in
 * {@link RouterService}, with an additional {@link ClientID} parameter which serves the purpose
 * of identifying the calling client and determining its permissions.
 * </p>
 * <p>
 * To avoid redundancy, the semantics of methods that only pass through their arguments are not
 * explicitly documented here again. See the corresponding API documentation in
 * {@link RouterService} for these, instead.
 * </p>
 */
public interface RemoteRouterService
    extends Remote {

  // CHECKSTYLE:OFF
  @Deprecated
  @ScheduledApiChange(when = "6.0", details = "Will be removed.")
  public void updatePathLock(ClientID clientId, TCSObjectReference<Path> ref, boolean locked)
      throws RemoteException;

  @Deprecated
  @ScheduledApiChange(when = "6.0", details = "Will be removed.")
  public void updateRoutingTopology(ClientID clientId)
      throws RemoteException;

  @ScheduledApiChange(when = "6.0", details = "Default implementation will be removed.")
  default public void updateRoutingTopology(ClientID clientId, Set<TCSObjectReference<Path>> refs)
      throws RemoteException {
    updateRoutingTopology(clientId);
  }

  @Deprecated
  public Map<TCSObjectReference<Point>, Route> computeRoutes(
      ClientID clientId,
      TCSObjectReference<Vehicle> vehicleRef,
      TCSObjectReference<Point> sourcePointRef,
      Set<TCSObjectReference<Point>> destinationPointRefs)
      throws RemoteException;

  public Map<TCSObjectReference<Point>, Route> computeRoutes(
      ClientID clientId,
      TCSObjectReference<Vehicle> vehicleRef,
      TCSObjectReference<Point> sourcePointRef,
      Set<TCSObjectReference<Point>> destinationPointRefs,
      Set<TCSResourceReference<?>> resourcesToAvoid)
      throws RemoteException;
  // CHECKSTYLE:ON
}
