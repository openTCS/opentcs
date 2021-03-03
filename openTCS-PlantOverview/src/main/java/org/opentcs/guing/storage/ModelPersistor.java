/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.storage;

import java.io.IOException;
import org.opentcs.guing.model.ModelComponent;

/**
 * Interface for classes that persist <code>ModelComponents</code>.
 *
 * @author Tobias Marquardt (Fraunhofer IML)
 */
interface ModelPersistor {

  /**
   * Initialize the ModelPersistor.
   */
  public void init();

  /**
   * Persist a model component.
   *
   * @param component The component.
   */
  public void persist(ModelComponent component);

  /**
   * Finalize the persistence of the model components.
   * After calling this method, this <code>ModelPersistor</code> instance shall
   * not be re-used for persisting model components until <code>init()</code>
   * has been called again.
   */
  public void close() throws IOException;
}
