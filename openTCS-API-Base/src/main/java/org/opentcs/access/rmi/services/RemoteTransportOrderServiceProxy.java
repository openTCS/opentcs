/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.rmi.services;

import java.rmi.RemoteException;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.to.order.OrderSequenceCreationTO;
import org.opentcs.access.to.order.TransportOrderCreationTO;
import org.opentcs.components.kernel.services.TransportOrderService;
import org.opentcs.data.ObjectExistsException;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.TransportOrder;

/**
 * The default implementation of the transport order service.
 * Delegates method invocations to the corresponding remote service.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
class RemoteTransportOrderServiceProxy
    extends RemoteTCSObjectServiceProxy<RemoteTransportOrderService>
    implements TransportOrderService {

  @Override
  public OrderSequence createOrderSequence(OrderSequenceCreationTO to)
      throws KernelRuntimeException {
    checkServiceAvailability();

    try {
      return getRemoteService().createOrderSequence(getClientId(), to);
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }

  @Override
  public TransportOrder createTransportOrder(TransportOrderCreationTO to)
      throws ObjectUnknownException, ObjectExistsException, KernelRuntimeException {
    checkServiceAvailability();

    try {
      return getRemoteService().createTransportOrder(getClientId(), to);
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }

  @Override
  public void markOrderSequenceComplete(TCSObjectReference<OrderSequence> ref)
      throws ObjectUnknownException, KernelRuntimeException {
    checkServiceAvailability();

    try {
      getRemoteService().markOrderSequenceComplete(getClientId(), ref);
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }
}
