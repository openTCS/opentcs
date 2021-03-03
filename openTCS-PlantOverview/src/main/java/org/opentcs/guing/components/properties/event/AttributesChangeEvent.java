/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */

package org.opentcs.guing.components.properties.event;

import java.util.EventObject;
import org.opentcs.guing.model.ModelComponent;

/**
 * Erweiterung von PropertiesModelChangeEvent um das Objekt, dessen Properties
 * geändert wurden. Wichtig für eine Klasse, die sich bei mehreren
 * PropertiesModel-Objekten als Listener registriert.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class AttributesChangeEvent
    extends EventObject {

  /**
   * Das Model.
   */
  protected ModelComponent fModelComponent;

  /**
   * Creates a new instance of ModelComponentChangeEvent
   *
   * @param listener
   * @param model
   */
  public AttributesChangeEvent(AttributesChangeListener listener, ModelComponent model) {
    super(listener);
    fModelComponent = model;
  }

  /**
   * @return Das Modell.
   */
  public ModelComponent getModel() {
    return fModelComponent;
  }

  /**
   * @return Der Verursacher.
   */
  public AttributesChangeListener getInitiator() {
    return (AttributesChangeListener) getSource();
  }
}
