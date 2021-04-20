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
import org.opentcs.components.kernel.services.PeripheralDispatcherService;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.TCSResourceReference;

/**
 * Declares the methods provided by the {@link PeripheralDispatcherService} via RMI.
 *
 * <p>
 * The majority of the methods declared here have signatures analogous to their counterparts in
 * {@link PeripheralDispatcherService}, with an additional {@link ClientID} parameter which serves
 * the purpose of identifying the calling client and determining its permissions.
 * </p>
 * <p>
 * To avoid redundancy, the semantics of methods that only pass through their arguments are not
 * explicitly documented here again. See the corresponding API documentation in
 * {@link PeripheralDispatcherService} for these, instead.
 * </p>
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface RemotePeripheralDispatcherService
    extends Remote {

  // CHECKSTYLE:OFF
  void dispatch(ClientID clientId)
      throws RemoteException;

  void withdrawByLocation(ClientID clientId, TCSResourceReference<Location> ref)
      throws RemoteException;
  // CHECKSTYLE:ON
}
