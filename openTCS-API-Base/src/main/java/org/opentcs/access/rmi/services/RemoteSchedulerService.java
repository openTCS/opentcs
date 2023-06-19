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

/**
 * Declares the methods provided by the {@code SchedulerService} via RMI.
 *
 * <p>
 * The majority of the methods declared here have signatures analogous to their counterparts in
 * {@code SchedulerService}, with an additional {@link ClientID} parameter which serves the purpose
 * of identifying the calling client and determining its permissions.
 * </p>
 * <p>
 * To avoid redundancy, the semantics of methods that only pass through their arguments are not
 * explicitly documented here again. See the corresponding API documentation in
 * {@code SchedulerService} for these, instead.
 * </p>
 */
@Deprecated
public interface RemoteSchedulerService
    extends Remote {

  // CHECKSTYLE:OFF
  public org.opentcs.access.SchedulerAllocationState fetchSchedulerAllocations(ClientID clientId)
      throws RemoteException;
  // CHECKSTYLE:ON
}
