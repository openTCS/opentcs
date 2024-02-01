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
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.PlantModel;
import org.opentcs.util.annotations.ScheduledApiChange;

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
 */
public interface RemotePlantModelService
    extends RemoteTCSObjectService,
            Remote {

  // CHECKSTYLE:OFF
  PlantModel getPlantModel(ClientID clientId)
      throws RemoteException;

  void createPlantModel(ClientID clientId, PlantModelCreationTO to)
      throws RemoteException;

  String getModelName(ClientID clientId)
      throws RemoteException;

  Map<String, String> getModelProperties(ClientID clientId)
      throws RemoteException;

  void updateLocationLock(ClientID clientId, TCSObjectReference<Location> ref, boolean locked)
      throws RemoteException;

  @ScheduledApiChange(when = "6.0", details = "Default implementation will be removed.")
  default void updatePathLock(ClientID clientId, TCSObjectReference<Path> ref, boolean locked)
      throws RemoteException {
  }
  // CHECKSTYLE:ON
}
