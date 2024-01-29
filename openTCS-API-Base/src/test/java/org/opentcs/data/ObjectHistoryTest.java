/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.opentcs.data.ObjectHistory.Entry;

/**
 * Unit tests for {@link ObjectHistory}.
 */
class ObjectHistoryTest {

  @Test
  void checkIfObjectHistoryInitiallyIsEmpty() {
    ObjectHistory history = new ObjectHistory();
    assertThat(history.getEntries(), hasSize(0));
  }

  @Test
  void checkIfEntryListIsUnmodifiable() {
    Entry entry1 = new Entry(Instant.now(), "eventCode1");
    Entry entry2 = new Entry(Instant.now(), "eventCode2");

    ObjectHistory history = new ObjectHistory().withEntries(List.of(entry1));

    assertThrows(UnsupportedOperationException.class,
                 () -> history.getEntries().add(entry2));
  }

  @Test
  void testObjectHistoryCreationWithEntries() {
    Entry entry1 = new Entry(Instant.now(), "eventCode1");
    Entry entry2 = new Entry(Instant.now(), "eventCode2");

    List<Entry> entries = Arrays.asList(entry1, entry2);

    ObjectHistory history = new ObjectHistory().withEntries(entries);

    assertThat(history.getEntries(), hasSize(2));
    assertThat(history.getEntries(), contains(entry1, entry2));
  }

  @Test
  void checkIfObjectHistoryWithEntryAppendedCreateAndAppendTheGivenEntriesCorrect() {
    Entry entry1 = new Entry(Instant.now(), "eventCode1");
    Entry entry2 = new Entry(Instant.now(), "eventCode2");

    ObjectHistory history = new ObjectHistory();

    history = history.withEntryAppended(entry1);
    history = history.withEntryAppended(entry2);

    assertThat(history.getEntries(), hasSize(2));
    assertThat(history.getEntries(), contains(entry1, entry2));
  }

  @Test
  void checkIfEntryHasTimestampEventCodeAndSupplement() {
    Instant timestamp = Instant.now();
    Entry entry = new Entry(timestamp, "eventCode1", "supplement");

    assertEquals(entry.getTimestamp(), timestamp);
    assertEquals(entry.getEventCode(), "eventCode1");
    assertEquals(entry.getSupplement(), "supplement");
  }

  @Test
  void throwIfSupplementIsNotSerializable() {
    assertThrows(IllegalArgumentException.class,
                 () -> new Entry(Instant.now(), "eventCode", new Object()));
  }

  @Test
  void checkIfEntryHasNoSupplement() {
    Instant timestamp = Instant.now();
    Entry entry = new Entry(timestamp, "eventCode1");

    assertEquals(entry.getTimestamp(), timestamp);
    assertEquals(entry.getEventCode(), "eventCode1");
    assertEquals(entry.getSupplement(), "");
  }

  @Test
  void checkIfEntryHasTimestamp() {
    Entry entry = new Entry("eventCode1", "supplement");

    assertTrue(entry.getTimestamp() != null);
    assertEquals(entry.getEventCode(), "eventCode1");
    assertEquals(entry.getSupplement(), "supplement");
  }

  @Test
  void checkIfEntryHasTimestampAnNoSupplement() {
    Entry entry = new Entry("eventCode1");

    assertTrue(entry.getTimestamp() != null);
    assertEquals(entry.getEventCode(), "eventCode1");
    assertEquals(entry.getSupplement(), "");
  }

}
