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
import org.opentcs.access.rmi.ClientID;
import org.opentcs.components.kernel.services.DispatcherService;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.ReroutingType;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * Declares the methods provided by the {@link DispatcherService} via RMI.
 *
 * <p>
 * The majority of the methods declared here have signatures analogous to their counterparts in
 * {@link DispatcherService}, with an additional {@link ClientID} parameter which serves the purpose
 * of identifying the calling client and determining its permissions.
 * </p>
 * <p>
 * To avoid redundancy, the semantics of methods that only pass through their arguments are not
 * explicitly documented here again. See the corresponding API documentation in
 * {@link DispatcherService} for these, instead.
 * </p>
 */
public interface RemoteDispatcherService
    extends Remote {

  // CHECKSTYLE:OFF
  void dispatch(ClientID clientId)
      throws RemoteException;

  void withdrawByVehicle(ClientID clientId,
                         TCSObjectReference<Vehicle> ref,
                         boolean immediateAbort)
      throws RemoteException;

  void withdrawByTransportOrder(ClientID clientId,
                                TCSObjectReference<TransportOrder> ref,
                                boolean immediateAbort)
      throws RemoteException;

  @ScheduledApiChange(when = "6.0", details = "Default implementation will be removed.")
  default void reroute(ClientID clientId,
                       TCSObjectReference<Vehicle> ref,
                       ReroutingType reroutingType)
      throws RemoteException {
  }

  @ScheduledApiChange(when = "6.0", details = "Default implementation will be removed.")
  default void rerouteAll(ClientID clientId,
                          ReroutingType reroutingType)
      throws RemoteException {
  }

  @ScheduledApiChange(when = "6.0", details = "Default implementation will be removed.")
  default void assignNow(ClientID clientId, TCSObjectReference<TransportOrder> ref)
      throws RemoteException {
  }
  // CHECKSTYLE:ON
}
