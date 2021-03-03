/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.components.drawing.course;

import java.util.EventObject;

/**
 * Interface für Klassen, die an Änderungen des Koordinaten-Ursprungs
 * interessiert sind.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public interface OriginChangeListener {

  /**
   * Nachricht, dass sich die Position des Ursprungs geändert hat.
   *
   * @param evt das Ereignis
   */
  void originLocationChanged(EventObject evt);

  /**
   * Nachricht, dass sich der Maßstab geändert hat.
   *
   * @param evt das Ereignis
   */
  void originScaleChanged(EventObject evt);
}
