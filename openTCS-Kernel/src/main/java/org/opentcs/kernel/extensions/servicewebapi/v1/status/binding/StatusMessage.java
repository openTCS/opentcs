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
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.time.Instant;

/**
 * A generic status message.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = OrderStatusMessage.class, name = "TransportOrder")
  ,
  @JsonSubTypes.Type(value = VehicleStatusMessage.class, name = "Vehicle")
})
public abstract class StatusMessage {

  @JsonProperty(required = true)
  @JsonPropertyDescription("The (unique) sequence number of this status message")
  private long sequenceNumber;

  @JsonProperty(required = true)
  @JsonPropertyDescription("When this status message was created")
  private Instant creationTimeStamp = Instant.now();

  /**
   * Creates a new instance.
   */
  public StatusMessage() {
  }

  public long getSequenceNumber() {
    return sequenceNumber;
  }

  public void setSequenceNumber(long sequenceNumber) {
    this.sequenceNumber = sequenceNumber;
  }

  public Instant getCreationTimeStamp() {
    return creationTimeStamp;
  }

  public void setCreationTimeStamp(Instant creationTimeStamp) {
    this.creationTimeStamp = creationTimeStamp;
  }

}
