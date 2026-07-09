// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.ObjectHistoryTO;

// CHECKSTYLE:OFF
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Accessors(chain = true)
@JsonPropertyOrder(alphabetic = true)
public class TransportOrderTO {

  @Nonnull
  private String name;
  @Nonnull
  @JsonPropertyOrder(alphabetic = true)
  private Map<String, String> properties;
  @Nonnull
  private ObjectHistoryTO history;
  @Nonnull
  private String type;
  @Nonnull
  private List<String> dependencies;
  @Nonnull
  private List<DriveOrderTO> driveOrders;
  @Nullable
  private String peripheralReservationToken;
  private int currentDriveOrderIndex;
  private int currentRouteStepIndex;
  @Nonnull
  private StateTO state;
  @Nonnull
  private Instant creationTime;
  @Nullable
  private Instant deadline;
  @Nullable
  private Instant finishedTime;
  @Nullable
  private String intendedVehicle;
  @Nullable
  private String processingVehicle;
  @Nullable
  private String wrappingSequence;
  private boolean dispensable;

  public enum StateTO {
    RAW,
    ACTIVE,
    DISPATCHABLE,
    BEING_PROCESSED,
    WITHDRAWN,
    FINISHED,
    FAILED,
    UNROUTABLE
  }

  @NoArgsConstructor
  @Getter
  @Setter
  @EqualsAndHashCode
  @ToString
  @Accessors(chain = true)
  @JsonPropertyOrder(alphabetic = true)
  public static class DriveOrderTO {

    @Nonnull
    private String name;
    @Nonnull
    private DestinationTO destination;
    @Nonnull
    private String transportOrder;
    @Nullable
    private RouteTO route;
    @Nonnull
    private StateTO state;

    public enum StateTO {
      PRISTINE,
      TRAVELLING,
      OPERATING,
      FINISHED,
      FAILED
    }

    @NoArgsConstructor
    @Getter
    @Setter
    @EqualsAndHashCode
    @ToString
    @Accessors(chain = true)
    @JsonPropertyOrder(alphabetic = true)
    public static class DestinationTO {

      @Nonnull
      private String destination;
      @Nonnull
      private String operation;
      @Nonnull
      @JsonPropertyOrder(alphabetic = true)
      private Map<String, String> properties;
    }

    @NoArgsConstructor
    @Getter
    @Setter
    @EqualsAndHashCode
    @ToString
    @Accessors(chain = true)
    @JsonPropertyOrder(alphabetic = true)
    public static class RouteTO {

      private long costs;
      @Nonnull
      private List<StepTO> steps;

      @NoArgsConstructor
      @Getter
      @Setter
      @EqualsAndHashCode
      @ToString
      @Accessors(chain = true)
      @JsonPropertyOrder(alphabetic = true)
      public static class StepTO {

        @Nullable
        private String path;
        @Nullable
        private String sourcePoint;
        @Nonnull
        private String destinationPoint;
        @Nonnull
        private VehicleOrientationTO vehicleOrientation;
        private int routeIndex;
        private long costs;
        private boolean executionAllowed;
        @Nullable
        private ReroutingTypeTO reroutingType;

        public enum VehicleOrientationTO {
          FORWARD,
          BACKWARD,
          UNDEFINED
        }

        public enum ReroutingTypeTO {
          REGULAR,
          FORCED
        }
      }
    }
  }
}
// CHECKSTYLE:ON
