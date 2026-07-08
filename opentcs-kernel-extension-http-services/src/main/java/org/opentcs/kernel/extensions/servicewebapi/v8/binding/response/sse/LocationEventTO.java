// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.sse;

import javax.annotation.Nullable;
import org.opentcs.data.TCSObjectEvent;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.LocationTO;

/**
 * A transfer object (to be used with the SSE API) representing a {@link TCSObjectEvent} for a
 * location.
 */
public class LocationEventTO {

  private final LocationTO currentObjectState;
  private final LocationTO previousObjectState;

  public LocationEventTO(
      @Nullable
      LocationTO currentObjectState,
      @Nullable
      LocationTO previousObjectState
  ) {
    this.currentObjectState = currentObjectState;
    this.previousObjectState = previousObjectState;
  }

  @Nullable
  public LocationTO getCurrentObjectState() {
    return currentObjectState;
  }

  @Nullable
  public LocationTO getPreviousObjectState() {
    return previousObjectState;
  }
}
