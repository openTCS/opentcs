/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.base.components.properties.event;

import java.util.EventObject;
import org.opentcs.guing.base.model.ModelComponent;

/**
 * An event that notifies all {@link AttributesChangeListener}s that a model component has been
 * changed.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class AttributesChangeEvent
    extends EventObject {

  /**
   * The model.
   */
  protected ModelComponent fModelComponent;

  /**
   * Creates a new instance.
   *
   * @param listener The listener.
   * @param model The model component.
   */
  public AttributesChangeEvent(AttributesChangeListener listener, ModelComponent model) {
    super(listener);
    fModelComponent = model;
  }

  /**
   * @return the model.
   */
  public ModelComponent getModel() {
    return fModelComponent;
  }

  /**
   * @return the initiator.
   */
  public AttributesChangeListener getInitiator() {
    return (AttributesChangeListener) getSource();
  }
}
