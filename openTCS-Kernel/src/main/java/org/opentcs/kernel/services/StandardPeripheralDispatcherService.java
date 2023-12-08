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
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.components.kernel.PeripheralJobDispatcher;
import org.opentcs.components.kernel.services.PeripheralDispatcherService;
import org.opentcs.customizations.kernel.GlobalSyncObject;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.data.peripherals.PeripheralJob;
import org.opentcs.kernel.workingset.TCSObjectRepository;

/**
 * This class is the standard implementation of the {@link PeripheralDispatcherService} interface.
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
  private final TCSObjectRepository objectRepo;
  /**
   * The peripheral job dispatcher.
   */
  private final PeripheralJobDispatcher dispatcher;

  /**
   * Creates a new instance.
   *
   * @param globalSyncObject The kernel threads' global synchronization object.
   * @param objectRepo The object repo to be used.
   * @param dispatcher The peripheral job dispatcher.
   */
  @Inject
  public StandardPeripheralDispatcherService(@GlobalSyncObject Object globalSyncObject,
                                             TCSObjectRepository objectRepo,
                                             PeripheralJobDispatcher dispatcher) {
    this.globalSyncObject = requireNonNull(globalSyncObject, "globalSyncObject");
    this.objectRepo = requireNonNull(objectRepo, "objectRepo");
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
    requireNonNull(ref, "ref");

    synchronized (globalSyncObject) {
      dispatcher.withdrawJob(objectRepo.getObject(Location.class, ref));
    }
  }

  @Override
  public void withdrawByPeripheralJob(TCSObjectReference<PeripheralJob> ref)
      throws ObjectUnknownException, KernelRuntimeException {
    requireNonNull(ref, "ref");

    synchronized (globalSyncObject) {
      dispatcher.withdrawJob(objectRepo.getObject(PeripheralJob.class, ref));
    }
  }
}
