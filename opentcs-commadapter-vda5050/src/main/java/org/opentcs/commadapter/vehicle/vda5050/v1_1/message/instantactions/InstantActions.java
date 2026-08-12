// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v1_1.message.instantactions;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.Header;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.common.Action;

/**
 * Defines actions that an AGV is to execute as soon as they arrive.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InstantActions
    extends
      Header {

  /**
   * The path to the JSON schema file.
   */
  public static final String JSON_SCHEMA_PATH
      = "/org/opentcs/commadapter/vehicle/vda5050/v1_1/schemas/instantActions.schema.json";
  /**
   * List of actions to be executed as soon as they arrive and which are not part of a
   * regular order.
   */
  private List<Action> instantActions;

  /**
   * Creates a new instance.
   * <p>
   * Convenient constructor for creating a new instance without explicitly providing information
   * about the {@link Header} content. Instead, defaults (such as e.g. zero values or empty strings)
   * are used. For instances created with this constructor, the header content must therefore be set
   * subsequently.
   */
  public InstantActions() {
    this(0L, Instant.EPOCH, "", "", "", new ArrayList<>());
  }

  @JsonCreator
  public InstantActions(
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
      @JsonProperty(required = true, value = "instantActions")
      List<Action> actions
  ) {
    super(headerId, timestamp, version, manufacturer, serialNumber);
    this.instantActions = requireNonNull(actions, "actions");
  }

  public List<Action> getInstantActions() {
    return instantActions;
  }

  public InstantActions setInstantActions(List<Action> actions) {
    this.instantActions = requireNonNull(actions, "actions");
    return this;
  }

  @Override
  public String toString() {
    return "InstantActions{"
        + "header=" + super.toString()
        + ", instantActions=" + instantActions
        + '}';
  }
}
