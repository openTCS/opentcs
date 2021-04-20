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
import org.opentcs.components.kernel.services.PeripheralService;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.drivers.peripherals.PeripheralAdapterCommand;
import org.opentcs.drivers.peripherals.PeripheralCommAdapterDescription;
import org.opentcs.drivers.peripherals.PeripheralProcessModel;
import org.opentcs.drivers.peripherals.management.PeripheralAttachmentInformation;

/**
 * Declares the methods provided by the {@link PeripheralService} via RMI.
 *
 * <p>
 * The majority of the methods declared here have signatures analogous to their counterparts in
 * {@link PeripheralService}, with an additional {@link ClientID} parameter which serves the purpose
 * of identifying the calling client and determining its permissions.
 * </p>
 * <p>
 * To avoid redundancy, the semantics of methods that only pass through their arguments are not
 * explicitly documented here again. See the corresponding API documentation in
 * {@link PeripheralService} for these, instead.
 * </p>
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface RemotePeripheralService
    extends RemoteTCSObjectService,
            Remote {

  // CHECKSTYLE:OFF
  void attachCommAdapter(ClientID clientId,
                         TCSResourceReference<Location> ref,
                         PeripheralCommAdapterDescription description)
      throws RemoteException;

  void disableCommAdapter(ClientID clientId, TCSResourceReference<Location> ref)
      throws RemoteException;

  void enableCommAdapter(ClientID clientId, TCSResourceReference<Location> ref)
      throws RemoteException;

  PeripheralAttachmentInformation fetchAttachmentInformation(ClientID clientId,
                                                             TCSResourceReference<Location> ref)
      throws RemoteException;

  PeripheralProcessModel fetchProcessModel(ClientID clientId, TCSResourceReference<Location> ref)
      throws RemoteException;

  void sendCommAdapterCommand(ClientID clientId,
                              TCSResourceReference<Location> ref,
                              PeripheralAdapterCommand command)
      throws RemoteException;
  // CHECKSTYLE:ON
}
