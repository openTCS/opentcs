/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.dialogs;

/**
 * Defines a dialog that allows for easy editing of a property.
 * The actual editing of the properties is implemented in a {@link DetailsDialogContent}.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public interface DetailsDialog {

  /**
   * Returns the {@link DetailsDialogContent} that is used to edit the property.
   *
   * @return
   */
  DetailsDialogContent getDialogContent();

  /**
   * Activates the dialog.
   */
  void activate();
}
