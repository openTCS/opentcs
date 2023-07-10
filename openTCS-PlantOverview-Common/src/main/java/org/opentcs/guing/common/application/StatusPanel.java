/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.application;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import static java.util.Objects.requireNonNull;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A panel at the bottom of the view, showing the mouse position and status.
 */
public class StatusPanel
    extends JPanel {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(StatusPanel.class);
  /**
   * A text field for status messages.
   */
  private final JTextField textFieldStatus = new JTextField();
  /**
   * A text field for cursor positions/coordinates.
   */
  private final JTextField textFieldPosition = new JTextField();

  /**
   * Creates a new instance.
   */
  public StatusPanel() {
    initComponents();
  }

  private void initComponents() {
    textFieldStatus.setText(null);
    textFieldPosition.setText(null);

    removeAll();
    setLayout(new GridBagLayout());

    textFieldPosition.setEditable(false);
    GridBagConstraints gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    add(textFieldPosition, gridBagConstraints);

    textFieldStatus.setEditable(false);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 0.8;
    add(textFieldStatus, gridBagConstraints);
  }

  /**
   * Clears the status textfield, removing any logged messages.
   */
  public void clear() {
    textFieldStatus.setForeground(Color.black);
    textFieldStatus.setText("");
  }

  /**
   * Text display in the status bar (at the bottom).
   *
   * @param level Log-Level, determines the text color.
   * @param text Text to display.
   */
  public void setLogMessage(Level level, String text) {
    if (level == Level.SEVERE) {
      showOptionPane(text);
      textFieldStatus.setForeground(Color.magenta);
      LOG.error(text);
    }
    else if (level == Level.WARNING) {
      showOptionPane(text);
      textFieldStatus.setForeground(Color.red);
      LOG.warn(text);
    }
    else if (level == Level.INFO) {
      textFieldStatus.setForeground(Color.blue);
      LOG.info(text);
    }
    else {
      textFieldStatus.setForeground(Color.black);
      LOG.info(text);
    }

    textFieldStatus.setText(text);
  }

  /**
   * Sets the given text to the position text field.
   *
   * @param text The text to set.
   */
  public void setPositionText(String text) {
    requireNonNull(text, "text");

    // Add a space in front of the position text to avoid that a part of the lefthand side gets cut
    // off with some graphical environments (observed on Windows 10).
    textFieldPosition.setText(" " + text);
    revalidate();
  }

  private void showOptionPane(String text) {
    JOptionPane.showMessageDialog(this.getParent(), text, "", JOptionPane.ERROR_MESSAGE);
  }
}
