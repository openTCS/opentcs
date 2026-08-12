// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v2_0.controlcenter;

import java.util.Objects;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import org.opentcs.data.TCSObject;

/**
 * This class extends the BasicComboBoxEditor to display TCSObjects in editable JComboBoxs.
 */
public class TCSObjectComboBoxEditor
    extends
      BasicComboBoxEditor {

  /**
   * The last selected item in the combo box.
   */
  private Object selectedObject;

  /**
   * Creates a new instance.
   */
  public TCSObjectComboBoxEditor() {
  }

  @Override
  public void setItem(Object o) {
    if (!(o instanceof TCSObject<?>)) {
      super.setItem(o);
      selectedObject = null;
      return;
    }

    editor.setText(((TCSObject<?>) o).getName());
    selectedObject = o;
  }

  @Override
  public Object getItem() {
    if (!(selectedObject instanceof TCSObject<?>)) {
      return super.getItem();
    }

    TCSObject<?> selectedTCSObject = (TCSObject<?>) selectedObject;
    if (Objects.equals(editor.getText(), selectedTCSObject.getName())) {
      return selectedTCSObject;
    }
    return editor.getText();
  }
}
