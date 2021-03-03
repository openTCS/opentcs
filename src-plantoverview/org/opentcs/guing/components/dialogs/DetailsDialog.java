/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.components.dialogs;

/**
 * Interface für Dialoge, die das komfortable Bearbeiten von Attributen
 * ermöglichen. Der Dialog selbst gibt nur den Rahmen vor, z.B. einen Ok- und
 * einen Cancel-Button. Die Komponente, mit der die eigentliche Bearbeitung
 * eines Attributs erfolgt, ist vom Typ DialogContent und wird dem Dialog 
 * hinzugefügt.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public interface DetailsDialog {

  /**
   * Liefert die Komponente, mit der die Einstellungen des Attributs komfortabel
   * vorgenommen werden können.
   *
   * @return
   */
  DetailsDialogContent getDialogContent();

  /**
   * Aktiviert den Dialog. Das sollte durch einen Klienten immer dann ausgelöst
   * werden, wenn der Dialog sichtbar gemacht wird. Wird benötigt, um die
   * Funktion "Änderungen für alle Fahrzeugtypen übernehmen" realisieren zu
   * können.
   */
  void activate();
}
