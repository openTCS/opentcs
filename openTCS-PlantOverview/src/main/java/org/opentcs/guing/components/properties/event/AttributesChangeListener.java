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
 * Interface, das Controller/Views implementieren. Ändert sich das Model, so
 * werden alle Controller/Views, die sich zuvor als Listener registriert haben,
 * über diese Änderung benachrichtigt.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @see PropertiesModel
 * @see PropertiesModelChangeEvent
 */
public interface AttributesChangeListener {

  /**
   * Information für den View, dass sich die Eigenschaften des Models geändert
   * haben. Der View ist nun selbst dafür zuständig, sich zu aktualisieren.
   *
   * @param e
   */
  void propertiesChanged(AttributesChangeEvent e);
}
