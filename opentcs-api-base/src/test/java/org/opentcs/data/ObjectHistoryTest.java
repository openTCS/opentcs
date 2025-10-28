// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.data;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
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

    assertThrows(
        UnsupportedOperationException.class,
        () -> history.getEntries().add(entry2)
    );
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
  void checkIfEntryHasTimestampEventCodeAndSupplements() {
    Instant timestamp = Instant.now();
    Entry entry = new Entry(timestamp, "eventCode1", List.of("supplement"));

    assertEquals(entry.getTimestamp(), timestamp);
    assertThat(entry.getEventCode(), is(equalTo("eventCode1")));
    assertThat(entry.getSupplements(), hasSize(1));
    assertThat(entry.getSupplements(), contains("supplement"));
  }

  @Test
  void checkIfEntryHasNoSupplements() {
    Instant timestamp = Instant.now();
    Entry entry = new Entry(timestamp, "eventCode1");

    assertEquals(entry.getTimestamp(), timestamp);
    assertThat(entry.getEventCode(), is(equalTo("eventCode1")));
    assertThat(entry.getSupplements(), is(empty()));
  }

  @Test
  void checkIfEntryHasTimestamp() {
    Entry entry = new Entry("eventCode1", List.of("supplement"));

    assertThat(entry.getTimestamp(), is(notNullValue()));
    assertThat(entry.getEventCode(), is(equalTo("eventCode1")));
    assertThat(entry.getSupplements(), contains("supplement"));
  }

  @Test
  void checkIfEntryHasTimestampAndNoSupplements() {
    Entry entry = new Entry("eventCode1");

    assertThat(entry.getTimestamp(), is(notNullValue()));
    assertThat(entry.getEventCode(), is(equalTo("eventCode1")));
    assertThat(entry.getSupplements(), is(empty()));
  }

}
