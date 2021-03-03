/*
 * openTCS copyright information:
 * Copyright (c) 2007 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.controlcenter.vehicles;

import org.opentcs.drivers.Message;

/**
 * A <code>MessageFilter</code> filter the messages depending
 * on some specified criteria.
 *
 * @author Iryna Felko (Fraunhofer IML)
 */
interface MessageFilter {

  /**
   * Checks if the given message is acceptable for this filter.
   *
   * @param message The message to be checked.
   * @return Returns <code>true</code> if, and only if, the message is accepted.
   */
  boolean accept(Message message);
}
