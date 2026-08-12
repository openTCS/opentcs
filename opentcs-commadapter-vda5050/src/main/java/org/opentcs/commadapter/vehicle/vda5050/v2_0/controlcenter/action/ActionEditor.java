// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v2_0.controlcenter.action;

import java.util.Objects;
import javax.swing.ComboBoxEditor;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.Action;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.BlockingType;

/**
 * A {@link ComboBoxEditor} for {@link Action}s.
 * <p>
 * This {@link ComboBoxEditor} displays {@link Action}s using their {@link Action#getActionType()
 * action type}. When a value is entered into an editable combo box with this editor, a new
 * {@link Action} instance is created with the entered value used as the action's action type. The
 * created instance is then set as the combo box's selected item.
 */
public class ActionEditor
    extends
      BasicComboBoxEditor {

  /**
   * The {@link Action} edited last in the combo box.
   */
  private Action action;

  /**
   * Creates a new instance.
   */
  public ActionEditor() {
  }

  @Override
  public void setItem(Object anObject) {
    if (!(anObject instanceof Action)) {
      // Default behavior for non actions (primarily for handling null).
      super.setItem(anObject);
      return;
    }

    action = (Action) anObject;
    editor.setText(action.getActionType());
  }

  @Override
  public Object getItem() {
    String newValue = editor.getText();
    if (action != null && Objects.equals(action.getActionType(), newValue)) {
      // The editor content didn't change. No need to create a new action.
      return action;
    }

    return new EditorAction(newValue);
  }

  /**
   * Represents an {@link Action} created by {@link ActionEditor} as a result of a manual input into
   * an editable combo box.
   */
  public static class EditorAction
      extends
        Action {

    public EditorAction(String actionType) {
      super(actionType, "", BlockingType.NONE);
    }
  }
}
