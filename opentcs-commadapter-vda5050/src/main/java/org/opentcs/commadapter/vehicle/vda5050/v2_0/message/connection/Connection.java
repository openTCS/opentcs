// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v2_0.message.connection;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import javax.annotation.Nonnull;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.Header;

/**
 * Describes the state of the connection between the AGV and the message broker.
 * <p>
 * Also used as a last will message in case the AGV abruptly disconnects from the message broker.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Connection
    extends
      Header {

  /**
   * The path to the JSON schema file.
   */
  public static final String JSON_SCHEMA_PATH
      = "/org/opentcs/commadapter/vehicle/vda5050/v2_0/schemas/connection.schema.json";
  /**
   * The AGV's connection state.
   */
  private ConnectionState connectionState;

  // CHECKSTYLE:OFF (Long lines because some of these parameter declarations are very long.)
  @JsonCreator
  public Connection(
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
      String serialNumber,
      @Nonnull
      @JsonProperty(required = true, value = "connectionState")
      ConnectionState connectionState
  ) {
    super(headerId, timestamp, version, manufacturer, serialNumber);
    this.connectionState = requireNonNull(connectionState, "connectionState");
  }
  // CHECKSTYLE:ON

  public ConnectionState getConnectionState() {
    return connectionState;
  }

  public Connection setConnectionState(
      @Nonnull
      ConnectionState connectionState
  ) {
    this.connectionState = requireNonNull(connectionState, "connectionState");
    return this;
  }

  @Override
  public String toString() {
    return "Connection{"
        + "header=" + super.toString()
        + ", connectionState=" + connectionState
        + '}';
  }
}
