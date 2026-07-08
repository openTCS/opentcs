// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.sse;

import javax.annotation.Nullable;
import org.opentcs.data.TCSObjectEvent;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.PeripheralJobTO;

/**
 * A transfer object (to be used with the SSE API) representing a {@link TCSObjectEvent} for a
 * peripheral job.
 */
public class PeripheralJobEventTO {

  private final PeripheralJobTO currentObjectState;
  private final PeripheralJobTO previousObjectState;

  public PeripheralJobEventTO(
      @Nullable
      PeripheralJobTO currentObjectState,
      @Nullable
      PeripheralJobTO previousObjectState
  ) {
    this.currentObjectState = currentObjectState;
    this.previousObjectState = previousObjectState;
  }

  @Nullable
  public PeripheralJobTO getCurrentObjectState() {
    return currentObjectState;
  }

  @Nullable
  public PeripheralJobTO getPreviousObjectState() {
    return previousObjectState;
  }
}
