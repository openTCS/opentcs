/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.statistics;

import java.io.IOException;
import static java.util.Objects.requireNonNull;
import static org.opentcs.util.Assertions.checkArgument;

/**
 * A single record in the dump file.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class StatisticsRecord {

  /**
   * A separator for fields in the record file.
   */
  private static final String FIELD_SEPARATOR = "||";
  /**
   * Regular expression to find field separators in the record file.
   */
  private static final String FIELD_SEPARATOR_REGEXP = "\\|\\|";
  /**
   * Point of time when the record was created.
   */
  private final long timestamp;
  /**
   * The event that occured.
   */
  private final StatisticsEvent event;
  /**
   * A label/object name related to the event.
   */
  private final String label;

  /**
   * Creates a new instance with the given values.
   *
   * @param timestamp Point of time when the record was created.
   * @param event The event that occured.
   * @param label A label/object name related to the event.
   */
  public StatisticsRecord(final long timestamp,
                          final StatisticsEvent event,
                          final String label) {
    this.timestamp = timestamp;
    this.event = requireNonNull(event, "event");
    this.label = requireNonNull(label, "label");
  }

  /**
   * Returns this record's timestamp.
   *
   * @return This record's timestamp.
   */
  public long getTimestamp() {
    return timestamp;
  }

  /**
   * Returns this record's event.
   *
   * @return This record's event.
   */
  public StatisticsEvent getEvent() {
    return event;
  }

  /**
   * Returns this record's label.
   *
   * @return This record's label.
   */
  public String getLabel() {
    return label;
  }

  @Override
  public String toString() {
    return timestamp + FIELD_SEPARATOR + event.name() + FIELD_SEPARATOR + label;
  }

  /**
   * Parses the given string and creates a record object from it.
   *
   * @param input The string to be parsed.
   * @return A record object created from the given string.
   * @throws IOException If the given input string could not be parsed.
   */
  public static StatisticsRecord parseRecord(String input)
      throws IOException {
    requireNonNull(input, "input");
    checkArgument(!input.isEmpty(), "input is empty");

    String[] splitInput = input.split(FIELD_SEPARATOR_REGEXP, 3);
    if (splitInput.length < 3) {
      throw new IOException("Splitting '" + input + "' with '"
          + FIELD_SEPARATOR + "' results in too few elements ("
          + splitInput.length + ").");
    }
    long timestamp = Long.parseLong(splitInput[0]);
    StatisticsEvent event = StatisticsEvent.valueOf(splitInput[1]);
    String label = splitInput[2];

    return new StatisticsRecord(timestamp, event, label);
  }
}
