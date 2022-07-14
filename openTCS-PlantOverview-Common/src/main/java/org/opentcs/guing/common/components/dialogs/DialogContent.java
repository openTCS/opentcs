/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.dialogs;

import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Base implementation for a dialog and tab content.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public abstract class DialogContent
    extends JPanel {

  /**
   * Title of the component in a dialog.
   */
  protected String fDialogTitle;
  /**
   * Title of the component in a tab.
   */
  protected String fTabTitle;
  /**
   * Indicates that the update of the value has failed.
   */
  protected boolean updateFailed;
  /**
   * Whether or not this dialog is modal.
   */
  protected boolean fModal;
  /**
   * Parent dialog where this content is added to.
   */
  protected StandardContentDialog fDialog;

  /**
   * Creates a new instance of AbstractDialogContent.
   */
  public DialogContent() {
    setModal(true);
  }

  /**
   * Returns the component.
   *
   * @return The component of this dialog.
   */
  public JComponent getComponent() {
    return this;
  }

  /**
   * Sets the dialog to be modal.
   *
   * @param modal true if the dialog should be modal.
   */
  public final void setModal(boolean modal) {
    fModal = modal;
  }

  /**
   * Returns whether or not the component is modal.
   * 
   * @return Whether or not the component is modal.
   */
  public boolean getModal() {
    return fModal;
  }

  /**
   * Returns the title for a tab.
   *
   * @return The title for a tab.
   */
  public String getTabTitle() {
    return fTabTitle;
  }

  /**
   * Returns the title for a dialog.
   *
   * @return The title for a dialog.
   */
  public String getDialogTitle() {
    return fDialogTitle;
  }

  /**
   * Set the title for a dialog.
   *
   * @param title The new title for a dialog.
   */
  protected void setDialogTitle(String title) {
    fDialogTitle = title;
  }

  /**
   * Set the title for a tab.
   *
   * @param title The new title for a tab.
   */
  protected void setTabTitle(String title) {
    fTabTitle = title;
  }

  /**
   * Notifies the registered listeners that the dialog would like to close.
   */
  protected void notifyRequestClose() {
    fDialog.requestClose();
  }

  /**
   * Returns whether or not the update of the UI elements failed.
   *
   * @return true if the update failed.
   */
  public boolean updateFailed() {
    return updateFailed;
  }

  /**
   * Initialises the dialog elements.
   */
  public abstract void initFields();

  /**
   * Updates the values from the dialog elements.
   */
  public abstract void update();
}
