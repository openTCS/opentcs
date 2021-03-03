/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application;

import java.awt.Dimension;
import java.awt.Font;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.DefaultCaret;
import org.opentcs.data.message.Message;

/**
 * A panel that displays kernel messages.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class KernelStatusPanel
    extends JScrollPane {

  /**
   * Formats time stamps for messages.
   */
  private static final DateFormat format
      = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  /**
   * A text area for the output.
   */
  private final JTextArea statusTextArea = new JTextArea();

  /**
   * Creates a new instance.
   */
  public KernelStatusPanel() {
    initComponents();
  }

  /**
   * Logs a message to the status text area.
   *
   * @param message The message to log.
   */
  public void log(Message message) {
    if (message == null) {
      return;
    }

    statusTextArea.append(format.format(message.getTimestamp()) + " "
        + message.getType() + ": " + message.getMessage() + "\n");
    statusTextArea.setCaretPosition(statusTextArea.getDocument().getLength());
  }

  private void initComponents() {
    DefaultCaret caret = (DefaultCaret) statusTextArea.getCaret();
    caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
    setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    setAutoscrolls(true);
    setPreferredSize(new Dimension(183, 115));

    statusTextArea.setEditable(false);
    statusTextArea.setColumns(20);
    statusTextArea.setFont(new Font("Monospaced", 0, 11)); // NOI18N
    statusTextArea.setLineWrap(true);
    statusTextArea.setRows(5);
    statusTextArea.setWrapStyleWord(true);
    setViewportView(statusTextArea);
  }
}
