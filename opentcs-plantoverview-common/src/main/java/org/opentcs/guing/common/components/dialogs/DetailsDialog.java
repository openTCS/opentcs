// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.common.components.dialogs;

/**
 * Defines a dialog that allows for easy editing of a property.
 * The actual editing of the properties is implemented in a {@link DetailsDialogContent}.
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
