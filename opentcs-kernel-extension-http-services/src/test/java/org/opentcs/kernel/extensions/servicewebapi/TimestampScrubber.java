// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi;

import java.time.Instant;
import org.approvaltests.scrubbers.RegExScrubber;

/**
 * A scrubber that replaces timestamps in the format "YYYY-MM-DD'T'hh:mm:ss.fffffffff'Z'" with the
 * string "[Timestamp]".
 */
public class TimestampScrubber
    extends
      RegExScrubber {

  /**
   * The regular expression pattern that matches {@link Instant} timestamps and also considers
   * {@link Instant#MAX} and {@link Instant#MIN}.
   */
  private static final String INSTANT_PATTERN
      = "[+-]?\\d{4,10}-\\d{1,2}-\\d{1,2}T\\d{1,2}:\\d{2}(:\\d{2})?(\\.\\d{1,9})?Z";

  /**
   * Creates a new instance.
   */
  public TimestampScrubber() {
    super(INSTANT_PATTERN, "[Timestamp]");
  }
}
