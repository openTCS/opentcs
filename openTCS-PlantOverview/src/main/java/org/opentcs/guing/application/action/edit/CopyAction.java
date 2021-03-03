/**
 * (c): IML, JHotDraw.
 *
 */
package org.opentcs.guing.application.action.edit;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import javax.swing.JComponent;
import org.jhotdraw.app.action.edit.AbstractSelectionAction;
import org.opentcs.guing.components.EditableComponent;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * Copies the selected region and place its contents into the system clipboard.
 * This action acts on the last EditableComponent / {@code JTextComponent}
 * which had the focus when the {@code ActionEvent} was generated.
 * This action is called when the user selects the "Copy" item in the Edit menu.
 * The menu item is automatically created by the application.
 *
 * @author Werner Randelshofer
 */
public class CopyAction
    extends AbstractSelectionAction {

  public final static String ID = "edit.copy";

  /**
   * Creates a new instance which acts on the currently focused component.
   */
  public CopyAction() {
    this(null);
  }

  /**
   * Creates a new instance which acts on the specified component.
   *
   * @param target The target of the action. Specify null for the currently
   * focused component.
   */
  public CopyAction(JComponent target) {
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
          ((EditableComponent) cFocusOwner).copySelectedItems();
        }
      }
    }

    // "Old" version with JHotDraw clipboard
//    JComponent cTarget = target;
//
//    if (cTarget == null && (cFocusOwner instanceof JComponent)) {
//      cTarget = (JComponent) cFocusOwner;
//    }
//    // Note: copying is allowed for disabled components
//    if (cTarget != null && cTarget.getTransferHandler() != null) {
//      cTarget.getTransferHandler().exportToClipboard(cTarget, ClipboardUtil.getClipboard(), TransferHandler.COPY);
//    }
  }
}
