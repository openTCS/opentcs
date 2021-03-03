/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
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
