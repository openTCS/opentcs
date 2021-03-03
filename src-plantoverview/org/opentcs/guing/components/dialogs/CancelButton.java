/**
 * Copyright (c) 2007 - 2013 Fraunhofer Institut fuer Materialfluss und Logistik
 * (FhG-IML). All rights reserved.
 */
package org.opentcs.guing.components.dialogs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.JButton;
import javax.swing.KeyStroke;

/**
 * Cancel Button which closes a dialog by pressing ESC.
 * 
 * @author Heinz Huber (Fraunhofer IML)
 */
public class CancelButton
		extends JButton {

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

		registerKeyboardAction(al, "PressedESCAPE",
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JButton.WHEN_IN_FOCUSED_WINDOW);
	}
}
