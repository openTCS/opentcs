// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.sse;

import javax.annotation.Nullable;
import org.opentcs.data.TCSObjectEvent;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.VehicleTO;

/**
 * A transfer object (to be used with the SSE API) representing a {@link TCSObjectEvent} for a
 * vehicle.
 */
public class VehicleEventTO {

  private final VehicleTO currentObjectState;
  private final VehicleTO previousObjectState;

  public VehicleEventTO(
      @Nullable
      VehicleTO currentObjectState,
      @Nullable
      VehicleTO previousObjectState
  ) {
    this.currentObjectState = currentObjectState;
    this.previousObjectState = previousObjectState;
  }

  @Nullable
  public VehicleTO getCurrentObjectState() {
    return currentObjectState;
  }

  @Nullable
  public VehicleTO getPreviousObjectState() {
    return previousObjectState;
  }
}
