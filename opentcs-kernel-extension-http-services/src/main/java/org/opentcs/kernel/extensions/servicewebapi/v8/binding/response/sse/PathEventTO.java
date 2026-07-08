// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.sse;

import javax.annotation.Nullable;
import org.opentcs.data.TCSObjectEvent;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.PathTO;

/**
 * A transfer object (to be used with the SSE API) representing a {@link TCSObjectEvent} for a
 * path.
 */
public class PathEventTO {

  private final PathTO currentObjectState;
  private final PathTO previousObjectState;

  public PathEventTO(
      @Nullable
      PathTO currentObjectState,
      @Nullable
      PathTO previousObjectState
  ) {
    this.currentObjectState = currentObjectState;
    this.previousObjectState = previousObjectState;
  }

  @Nullable
  public PathTO getCurrentObjectState() {
    return currentObjectState;
  }

  @Nullable
  public PathTO getPreviousObjectState() {
    return previousObjectState;
  }
}
