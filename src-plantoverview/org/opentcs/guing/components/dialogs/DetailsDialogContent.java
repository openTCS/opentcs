/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */

package org.opentcs.guing.components.dialogs;

import org.opentcs.guing.components.properties.type.Property;

/**
 * Interface for components to edit properties.
 * Classes that implement this interface are generally embedded in a dialog.
 * The dialog then calls these methods.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface DetailsDialogContent {

  /**
   * Writes the values of the dialog back to the attribute object.
   * This should happen when the user clicked "OK".
   */
  void updateValues();

  /**
   * Returns the title of the dialog.
   *
   * @return The title.
   */
  String getTitle();

  /**
   * Sets the property.
   *
   * @param property The property.
   */
  void setProperty(Property property);

  /**
   * Returns the property.
   *
   * @return The property.
   */
  Property getProperty();
}
