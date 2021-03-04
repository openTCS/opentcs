/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.rmi.services;

import java.rmi.RemoteException;
import java.util.Set;
import java.util.function.Predicate;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectReference;

/**
 * The default implementation of the tcs object service.
 * Delegates method invocations to the corresponding remote service.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 * @param <R> The remote service's type.
 */
abstract class RemoteTCSObjectServiceProxy<R extends RemoteTCSObjectService>
    extends AbstractRemoteServiceProxy<R>
    implements TCSObjectService {

  @Override
  public <T extends TCSObject<T>> T fetchObject(Class<T> clazz, TCSObjectReference<T> ref)
      throws KernelRuntimeException {
    checkServiceAvailability();

    try {
      return getRemoteService().fetchObject(getClientId(), clazz, ref);
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }

  @Override
  public <T extends TCSObject<T>> T fetchObject(Class<T> clazz, String name)
      throws KernelRuntimeException {
    checkServiceAvailability();

    try {
      return getRemoteService().fetchObject(getClientId(), clazz, name);
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }

  @Override
  public <T extends TCSObject<T>> Set<T> fetchObjects(Class<T> clazz)
      throws KernelRuntimeException {
    checkServiceAvailability();

    try {
      return getRemoteService().fetchObjects(getClientId(), clazz);
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }

  @Override
  public <T extends TCSObject<T>> Set<T> fetchObjects(Class<T> clazz,
                                                      Predicate<? super T> predicate)
      throws KernelRuntimeException {
    checkServiceAvailability();

    try {
      return getRemoteService().fetchObjects(getClientId(), clazz, predicate);
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }

  @Override
  public void updateObjectProperty(
      TCSObjectReference<?> ref, String key, String value)
      throws ObjectUnknownException, KernelRuntimeException {
    checkServiceAvailability();

    try {
      getRemoteService().updateObjectProperty(getClientId(), ref, key, value);
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }
}
