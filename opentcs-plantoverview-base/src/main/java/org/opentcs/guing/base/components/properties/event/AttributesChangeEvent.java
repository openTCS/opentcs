// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.base.components.properties.event;

import java.util.EventObject;
import org.opentcs.guing.base.model.ModelComponent;

/**
 * An event that notifies all {@link AttributesChangeListener}s that a model component has been
 * changed.
 */
public class AttributesChangeEvent
    extends
      EventObject {

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
