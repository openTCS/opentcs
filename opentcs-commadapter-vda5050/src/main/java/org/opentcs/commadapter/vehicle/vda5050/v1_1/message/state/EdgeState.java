// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v1_1.message.state;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import javax.annotation.Nonnull;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.common.Trajectory;

/**
 * Information about an edge the AGV still has to traverse.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EdgeState
    implements
      Serializable {

  /**
   * Unique edge identification.
   */
  private String edgeId;
  /**
   * Sequence id to differentiate between multiple edges with the same {@code edgeId}.
   */
  private Long sequenceId;
  /**
   * [Optional] Additional information on the edge.
   */
  private String edgeDescription;
  /**
   * Whether the edge is released or not.
   * <p>
   * Interpretation of values:
   * <ul>
   * <li>{@code true} (released) indicates that the edge is part of the base.</li>
   * <li>{@code false} (planned) indicates that the edge is part of the horizon.</li>
   * </ul>
   */
  private Boolean released;
  /**
   * [Optional] The trajectory is to be communicated as NURBS.
   * <p>
   * Trajectory segments are from the point where the AGV starts to enter the edge until the point
   * where it reports that the next node was traversed.
   */
  private Trajectory trajectory;

  @JsonCreator
  public EdgeState(
      @Nonnull
      @JsonProperty(required = true, value = "edgeId")
      String edgeId,
      @Nonnull
      @JsonProperty(required = true, value = "sequenceId")
      Long sequenceId,
      @Nonnull
      @JsonProperty(required = true, value = "released")
      Boolean released
  ) {
    this.edgeId = requireNonNull(edgeId, "edgeId");
    this.sequenceId = requireNonNull(sequenceId, "sequenceId");
    this.released = requireNonNull(released, "released");
  }

  public String getEdgeId() {
    return edgeId;
  }

  public EdgeState setEdgeId(
      @Nonnull
      String edgeId
  ) {
    this.edgeId = requireNonNull(edgeId, "edgeId");
    return this;
  }

  public String getEdgeDescription() {
    return edgeDescription;
  }

  public EdgeState setEdgeDescription(String edgeDescription) {
    this.edgeDescription = edgeDescription;
    return this;
  }

  public Boolean isReleased() {
    return released;
  }

  public EdgeState setReleased(
      @Nonnull
      Boolean released
  ) {
    this.released = requireNonNull(released, "released");
    return this;
  }

  public Long getSequenceId() {
    return sequenceId;
  }

  public EdgeState setSequenceId(
      @Nonnull
      Long sequenceId
  ) {
    this.sequenceId = requireNonNull(sequenceId, "sequenceId");
    return this;
  }

  public Trajectory getTrajectory() {
    return trajectory;
  }

  public EdgeState setTrajectory(Trajectory trajectory) {
    this.trajectory = trajectory;
    return this;
  }

  @Override
  public String toString() {
    return "EdgeState{" + "edgeId=" + edgeId
        + ", sequenceId=" + sequenceId
        + ", edgeDescription=" + edgeDescription
        + ", released=" + released
        + ", trajectory=" + trajectory
        + '}';
  }

}
