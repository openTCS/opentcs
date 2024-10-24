// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.binding.getevents;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.time.Instant;

/**
 * A generic status message.
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
  {
      @JsonSubTypes.Type(value = OrderStatusMessage.class, name = "TransportOrder"),
      @JsonSubTypes.Type(value = VehicleStatusMessage.class, name = "Vehicle"),
      @JsonSubTypes.Type(value = PeripheralJobStatusMessage.class, name = "PeripheralJob")
  }
)
public abstract class StatusMessage {

  private long sequenceNumber;

  private Instant creationTimeStamp = Instant.now();

  /**
   * Creates a new instance.
   */
  public StatusMessage() {
  }

  public long getSequenceNumber() {
    return sequenceNumber;
  }

  public StatusMessage setSequenceNumber(long sequenceNumber) {
    this.sequenceNumber = sequenceNumber;
    return this;
  }

  public Instant getCreationTimeStamp() {
    return creationTimeStamp;
  }

  public StatusMessage setCreationTimeStamp(Instant creationTimeStamp) {
    this.creationTimeStamp = creationTimeStamp;
    return this;
  }

}
