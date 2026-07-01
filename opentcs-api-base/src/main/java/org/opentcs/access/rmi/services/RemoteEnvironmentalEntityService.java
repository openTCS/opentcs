// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.access.rmi.services;

import java.rmi.Remote;
import java.rmi.RemoteException;
import org.opentcs.access.rmi.ClientID;
import org.opentcs.access.to.model.EnvironmentalEntityCreationTO;
import org.opentcs.components.kernel.services.EnvironmentalEntityService;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Envelope;
import org.opentcs.data.model.EnvironmentalEntity;
import org.opentcs.data.model.Pose;

/**
 * Declares the methods provided by the {@link EnvironmentalEntityService} via RMI.
 *
 * <p>
 * The majority of the methods declared here have signatures analogous to their counterparts in
 * {@link EnvironmentalEntityService}, with an additional {@link ClientID} parameter which serves
 * the purpose of identifying the calling client and determining its permissions.
 * </p>
 * <p>
 * To avoid redundancy, the semantics of methods that only pass through their arguments are not
 * explicitly documented here again. See the corresponding API documentation in
 * {@link EnvironmentalEntityService} for these, instead.
 * </p>
 */
public interface RemoteEnvironmentalEntityService
    extends
      RemoteTCSObjectService,
      Remote {

  // CHECKSTYLE:OFF
  EnvironmentalEntity createEnvironmentalEntity(ClientID clientId, EnvironmentalEntityCreationTO to)
      throws RemoteException;

  void updateEnvironmentalEntityEnvelope(
      ClientID clientId,
      TCSObjectReference<EnvironmentalEntity> ref,
      Envelope envelope
  )
      throws RemoteException;

  void updateEnvironmentalEntityPose(
      ClientID clientId,
      TCSObjectReference<EnvironmentalEntity> ref,
      Pose pose
  )
      throws RemoteException;

  void updateEnvironmentalEntityIntegrationLevel(
      ClientID clientId,
      TCSObjectReference<EnvironmentalEntity> ref,
      EnvironmentalEntity.IntegrationLevel integrationLevel
  )
      throws RemoteException;

  void markEnvironmentalEntityRetired(
      ClientID clientId,
      TCSObjectReference<EnvironmentalEntity> ref
  )
      throws RemoteException;
  // CHECKSTYLE:ON
}
