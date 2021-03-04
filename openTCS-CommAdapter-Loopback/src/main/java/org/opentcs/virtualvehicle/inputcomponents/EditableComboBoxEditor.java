/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.virtualvehicle.inputcomponents;

import java.util.HashSet;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import org.opentcs.data.TCSObject;

/**
 * An editor for editable comboBoxes.
 *
 * @author Mustafa Yalciner (Fraunhofer IML)
 */
public class EditableComboBoxEditor
    extends BasicComboBoxEditor
    implements ListDataListener {

  private final Set<TCSObject<?>> content = new HashSet<>();
  private final JComboBox<?> comboBox;

  /**
   * creates and instance and configures an EditableComboBoxListener for the editor.
   *
   * @param validationListeners validation listeners.
   * @param comboBox the comboBox that is edited.
   */
  public EditableComboBoxEditor(List<ValidationListener> validationListeners,
                                JComboBox<?> comboBox) {
    this.comboBox = requireNonNull(comboBox, "comboBox");
    EditableComboBoxListener comboBoxListener = new EditableComboBoxListener(content,
                                                                             validationListeners,
                                                                             editor);
    editor.getDocument().addDocumentListener(comboBoxListener);

  }

  @Override
  public void intervalAdded(ListDataEvent e) {
    loadContent();
  }

  @Override
  public void intervalRemoved(ListDataEvent e) {
    loadContent();
  }

  @Override
  public void contentsChanged(ListDataEvent e) {
    loadContent();
  }

  private void loadContent() {
    //get the current comboBoxModel and add the modelelements to content
    ComboBoxModel<?> model = comboBox.getModel();
    for (int i = 0; i < model.getSize(); i++) {
      if (model.getElementAt(i) instanceof TCSObject<?>) {
        content.add(((TCSObject<?>) model.getElementAt(i)));
      }
    }
  }

  @Override
  public Object getItem() {
    //if the panel tries to capture the input, this method guarantees that 
    for (TCSObject<?> p : content) {
      if (p.getName().equals(editor.getText())) {
        return p;
      }
    }
    return null;
  }

  @Override
  public void setItem(Object anObject) {
    //works like a renderer using the names of TCSObjects
    if (anObject instanceof TCSObject<?>) {
      editor.setText(((TCSObject<?>) anObject).getName());
    }
    else if (anObject != null) {
      editor.setText(anObject.toString());
    }
  }

}
