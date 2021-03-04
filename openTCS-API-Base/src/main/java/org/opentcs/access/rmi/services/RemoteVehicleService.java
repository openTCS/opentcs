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
import java.util.Set;
import org.opentcs.access.rmi.ClientID;
import org.opentcs.components.kernel.services.VehicleService;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.AdapterCommand;
import org.opentcs.drivers.vehicle.VehicleCommAdapterDescription;
import org.opentcs.drivers.vehicle.management.AttachmentInformation;
import org.opentcs.drivers.vehicle.management.VehicleProcessModelTO;

/**
 * Declares the methods provided by the {@link VehicleService} via RMI.
 *
 * <p>
 * The majority of the methods declared here have signatures analogous to their counterparts in
 * {@link VehicleService}, with an additional {@link ClientID} parameter which serves the purpose
 * of identifying the calling client and determining its permissions.
 * </p>
 * <p>
 * To avoid redundancy, the semantics of methods that only pass through their arguments are not
 * explicitly documented here again. See the corresponding API documentation in
 * {@link VehicleService} for these, instead.
 * </p>
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface RemoteVehicleService
    extends RemoteTCSObjectService,
            Remote {

  void attachCommAdapter(ClientID clientId, TCSObjectReference<Vehicle> ref,
                         VehicleCommAdapterDescription description)
      throws RemoteException;

  void disableCommAdapter(ClientID clientId, TCSObjectReference<Vehicle> ref)
      throws RemoteException;

  void enableCommAdapter(ClientID clientId, TCSObjectReference<Vehicle> ref)
      throws RemoteException;

  AttachmentInformation fetchAttachmentInformation(ClientID clientId,
                                                   TCSObjectReference<Vehicle> ref)
      throws RemoteException;

  VehicleProcessModelTO fetchProcessModel(ClientID clientId, TCSObjectReference<Vehicle> ref)
      throws RemoteException;

  void sendCommAdapterCommand(ClientID clientId,
                              TCSObjectReference<Vehicle> ref,
                              AdapterCommand command)
      throws RemoteException;

  void sendCommAdapterMessage(ClientID clientId,
                              TCSObjectReference<Vehicle> vehicleRef,
                              Object message)
      throws RemoteException;

  void updateVehicleIntegrationLevel(ClientID clientId,
                                     TCSObjectReference<Vehicle> ref,
                                     Vehicle.IntegrationLevel integrationLevel)
      throws RemoteException;

  void updateVehicleProcessableCategories(ClientID clientId,
                                          TCSObjectReference<Vehicle> ref,
                                          Set<String> processableCategories)
      throws RemoteException;
}
