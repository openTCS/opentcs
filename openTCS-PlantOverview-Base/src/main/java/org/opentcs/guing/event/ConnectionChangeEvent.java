/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.event;

import java.util.EventObject;
import org.opentcs.guing.model.elements.AbstractConnection;

/**
 * An event for changes on connections.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class ConnectionChangeEvent
    extends EventObject {

  /**
   * Creates a new instance of ConnectionChangeEvent.
   *
   * @param connection The connection that has changed.
   */
  public ConnectionChangeEvent(AbstractConnection connection) {
    super(connection);
  }
}
