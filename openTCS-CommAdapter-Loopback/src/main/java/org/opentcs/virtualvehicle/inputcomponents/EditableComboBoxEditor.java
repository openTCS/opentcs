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
import java.util.function.Function;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.plaf.basic.BasicComboBoxEditor;

/**
 * An editor for editable combo boxes.
 *
 * @author Mustafa Yalciner (Fraunhofer IML)
 */
public class EditableComboBoxEditor<E>
    extends BasicComboBoxEditor
    implements ListDataListener {

  /**
   * Represents the model of the comboBox as a set.
   */
  private final Set<E> content = new HashSet<>();
  /**
   * The relevant combobox.
   */
  private final JComboBox<E> comboBox;
  /**
   * Returns the string representation for the combo box's selected item.
   */
  private final Function<E, String> representer;

  /**
   * Creates and instance and configures an {@link EditableComboBoxListener} for the editor.
   *
   * @param validationListeners validation listeners.
   * @param comboBox the comboBox that is edited.
   * @param representer Returns the string representation for the combo box's selected item.
   */
  public EditableComboBoxEditor(List<ValidationListener> validationListeners,
                                JComboBox<E> comboBox,
                                Function<E, String> representer) {
    this.comboBox = requireNonNull(comboBox, "comboBox");
    this.representer = requireNonNull(representer, "representer");
    editor.getDocument().addDocumentListener(new EditableComboBoxListener<>(content,
                                                                            validationListeners,
                                                                            editor,
                                                                            representer));
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
    ComboBoxModel<E> model = comboBox.getModel();
    for (int i = 0; i < model.getSize(); i++) {

      content.add(model.getElementAt(i));

    }
  }

  @Override
  public Object getItem() {
    //if the panel tries to capture the input, this method guarantees that 
    for (E p : content) {
      if (representer.apply(p).equals(editor.getText())) {
        return p;
      }
    }
    return null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void setItem(Object anObject) {
    editor.setText(representer.apply((E) anObject));
  }
}
