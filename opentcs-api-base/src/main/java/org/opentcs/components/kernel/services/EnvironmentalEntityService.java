// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.components.kernel.services;

import org.opentcs.access.to.model.EnvironmentalEntityCreationTO;
import org.opentcs.data.ObjectExistsException;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Envelope;
import org.opentcs.data.model.EnvironmentalEntity;
import org.opentcs.data.model.Pose;

/**
 * Provides functionality related to {@link EnvironmentalEntity}s.
 */
public interface EnvironmentalEntityService
    extends
      TCSObjectService {

  /**
   * Creates a new environmental entity.
   *
   * @param to Describes the entity to be created.
   * @return The newly created environmental entity.
   * @throws ObjectExistsException If an object with the same name already exists in the model.
   */
  EnvironmentalEntity createEnvironmentalEntity(EnvironmentalEntityCreationTO to)
      throws ObjectExistsException;

  /**
   * Updates an environmental entity's envelope.
   *
   * @param ref A reference to the entity to be modified.
   * @param envelope The new envelope of the entity.
   * @throws ObjectUnknownException If the referenced entity does not exist.
   */
  void updateEnvironmentalEntityEnvelope(
      TCSObjectReference<EnvironmentalEntity> ref,
      Envelope envelope
  )
      throws ObjectUnknownException;

  /**
   * Updates an environmental entity's pose.
   *
   * @param ref A reference to the entity to be modified.
   * @param pose The new pose of the entity.
   * @throws ObjectUnknownException If the referenced entity does not exist.
   */
  void updateEnvironmentalEntityPose(
      TCSObjectReference<EnvironmentalEntity> ref,
      Pose pose
  )
      throws ObjectUnknownException;

  /**
   * Updates an environmental entity's integration level.
   *
   * @param ref A reference to the entity to be modified.
   * @param integrationLevel The new integration level of the entity.
   * @throws ObjectUnknownException If the referenced entity does not exist.
   */
  void updateEnvironmentalEntityIntegrationLevel(
      TCSObjectReference<EnvironmentalEntity> ref,
      EnvironmentalEntity.IntegrationLevel integrationLevel
  )
      throws ObjectUnknownException;

  /**
   * Marks an environmental entity as retired.
   *
   * @param ref A reference to the entity to be removed.
   * @throws ObjectUnknownException If the referenced entity does not exist.
   */
  void markEnvironmentalEntityRetired(TCSObjectReference<EnvironmentalEntity> ref)
      throws ObjectUnknownException;
}
