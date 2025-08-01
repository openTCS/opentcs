// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TimestampScrubberTest {

  private TimestampScrubber scrubber;

  @BeforeEach
  void setUp() {
    scrubber = new TimestampScrubber();
  }

  @Test
  void scrubsInstantTimestamps() {
    String result = scrubber.scrub(Instant.parse("2025-07-22T12:03:00Z").toString());
    assertThat(result).isEqualTo("[Timestamp]");

    result = scrubber.scrub(Instant.ofEpochMilli(123456789).toString());
    assertThat(result).isEqualTo("[Timestamp]");

    result = scrubber.scrub(Instant.EPOCH.toString());
    assertThat(result).isEqualTo("[Timestamp]");

    result = scrubber.scrub(Instant.MIN.toString());
    assertThat(result).isEqualTo("[Timestamp]");

    result = scrubber.scrub(Instant.MAX.toString());
    assertThat(result).isEqualTo("[Timestamp]");
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
          "2025-07-22T12:03:00.1Z",
          "2025-07-22T12:03:00.123Z",
          "2025-07-22T12:03:00.1234Z",
          "2025-07-22T12:03:00.12345Z",
          "2025-07-22T12:03:00.123456Z",
          "2025-07-22T12:03:00.1234567Z",
          "2025-07-22T12:03:00.12345678Z",
          "2025-07-22T12:03:00.123456789Z",
      }
  )
  void scrubsInstantTimestampWithFractions(String timestamp) {
    String result = scrubber.scrub(timestamp);
    assertThat(result).isEqualTo("[Timestamp]");
  }
}
