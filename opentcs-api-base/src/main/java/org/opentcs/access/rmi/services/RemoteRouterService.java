// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
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
    extends
      Remote {

  // CHECKSTYLE:OFF
  public void updateRoutingTopology(ClientID clientId, Set<TCSObjectReference<Path>> refs)
      throws RemoteException;

  Map<TCSObjectReference<Point>, Set<Route>> computeRoutes(
      ClientID clientId,
      TCSObjectReference<Vehicle> vehicleRef,
      TCSObjectReference<Point> sourcePointRef,
      Set<TCSObjectReference<Point>> destinationPointRefs,
      Set<TCSResourceReference<?>> resourcesToAvoid,
      int maxRoutesPerDestinationPoint
  )
      throws RemoteException;
  // CHECKSTYLE:ON
}
