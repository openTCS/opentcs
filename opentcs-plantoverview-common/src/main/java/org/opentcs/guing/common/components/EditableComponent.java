// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.common.components;

/**
 */
public interface EditableComponent
    extends
      org.jhotdraw.gui.EditableComponent {

  /**
   * Delete the components that are currently selected in the tree and save
   * them to allow restoring by a Paste operation.
   */
  void cutSelectedItems();

  /**
   * Save the components that are currently selected in the tree
   * to allow creating a clone by a Paste operation.
   */
  void copySelectedItems();

  /**
   *
   */
  void pasteBufferedItems();
}
