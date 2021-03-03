package org.opentcs.guing.application;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * A panel at the bottom of the view, showing the mouse position and status.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class StatusPanel
    extends JPanel {

  /**
   * This class's logger.
   */
  private static final Logger log
      = Logger.getLogger(StatusPanel.class.getName());
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
    textFieldPosition.setText("X 0000 Y 0000 W 0000 H 0000");
    textFieldPosition.setMinimumSize(new Dimension(240, 20));
    textFieldPosition.setPreferredSize(new Dimension(240, 20));
    GridBagConstraints gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
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
   * Text display in the status bar (at the bottom).
   *
   * @param level Log-Level, determines the text color.
   * @param text Text to display.
   */
  public void setLogMessage(Level level, String text) {
    if (level == Level.SEVERE) {
      textFieldStatus.setForeground(Color.magenta);
      log.severe(text);
    }
    else if (level == Level.WARNING) {
      textFieldStatus.setForeground(Color.red);
      log.warning(text);
    }
    else if (level == Level.INFO) {
      textFieldStatus.setForeground(Color.blue);
      log.info(text);
    }
    else {
      textFieldStatus.setForeground(Color.black);
      log.info(text);
    }

    textFieldStatus.setText(text);
  }

  /**
   * Sets the given text to the position text field.
   *
   * @param text The text to set.
   */
  public void setPositionText(String text) {
    Objects.requireNonNull(text, "text is null");
    textFieldPosition.setText(text);
  }
}
