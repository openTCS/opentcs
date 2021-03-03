/*
 * openTCS copyright information:
 * Copyright (c) 2008 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.drivers;

/**
 * A callback interface for implementing views on the communication adapter's
 * state.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface CommunicationAdapterView {
  /**
   * Called when the state of the controller, i.e. the communication adapter,
   * has changed and the view might need to be updated.
   */
  void update();
}
