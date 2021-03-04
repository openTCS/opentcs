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
import org.opentcs.data.order.TransportOrder;

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
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface RemoteDispatcherService
    extends Remote {

  void dispatch(ClientID clientId)
      throws RemoteException;

  @Deprecated
  void releaseVehicle(ClientID clientId, TCSObjectReference<Vehicle> ref)
      throws RemoteException;

  @Deprecated
  void withdrawByVehicle(ClientID clientId,
                         TCSObjectReference<Vehicle> ref,
                         boolean immediateAbort,
                         boolean disableVehicle)
      throws RemoteException;

  @Deprecated
  void withdrawByTransportOrder(ClientID clientId,
                                TCSObjectReference<TransportOrder> ref,
                                boolean immediateAbort,
                                boolean disableVehicle)
      throws RemoteException;

  void withdrawByVehicle(ClientID clientId,
                         TCSObjectReference<Vehicle> ref,
                         boolean immediateAbort)
      throws RemoteException;

  void withdrawByTransportOrder(ClientID clientId,
                                TCSObjectReference<TransportOrder> ref,
                                boolean immediateAbort)
      throws RemoteException;
}
