// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.binding.sse;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.opentcs.data.TCSObjectEvent;

/**
 * A transfer object (to be used with the SSE API) representing a {@link TCSObjectEvent} for an
 * order sequence.
 */
public class OrderSequenceEventTO {

  private final OrderSequenceTO currentObjectState;
  private final OrderSequenceTO previousObjectState;

  public OrderSequenceEventTO(
      @Nullable
      OrderSequenceTO currentObjectState,
      @Nullable
      OrderSequenceTO previousObjectState
  ) {
    this.currentObjectState = currentObjectState;
    this.previousObjectState = previousObjectState;
  }

  public OrderSequenceTO getCurrentObjectState() {
    return currentObjectState;
  }

  public OrderSequenceTO getPreviousObjectState() {
    return previousObjectState;
  }

  // CHECKSTYLE:OFF
  @NoArgsConstructor
  @Getter
  @Setter
  @Accessors(chain = true)
  public static class OrderSequenceTO {

    private String name;
    private Map<String, String> properties;
    private ObjectHistoryTO history;
    private String type;
    private List<String> orders;
    private int finishedIndex;
    private boolean complete;
    private boolean finished;
    private boolean failureFatal;
    private String intendedVehicle;
    private String processingVehicle;
    private Instant creationTime;
    private Instant finishedTime;
  }
  // CHECKSTYLE:ON
}
