/**
 * (c): IML, JHotDraw.
 *
 */
package org.opentcs.guing.application.action.edit;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import javax.swing.JComponent;
import javax.swing.text.JTextComponent;
import org.opentcs.guing.components.EditableComponent;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * Clears (de-selects) the selected region.
 * This action acts on the last {@code JTextComponent} which had the focus
 * when the {@code ActionEvent} was generated.
 * This action is called when the user selects the "Clear Selection" item
 * in the Edit menu. The menu item is automatically created by the application.
 *
 * @author Werner Randelshofer.
 */
public class ClearSelectionAction
    extends org.jhotdraw.app.action.edit.AbstractSelectionAction {

  public final static String ID = "edit.clearSelection";

  /**
   * Creates a new instance which acts on the currently focused component.
   */
  public ClearSelectionAction() {
    this(null);
  }

  /**
   * Creates a new instance which acts on the specified component.
   *
   * @param target The target of the action. Specify null for the currently
   * focused component.
   */
  public ClearSelectionAction(JComponent target) {
    super(target);
    ResourceBundleUtil.getBundle().configureAction(this, ID, false);
  }

  @Override
  public void actionPerformed(ActionEvent evt) {
    JComponent cTarget = target;
    Component cFocusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();

    if (cTarget == null && (cFocusOwner instanceof JComponent)) {
      cTarget = (JComponent) cFocusOwner;
    }

    if (cTarget != null && cTarget.isEnabled()) {
      if (cTarget instanceof EditableComponent) {
        ((EditableComponent) cTarget).clearSelection();
      }
      else if (cTarget instanceof JTextComponent) {
        JTextComponent tc = ((JTextComponent) cTarget);
        tc.select(tc.getSelectionStart(), tc.getSelectionStart());
      }
      else {
        cTarget.getToolkit().beep();
      }
    }
  }

  @Override
  protected void updateEnabled() {
    if (target != null) {
      setEnabled(target.isEnabled());
    }
  }
}
