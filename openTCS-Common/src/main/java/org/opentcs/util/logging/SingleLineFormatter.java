/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * A <code>Formatter</code> for <code>LogRecord</code>s that formats messages
 * for output on a single line (except when a <code>Throwable</code> is
 * associated with the <code>LogRecord</code>.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class SingleLineFormatter
    extends Formatter {

  /**
   * A <code>DateFormat</code> instance for formatting timestamps.
   */
  private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd-HH:mm:ss-SSS");
  /**
   * A <code>Date</code> instance for formatting timestamps.
   */
  private final Date date = new Date();
  /**
   * Line separator for wrapping lines at the end of log messages.
   */
  private final String lineSeparator = System.getProperty("line.separator");

  /**
   * Creates a new SingleLineFormatter.
   */
  public SingleLineFormatter() {
  }

  @Override
  public synchronized String format(LogRecord record) {
    StringBuilder result = new StringBuilder();
    date.setTime(record.getMillis());
    result.append(DATE_FORMAT.format(date));
    result.append(" [");
    result.append(record.getLevel().getLocalizedName());
    result.append("|");

    if (record.getSourceClassName() != null) {
      result.append(record.getSourceClassName());
    }
    else {
      result.append(record.getLoggerName());
    }

    result.append("@").append(Thread.currentThread().getName()).append("] ");

    if (record.getSourceMethodName() != null) {
      result.append('[');
      result.append(record.getSourceMethodName());
      result.append("()]");
    }

    result.append(": ");
    result.append(formatMessage(record));

    if (record.getThrown() != null) {
      StringWriter sWriter = new StringWriter();
      PrintWriter pWriter = new PrintWriter(sWriter);
      record.getThrown().printStackTrace(pWriter);
      pWriter.close();
      result.append(lineSeparator);
      result.append(sWriter.toString());
    }

    result.append(lineSeparator);
    return result.toString();
  }
}
