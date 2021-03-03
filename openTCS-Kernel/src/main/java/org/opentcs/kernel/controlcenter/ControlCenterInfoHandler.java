/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.controlcenter;

import java.util.Objects;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import org.opentcs.util.configuration.ConfigurationStore;

/**
 * A logging handler that writes all INFO-logs to KernelControlCenter's
 * logging text area.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class ControlCenterInfoHandler
    extends Handler {

  /**
   * This class's Logger.
   */
  private static final Logger log =
      Logger.getLogger(ControlCenterInfoHandler.class.getName());
  /**
   * This class's ConfigurationStore.
   */
  private static final ConfigurationStore configStore =
      ConfigurationStore.getStore(ControlCenterInfoHandler.class.getName());
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
    this.setFormatter(new SimpleFormatter());
    //this.setFormatter(new SingleLineFormatter());
    maxDocLength = configStore.getInt("maxCharactersAllowedInKernelLoggingArea",
                                      3000);
    autoScroll = true;

    // Set a configurable log level.
    String levelPropName = getClass().getName() + ".level";
    String levelProp = LogManager.getLogManager().getProperty(levelPropName);
    setLevel(levelProp == null ? Level.INFO : Level.parse(levelProp));
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

  @Override
  public void publish(final LogRecord record) {
    Objects.requireNonNull(record);
    if (!isLoggable(record)) {
      return;
    }
    Runnable runPublish = new Runnable() {

      @Override
      public void run() {
        publishP(record);
      }
    };

    SwingUtilities.invokeLater(runPublish);
  }

  /**
   * Writes the actual message.
   * 
   * @param record The message
   */
  private void publishP(LogRecord record) {
    DefaultCaret caret = (DefaultCaret) textArea.getCaret();
    if (autoScroll) {
      caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
      textArea.setCaretPosition(textArea.getDocument().getLength());
    }
    else {
      caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
    }

    textArea.append(this.getFormatter().format(record));
    checkLength();
  }

  @Override
  public void flush() {
  }

  @Override
  public void close() throws SecurityException {
  }

  /**
   * Checks if the length of the document in our textArea is greater than
   * our <code>maxDocLength</code> and cuts it if neccessary.
   */
  private synchronized void checkLength() {
    SwingUtilities.invokeLater(new Runnable() {

      @Override
      public void run() {
        int docLength = textArea.getDocument().getLength();

        if (docLength > maxDocLength) {
          try {
            textArea.getDocument().remove(0, docLength - maxDocLength);
          }
          catch (BadLocationException e) {
            log.log(Level.WARNING, "Caught exception", e);
          }
        }
      }
    });
  }
}
