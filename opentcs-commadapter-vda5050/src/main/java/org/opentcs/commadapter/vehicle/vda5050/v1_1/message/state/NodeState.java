// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v1_1.message.state;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import javax.annotation.Nonnull;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.common.NodePosition;

/**
 * Information about a node the AGV still has to traverse.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NodeState
    implements
      Serializable {

  /**
   * Unique node identification.
   */
  private String nodeId;
  /**
   * Sequence id to differentiate between multiple nodes with the same {@code nodeId}.
   */
  private Long sequenceId;
  /**
   * [Optional] Additional information on the node.
   */
  private String nodeDescription;
  /**
   * [Optional] The node position.
   * <p>
   * Can be sent additionally, e.g. for debugging purposes.
   */
  private NodePosition nodePosition;
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

  @JsonCreator
  public NodeState(
      @Nonnull
      @JsonProperty(required = true, value = "nodeId")
      String nodeId,
      @Nonnull
      @JsonProperty(required = true, value = "sequenceId")
      Long sequenceId,
      @Nonnull
      @JsonProperty(required = true, value = "released")
      Boolean released
  ) {
    this.nodeId = requireNonNull(nodeId, "nodeId");
    this.sequenceId = requireNonNull(sequenceId, "sequenceId");
    this.released = requireNonNull(released, "released");
  }

  public String getNodeId() {
    return nodeId;
  }

  public NodeState setNodeId(
      @Nonnull
      String nodeId
  ) {
    this.nodeId = requireNonNull(nodeId);
    return this;
  }

  public Long getSequenceId() {
    return sequenceId;
  }

  public NodeState setSequenceId(
      @Nonnull
      Long sequenceId
  ) {
    this.sequenceId = requireNonNull(sequenceId, "sequenceId");
    return this;
  }

  public NodePosition getNodePosition() {
    return nodePosition;
  }

  public NodeState setNodePosition(NodePosition nodePosition) {
    this.nodePosition = nodePosition;
    return this;
  }

  public String getNodeDescription() {
    return nodeDescription;
  }

  public NodeState setNodeDescription(String nodeDescription) {
    this.nodeDescription = nodeDescription;
    return this;
  }

  public Boolean isReleased() {
    return released;
  }

  public NodeState setReleased(
      @Nonnull
      Boolean released
  ) {
    this.released = requireNonNull(released, "released");
    return this;
  }

  @Override
  public String toString() {
    return "NodeState{" + "nodeId=" + nodeId
        + ", sequenceId=" + sequenceId
        + ", nodeDescription=" + nodeDescription
        + ", nodePosition=" + nodePosition
        + ", released=" + released
        + '}';
  }

}
