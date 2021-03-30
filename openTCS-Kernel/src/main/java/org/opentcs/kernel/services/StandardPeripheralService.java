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
import org.opentcs.components.kernel.services.InternalPeripheralService;
import org.opentcs.components.kernel.services.PeripheralService;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.customizations.kernel.GlobalSyncObject;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.PeripheralInformation;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.data.peripherals.PeripheralJob;
import org.opentcs.drivers.peripherals.PeripheralAdapterCommand;
import org.opentcs.drivers.peripherals.PeripheralCommAdapterDescription;
import org.opentcs.drivers.peripherals.PeripheralProcessModel;
import org.opentcs.drivers.peripherals.management.PeripheralAttachmentInformation;
import org.opentcs.kernel.peripherals.PeripheralAttachmentManager;
import org.opentcs.kernel.peripherals.PeripheralEntry;
import org.opentcs.kernel.peripherals.PeripheralEntryPool;
import org.opentcs.kernel.workingset.Model;

/**
 * This class is the standard implementation of the {@link PeripheralService} interface.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class StandardPeripheralService
    extends AbstractTCSObjectService
    implements InternalPeripheralService {

  /**
   * A global object to be used for synchronization within the kernel.
   */
  private final Object globalSyncObject;
  /**
   * The attachment manager.
   */
  private final PeripheralAttachmentManager attachmentManager;
  /**
   * The pool of peripheral entries.
   */
  private final PeripheralEntryPool peripheralEntryPool;
  /**
   * The model facade to the object pool.
   */
  private final Model model;

  /**
   * Creates a new instance.
   *
   * @param objectService The tcs object service.
   * @param globalSyncObject The kernel threads' global synchronization object.
   * @param attachmentManager The attachment manager.
   * @param peripheralEntryPool The pool of peripheral entries.
   * @param model The model to be used.
   */
  @Inject
  public StandardPeripheralService(TCSObjectService objectService,
                                   @GlobalSyncObject Object globalSyncObject,
                                   PeripheralAttachmentManager attachmentManager,
                                   PeripheralEntryPool peripheralEntryPool,
                                   Model model) {
    super(objectService);
    this.globalSyncObject = requireNonNull(globalSyncObject, "globalSyncObject");
    this.attachmentManager = requireNonNull(attachmentManager, "attachmentManager");
    this.peripheralEntryPool = requireNonNull(peripheralEntryPool, "peripheralEntryPool");
    this.model = requireNonNull(model, "model");
  }

  @Override
  public void attachCommAdapter(TCSResourceReference<Location> ref,
                                PeripheralCommAdapterDescription description)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      attachmentManager.attachAdapterToLocation(ref, description);
    }
  }

  @Override
  public void disableCommAdapter(TCSResourceReference<Location> ref)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      peripheralEntryPool.getEntryFor(ref).getCommAdapter().disable();
    }
  }

  @Override
  public void enableCommAdapter(TCSResourceReference<Location> ref)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      peripheralEntryPool.getEntryFor(ref).getCommAdapter().enable();
    }
  }

  @Override
  public PeripheralAttachmentInformation fetchAttachmentInformation(
      TCSResourceReference<Location> ref)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      return attachmentManager.getAttachmentInformation(ref);
    }
  }

  @Override
  public PeripheralProcessModel fetchProcessModel(TCSResourceReference<Location> ref)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      return peripheralEntryPool.getEntryFor(ref).getCommAdapter().getProcessModel();
    }
  }

  @Override
  public void sendCommAdapterCommand(TCSResourceReference<Location> ref,
                                     PeripheralAdapterCommand command)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      PeripheralEntry entry = peripheralEntryPool.getEntryFor(ref);
      synchronized (entry.getCommAdapter()) {
        entry.getCommAdapter().execute(command);
      }
    }
  }

  @Override
  public void updatePeripheralProcState(TCSResourceReference<Location> ref,
                                        PeripheralInformation.ProcState state)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      model.setLocationProcState(ref, state);
    }
  }

  @Override
  public void updatePeripheralReservationToken(TCSResourceReference<Location> ref,
                                               String reservationToken)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      model.setLocationReservationToken(ref, reservationToken);
    }
  }

  @Override
  public void updatePeripheralState(TCSResourceReference<Location> ref,
                                    PeripheralInformation.State state)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      model.setLocationState(ref, state);
    }
  }

  @Override
  public void updatePeripheralJob(TCSResourceReference<Location> ref,
                                  TCSObjectReference<PeripheralJob> peripheralJob)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      model.setLocationPeripheralJob(ref, peripheralJob);
    }
  }
}
