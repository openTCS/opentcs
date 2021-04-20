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
import org.opentcs.access.to.peripherals.PeripheralJobCreationTO;
import org.opentcs.components.kernel.services.PeripheralJobService;
import org.opentcs.data.peripherals.PeripheralJob;

/**
 * Declares the methods provided by the {@link PeripheralJobService} via RMI.
 *
 * <p>
 * The majority of the methods declared here have signatures analogous to their counterparts in
 * {@link PeripheralJobService}, with an additional {@link ClientID} parameter which serves the
 * purpose of identifying the calling client and determining its permissions.
 * </p>
 * <p>
 * To avoid redundancy, the semantics of methods that only pass through their arguments are not
 * explicitly documented here again. See the corresponding API documentation in
 * {@link PeripheralJobService} for these, instead.
 * </p>
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface RemotePeripheralJobService
    extends RemoteTCSObjectService,
            Remote {

  // CHECKSTYLE:OFF
  PeripheralJob createPeripheralJob(ClientID clientId, PeripheralJobCreationTO to)
      throws RemoteException;
  // CHECKSTYLE:ON
}
