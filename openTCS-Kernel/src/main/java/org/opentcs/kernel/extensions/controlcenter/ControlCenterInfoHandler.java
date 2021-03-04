/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.controlcenter;

import com.google.inject.assistedinject.Assisted;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import org.opentcs.access.NotificationPublicationEvent;
import org.opentcs.data.notification.UserNotification;
import org.opentcs.util.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A logging handler that writes all INFO-logs to KernelControlCenter's logging text area.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class ControlCenterInfoHandler
    implements EventHandler {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ControlCenterInfoHandler.class);
  /**
   * This class's configuration.
   */
  private final ControlCenterConfiguration configuration;
  /**
   * Formats time stamps.
   */
  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter
      .ofLocalizedDateTime(FormatStyle.SHORT)
      .withLocale(Locale.getDefault())
      .withZone(ZoneId.systemDefault());
  /**
   * The text area we're writing in.
   */
  private final JTextArea textArea;
  /**
   * A flag whether the text area scrolls.
   */
  private boolean autoScroll;

  /**
   * Creates a new ControlCenterInfoHandler.
   *
   * @param textArea The textArea we are writing to.
   * @param configuration This class' configuration.
   */
  @Inject
  public ControlCenterInfoHandler(@Assisted JTextArea textArea,
                                  ControlCenterConfiguration configuration) {
    this.textArea = requireNonNull(textArea, "textArea");
    this.configuration = requireNonNull(configuration, "configuration");

    autoScroll = true;
  }

  @Override
  public void onEvent(Object event) {
    if (!(event instanceof NotificationPublicationEvent)) {
      return;
    }
    SwingUtilities.invokeLater(
        () -> publish(((NotificationPublicationEvent) event).getNotification())
    );
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
    return DATE_FORMAT.format(notification.getTimestamp())
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

      if (docLength > configuration.loggingAreaCapacity()) {
        try {
          textArea.getDocument().remove(0, docLength - configuration.loggingAreaCapacity());
        }
        catch (BadLocationException e) {
          LOG.warn("Caught exception", e);
        }
      }
    });
  }
}
