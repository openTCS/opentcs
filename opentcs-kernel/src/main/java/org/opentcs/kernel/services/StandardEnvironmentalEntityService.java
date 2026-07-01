// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.services;

import static java.util.Objects.requireNonNull;

import jakarta.inject.Inject;
import org.opentcs.access.to.model.EnvironmentalEntityCreationTO;
import org.opentcs.components.kernel.services.EnvironmentalEntityService;
import org.opentcs.components.kernel.services.InternalTCSObjectService;
import org.opentcs.customizations.kernel.GlobalSyncObject;
import org.opentcs.data.ObjectExistsException;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Envelope;
import org.opentcs.data.model.EnvironmentalEntity;
import org.opentcs.data.model.Pose;
import org.opentcs.kernel.workingset.PlantModelManager;
import org.opentcs.kernel.workingset.TCSObjectRepository;

/**
 * Standard implementation of the {@link EnvironmentalEntityService}.
 */
public class StandardEnvironmentalEntityService
    extends
      AbstractTCSObjectService
    implements
      EnvironmentalEntityService {

  /**
   * A global object to be used for synchronization within the kernel.
   */
  private final Object globalSyncObject;
  /**
   * The container of all course model and transport order objects.
   */
  private final TCSObjectRepository globalObjectPool;
  /**
   * The plant model manager.
   */
  private final PlantModelManager plantModelManager;

  /**
   * Creates a new instance.
   *
   * @param objectService The object service.
   * @param globalSyncObject The kernel threads' global synchronization object.
   * @param globalObjectPool The object pool to be used.
   * @param plantModelManager The plant model manager to be used.
   */
  @Inject
  public StandardEnvironmentalEntityService(
      InternalTCSObjectService objectService,
      @GlobalSyncObject
      Object globalSyncObject,
      TCSObjectRepository globalObjectPool,
      PlantModelManager plantModelManager
  ) {
    super(objectService);
    this.globalSyncObject = requireNonNull(globalSyncObject, "globalSyncObject");
    this.globalObjectPool = requireNonNull(globalObjectPool, "globalObjectPool");
    this.plantModelManager = requireNonNull(plantModelManager, "plantModelManager");
  }

  @Override
  public EnvironmentalEntity createEnvironmentalEntity(EnvironmentalEntityCreationTO to)
      throws ObjectExistsException {
    synchronized (globalSyncObject) {
      return plantModelManager.createEnvironmentalEntity(to);
    }
  }

  @Override
  public void updateEnvironmentalEntityEnvelope(
      TCSObjectReference<EnvironmentalEntity> ref,
      Envelope envelope
  )
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      plantModelManager.setEnvironmentalEntityEnvelope(ref, envelope);
    }
  }

  @Override
  public void updateEnvironmentalEntityPose(TCSObjectReference<EnvironmentalEntity> ref, Pose pose)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      plantModelManager.setEnvironmentalEntityPose(ref, pose);
    }
  }

  @Override
  public void updateEnvironmentalEntityIntegrationLevel(
      TCSObjectReference<EnvironmentalEntity> ref,
      EnvironmentalEntity.IntegrationLevel integrationLevel
  )
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      plantModelManager.setEnvironmentalEntityIntegrationLevel(ref, integrationLevel);
    }
  }

  @Override
  public void markEnvironmentalEntityRetired(TCSObjectReference<EnvironmentalEntity> ref)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      EnvironmentalEntity entity = globalObjectPool.getObject(EnvironmentalEntity.class, ref);
      if (entity.isRetired()) {
        return;
      }

      plantModelManager.setEnvironmentalEntityRetired(ref);
    }
  }
}
