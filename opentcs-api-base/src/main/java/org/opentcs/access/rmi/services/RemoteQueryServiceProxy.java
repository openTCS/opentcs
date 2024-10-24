// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.access.rmi.services;

import java.rmi.RemoteException;
import org.opentcs.components.kernel.Query;
import org.opentcs.components.kernel.services.QueryService;

/**
 * The default implementation of the query service.
 * Delegates method invocations to the corresponding remote service.
 */
class RemoteQueryServiceProxy
    extends
      AbstractRemoteServiceProxy<RemoteQueryService>
    implements
      QueryService {

  /**
   * Creates a new instance.
   */
  RemoteQueryServiceProxy() {
  }

  @Override
  public <T> T query(Query<T> query) {
    checkServiceAvailability();

    try {
      return getRemoteService().query(getClientId(), query);
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }
}
