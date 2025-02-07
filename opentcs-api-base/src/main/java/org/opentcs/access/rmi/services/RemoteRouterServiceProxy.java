// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.access.rmi.services;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.Set;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.components.kernel.services.RouterService;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.Route;

/**
 * The default implementation of the router service.
 * Delegates method invocations to the corresponding remote service.
 */
class RemoteRouterServiceProxy
    extends
      AbstractRemoteServiceProxy<RemoteRouterService>
    implements
      RouterService {

  /**
   * Creates a new instance.
   */
  RemoteRouterServiceProxy() {
  }

  @Override
  public void updateRoutingTopology(Set<TCSObjectReference<Path>> refs)
      throws KernelRuntimeException {
    checkServiceAvailability();

    try {
      getRemoteService().updateRoutingTopology(getClientId(), refs);
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }

  @Override
  @Deprecated
  public Map<TCSObjectReference<Point>, Route> computeRoutes(
      TCSObjectReference<Vehicle> vehicleRef,
      TCSObjectReference<Point> sourcePointRef,
      Set<TCSObjectReference<Point>> destinationPointRefs,
      Set<TCSResourceReference<?>> resourcesToAvoid
  )
      throws KernelRuntimeException {
    checkServiceAvailability();

    try {
      return getRemoteService().computeRoutes(
          getClientId(),
          vehicleRef,
          sourcePointRef,
          destinationPointRefs,
          resourcesToAvoid
      );
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
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
    checkServiceAvailability();

    try {
      return getRemoteService().computeRoutes(
          getClientId(),
          vehicleRef,
          sourcePointRef,
          destinationPointRefs,
          resourcesToAvoid,
          maxRoutesPerDestinationPoint
      );
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }
}
