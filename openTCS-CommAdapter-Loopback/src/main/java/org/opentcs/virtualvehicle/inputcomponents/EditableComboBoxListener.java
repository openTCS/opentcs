/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.virtualvehicle.inputcomponents;

import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.opentcs.data.TCSObject;

/**
 * A validator for the input of the textfield.
 *
 * @author Mustafa Yalciner (Fraunhofer IML)
 */
public class EditableComboBoxListener
    implements DocumentListener {

  private final JTextField textField;
  private final List<ValidationListener> validationListeners;
  private final Set<TCSObject<?>> content;

  /**
   * Creates an instance.
   *
   * @param content the elements of the comboBox' dropdownlist.
   * @param validationListeners the listeners to be notified about the validity of the user input.
   * @param textField the textfield with the input to be validated.
   */
  public EditableComboBoxListener(Set<TCSObject<?>> content,
                                  List<ValidationListener> validationListeners,
                                  JTextField textField) {

    this.textField = requireNonNull(textField, "textField");
    this.validationListeners = requireNonNull(validationListeners, "validationListeners");
    this.content = requireNonNull(content, "content");
  }

  @Override
  public void insertUpdate(DocumentEvent e) {
    validate();
  }

  @Override
  public void removeUpdate(DocumentEvent e) {
    validate();
  }

  @Override
  public void changedUpdate(DocumentEvent e) {
    validate();
  }

  private void notifyValidationListeners(boolean isValid) {
    for (ValidationListener valListener : validationListeners) {
      valListener.validityChanged(new ValidationEvent(this, isValid));
    }
  }

  private void validate() {
    if (textField.getText().equals("")) {
      notifyValidationListeners(true);
      return;
    }

    for (TCSObject<?> element : content) {
      if (element.getName().equals(textField.getText())) {
        notifyValidationListeners(true);
        return;
      }
    }
    notifyValidationListeners(false);
  }

}
