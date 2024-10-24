// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.util.gui.dialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.JButton;
import javax.swing.KeyStroke;

/**
 * Cancel Button which closes a dialog by pressing ESC.
 */
public class CancelButton
    extends
      JButton {

  /**
   * Creates a new instance.
   */
  public CancelButton() {
    this(null);
  }

  /**
   * Creates a new instance.
   *
   * @param text Label of this button.
   */
  @SuppressWarnings("this-escape")
  public CancelButton(String text) {
    super(text);

    ActionListener al = new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent event) {
        String cmd = event.getActionCommand();

        if (cmd.equals("PressedESCAPE")) {
          doClick();
        }
      }
    };

    registerKeyboardAction(
        al, "PressedESCAPE",
        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
        JButton.WHEN_IN_FOCUSED_WINDOW
    );
  }
}
