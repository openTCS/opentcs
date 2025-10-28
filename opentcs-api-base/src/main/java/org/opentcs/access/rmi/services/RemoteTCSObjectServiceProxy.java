// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.access.rmi.services;

import java.rmi.RemoteException;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.ObjectHistory;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectReference;

/**
 * The default implementation of the tcs object service.
 * Delegates method invocations to the corresponding remote service.
 *
 * @param <R> The remote service's type.
 */
abstract class RemoteTCSObjectServiceProxy<R extends RemoteTCSObjectService>
    extends
      AbstractRemoteServiceProxy<R>
    implements
      TCSObjectService {

  @Override
  public <T extends TCSObject<T>> Optional<T> fetch(Class<T> clazz, TCSObjectReference<T> ref)
      throws KernelRuntimeException {
    checkServiceAvailability();

    try {
      return Optional.ofNullable(getRemoteService().fetch(getClientId(), clazz, ref));
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }

  @Override
  public <T extends TCSObject<T>> Optional<T> fetch(Class<T> clazz, String name)
      throws KernelRuntimeException {
    checkServiceAvailability();

    try {
      return Optional.ofNullable(getRemoteService().fetch(getClientId(), clazz, name));
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }

  @Override
  public <T extends TCSObject<T>> Set<T> fetch(Class<T> clazz)
      throws KernelRuntimeException {
    checkServiceAvailability();

    try {
      return getRemoteService().fetch(getClientId(), clazz);
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }

  @Override
  public <T extends TCSObject<T>> Set<T> fetch(
      Class<T> clazz,
      Predicate<? super T> predicate
  )
      throws KernelRuntimeException {
    checkServiceAvailability();

    try {
      return getRemoteService().fetch(getClientId(), clazz, predicate);
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }

  @Override
  public void updateObjectProperty(TCSObjectReference<?> ref, String key, String value)
      throws ObjectUnknownException,
        KernelRuntimeException {
    checkServiceAvailability();

    try {
      getRemoteService().updateObjectProperty(getClientId(), ref, key, value);
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }

  @Override
  public void appendObjectHistoryEntry(TCSObjectReference<?> ref, ObjectHistory.Entry entry)
      throws ObjectUnknownException,
        KernelRuntimeException {
    checkServiceAvailability();

    try {
      getRemoteService().appendObjectHistoryEntry(getClientId(), ref, entry);
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }

}
