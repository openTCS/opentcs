// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared;

import static java.util.Objects.requireNonNull;

import java.time.Instant;
import java.util.List;

/**
 */
public class ObjectHistoryTO {
  private List<ObjectHistoryEntryTO> entries;

  public ObjectHistoryTO(
      List<ObjectHistoryEntryTO> entries
  ) {
    this.entries = requireNonNull(entries, "entries");
  }

  public List<ObjectHistoryEntryTO> getEntries() {
    return entries;
  }

  public ObjectHistoryTO setEntries(List<ObjectHistoryEntryTO> entries) {
    this.entries = entries;
    return this;
  }

  /**
   */
  public static class ObjectHistoryEntryTO {
    private Instant timestamp;
    private String eventCode;
    private List<String> supplements;

    public ObjectHistoryEntryTO(
        Instant timeStamp,
        String eventCode,
        List<String> supplements
    ) {
      this.timestamp = requireNonNull(timeStamp, "timeStamp");
      this.eventCode = requireNonNull(eventCode, "eventCode");
      this.supplements = requireNonNull(supplements, "supplements");
    }

    public Instant getTimestamp() {
      return timestamp;
    }

    public ObjectHistoryEntryTO setTimestamp(Instant timestamp) {
      this.timestamp = timestamp;
      return this;
    }

    public String getEventCode() {
      return eventCode;
    }

    public ObjectHistoryEntryTO setEventCode(String eventCode) {
      this.eventCode = eventCode;
      return this;
    }

    public List<String> getSupplements() {
      return supplements;
    }

    public ObjectHistoryEntryTO setSupplements(List<String> supplements) {
      this.supplements = supplements;
      return this;
    }
  }
}
