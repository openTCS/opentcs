// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.opentcs.data.model.Vehicle;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.BoundingBoxTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.ColorTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.ObjectHistoryTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.PoseTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.ResourceTO;

/**
 * A transfer object representing a {@link Vehicle} instance.
 */
// CHECKSTYLE:OFF
@NoArgsConstructor
@Getter
@Setter
@Accessors(chain = true)
@JsonPropertyOrder(alphabetic = true)
public class VehicleTO {

  @Nonnull
  private String name;
  @Nonnull
  @JsonPropertyOrder(alphabetic = true)
  private Map<String, String> properties;
  @Nonnull
  private ObjectHistoryTO history;
  @Nonnull
  private BoundingBoxTO boundingBox;
  @Nonnull
  private EnergyLevelThresholdSetTO energyLevelThresholdSet;
  private int energyLevel;
  private int maxVelocity;
  private int maxReverseVelocity;
  @Nonnull
  private String rechargeOperation;
  @Nonnull
  private List<LoadHandlingDeviceTO> loadHandlingDevices;
  @Nonnull
  private TimestampedVehicleStateTO state;
  @Nonnull
  private TimestampedVehicleProcStateTO procState;
  @Nonnull
  private IntegrationLevelTO integrationLevel;
  private boolean paused;
  @Nullable
  private String transportOrder;
  @Nullable
  private String orderSequence;
  @Nonnull
  private List<AcceptableOrderTypeTO> acceptableOrderTypes;
  @Nonnull
  private List<List<ResourceTO>> claimedResources;
  @Nonnull
  private List<List<ResourceTO>> allocatedResources;
  @Nullable
  private String currentPosition;
  @Nonnull
  private PoseTO pose;
  @Nonnull
  private String envelopeKey;
  @Nonnull
  private LayoutTO layout;

  @NoArgsConstructor
  @Getter
  @Setter
  @Accessors(chain = true)
  @JsonPropertyOrder(alphabetic = true)
  public static class EnergyLevelThresholdSetTO {

    private int energyLevelCritical;
    private int energyLevelGood;
    private int energyLevelSufficientlyRecharged;
    private int energyLevelFullyRecharged;
  }

  @NoArgsConstructor
  @Getter
  @Setter
  @Accessors(chain = true)
  @JsonPropertyOrder(alphabetic = true)
  public static class LoadHandlingDeviceTO {

    @Nonnull
    private String label;
    private boolean full;
  }

  @NoArgsConstructor
  @Getter
  @Setter
  @Accessors(chain = true)
  @JsonPropertyOrder(alphabetic = true)
  public static class TimestampedVehicleStateTO {

    @Nonnull
    private StateTO state;
    @Nonnull
    private Instant timestamp;
  }

  @NoArgsConstructor
  @Getter
  @Setter
  @Accessors(chain = true)
  @JsonPropertyOrder(alphabetic = true)
  public static class TimestampedVehicleProcStateTO {

    @Nonnull
    private ProcStateTO procState;
    @Nonnull
    private Instant timestamp;
  }

  public enum IntegrationLevelTO {
    TO_BE_IGNORED,
    TO_BE_NOTICED,
    TO_BE_RESPECTED,
    TO_BE_UTILIZED
  }

  public enum StateTO {
    UNKNOWN,
    UNAVAILABLE,
    ERROR,
    IDLE,
    EXECUTING,
    CHARGING
  }

  public enum ProcStateTO {
    IDLE,
    AWAITING_ORDER,
    PROCESSING_ORDER
  }

  @NoArgsConstructor
  @Getter
  @Setter
  @Accessors(chain = true)
  @JsonPropertyOrder(alphabetic = true)
  public static class AcceptableOrderTypeTO {

    @Nonnull
    private String name;
    private int priority;
  }

  @NoArgsConstructor
  @Getter
  @Setter
  @Accessors(chain = true)
  @JsonPropertyOrder(alphabetic = true)
  public static class LayoutTO {

    @Nonnull
    private ColorTO routeColor;
  }
}
// CHECKSTYLE:ON
