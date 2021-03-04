/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.exchange.adapter;

import java.io.Serializable;
import javax.annotation.Nullable;
import org.opentcs.access.Kernel;
import org.opentcs.data.TCSObject;
import org.opentcs.data.model.visualization.ModelLayoutElement;
import org.opentcs.guing.model.ModelComponent;

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
   * Registers itself at the kernel and model object
   */
  void register();

  /**
   * Returns the model component.
   *
   * @return The model component.
   */
  ModelComponent getModel();

  /**
   * Reads the current properties from the kernel and adopts these for
   * the model object.
   *
   * @param kernel A reference to the kernel, in case the adapter needs to
   * access data that is not contained in the given kernel object.
   * @param tcsObject The kernel's object.
   * @param layoutElement A nullable layout element. If present, the process
   * adapter will update the model object's layout data, too.
   */
  void updateModelProperties(Kernel kernel,
                             TCSObject<?> tcsObject,
                             @Nullable ModelLayoutElement layoutElement);

  /**
   * Reads the current properties from the model and adopts these for the
   * kernel object.
   *
   * @param plantModel The transfer object describing a plant model
   */
  void storeToPlantModel(org.opentcs.access.to.model.PlantModelCreationTO plantModel);
}
