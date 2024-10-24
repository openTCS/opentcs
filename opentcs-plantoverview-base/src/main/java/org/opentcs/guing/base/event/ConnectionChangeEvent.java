// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.base.event;

import java.util.EventObject;
import org.opentcs.guing.base.model.elements.AbstractConnection;

/**
 * An event for changes on connections.
 */
public class ConnectionChangeEvent
    extends
      EventObject {

  /**
   * Creates a new instance of ConnectionChangeEvent.
   *
   * @param connection The connection that has changed.
   */
  public ConnectionChangeEvent(AbstractConnection connection) {
    super(connection);
  }
}
