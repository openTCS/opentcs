/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.virtualvehicle.inputcomponents;

import java.util.Objects;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 * Abstract base class for <code>InputPanels</code> that use text fields for input.
 * The main purpose of this class is to provide an easy to use way to validate
 * text inputs using it's nested class {@link TextInputPanel.TextInputValidator 
 * TextInputValidator}.
 *
 * @author Tobias Marquardt (Fraunhofer IML)
 */
public abstract class TextInputPanel
    extends InputPanel {

  /**
   * Create a new instance of <code>TextInputPanel</code>.
   * @param title The title of this panel.
   */
  public TextInputPanel(String title) {
    super(title);
  }

  /**
   * <p>
   * Mark the input of the specified <code>Document</code> as valid/invalid
   * and send {@link ValidationEvent ValidationEvents} to the attached
   * {@link ValidationListener ValidationListeners}. 
   * The <code>Document</code> should be related to an input component in this
   * panel. 
   * </p>
   * <p>
   * <b>Note</b>:<br/>
   * The default implementation just forwards the call to
   * {@link InputPanel#setInputValid(boolean)} without respect to the 
   * <code>Document</code>. Therefore subclasses with multiple Documents
   * should overwrite this method to for example check if <b>all</b> input
   * fields are valid and then decide if 
   * {@link InputPanel#setInputValid(boolean)} should be called or not.
   * </p>
   * @param valid true, if the content of the <code>Document</code> is valid
   * @param doc the <code>Document</code>
   */
  protected void setInputValid(boolean valid, Document doc) {
    setInputValid(valid);
  }

  /**
   * <p>
   * A {@link javax.swing.event.DocumentListener DocumentListener} that can be 
   * used by subclasses of {@link TextInputPanel} to validate input in 
   * {@link javax.swing.JTextField JTextFields} and other Components that
   * use {@link javax.swing.text.Document Documents}.
   * It listens to the DocumentEvents of a 
   * <code>Document</code> and validates the Document's 
   * content against a specified regular expression. Some convenient regular
   * expressions are provided as <code>public static</code> variables. 
   * After validation {@link TextInputPanel#setInputValid(boolean, 
   * javax.swing.text.Document)} is called.
   * </p>
   * <p>
   * <b>Note:</b><br/>
   * The, for convenience, provided regular expressions do
   * NOT check whether the given number really fits into the range of the 
   * corresponding data type (e.g. <code>int</code> for <code>REGEX_INT</code>).
   * </p>
   * @see TextInputPanel#setInputValid(boolean, javax.swing.text.Document) 
   */
  public class TextInputValidator
      implements DocumentListener {

    /**
     * Regular expression that accepts a floating point 
     * number of arbitary length.
     * The decimal point and positions after it can be omitted.
     * <p>
     * Examples:
     * <ul>
     * <li>3.0 is valid</li>
     * <li>-3 is valid</li>
     * <li>3. is invalid</li>
     * <li>.3 is invalid</li>
     * </ul>
     * </p>
     */
    public static final String REGEX_FLOAT = "[-+]?[0-9]+(\\.[0-9]+)?";
    /**
     * Regular expression that accepts a positive floating point number of 
     * arbitrary length and 0. 
     */
    public static final String REGEX_FLOAT_POS = "\\+?[0-9]+(\\.[0-9]+)?";
    /**
     * Regular expression that accepts a negative floating point number of 
     * arbitrary length and 0. 
     */
    public static final String REGEX_FLOAT_NEG = "-[0-9]+(\\.[0-9]+)?|0+(\\.0+)?";
    /** 
     * Regular expression that accepts any integer of 
     * arbitrary length.
     */
    public static final String REGEX_INT = "[-+]?[0-9]+";
    /** 
     * Regular expression that accepts any positive integer of arbitrary length
     * and 0.
     */
    public static final String REGEX_INT_POS = "\\+?[0-9]+";
    /** 
     * Regular expression that accepts any negative integer of arbitrary length
     * and 0.
     */
    public static final String REGEX_INT_NEG = "-[0-9]+|0+";
    /**
     * Regular expression that accepts an integer in the interval [0,100].
     */
    public static final String REGEX_INT_RANGE_0_100 = "[0-9]|[1-9][0-9]|100";
    /**
     * Regular expression that accepts anything except an empty (or 
     * whitespace-only) string.
     */
    public static final String REGEX_NOT_EMPTY = ".*\\S.*";
    /**
     * Regular expression to validate the documents text against.
     */
    private final String format;

    /**
     * Create an instance of <code>TextInputValidator</code>.
     * @param format The regular expression to use for validation.
     */
    protected TextInputValidator(String format) {
      this.format = Objects.requireNonNull(format);
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
      validate(e.getDocument());
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
      validate(e.getDocument());
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
      // Do nothing 
    }

    /**
     * Validate the specified <code>Document</code> and set the validation
     * state in the {@link InputPanel} accordingly.
     * @param doc The <code>Document</code> to validate.
     */
    private void validate(Document doc) {
      String text;
      try {
        text = doc.getText(0, doc.getLength());
      }
      catch (BadLocationException e) {
        //TODO 
        return;
      }
      setInputValid(text.matches(format), doc);
    }
  }
}
