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
import org.opentcs.access.rmi.ClientID;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.components.kernel.services.PlantModelService;

/**
 * Declares the methods provided by the {@link PlantModelService} via RMI.
 *
 * <p>
 * The majority of the methods declared here have signatures analogous to their counterparts in
 * {@link PlantModelService}, with an additional {@link ClientID} parameter which serves the purpose
 * of identifying the calling client and determining its permissions.
 * </p>
 * <p>
 * To avoid redundancy, the semantics of methods that only pass through their arguments are not
 * explicitly documented here again. See the corresponding API documentation in
 * {@link PlantModelService} for these, instead.
 * </p>
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface RemotePlantModelService
    extends RemoteTCSObjectService,
            Remote {

  void createPlantModel(ClientID clientId, PlantModelCreationTO to)
      throws RemoteException;

  @Deprecated
  String getLoadedModelName(ClientID clientId)
      throws RemoteException;

  String getModelName(ClientID clientId)
      throws RemoteException;

  Map<String, String> getModelProperties(ClientID clientId)
      throws RemoteException;

  @Deprecated
  String getPersistentModelName(ClientID clientId)
      throws RemoteException;
}
