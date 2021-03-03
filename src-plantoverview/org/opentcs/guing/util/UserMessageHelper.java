/**
 * (c): IML, 2014.
 */
package org.opentcs.guing.util;

import javax.swing.JOptionPane;

/**
 * A helper class that shows a message dedicated to the user in a
 * dialog.
 *
 * @author pseifert
 */
public class UserMessageHelper {

  /**
   * Shows a message dialog to the user centered in the screen.
   *
   * @param title The title of the dialog.
   * @param message The message to be shown.
   * @param type The type of the message.
   */
  public void showMessageDialog(String title,
                                String message,
                                Type type) {
    showJOptionPane(title, message, type);
  }

  private void showJOptionPane(String title,
                               String message,
                               Type type) {
    int jOptionType;
    switch (type) {
      case ERROR:
        jOptionType = JOptionPane.ERROR_MESSAGE;
        break;
      case INFO:
        jOptionType = JOptionPane.INFORMATION_MESSAGE;
        break;
      case PLAIN:
        jOptionType = JOptionPane.INFORMATION_MESSAGE;
        break;
      default:
        jOptionType = JOptionPane.PLAIN_MESSAGE;
    }
    JOptionPane.showMessageDialog(null,
                                  message,
                                  title,
                                  jOptionType);
  }

  public enum Type {

    PLAIN, INFO, ERROR;
  }
}
