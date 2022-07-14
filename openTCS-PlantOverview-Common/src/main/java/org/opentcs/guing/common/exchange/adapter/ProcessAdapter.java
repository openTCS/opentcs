/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.exchange.adapter;

import java.io.Serializable;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.TCSObject;
import org.opentcs.guing.base.model.ModelComponent;
import org.opentcs.guing.common.model.SystemModel;

/**
 * Receives messages from a <code>ModelComponent</code> and its kernel
 * equivalent and delegates them to the respective other one.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface ProcessAdapter
    extends Serializable {

  /**
   * Reads the current properties from the kernel and adopts these for the model object.
   *
   * @param objectService A reference to the object service, in case the adapter needs to access
   * data that is not contained in the given kernel object.
   * @param modelComponent The model component to adopt the properties for.
   * @param systemModel A reference to the system model, in case the adapter needs to access
   * other model components as well.
   * @param tcsObject The kernel's object.
   */
  void updateModelProperties(TCSObject<?> tcsObject,
                             ModelComponent modelComponent,
                             SystemModel systemModel,
                             TCSObjectService objectService);

  /**
   * Reads the current properties from the model component and adopts these for the kernel object.
   *
   * @param modelComponent The model component to read properties from.
   * @param systemModel A reference to the system model, in case the adapter needs to access
   * other model components as well.
   * @param plantModel A transfer object describing the current plant model data.
   * @return A new transfer object, describing the plant model data with the model component's data
   * merged.
   */
  PlantModelCreationTO storeToPlantModel(ModelComponent modelComponent,
                                         SystemModel systemModel,
                                         PlantModelCreationTO plantModel);
}
