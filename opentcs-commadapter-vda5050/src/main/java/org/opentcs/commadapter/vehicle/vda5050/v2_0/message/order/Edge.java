// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v2_0.message.order;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Nonnull;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.Action;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.Trajectory;

/**
 * A directional connection between two {@link Node}s.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Edge
    implements
      Serializable {

  /**
   * Unique edge identification.
   */
  private String edgeId;
  /**
   * Number to track the sequence of nodes and edges in an order and to simplify order updates.
   * <p>
   * The variable {@code sequenceId} runs across all nodes and edges of the same order and is reset
   * when a new {@code orderId} is issued.
   */
  private Long sequenceId;
  /**
   * [Optional] Additional information on the edge.
   */
  private String edgeDescription;
  /**
   * Whether the node is released or not.
   * <p>
   * Interpretation of values:
   * <ul>
   * <li>{@code true} (released) indicates that the node is part of the base.</li>
   * <li>{@code false} (planned) indicates that the node is part of the horizon.</li>
   * </ul>
   */
  private Boolean released;
  /**
   * The {@code nodeId} of the start node.
   */
  private String startNodeId;
  /**
   * The {@code nodeId} of the end node.
   */
  private String endNodeId;
  /**
   * [Optional] Permitted maximum speed on the edge(in m/s).
   * <p>
   * Speed is defined by the fastest measurement of the vehicle.
   */
  private Double maxSpeed;
  /**
   * [Optional] Permitted maximum height of the vehicle on the edge, including the load (in m).
   */
  private Double maxHeight;
  /**
   * [Optional] Permitted minimal height of the load handling device on the edge (in m).
   */
  private Double minHeight;
  /**
   * [Optional] Orientation of the AGV on the edge.
   * <p>
   * The value {@code orientationType} defines if it has to be interpreted relative to the global
   * project specific map coordinate system or tangential to the edge. In case of interpreted
   * tangential to the edge 0.0 = forwards and PI = backwards.
   * <p>
   * If AGV starts in different orientation, rotate the vehicle on the edge to the desired
   * orientation if {@code rotationAllowed} is set to {@code true}. If {@code rotationAllowed} is
   * {@code false}, rotate before entering the edge. If that is not possible, reject the order.
   * <p>
   * If no trajectory is defined, apply the rotation to the direct path between the two connecting
   * nodes of the edge. If a trajectory is defined for the edge, apply the orientation to the
   * trajectory.
   */
  private Double orientation;
  /**
   * [Optional] Sets direction at junctions for line-guided vehicles, to be defined initially
   * (vehicle-individual).
   * <p>
   * Example: left, right, straight.
   */
  private String direction;
  /**
   * [Optional] Whether rotation on the edge is allowed or not.
   */
  private Boolean rotationAllowed;
  /**
   * [Optional] Maximum rotation speed (in rad/s).
   */
  private Double maxRotationSpeed;
  /**
   * [Optional] Length of the path from {@code startNode} to {@code endNode} (in m).
   * <p>
   * This value is used by line-guided AGVs to decrease their speed before reaching a stop position.
   */
  private Double length;
  /**
   * [Optional] Trajectory object for this edge as NURBS.
   * <p>
   * Defines the curve on which the AGV should move between {@code startNode} and {@code endNode}.
   * Can be omitted if an AGV cannot process trajectories or if an AGV plans its own trajectory.
   */
  private Trajectory trajectory;
  /**
   * List of {@link Action}s to be executed on the node.
   * <p>
   * An action triggered by an edge will only be active for the time that the AGV is traversing the
   * edge which triggered the action. When the AGV leaves the edge, the action will stop and the
   * state before entering the edge will be restored. Empty array if no actions required.
   */
  private List<Action> actions;

  @JsonCreator
  public Edge(
      @Nonnull
      @JsonProperty(required = true, value = "edgeId")
      String edgeId,
      @Nonnull
      @JsonProperty(required = true, value = "sequenceId")
      Long sequenceId,
      @Nonnull
      @JsonProperty(required = true, value = "released")
      Boolean released,
      @Nonnull
      @JsonProperty(required = true, value = "startNodeId")
      String startNodeId,
      @Nonnull
      @JsonProperty(required = true, value = "endNodeId")
      String endNodeId,
      @Nonnull
      @JsonProperty(required = true, value = "actions")
      List<Action> actions
  ) {
    this.edgeId = requireNonNull(edgeId, "edgeId");
    this.sequenceId = requireNonNull(sequenceId, "sequenceId");
    this.released = requireNonNull(released, "released");
    this.startNodeId = requireNonNull(startNodeId, "startNodeId");
    this.endNodeId = requireNonNull(endNodeId, "endNodeId");
    this.actions = requireNonNull(actions, "actions");
  }

  public String getEdgeId() {
    return edgeId;
  }

  public Edge setEdgeId(
      @Nonnull
      String edgeId
  ) {
    this.edgeId = requireNonNull(edgeId, "edgeId");
    return this;
  }

  public Long getSequenceId() {
    return sequenceId;
  }

  public Edge setSequenceId(
      @Nonnull
      Long sequenceId
  ) {
    this.sequenceId = requireNonNull(sequenceId, "sequenceId");
    return this;
  }

  public String getEdgeDescription() {
    return edgeDescription;
  }

  public Edge setEdgeDescription(String edgeDescription) {
    this.edgeDescription = edgeDescription;
    return this;
  }

  public Boolean isReleased() {
    return released;
  }

  public Edge setReleased(
      @Nonnull
      Boolean released
  ) {
    this.released = requireNonNull(released, "released");
    return this;
  }

  public String getStartNodeId() {
    return startNodeId;
  }

  public Edge setStartNodeId(
      @Nonnull
      String startNodeId
  ) {
    this.startNodeId = requireNonNull(startNodeId, "startNodeId");
    return this;
  }

  public String getEndNodeId() {
    return endNodeId;
  }

  public Edge setEndNodeId(
      @Nonnull
      String endNodeId
  ) {
    this.endNodeId = requireNonNull(endNodeId, "endNodeId");
    return this;
  }

  public Double getMaxSpeed() {
    return maxSpeed;
  }

  public Edge setMaxSpeed(Double maxSpeed) {
    this.maxSpeed = maxSpeed;
    return this;
  }

  public Double getMaxHeight() {
    return maxHeight;
  }

  public Edge setMaxHeight(Double maxHeight) {
    this.maxHeight = maxHeight;
    return this;
  }

  public Double getMinHeight() {
    return minHeight;
  }

  public Edge setMinHeight(Double minHeight) {
    this.minHeight = minHeight;
    return this;
  }

  public Double getOrientation() {
    return orientation;
  }

  public Edge setOrientation(Double orientation) {
    this.orientation = orientation;
    return this;
  }

  public String getDirection() {
    return direction;
  }

  public Edge setDirection(String direction) {
    this.direction = direction;
    return this;
  }

  public Boolean getRotationAllowed() {
    return rotationAllowed;
  }

  public Edge setRotationAllowed(Boolean rotationAllowed) {
    this.rotationAllowed = rotationAllowed;
    return this;
  }

  public Double getMaxRotationSpeed() {
    return maxRotationSpeed;
  }

  public Edge setMaxRotationSpeed(Double maxRotationSpeed) {
    this.maxRotationSpeed = maxRotationSpeed;
    return this;
  }

  public Trajectory getTrajectory() {
    return trajectory;
  }

  public Edge setTrajectory(Trajectory trajectory) {
    this.trajectory = trajectory;
    return this;
  }

  public List<Action> getActions() {
    return actions;
  }

  public Edge setActions(
      @Nonnull
      List<Action> actions
  ) {
    this.actions = requireNonNull(actions, "actions");
    return this;
  }

  public Double getLength() {
    return length;
  }

  public Edge setLength(Double length) {
    this.length = length;
    return this;
  }

  @Override
  public String toString() {
    return "Edge{" + "edgeId=" + edgeId
        + ", sequenceId=" + sequenceId
        + ", edgeDescription=" + edgeDescription
        + ", released=" + released
        + ", startNodeId=" + startNodeId
        + ", endNodeId=" + endNodeId
        + ", maxSpeed=" + maxSpeed
        + ", maxHeight=" + maxHeight
        + ", minHeight=" + minHeight
        + ", orientation=" + orientation
        + ", direction=" + direction
        + ", rotationAllowed=" + rotationAllowed
        + ", maxRotationSpeed=" + maxRotationSpeed
        + ", length=" + length
        + ", trajectory=" + trajectory
        + ", actions=" + actions
        + '}';
  }

}
