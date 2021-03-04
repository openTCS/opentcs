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
import java.util.Set;
import java.util.function.Predicate;
import org.opentcs.access.rmi.ClientID;
import org.opentcs.components.kernel.services.TCSObjectService;
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
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface RemoteTCSObjectService
    extends Remote {

  <T extends TCSObject<T>> T fetchObject(ClientID clientId,
                                         Class<T> clazz,
                                         TCSObjectReference<T> ref)
      throws RemoteException;

  <T extends TCSObject<T>> T fetchObject(ClientID clientId, Class<T> clazz, String name)
      throws RemoteException;

  <T extends TCSObject<T>> Set<T> fetchObjects(ClientID clientId, Class<T> clazz)
      throws RemoteException;

  <T extends TCSObject<T>> Set<T> fetchObjects(ClientID clientId,
                                               Class<T> clazz,
                                               Predicate<? super T> predicate)
      throws RemoteException;

  void updateObjectProperty(ClientID clientId,
                            TCSObjectReference<?> ref,
                            String key,
                            String value)
      throws RemoteException;
}
