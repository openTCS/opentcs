// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v1_1.message;

import static java.util.Objects.requireNonNull;
import static org.opentcs.commadapter.vehicle.vda5050.common.Limits.UINT32_MAX_VALUE;
import static org.opentcs.util.Assertions.checkInRange;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.time.Instant;
import javax.annotation.Nonnull;

/**
 * Defines basic information contained in all VDA5050 messages.
 */
public abstract class Header
    implements
      Serializable {

  /**
   * {@code headerId} of a message.
   * <p>
   * The {@code headerId} is defined per topic and incremented by 1 with each sent (but not
   * necessarily received) message.
   */
  private Long headerId;
  /**
   * Timestamp in ISO8601 format. (I.e. YYYY-MM-DDTHH:mm:ss.sssZ)
   */
  private Instant timestamp;
  /**
   * Version of the protocol [Major].[Minor].[Patch].
   */
  private String version;
  /**
   * Manufacturer of the AGV.
   */
  private String manufacturer;
  /**
   * Serial number of the AGV.
   */
  private String serialNumber;

  public Header() {
  }

  @JsonCreator
  public Header(
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
    this.headerId = checkInRange(
        requireNonNull(headerId, "headerId"),
        0,
        UINT32_MAX_VALUE,
        "headerId"
    );
    this.timestamp = requireNonNull(timestamp, "timestamp");
    this.version = requireNonNull(version, "version");
    this.manufacturer = requireNonNull(manufacturer, "manufacturer");
    this.serialNumber = requireNonNull(serialNumber, "serialNumber");
  }

  public Long getHeaderId() {
    return headerId;
  }

  public Header setHeaderId(
      @Nonnull
      Long headerId
  ) {
    this.headerId = checkInRange(
        requireNonNull(headerId, "headerId"),
        0,
        UINT32_MAX_VALUE,
        "headerId"
    );
    return this;
  }

  public Instant getTimestamp() {
    return timestamp;
  }

  public Header setTimestamp(
      @Nonnull
      Instant timestamp
  ) {
    this.timestamp = requireNonNull(timestamp, "timestamp");
    return this;
  }

  public String getVersion() {
    return version;
  }

  public Header setVersion(
      @Nonnull
      String version
  ) {
    this.version = requireNonNull(version, "version");
    return this;
  }

  public String getManufacturer() {
    return manufacturer;
  }

  public Header setManufacturer(
      @Nonnull
      String manufacturer
  ) {
    this.manufacturer = requireNonNull(manufacturer, "manufacturer");
    return this;
  }

  public String getSerialNumber() {
    return serialNumber;
  }

  public Header setSerialNumber(
      @Nonnull
      String serialNumber
  ) {
    this.serialNumber = requireNonNull(serialNumber, "serialNumber");
    return this;
  }

  @Override
  public String toString() {
    return "Header{"
        + "headerId=" + headerId
        + ", timestamp=" + timestamp
        + ", version=" + version
        + ", manufacturer=" + manufacturer
        + ", serialNumber=" + serialNumber
        + '}';
  }
}
