/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.virtualvehicle.inputcomponents;

import java.util.LinkedList;
import java.util.List;

/**
 * Base class for panels that provide input methods for the user and can be
 * embedded in an {@link InputDialog}.
 *
 * @author Tobias Marquardt (Fraunhofer IML)
 */
public abstract class InputPanel
    extends javax.swing.JPanel {

  /**
   * Object to store the user's input. Must be explicitly set via captureInput()
   * or doReset(). What exactly this input is depends on the actual
   * implementation of InputPanel's subclass.
   */
  protected Object input;
  /**
   * If true, the panel's state/input can be reset via doReset() to a default
   * value.
   * By default <code>InputPanels</code> are not resetable.
   */
  protected boolean resetable;
  /**
   * List of ValidationListeners that will receive ValidationEvents.
   */
  private final List<ValidationListener> validationListeners = new LinkedList<>();
  /**
   * Title of the panel. Might be used by the surrounding dialog.
   */
  private final String title;
  /**
   * Indicates whether the current user input is valid.
   * If the panel validates the input this can be changed via setInputValid().
   */
  private boolean inputValid = true;

  /**
   * Create a new instance of InputPanel.
   * @param title The title of this panel.
   */
  public InputPanel(String title) {
    this.title = title;
  }

  /**
   * Get the title of this panel. 
   * @return The title
   */
  public String getTitle() {
    return title;
  }

  /**
   * Add a {@link ValidationListener} that will receive 
   * {@link ValidationEvent ValidationEvents}
   * whenever the validity of the input in this panel changes. The 
   * {@link ValidationListener} will receive a {@link ValidationEvent} with the
   * current validity state immediately after beeing added.
   * If the panel does not validate it's input the validity will never change.
   * @param listener The {@link ValidationListener}
   */
  public void addValidationListener(ValidationListener listener) {
    validationListeners.add(listener);
    // Fire initial validation event for this listener
    listener.validityChanged(new ValidationEvent(this, inputValid));
  }

  /**
   * Mark the input of the panel as valid/invalid and send 
   * {@link ValidationEvent ValidationEvents}
   * to the attached {@link ValidationListener ValidationListeners}.
   * The Validity should only be changed via this method!
   * @param valid true, if input is valid. false otherwise.
   */
  protected void setInputValid(boolean valid) {
    boolean changed = valid != inputValid;
    inputValid = valid;
    if (changed) {
      ValidationEvent e = new ValidationEvent(this, valid);
      for (ValidationListener l : validationListeners) {
        l.validityChanged(e);
      }
    }
  }
/**
 * 
 * @return The validation Listeners.
 */
  public List<ValidationListener> getValidationListeners() {
    return validationListeners;
  }

  /**
   * Determine if the current input in the panel is valid.
   * If the input isn't validated this will always return <code>true</code>.
   * @see #addValidationListener
   * @return <code>true</code> if input is valid, <code>false</code> otherwise.
   */
  public boolean isInputValid() {
    return inputValid;
  }

  /**
   * Get the user input from the panel. If the input wasn't captured before
   * (see {@link #captureInput()}) null is returned. Otherwise it depends on the 
   * concrete implementing panel what the input can look like.
   * @return The input
   */
  public Object getInput() {
    return input;
  }

  /**
   * Tells the panel to get and store the user input which will be available
   * via {@link #getInput()} afterwards.
   * Usually this method should be called from the enclosing dialog when the
   * ok button is pressed. It is not intended to be used by the user of the 
   * panel!
   */
  protected abstract void captureInput();

  /**
   * Returns whether the content of this panel can be reset to a default value.
   * If the panel is resetable the enclosing dialog might want to show a reset
   * button.
   * @see #doReset()
   * @return panel is resetable?
   */
  public boolean isResetable() {
    return resetable;
  }

  /**
   * Inform the panel, that it should reset it's input values (probably because
   * the reset button in the enclosing dialog was pressed).
   * It's up to the specific panel itself to decide what is resetted. 
   * The default implementation does nothing.
   * It should be overwritten in subclasses if reset functionality is needed.
   */
  public void doReset() {
    // Do nothing here.
  }

  /**
   * <p>
   * An interface that can be used to implement the builder-pattern
   * (see Joshua Bloch's <i>Effective Java</i>).
   * <br />
   * As an <code>InputPanel</code> might have many required and/or optional
   * parameters it can be more convinient to use a Builder class instead of
   * public constructors.
   * A builder should implement a public constructor with required parameters
   * for the panel and public setters for optional parameters.
   * The <code>InputPanel</code> is created by the {@link #build} method.
   * <br />
   * For an example implementation see {@link SingleTextInputPanel.Builder}.
   * </p>
   * <p>
   * <b>Usage</b>:
   * <ol>
   * <li>Instanciate the builder, passing required parameters to the 
   * constructor.</li>
   * <li>Set optional parameters via the other public methods.</li>
   * <li>Actually build the panel according to the previously specified 
   * parameters using the build() method.</li>
   * </ol>
   * </p>
   * <p>
   * <b>Note</b>: 
   * The parameter methods should always return the builder itself, so the 
   * creation of a panel can be done in a single statement (see the 
   * <i>Builder-Pattern</i> in Joshua Bloch's <i>Effective Java</i>).
   * </p>
   */
  public interface Builder {

    /**
     * Finally build the {@link InputPanel} as described by this 
     * <code>Builder</code>.
     * @return The created <code>InputPanel</code>.
     */
    InputPanel build();
  }
}
