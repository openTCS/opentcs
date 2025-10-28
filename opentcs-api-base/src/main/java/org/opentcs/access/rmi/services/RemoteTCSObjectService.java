// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.access.rmi.services;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;
import java.util.function.Predicate;
import org.opentcs.access.rmi.ClientID;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.ObjectHistory;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectReference;

/**
 * Declares the methods provided by the {@link TCSObjectService} via RMI.
 *
 * <p>
 * The majority of the methods declared here have signatures analogous to their counterparts in
 * {@link TCSObjectService}, with an additional {@link ClientID} parameter which serves the purpose
 * of identifying the calling client and determining its permissions.
 * </p>
 * <p>
 * To avoid redundancy, the semantics of methods that only pass through their arguments are not
 * explicitly documented here again. See the corresponding API documentation in
 * {@link TCSObjectService} for these, instead.
 * </p>
 */
public interface RemoteTCSObjectService
    extends
      Remote {

  // CHECKSTYLE:OFF
  <T extends TCSObject<T>> T fetch(
      ClientID clientId,
      Class<T> clazz,
      TCSObjectReference<T> ref
  )
      throws RemoteException;

  <T extends TCSObject<T>> T fetch(ClientID clientId, Class<T> clazz, String name)
      throws RemoteException;

  <T extends TCSObject<T>> Set<T> fetch(ClientID clientId, Class<T> clazz)
      throws RemoteException;

  <T extends TCSObject<T>> Set<T> fetch(
      ClientID clientId,
      Class<T> clazz,
      Predicate<? super T> predicate
  )
      throws RemoteException;

  void updateObjectProperty(
      ClientID clientId,
      TCSObjectReference<?> ref,
      String key,
      String value
  )
      throws RemoteException;

  void appendObjectHistoryEntry(
      ClientID clientId,
      TCSObjectReference<?> ref,
      ObjectHistory.Entry entry
  )
      throws RemoteException;
  // CHECKSTYLE:ON
}
