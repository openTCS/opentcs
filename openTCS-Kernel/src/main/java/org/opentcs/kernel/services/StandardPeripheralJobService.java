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
import org.opentcs.access.to.peripherals.PeripheralJobCreationTO;
import org.opentcs.components.kernel.services.InternalPeripheralJobService;
import org.opentcs.components.kernel.services.PeripheralJobService;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.customizations.kernel.GlobalSyncObject;
import org.opentcs.data.ObjectExistsException;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.peripherals.PeripheralJob;
import org.opentcs.kernel.workingset.PeripheralJobPoolManager;

/**
 * This class is the standard implementation of the {@link PeripheralJobService} interface.
 */
public class StandardPeripheralJobService
    extends AbstractTCSObjectService
    implements InternalPeripheralJobService {

  /**
   * A global object to be used for synchronization within the kernel.
   */
  private final Object globalSyncObject;
  /**
   * The job pool manager.
   */
  private final PeripheralJobPoolManager jobPoolManager;

  /**
   * Creates a new instance.
   *
   * @param objectService The tcs obejct service.
   * @param globalSyncObject The kernel threads' global synchronization object.
   * @param jobPoolManager The job pool manager to be used.
   */
  @Inject
  public StandardPeripheralJobService(TCSObjectService objectService,
                                      @GlobalSyncObject Object globalSyncObject,
                                      PeripheralJobPoolManager jobPoolManager) {
    super(objectService);
    this.globalSyncObject = requireNonNull(globalSyncObject, "globalSyncObject");
    this.jobPoolManager = requireNonNull(jobPoolManager, "jobPoolManager");
  }

  @Override
  public void updatePeripheralJobState(TCSObjectReference<PeripheralJob> ref,
                                       PeripheralJob.State state)
      throws ObjectUnknownException {
    requireNonNull(ref, "ref");
    requireNonNull(state, "state");

    synchronized (globalSyncObject) {
      jobPoolManager.setPeripheralJobState(ref, state);
    }
  }

  @Override
  public PeripheralJob createPeripheralJob(PeripheralJobCreationTO to)
      throws ObjectUnknownException, ObjectExistsException, KernelRuntimeException {
    requireNonNull(to, "to");

    synchronized (globalSyncObject) {
      return jobPoolManager.createPeripheralJob(to);
    }
  }
}
