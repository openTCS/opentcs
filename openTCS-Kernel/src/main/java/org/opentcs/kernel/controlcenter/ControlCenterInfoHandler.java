/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.controlcenter;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.Objects;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import org.opentcs.access.TCSNotificationEvent;
import org.opentcs.data.notification.UserNotification;
import org.opentcs.util.configuration.ConfigurationStore;
import org.opentcs.util.eventsystem.EventListener;
import org.opentcs.util.eventsystem.TCSEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A logging handler that writes all INFO-logs to KernelControlCenter's
 * logging text area.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class ControlCenterInfoHandler
    implements EventListener<TCSEvent> {

  /**
   * This class's Logger.
   */
  private static final Logger log
      = LoggerFactory.getLogger(ControlCenterInfoHandler.class);
  /**
   * This class's ConfigurationStore.
   */
  private static final ConfigurationStore configStore
      = ConfigurationStore.getStore(ControlCenterInfoHandler.class.getName());
  /**
   * Formats time stamps.
   */
  private static final DateTimeFormatter dateFormat = DateTimeFormatter
      .ofLocalizedDateTime(FormatStyle.SHORT)
      .withLocale(Locale.getDefault())
      .withZone(ZoneId.systemDefault());
  /**
   * The text area we're writing in.
   */
  private final JTextArea textArea;
  /**
   * The max length of the text area.
   */
  private int maxDocLength;
  /**
   * A flag whether the text area scrolls.
   */
  private boolean autoScroll;

  /**
   * Creates a new ControlCenterInfoHandler.
   *
   * @param textArea The textArea we are writing to.
   */
  public ControlCenterInfoHandler(JTextArea textArea) {
    this.textArea = Objects.requireNonNull(textArea);
    maxDocLength = configStore.getInt("maxCharactersAllowedInKernelLoggingArea",
                                      3000);
    autoScroll = true;
  }

  @Override
  public void processEvent(TCSEvent event) {
    if (!(event instanceof TCSNotificationEvent)) {
      return;
    }
    SwingUtilities.invokeLater(() -> {
      publish(((TCSNotificationEvent) event).getNotification());
    });
  }

  /**
   * Defines if the textArea autoscrolls.
   *
   * @param autoScroll true if it should, false otherwise
   */
  public void setAutoScroll(boolean autoScroll) {
    this.autoScroll = autoScroll;
  }

  /**
   * Sets a new maximum length of the text area.
   *
   * @param length The new length
   */
  public void setMaxDocLength(int length) {
    if (length >= 1000) {
      configStore.setInt("maxCharactersAllowedInKernelLoggingArea", length);
      this.maxDocLength = length;
      this.checkLength();
    }
    else {
      throw new IllegalArgumentException("New length is too short.");
    }
  }

  /**
   * Returns the maximum length of the text area.
   *
   * @return The maximum length of the text area.
   */
  public int getMaxDocLength() {
    return maxDocLength;
  }

  /**
   * Displays the notification.
   *
   * @param notification The notification
   */
  private void publish(UserNotification notification) {
    DefaultCaret caret = (DefaultCaret) textArea.getCaret();
    if (autoScroll) {
      caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
      textArea.setCaretPosition(textArea.getDocument().getLength());
    }
    else {
      caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
    }

    textArea.append(format(notification));
    textArea.append("\n");
    checkLength();
  }

  private String format(UserNotification notification) {
    return dateFormat.format(notification.getTimestamp())
        + " " + notification.getLevel()
        + ": [" + notification.getSource() + "] "
        + notification.getText();
  }

  /**
   * Checks if the length of the document in our textArea is greater than
   * our <code>maxDocLength</code> and cuts it if neccessary.
   */
  private synchronized void checkLength() {
    SwingUtilities.invokeLater(() -> {
      int docLength = textArea.getDocument().getLength();

      if (docLength > maxDocLength) {
        try {
          textArea.getDocument().remove(0, docLength - maxDocLength);
        }
        catch (BadLocationException e) {
          log.warn("Caught exception", e);
        }
      }
    });
  }
}
