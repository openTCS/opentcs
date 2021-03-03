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
 * Pastes the contents of the system clipboard at the caret position.
 * This action acts on the last EditableComponent / {@code JTextComponent}
 * which had the focus when the {@code ActionEvent} was generated.
 * This action is called when the user selects the "Paste" item in the Edit menu.
 * The menu item is automatically created by the application.
 *
 * @author Werner Randelshofer
 */
public class PasteAction
    extends org.jhotdraw.app.action.edit.AbstractSelectionAction {

  public final static String ID = "edit.paste";

  /**
   * Creates a new instance which acts on the currently focused component.
   */
  public PasteAction() {
    this(null);
  }

  /**
   * Creates a new instance which acts on the specified component.
   *
   * @param target The target of the action. Specify null for the currently
   * focused component.
   */
  public PasteAction(JComponent target) {
    super(target);
    ResourceBundleUtil.getBundle().configureAction(this, ID);
  }

  @Override
  public void actionPerformed(ActionEvent evt) {
    Component cFocusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();

    if (cFocusOwner instanceof JComponent) {
      if (cFocusOwner.isEnabled()) {
        if (cFocusOwner instanceof EditableComponent) {
          ((EditableComponent) cFocusOwner).pasteBufferedItems();
        }
      }
    }

    // "Old" version with JHotDraw clipboard
//    JComponent cTarget = target;
//
//    if (cTarget == null && (cFocusOwner instanceof JComponent)) {
//      cTarget = (JComponent) cFocusOwner;
//    }
//
//    if (cTarget != null && cTarget.isEnabled()) {
//      Transferable t = ClipboardUtil.getClipboard().getContents(cTarget);
//
//      if (t != null && cTarget.getTransferHandler() != null) {
//        cTarget.getTransferHandler().importData(cTarget, t);
//      }
//    }
  }

  @Override
  protected void updateEnabled() {
    if (target != null) {
      setEnabled(target.isEnabled());
    }
  }
}
