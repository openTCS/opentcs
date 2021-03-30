/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.rmi.services;

import java.rmi.RemoteException;
import org.opentcs.components.kernel.Query;
import org.opentcs.components.kernel.services.QueryService;

/**
 * The default implementation of the query service.
 * Delegates method invocations to the corresponding remote service.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class RemoteQueryServiceProxy
    extends AbstractRemoteServiceProxy<RemoteQueryService>
    implements QueryService {

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
