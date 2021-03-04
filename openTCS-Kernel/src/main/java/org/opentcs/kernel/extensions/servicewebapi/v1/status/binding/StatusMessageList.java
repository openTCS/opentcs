/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.status.binding;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import static java.util.Objects.requireNonNull;

/**
 * A set of status messages sent via the status channel.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class StatusMessageList {

  @JsonProperty(required = true)
  @JsonPropertyDescription("The point of time at which this data structure was created")
  private Instant timeStamp = Instant.now();

  @JsonProperty(required = true)
  @JsonPropertyDescription("The status messages")
  private List<StatusMessage> statusMessages = new LinkedList<>();

  /**
   * Creates a new instance.
   */
  public StatusMessageList() {
  }

  public List<StatusMessage> getStatusMessages() {
    return statusMessages;
  }

  public void setStatusMessages(List<StatusMessage> statusMessages) {
    this.statusMessages = requireNonNull(statusMessages, "statusMessages");
  }

  public Instant getTimeStamp() {
    return timeStamp;
  }

  public void setTimeStamp(Instant timeStamp) {
    this.timeStamp = requireNonNull(timeStamp, "timeStamp");
  }
}
