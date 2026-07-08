// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.sse;

import javax.annotation.Nullable;
import org.opentcs.data.TCSObjectEvent;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.OrderSequenceTO;

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
}
