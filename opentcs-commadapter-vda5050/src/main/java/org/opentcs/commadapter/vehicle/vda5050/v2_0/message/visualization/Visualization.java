// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v2_0.message.visualization;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import javax.annotation.Nonnull;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.Header;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.AgvPosition;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.Velocity;

/**
 * Describes information for visualization purposes.
 * <p>
 * Can be published at a higher rate if wanted. Since bandwidth may be expensive depending on the
 * update rate, all fields are optional.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Visualization
    extends
      Header {

  /**
   * The path to the JSON schema file.
   */
  public static final String JSON_SCHEMA_PATH
      = "/org/opentcs/commadapter/vehicle/vda5050/v2_0/schemas/visualization.schema.json";
  /**
   * [Optional] Current position of the AGV on the map.
   */
  private AgvPosition agvPosition;
  /**
   * [Optional] The AGV's velocity in vehicle coordinates.
   */
  private Velocity velocity;

  public Visualization() {
  }

  public Visualization(
      @Nonnull
      @JsonProperty(required = true, value = "headerId")
      Long headerId,
      @Nonnull
      @JsonProperty(required = true, value = "timestamp")
      Instant timestamp,
      @Nonnull
      @JsonProperty(required = true, value = "version")
      String version,
      @Nonnull
      @JsonProperty(required = true, value = "manufacturer")
      String manufacturer,
      @Nonnull
      @JsonProperty(required = true, value = "serialNumber")
      String serialNumber
  ) {
    super(headerId, timestamp, version, manufacturer, serialNumber);
  }

  public AgvPosition getAgvPosition() {
    return agvPosition;
  }

  public Visualization setAgvPosition(AgvPosition agvPosition) {
    this.agvPosition = agvPosition;
    return this;
  }

  public Velocity getVelocity() {
    return velocity;
  }

  public Visualization setVelocity(Velocity velocity) {
    this.velocity = velocity;
    return this;
  }

  @Override
  public String toString() {
    return "Visualization{"
        + "header=" + super.toString()
        + ", agvPosition=" + agvPosition
        + ", velocity=" + velocity
        + '}';
  }
}
