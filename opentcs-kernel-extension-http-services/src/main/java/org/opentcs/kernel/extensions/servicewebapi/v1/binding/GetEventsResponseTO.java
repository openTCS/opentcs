// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.binding;

import static java.util.Objects.requireNonNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.getevents.StatusMessage;

/**
 * A set of status messages sent via the status channel.
 */
public class GetEventsResponseTO {

  private Instant timeStamp = Instant.now();

  private List<StatusMessage> statusMessages = new ArrayList<>();

  /**
   * Creates a new instance.
   */
  public GetEventsResponseTO() {
  }

  public List<StatusMessage> getStatusMessages() {
    return statusMessages;
  }

  public GetEventsResponseTO setStatusMessages(List<StatusMessage> statusMessages) {
    this.statusMessages = requireNonNull(statusMessages, "statusMessages");
    return this;
  }

  public Instant getTimeStamp() {
    return timeStamp;
  }

  public GetEventsResponseTO setTimeStamp(Instant timeStamp) {
    this.timeStamp = requireNonNull(timeStamp, "timeStamp");
    return this;
  }
}
