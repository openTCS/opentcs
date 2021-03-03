/**
 * (c): IML, IFAK.
 *
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
