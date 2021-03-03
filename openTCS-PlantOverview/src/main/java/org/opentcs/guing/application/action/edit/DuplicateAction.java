/**
 * (c): IML, JHotDraw.
 *
 */
package org.opentcs.guing.application.action.edit;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import javax.swing.JComponent;
import org.opentcs.guing.components.EditableComponent;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * Duplicates the selected region.
 * This action acts on the last EditableComponent / {@code JTextComponent}
 * which had the focus when the {@code ActionEvent} was generated.
 * This action is called when the user selects the "Duplicate" item
 * in the Edit menu. The menu item is automatically created by the application.
 *
 * @author Werner Randelshofer.
 */
public class DuplicateAction
    extends org.jhotdraw.app.action.edit.AbstractSelectionAction {

  public final static String ID = "edit.duplicate";

  /**
   * Creates a new instance which acts on the currently focused component.
   */
  public DuplicateAction() {
    this(null);
  }

  /**
   * Creates a new instance which acts on the specified component.
   *
   * @param target The target of the action. Specify null for the currently
   * focused component.
   */
  public DuplicateAction(JComponent target) {
    super(target);
    ResourceBundleUtil.getBundle().configureAction(this, ID);
  }

  @Override
  public void actionPerformed(ActionEvent evt) {
    Component cFocusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();

    if (cFocusOwner instanceof JComponent) {
      if (cFocusOwner.isEnabled()) {
        // Cut all selected UserObjects from the tree
        if (cFocusOwner instanceof EditableComponent) {
          ((EditableComponent) cFocusOwner).duplicate();
        }
      }
    }
  }
}
