/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.binding.outgoing;

import java.time.Instant;

/**
 * A generic status message.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
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
