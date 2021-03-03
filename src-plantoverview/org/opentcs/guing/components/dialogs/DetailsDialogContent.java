/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.components.dialogs;

import org.opentcs.guing.components.properties.type.Property;
import org.opentcs.guing.model.SystemModel;

/**
 * Interface for componentts to edit properties.
 * Classes that implement this interface are generally embedded in a dialog.
 * The dialog then calls these methods.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
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
  
  /**
   * Sets the current system model, in case the implementation needs to acquire
   * data from it.
   *
   * @param systemModel The current system model.
   */
  void setSystemModel(SystemModel systemModel);
}
