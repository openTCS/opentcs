/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import static org.opentcs.util.Assertions.checkArgument;

/**
 * A history of events related to an object.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ObjectHistory
    implements Serializable {

  /**
   * The actual history entries.
   */
  private final List<Entry> entries;

  /**
   * Creates a new instance.
   */
  public ObjectHistory() {
    this(new ArrayList<>());
  }

  /**
   * Creates a new instance with the given list of entries.
   *
   * @param entries
   */
  private ObjectHistory(List<Entry> entries) {
    this.entries = Collections.unmodifiableList(requireNonNull(entries, "entries"));
  }

  /**
   * Returns this history's entries.
   *
   * @return This history's entries.
   */
  public List<Entry> getEntries() {
    return entries;
  }

  /**
   * Returns a copy of this object, with the given entries.
   *
   * @param entries The entries.
   * @return A copy of this object, with the given entries.
   */
  public ObjectHistory withEntries(List<Entry> entries) {
    return new ObjectHistory(entries);
  }

  /**
   * Returns a copy of this object, with the given entry appended.
   *
   * @param entry The entry.
   * @return A copy of this object, with the given entry appended.
   */
  public ObjectHistory withEntryAppended(Entry entry) {
    requireNonNull(entry, "entry");

    List<Entry> newEntries = new ArrayList<>(entries.size() + 1);
    newEntries.addAll(entries);
    newEntries.add(entry);
    return new ObjectHistory(newEntries);
  }

  @Override
  public String toString() {
    return "ObjectHistory{" + "entries=" + entries + '}';
  }

  /**
   * An entry/event in a history.
   */
  public static class Entry
      implements Serializable {

    /**
     * The point of time at which the event occured.
     */
    private final Instant timestamp;
    /**
     * A code identifying the event that occured.
     */
    private final String eventCode;
    /**
     * Supplementary information about the event.
     * How this information is to be interpreted (if at all) depends on the respective event code.
     */
    private final Object supplement;

    /**
     * Creates a new instance.
     *
     * @param timestamp The point of time at which the event occured.
     * @param eventCode A code identifying the event that occured.
     * @param supplement Supplementary information about the event.
     * Must be {@link Serializable} and should provide a human-readable default representation from
     * its {@code toString()} method.
     */
    public Entry(Instant timestamp, String eventCode, Object supplement) {
      this.timestamp = requireNonNull(timestamp, "timestamp");
      this.eventCode = requireNonNull(eventCode, "eventCode");
      this.supplement = requireNonNull(supplement, "supplement");
      checkArgument(supplement instanceof Serializable, "supplement is not serializable");
    }

    /**
     * Creates a new instance with an empty supplement.
     *
     * @param timestamp The point of time at which the event occured.
     * @param eventCode A code identifying the event that occured.
     */
    public Entry(Instant timestamp, String eventCode) {
      this(timestamp, eventCode, "");
    }

    /**
     * Creates a new instance with the timestamp set to the current point of time.
     *
     * @param eventCode A code identifying the event that occured.
     * @param supplement Supplementary information about the event.
     * Must be {@link Serializable} and should provide a human-readable default representation from
     * its {@code toString()} method.
     */
    public Entry(String eventCode, Object supplement) {
      this(Instant.now(), eventCode, supplement);
    }

    /**
     * Creates a new instance with the timestamp set to the current point of time and an empty
     * supplement.
     *
     * @param eventCode A code identifying the event that occured.
     */
    public Entry(String eventCode) {
      this(eventCode, "");
    }

    /**
     * Returns this entry's timestamp.
     *
     * @return This entry's timestamp.
     */
    @Nonnull
    public Instant getTimestamp() {
      return timestamp;
    }

    /**
     * Returns this entry's event code.
     *
     * @return This entry's event code.
     */
    @Nonnull
    public String getEventCode() {
      return eventCode;
    }

    /**
     * Returns a supplemental object providing details about the event.
     *
     * @return A supplemental object providing details about the event.
     */
    @Nonnull
    public Object getSupplement() {
      return supplement;
    }

    @Override
    public String toString() {
      return "Entry{"
          + "timestamp=" + timestamp
          + ", eventCode=" + eventCode
          + ", supplement=" + supplement
          + '}';
    }
  }
}
