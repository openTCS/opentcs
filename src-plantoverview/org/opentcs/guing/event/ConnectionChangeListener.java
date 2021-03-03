/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.event;

/**
 * Interface for listeners that want to be informed on connection changes.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public interface ConnectionChangeListener {

  /**
   * Message from a connection that its components have changed.
   *
   * @param e The fired event.
   */
  void connectionChanged(ConnectionChangeEvent e);
}
