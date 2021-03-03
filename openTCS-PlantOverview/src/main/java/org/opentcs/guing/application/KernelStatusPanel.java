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
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import static java.util.Objects.requireNonNull;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.DefaultCaret;
import org.opentcs.data.notification.UserNotification;
import org.opentcs.guing.util.MessageDisplay;

/**
 * A panel that displays kernel messages.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class KernelStatusPanel
    extends JScrollPane
    implements MessageDisplay {

  /**
   * Formats time stamps.
   */
  private static final DateTimeFormatter dateFormat = DateTimeFormatter
      .ofLocalizedDateTime(FormatStyle.SHORT)
      .withLocale(Locale.getDefault())
      .withZone(ZoneId.systemDefault());
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
   * @param notification The message to log.
   */
  @Override
  public void display(UserNotification notification) {
    requireNonNull(notification, "message");

    statusTextArea.append(dateFormat.format(notification.getTimestamp()) + " "
        + notification.getLevel() + ": [" + notification.getSource() + "] "
        + notification.getText() + "\n");
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
