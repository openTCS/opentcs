/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.virtualvehicle.inputcomponents;

/**
 * A general input panel with a text field and optionally:
 * <ul>
 * <li>a message/text</li>
 * <li>a label</li>
 * <li>a unit label for the input</li>
 * </ul>
 * The input of the text field can be validated 
 * (see {@link Builder#enableValidation enableValidation}).
 * <br />
 * For instanciation the contained 
 * {@link SingleTextInputPanel.Builder Builder}-class must be used.
 * <br />
 * The <code>Object</code> that is returned by {@link InputPanel#getInput} is
 * a <code>String</code> (the text in the text field).
 *
 * @author Tobias Marquardt (Fraunhofer IML)
 */
public final class SingleTextInputPanel
    extends TextInputPanel {

  /**
   * If the panel is resetable this is the value the input is set to when
   * doReset() is called.
   */
  private Object resetValue;

  /** 
   * Creates new instance of SingleTextInputPanel.
   * The given title is not used in the panel itselft but can be used by
   * the enclosing component.
   * @param title The title of the panel.
   */
  private SingleTextInputPanel(String title) {
    super(title);
    initComponents();
  }

  /**
   * Enable input validation against the given regular expression. 
   * @see InputPanel#addValidationListener
   * @param format A regular expression.
   */
  private void enableInputValidation(String format) {
    if (format != null) {
      inputField.getDocument().addDocumentListener(new TextInputValidator(format));
    }
  }

  @Override
  protected void captureInput() {
    input = inputField.getText();
  }

  @Override
  public void doReset() {
    input = resetValue;
  }

  /**
   * See {@link InputPanel.Builder}.
   */
  public static class Builder
      implements InputPanel.Builder {

    /**
     * The panel's title.
     */
    private final String title;
    /**
     * Label of for the text field.
     */
    private String label;
    /**
     * Unit label of the text field.
     */
    private String unitLabel;
    /**
     * Initial value for the text field.
     */
    private String initialValue;
    /**
     * Message to be displayed in the panel.
     */
    private String message;
    /**
     * Regex for validation of the text field's content.
     */
    private String format;
    /**
     * Show a reset button in the panel.
     * Default is <code>false</code>. 
     */
    private boolean resetButton;
    /**
     * Value the input is reset to when the reset button is used.
     */
    private Object resetValue;

    /**
     * Create a new builder.
     * @param title Title of the panel.
     */
    public Builder(String title) {
      this.title = title;
    }

    @Override
    public InputPanel build() {
      SingleTextInputPanel panel = new SingleTextInputPanel(title);
      panel.enableInputValidation(format);
      panel.label.setText(label);
      panel.unitLabel.setText(unitLabel);
      panel.inputField.setText(initialValue);
      panel.messageLabel.setText(message);
      panel.resetable = resetButton;
      if (panel.resetable) {
        panel.resetValue = resetValue;
      }
      return panel;
    }

    /**
     * Set the label of the panel.
     * @param label The Label
     * @return the instance of this <code>Builder</code>
     */
    public Builder setLabel(String label) {
      this.label = label;
      return this;
    }

    /**
     * Set the initial value for the text field of the panel.
     * @param initialValue the initial value
     * @return the instance of this <code>Builder</code>
     */
    public Builder setInitialValue(String initialValue) {
      this.initialValue = initialValue;
      return this;
    }

    /**
     * Set the text for the unit label of the panel.
     * @param unitLabel the unit
     * @return the instance of this <code>Builder</code>
     */
    public Builder setUnitLabel(String unitLabel) {
      this.unitLabel = unitLabel;
      return this;
    }

    /**
     * Set the message of the panel.
     * The user of this method must take care for the line breaks in the message,
     * as it is not wrapped automatically!
     * @param message the message
     * @return the instance of this <code>Builder</code>
     */
    public Builder setMessage(String message) {
      this.message = message;
      return this;
    }

    /**
     * Make the panel validate it's input.
     * @param format The regular expression that will be used for validation.
     * @return the instance of this <code>Builder</code>
     */
    public Builder enableValidation(String format) {
      this.format = format;
      return this;
    }

    /**
     * Set a value the panel's input can be reset to.
     * @param resetValue the reset value
     * @return the instance of this <code>Builder</code>
     */
    public Builder enableResetButton(Object resetValue) {
      this.resetButton = true;
      this.resetValue = resetValue;
      return this;
    }
  }

  // CHECKSTYLE:OFF
  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    unitLabel = new javax.swing.JLabel();
    label = new javax.swing.JLabel();
    inputField = new javax.swing.JTextField();
    messageLabel = new javax.swing.JLabel();

    setLayout(new java.awt.GridBagLayout());

    unitLabel.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
    unitLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    unitLabel.setText("Unit-Label");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    add(unitLabel, gridBagConstraints);

    label.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
    label.setText("Label");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    add(label, gridBagConstraints);

    inputField.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
    inputField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
    inputField.setText("initial Value");
    inputField.setPreferredSize(new java.awt.Dimension(70, 20));
    inputField.addFocusListener(new java.awt.event.FocusAdapter() {
      public void focusGained(java.awt.event.FocusEvent evt) {
        inputFieldFocusGained(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    add(inputField, gridBagConstraints);

    messageLabel.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
    messageLabel.setText("Message");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    add(messageLabel, gridBagConstraints);
  }// </editor-fold>//GEN-END:initComponents

  private void inputFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_inputFieldFocusGained
    inputField.selectAll();
  }//GEN-LAST:event_inputFieldFocusGained
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JTextField inputField;
  private javax.swing.JLabel label;
  private javax.swing.JLabel messageLabel;
  private javax.swing.JLabel unitLabel;
  // End of variables declaration//GEN-END:variables
  //CHECKSTYLE:ON
}
