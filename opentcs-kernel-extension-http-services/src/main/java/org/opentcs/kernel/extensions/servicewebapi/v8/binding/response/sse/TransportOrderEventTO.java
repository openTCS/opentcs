// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.sse;

import javax.annotation.Nullable;
import org.opentcs.data.TCSObjectEvent;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.TransportOrderTO;

/**
 * A transfer object (to be used with the SSE API) representing a {@link TCSObjectEvent} for a
 * transport order.
 */
public class TransportOrderEventTO {

  private final TransportOrderTO currentObjectState;
  private final TransportOrderTO previousObjectState;

  public TransportOrderEventTO(
      @Nullable
      TransportOrderTO currentObjectState,
      @Nullable
      TransportOrderTO previousObjectState
  ) {
    this.currentObjectState = currentObjectState;
    this.previousObjectState = previousObjectState;
  }

  @Nullable
  public TransportOrderTO getCurrentObjectState() {
    return currentObjectState;
  }

  @Nullable
  public TransportOrderTO getPreviousObjectState() {
    return previousObjectState;
  }
}
