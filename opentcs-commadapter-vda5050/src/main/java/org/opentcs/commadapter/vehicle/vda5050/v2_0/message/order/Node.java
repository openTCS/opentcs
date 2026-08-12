// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v2_0.message.order;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Nonnull;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.Action;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.NodePosition;

/**
 * A node to be traversed for fulfilling an order.
 */
@JsonInclude(Include.NON_NULL)
public class Node
    implements
      Serializable {

  /**
   * Unique node identification.
   */
  private String nodeId;
  /**
   * Number to track the sequence of nodes and edges in an order and to simplify order updates.
   * <p>
   * The main purpose is to distinguish between a node which is passed more than once within one
   * {@code orderId}. The variable {@code sequenceId} runs across all nodes and edges of the same
   * order and is reset when a new {@code orderId} is issued.
   */
  private Long sequenceId;
  /**
   * [Optional] Additional information on the node.
   */
  private String nodeDescription;
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
   * [Optional] Defines the position on a map in world coordinates.
   * <p>
   * Optional for vehicle types that do not require a node position (e.g. line-guided vehicles).
   */
  private NodePosition nodePosition;
  /**
   * List of {@link Action}s to be executed on the node.
   * <p>
   * Their sequence in the list governs their sequence of execution. Empty list if no actions
   * required.
   */
  private List<Action> actions;

  @JsonCreator
  public Node(
      @Nonnull
      @JsonProperty(required = true, value = "nodeId")
      String nodeId,
      @Nonnull
      @JsonProperty(required = true, value = "sequenceId")
      Long sequenceId,
      @Nonnull
      @JsonProperty(required = true, value = "released")
      Boolean released,
      @Nonnull
      @JsonProperty(required = true, value = "actions")
      List<Action> actions
  ) {
    this.nodeId = requireNonNull(nodeId, "nodeId");
    this.sequenceId = requireNonNull(sequenceId, "sequenceId");
    this.released = requireNonNull(released, "released");
    this.actions = requireNonNull(actions, "actions");
  }

  public String getNodeId() {
    return nodeId;
  }

  public Node setNodeId(
      @Nonnull
      String nodeId
  ) {
    this.nodeId = requireNonNull(nodeId, "nodeId");
    return this;
  }

  public Long getSequenceId() {
    return sequenceId;
  }

  public Node setSequenceId(
      @Nonnull
      Long sequenceId
  ) {
    this.sequenceId = requireNonNull(sequenceId, "sequenceId");
    return this;
  }

  public String getNodeDescription() {
    return nodeDescription;
  }

  public Node setNodeDescription(String nodeDescription) {
    this.nodeDescription = nodeDescription;
    return this;
  }

  public boolean isReleased() {
    return released;
  }

  public Node setReleased(
      @Nonnull
      Boolean released
  ) {
    this.released = requireNonNull(released, "released");
    return this;
  }

  public NodePosition getNodePosition() {
    return nodePosition;
  }

  public Node setNodePosition(NodePosition nodePosition) {
    this.nodePosition = nodePosition;
    return this;
  }

  public List<Action> getActions() {
    return actions;
  }

  public Node setActions(
      @Nonnull
      List<Action> actions
  ) {
    this.actions = requireNonNull(actions, "actions");
    return this;
  }

  @Override
  public String toString() {
    return "Node{" + "nodeId=" + nodeId
        + ", sequenceId=" + sequenceId
        + ", nodeDescription=" + nodeDescription
        + ", released=" + released
        + ", nodePosition=" + nodePosition
        + ", actions=" + actions
        + '}';
  }

}
