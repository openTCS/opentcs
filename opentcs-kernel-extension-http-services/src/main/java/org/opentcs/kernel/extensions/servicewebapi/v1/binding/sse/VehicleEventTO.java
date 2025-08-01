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

  public VehicleTO getCurrentObjectState() {
    return currentObjectState;
  }

  public VehicleTO getPreviousObjectState() {
    return previousObjectState;
  }

  // CHECKSTYLE:OFF
  @NoArgsConstructor
  @Getter
  @Setter
  @Accessors(chain = true)
  public static class VehicleTO {

    private String name;
    private Map<String, String> properties;
    private ObjectHistoryTO history;
    private BoundingBoxTO boundingBox;
    private EnergyLevelThresholdSetTO energyLevelThresholdSet;
    private int energyLevel;
    private int maxVelocity;
    private int maxReverseVelocity;
    private String rechargeOperation;
    private List<LoadHandlingDeviceTO> loadHandlingDevices;
    private TimestampedVehicleStateTO state;
    private TimestampedVehicleProcStateTO procState;
    private IntegrationLevelTO integrationLevel;
    private boolean paused;
    private String transportOrder;
    private String orderSequence;
    private List<AcceptableOrderTypeTO> acceptableOrderTypes;
    private List<List<String>> claimedResources;
    private List<List<String>> allocatedResources;
    private String currentPosition;
    private PoseTO pose;
    private String envelopeKey;
    private LayoutTO layout;
  }

  @NoArgsConstructor
  @Getter
  @Setter
  @Accessors(chain = true)
  public static class BoundingBoxTO {

    private long length;
    private long width;
    private long height;
    private CoupleTO referenceOffset;
  }

  @NoArgsConstructor
  @Getter
  @Setter
  @Accessors(chain = true)
  public static class CoupleTO {

    private long x;
    private long y;
  }

  @NoArgsConstructor
  @Getter
  @Setter
  @Accessors(chain = true)
  public static class EnergyLevelThresholdSetTO {

    private int energyLevelCritical;
    private int energyLevelGood;
    private int energyLevelFullyRecharged;
    private int energyLevelSufficientlyRecharged;
  }

  @NoArgsConstructor
  @Getter
  @Setter
  @Accessors(chain = true)
  public static class LoadHandlingDeviceTO {

    private String label;
    private boolean full;
  }

  @NoArgsConstructor
  @Getter
  @Setter
  @Accessors(chain = true)
  public static class TimestampedVehicleStateTO {

    private StateTO state;
    private Instant timestamp;
  }

  @NoArgsConstructor
  @Getter
  @Setter
  @Accessors(chain = true)
  public static class TimestampedVehicleProcStateTO {

    private ProcStateTO procState;
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
  public static class AcceptableOrderTypeTO {

    private String name;
    private int priority;
  }

  @NoArgsConstructor
  @Getter
  @Setter
  @Accessors(chain = true)
  public static class PoseTO {

    private TripleTO position;
    private double orientationAngle;
  }

  @NoArgsConstructor
  @Getter
  @Setter
  @Accessors(chain = true)
  public static class TripleTO {

    private long x;
    private long y;
    private long z;
  }

  @NoArgsConstructor
  @Getter
  @Setter
  @Accessors(chain = true)
  public static class LayoutTO {

    private ColorTO routeColor;

    @NoArgsConstructor
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class ColorTO {

      private int red;
      private int green;
      private int blue;
    }
  }
  // CHECKSTYLE:ON
}
