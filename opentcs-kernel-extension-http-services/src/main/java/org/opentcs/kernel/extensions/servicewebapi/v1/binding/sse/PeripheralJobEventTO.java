// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.binding.sse;

import java.time.Instant;
import java.util.Map;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.opentcs.data.TCSObjectEvent;

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

  public PeripheralJobTO getCurrentObjectState() {
    return currentObjectState;
  }

  public PeripheralJobTO getPreviousObjectState() {
    return previousObjectState;
  }

  // CHECKSTYLE:OFF
  @NoArgsConstructor
  @Getter
  @Setter
  @Accessors(chain = true)
  public static class PeripheralJobTO {

    private String name;
    private Map<String, String> properties;
    private ObjectHistoryTO history;
    private String reservationToken;
    private String relatedVehicle;
    private String relatedTransportOrder;
    private PeripheralOperationTO peripheralOperation;
    private StateTO state;
    private Instant creationTime;
    private Instant finishedTime;

    @NoArgsConstructor
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class PeripheralOperationTO {

      private String location;
      private String operation;
      private ExecutionTriggerTO executionTrigger;
      private boolean completionRequired;

      public enum ExecutionTriggerTO {
        IMMEDIATE,
        AFTER_ALLOCATION,
        AFTER_MOVEMENT
      }
    }

    public enum StateTO {
      TO_BE_PROCESSED,
      BEING_PROCESSED,
      FINISHED,
      FAILED
    }
    // CHECKSTYLE:ON
  }
}
