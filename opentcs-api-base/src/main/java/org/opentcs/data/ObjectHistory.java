// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.data;

import static java.util.Objects.requireNonNull;
import static org.opentcs.util.Assertions.checkArgument;

import jakarta.annotation.Nonnull;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * A history of events related to an object.
 */
public class ObjectHistory
    implements
      Serializable {

  /**
   * The actual history entries.
   */
  private final List<Entry> entries;

  /**
   * Creates a new instance.
   */
  public ObjectHistory() {
    this(List.of());
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
      implements
        Serializable {

    /**
     * The point of time at which the event occurred.
     */
    private final Instant timestamp;
    /**
     * A code identifying the event that occurred.
     */
    private final String eventCode;
    /**
     * Supplementary information about the event.
     */
    private final List<String> supplements;

    /**
     * Creates a new instance.
     *
     * @param timestamp The point of time at which the event occurred.
     * @param eventCode A code identifying the event that occurred.
     * @param supplement Supplementary information about the event.
     * Must be {@link Serializable} and should provide a human-readable default representation from
     * its {@code toString()} method.
     * @deprecated Use {@link #Entry(Instant, String, List)} instead.
     */
    @Deprecated
    @ScheduledApiChange(when = "7.0", details = "Will be removed")
    public Entry(Instant timestamp, String eventCode, Object supplement) {
      this.timestamp = requireNonNull(timestamp, "timestamp");
      this.eventCode = requireNonNull(eventCode, "eventCode");
      checkArgument(supplement instanceof Serializable, "supplement is not serializable");
      this.supplements = List.of(supplement.toString());
    }

    /**
     * Creates a new instance.
     *
     * @param timestamp The point of time at which the event occurred.
     * @param eventCode A code identifying the event that occurred.
     * @param supplements Supplementary information about the event.
     */
    public Entry(Instant timestamp, String eventCode, List<String> supplements) {
      this.timestamp = requireNonNull(timestamp, "timestamp");
      this.eventCode = requireNonNull(eventCode, "eventCode");
      this.supplements = List.copyOf(requireNonNull(supplements, "supplements"));
    }

    /**
     * Creates a new instance with an empty supplement.
     *
     * @param timestamp The point of time at which the event occurred.
     * @param eventCode A code identifying the event that occurred.
     */
    public Entry(Instant timestamp, String eventCode) {
      this(timestamp, eventCode, List.of());
    }

    /**
     * Creates a new instance with the timestamp set to the current point of time.
     *
     * @param eventCode A code identifying the event that occurred.
     * @param supplement Supplementary information about the event.
     * Must be {@link Serializable} and should provide a human-readable default representation from
     * its {@code toString()} method.
     * @deprecated Use {@link #Entry(String, List)} instead.
     */
    @Deprecated
    @ScheduledApiChange(when = "7.0", details = "Will be removed")
    public Entry(String eventCode, Object supplement) {
      this(Instant.now(), eventCode, supplement);
    }

    /**
     * Creates a new instance with the timestamp set to the current point of time.
     *
     * @param eventCode A code identifying the event that occurred.
     * @param supplements Supplementary information about the event.
     */
    public Entry(String eventCode, List<String> supplements) {
      this(Instant.now(), eventCode, supplements);
    }

    /**
     * Creates a new instance with the timestamp set to the current point of time and an empty
     * supplement.
     *
     * @param eventCode A code identifying the event that occurred.
     */
    public Entry(String eventCode) {
      this(eventCode, List.of());
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
     * @deprecated Use {@link #getSupplements()} instead.
     */
    @Nonnull
    @Deprecated
    @ScheduledApiChange(when = "7.0", details = "Will be removed")
    public Object getSupplement() {
      return supplements.isEmpty()
          ? ""
          : supplements.getFirst();
    }

    /**
     * Returns supplementary information about the event.
     *
     * @return Supplementary information about the event.
     */
    @Nonnull
    public List<String> getSupplements() {
      return supplements;
    }

    @Override
    public String toString() {
      return "Entry{"
          + "timestamp=" + timestamp
          + ", eventCode=" + eventCode
          + ", supplements=" + supplements
          + '}';
    }
  }
}
