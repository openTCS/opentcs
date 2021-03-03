/**
 * Copyright (c) 2017 Fraunhofer IML
 */
package org.opentcs.guing.util;

import java.awt.Component;
import java.util.Collection;
import javax.swing.JOptionPane;

/**
 * Provides static methods to show customized {@link JOptionPane}s.
 *
 * @author Mats Wilhelm (Fraunhofer IML)
 */
public class JOptionPaneUtil {

  /**
   * Prevents external instantiation.
   */
  private JOptionPaneUtil() {
  }

  /**
   * Shows a dialog with a text area as content and an ok button.
   *
   * @param parent the parent frame
   * @param title the dialog title
   * @param description the description of the content
   * @param content the content
   */
  public static void showDialogWithTextArea(Component parent,
                                            String title,
                                            String description,
                                            Collection<String> content) {
    TextAreaDialog panel = new TextAreaDialog(parent, true, description);
    panel.setContent(content);
    panel.setTitle(title);
    panel.setLocationRelativeTo(null);
    panel.setVisible(true);
  }
}
