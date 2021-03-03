/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.util;

import javax.swing.JOptionPane;

/**
 * A helper class that shows a message dedicated to the user in a
 * dialog.
 *
 * @author Philipp Seifert (Fraunhofer IML)
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

  /**
   * Shows a dialog with the given options to choose from.
   *
   * @param title The title of the dialog.
   * @param message The message to be shown.
   * @param type The type of the message.
   * @param options The options that shall be selectable.
   * @return An int indicating the selected value or -1, if it was closed.
   */
  public int showOptionsDialog(String title,
                               String message,
                               Type type,
                               String[] options) {
    if (options == null || options.length == 0) {
      return -1;
    }
    return showJOptionsDialog(title, message, type, options);
  }

  /**
   * Shows a confirm dialog, offering three options: Yes, No, Cancel.
   *
   * @param title The title of the dialog.
   * @param message The message to be shown.
   * @param type The type of the message.
   * @return A {@link ReturnType} indicating the selected value.
   */
  public ReturnType showConfirmDialog(String title,
                                      String message,
                                      Type type) {
    return translateJOptionReturnType(showJOptionConfirmDialog(title, message, type));
  }

  private int showJOptionConfirmDialog(String title,
                                       String message,
                                       Type type) {
    int jOptionType = translateType(type);
    return JOptionPane.showConfirmDialog(null, message, title, jOptionType);
  }

  private int showJOptionsDialog(String title,
                                 String message,
                                 Type type,
                                 String[] options) {
    int n = JOptionPane.showOptionDialog(null,
                                         message,
                                         title,
                                         JOptionPane.DEFAULT_OPTION,
                                         translateType(type),
                                         null,
                                         options,
                                         options[0]);
    return n;
  }

  private int translateType(Type type) {
    int jOptionType;
    switch (type) {
      case ERROR:
        jOptionType = JOptionPane.ERROR_MESSAGE;
        break;
      case INFO:
        jOptionType = JOptionPane.INFORMATION_MESSAGE;
        break;
      case QUESTION:
        jOptionType = JOptionPane.YES_NO_OPTION;
        break;
      default:
        jOptionType = JOptionPane.PLAIN_MESSAGE;
    }
    return jOptionType;
  }

  private ReturnType translateJOptionReturnType(int type) {
    switch (type) {
      case JOptionPane.OK_OPTION:
        return ReturnType.OK;
      case JOptionPane.NO_OPTION:
        return ReturnType.NO;
      case JOptionPane.CANCEL_OPTION:
        return ReturnType.CANCEL;
      default:
        return ReturnType.CANCEL;
    }
  }

  private void showJOptionPane(String title,
                               String message,
                               Type type) {
    int jOptionType;
    jOptionType = translateType(type);
    JOptionPane.showMessageDialog(null,
                                  message,
                                  title,
                                  jOptionType);
  }

  public enum Type {

    PLAIN,
    INFO,
    ERROR,
    QUESTION;
  }

  public enum ReturnType {

    OK,
    NO,
    CANCEL;
  }
}
