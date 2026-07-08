// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.time.Instant;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.opentcs.data.peripherals.PeripheralJob;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.ObjectHistoryTO;

/**
 * A transfer object representing a {@link PeripheralJob} instance.
 */
// CHECKSTYLE:OFF
@NoArgsConstructor
@Getter
@Setter
@Accessors(chain = true)
@JsonPropertyOrder(alphabetic = true)
public class PeripheralJobTO {
  @Nonnull
  private String name;
  @Nonnull
  @JsonPropertyOrder(alphabetic = true)
  private Map<String, String> properties;
  @Nonnull
  private ObjectHistoryTO history;
  @Nonnull
  private String reservationToken;
  @Nullable
  private String relatedVehicle;
  @Nullable
  private String relatedTransportOrder;
  @Nonnull
  private PeripheralOperationTO peripheralOperation;
  @Nonnull
  private StateTO state;
  @Nonnull
  private Instant creationTime;
  @Nullable
  private Instant finishedTime;

  @NoArgsConstructor
  @Getter
  @Setter
  @Accessors(chain = true)
  public static class PeripheralOperationTO {

    @Nonnull
    private String location;
    @Nonnull
    private String operation;
    @Nonnull
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
