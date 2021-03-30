/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.services;

import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.opentcs.components.kernel.PeripheralJobDispatcher;
import org.opentcs.components.kernel.services.PeripheralDispatcherService;
import org.opentcs.customizations.kernel.GlobalSyncObject;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.kernel.workingset.TCSObjectPool;

/**
 * This class is the standard implementation of the {@link PeripheralDispatcherService} interface.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class StandardPeripheralDispatcherService
    implements PeripheralDispatcherService {

  /**
   * A global object to be used for synchronization within the kernel.
   */
  private final Object globalSyncObject;
  /**
   * The container of all course model and transport order objects.
   */
  private final TCSObjectPool globalObjectPool;
  /**
   * The peripheral job dispatcher.
   */
  private final PeripheralJobDispatcher dispatcher;

  /**
   * Creates a new instance.
   *
   * @param globalSyncObject The kernel threads' global synchronization object.
   * @param globalObjectPool The object pool to be used.
   * @param dispatcher The peripheral job dispatcher.
   */
  @Inject
  public StandardPeripheralDispatcherService(@GlobalSyncObject Object globalSyncObject,
                                             TCSObjectPool globalObjectPool,
                                             PeripheralJobDispatcher dispatcher) {
    this.globalSyncObject = requireNonNull(globalSyncObject, "globalSyncObject");
    this.globalObjectPool = requireNonNull(globalObjectPool, "globalObjectPool");
    this.dispatcher = requireNonNull(dispatcher, "dispatcher");
  }

  @Override
  public void dispatch() {
    synchronized (globalSyncObject) {
      dispatcher.dispatch();
    }
  }

  @Override
  public void withdrawByLocation(TCSResourceReference<Location> ref)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      dispatcher.withdrawJob(globalObjectPool.getObject(Location.class, ref));
    }
  }
}
