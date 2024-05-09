/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.rmi.services;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.Set;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.components.kernel.services.RouterService;
import org.opentcs.data.ObjectUnknownException;
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
    extends AbstractRemoteServiceProxy<RemoteRouterService>
    implements RouterService {

  /**
   * Creates a new instance.
   */
  RemoteRouterServiceProxy() {
  }

  @Override
  @Deprecated
  public void updatePathLock(TCSObjectReference<Path> ref, boolean locked)
      throws ObjectUnknownException, KernelRuntimeException {
    checkServiceAvailability();

    try {
      getRemoteService().updatePathLock(getClientId(), ref, locked);
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }

  @Override
  @Deprecated
  public void updateRoutingTopology()
      throws KernelRuntimeException {
    checkServiceAvailability();

    try {
      getRemoteService().updateRoutingTopology(getClientId());
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
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

  @Deprecated
  @Override
  public Map<TCSObjectReference<Point>, Route> computeRoutes(
      TCSObjectReference<Vehicle> vehicleRef,
      TCSObjectReference<Point> sourcePointRef,
      Set<TCSObjectReference<Point>> destinationPointRefs)
      throws KernelRuntimeException {
    checkServiceAvailability();

    try {
      return getRemoteService().computeRoutes(getClientId(),
                                              vehicleRef,
                                              sourcePointRef,
                                              destinationPointRefs);
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }
  
  @Override
  public Map<TCSObjectReference<Point>, Route> computeRoutes(
      TCSObjectReference<Vehicle> vehicleRef,
      TCSObjectReference<Point> sourcePointRef,
      Set<TCSObjectReference<Point>> destinationPointRefs,
      Set<TCSResourceReference<?>> resourcesToAvoid)
      throws KernelRuntimeException {
    checkServiceAvailability();

    try {
      return getRemoteService().computeRoutes(getClientId(),
                                              vehicleRef,
                                              sourcePointRef,
                                              destinationPointRefs,
                                              resourcesToAvoid);
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }
}
