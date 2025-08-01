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

  public TransportOrderTO getCurrentObjectState() {
    return currentObjectState;
  }

  public TransportOrderTO getPreviousObjectState() {
    return previousObjectState;
  }

  // CHECKSTYLE:OFF
  @NoArgsConstructor
  @Getter
  @Setter
  @Accessors(chain = true)
  public static class TransportOrderTO {

    private String name;
    private Map<String, String> properties;
    private ObjectHistoryTO history;
    private String type;
    private List<String> dependencies;
    private List<DriveOrderTO> driveOrders;
    private String peripheralReservationToken;
    private int currentDriveOrderIndex;
    private int currentRouteStepIndex;
    private StateTO state;
    private Instant creationTime;
    private Instant deadline;
    private Instant finishedTime;
    private String intendedVehicle;
    private String processingVehicle;
    private String wrappingSequence;
    private boolean dispensable;
  }

  @NoArgsConstructor
  @Getter
  @Setter
  @Accessors(chain = true)
  public static class DriveOrderTO {

    private String name;
    private DestinationTO destination;
    private String transportOrder;
    private RouteTO route;
    private StateTO state;

    @NoArgsConstructor
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class DestinationTO {

      private String destination;
      private String operation;
      private Map<String, String> properties;
    }

    @NoArgsConstructor
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class RouteTO {

      private List<StepTO> steps;
      private long costs;

      @NoArgsConstructor
      @Getter
      @Setter
      @Accessors(chain = true)
      public static class StepTO {

        private String path;
        private String sourcePoint;
        private String destinationPoint;
        private VehicleOrientationTO vehicleOrientation;
        private int routeIndex;
        private long costs;
        private boolean executionAllowed;
        private ReroutingTypeTO reroutingType;
      }

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

    public enum StateTO {
      PRISTINE,
      TRAVELLING,
      OPERATING,
      FINISHED,
      FAILED
    }
  }

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
  // CHECKSTYLE:ON
}
