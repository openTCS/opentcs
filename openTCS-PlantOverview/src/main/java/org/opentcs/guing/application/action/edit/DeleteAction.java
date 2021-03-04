/**
 * (c): IML, JHotDraw.
 *
 */
package org.opentcs.guing.application.action.edit;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import javax.swing.JComponent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import org.opentcs.guing.components.EditableComponent;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * Deletes the region at (or after) the caret position.
 * This action acts on the last EditableComponent} / {@code JTextComponent}
 * which had the focus when the {@code ActionEvent} was generated.
 * This action is called when the user selects the "Delete" item in
 * the Edit menu. The menu item is automatically created by the application.
 *
 * @author Werner Randelshofer
 */
public class DeleteAction
    extends TextAction {

  public final static String ID = "edit.delete";

  /**
   * Creates a new instance which acts on the currently focused component.
   */
  public DeleteAction() {
    super(ID);
    ResourceBundleUtil.getBundle().configureAction(this, ID);
  }

  @Override
  public void actionPerformed(ActionEvent evt) {
    Component cFocusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();

    if (cFocusOwner instanceof JComponent) {
      if (cFocusOwner.isEnabled()) {
        if (cFocusOwner instanceof EditableComponent) {
          // Delete all selected UserObjects from the tree or
          // delete all selected Figures from the DrawingView
          ((EditableComponent) cFocusOwner).delete();
        }
        else {
          deleteNextChar(evt);
        }
      }
    }
  }

  /**
   * This method was copied from
   * DefaultEditorKit.DeleteNextCharAction.actionPerformed(ActionEvent).
   *
   * @param e
   */
  public void deleteNextChar(ActionEvent e) {
    JTextComponent c = getTextComponent(e);
    boolean beep = true;

    if ((c != null) && (c.isEditable())) {
      try {
        javax.swing.text.Document doc = c.getDocument();
        Caret caret = c.getCaret();
        int dot = caret.getDot();
        int mark = caret.getMark();

        if (dot != mark) {
          doc.remove(Math.min(dot, mark), Math.abs(dot - mark));
          beep = false;
        }
        else if (dot < doc.getLength()) {
          doc.remove(dot, 1);
          beep = false;
        }
      }
      catch (BadLocationException bl) {
      }
    }

    if (beep) {
      Toolkit.getDefaultToolkit().beep();
    }
  }
}
