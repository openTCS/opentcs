/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.components.properties.event;

/**
 * Interface für Klassen, die benachrichtigt werden möchten, wenn der Benutzer
 * eine Tabellenzeile selektiert. Ein TableChangeListener registriert sich dazu
 * bei einer Tabelle.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public interface TableChangeListener
    extends java.util.EventListener {

  /**
   * Nachricht der Tabelle, dass eine Tabellenzeile selektiert wurde.
   *
   * @param event Das TableSelectionChangeEvent gibt Auskunft über die Tabelle,
   * in der die Selektierung stattfand, sowie das Attribut, das sich in der
   * selektierten Zeile befindet.
   */
  void tableSelectionChanged(TableSelectionChangeEvent event);

  /**
   * Nachricht der Tabelle, das Änderungen am Tabelleninhalt aufgetreten sind.
   */
  void tableModelChanged();
}
