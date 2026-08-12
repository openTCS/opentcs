// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state;

import static java.util.Objects.requireNonNull;
import static org.opentcs.commadapter.vehicle.vda5050.common.Limits.UINT32_MAX_VALUE;
import static org.opentcs.util.Assertions.checkInRange;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.Header;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.AgvPosition;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.Velocity;

/**
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class State
    extends
      Header {

  /**
   * The path to the JSON schema file.
   */
  public static final String JSON_SCHEMA_PATH
      = "/org/opentcs/commadapter/vehicle/vda5050/v2_0/schemas/state.schema.json";
  /**
   * Unique order identification of the current order or the previous finished order.
   * <p>
   * The {@code orderId} is kept until a new order is received. Empty string if no previous
   * {@code orderId} is available.
   */
  private String orderId;
  /**
   * Order update identification to identify that an order update has been accepted by the AGV.
   * <p>
   * {@code 0}, if no previous {@code orderUpdateId} is available.
   */
  private Long orderUpdateId;
  /**
   * [Optional] Unique ID of the zone set that the AGV currently uses for path planning.
   * <p>
   * Must be the same as the one used in the order, otherwise the AGV is to reject the order. If the
   * AGV does not use zones, this field can be omitted.
   */
  private String zoneSetId;
  /**
   * {@code nodeId} of the last reached node or, if AGV is currently on a node, {@code nodeId} of
   * the current node.
   * <p>
   * Empty string if no {@code lastNodeId} is available.
   */
  private String lastNodeId;
  /**
   * {@code sequenceId} of the last reached node or, if the AGV is currently on a node,
   * {@code sequenceId} of the current node.
   * <p>
   * {@code 0}, if no {@code lastNodeSequenceId} is available.
   */
  private Long lastNodeSequenceId;
  /**
   * List of {@link NodeState} objects that need to be traversed for fulfilling the order.
   * <p>
   * Empty, if the AGV is idle.
   */
  private List<NodeState> nodeStates;
  /**
   * List of {@link EdgeState} objects that need to be traversed for fulfilling the order.
   * <p>
   * Empty, if the AGV is idle.
   */
  private List<EdgeState> edgeStates;
  /**
   * [Optional] Current position of the AGV on the map.
   * <p>
   * Can only be omitted for AGVs without the capability to localize themselves, e.g. line-guided
   * AGVs.
   */
  private AgvPosition agvPosition;
  /**
   * [Optional] The AGV's velocity in vehicle coordinates.
   */
  private Velocity velocity;
  /**
   * [Optional] Loads that are currently handled by the AGV.
   * <p>
   * If the AGV cannot reason about load state, leave the list out of the state. If the AGV can
   * reason about the load state, but the list is empty, the AGV is considered unloaded.
   */
  private List<Load> loads;
  /**
   * Whether the AGV is driving and/or rotating.
   * <p>
   * Other movements of the AGV (e.g. lift movements) are not included here.
   */
  private Boolean driving;
  /**
   * [Optional] Whether the AGV is currently in a paused state.
   * <p>
   * An AGV can be in a paused state e.g. because of the push of a physical button on the AGV or
   * because of an instant action.
   */
  private Boolean paused;
  /**
   * [Optional] Whether the AGV is requesting a new base or not.
   * <p>
   * {@code true}, if the AGV is almost at the end of the base and will reduce speed if no new base
   * is transmitted.
   */
  private Boolean newBaseRequest;
  /**
   * [Optional] Used by line guided vehicles to indicate the distance it has been driving past
   * the {@code lastNodeId} (in m).
   */
  private Double distanceSinceLastNode;
  /**
   * List of the current actions and the actions which are yet to be finished.
   * <p>
   * This may include actions from previous nodes that are still in progress. When an action is
   * completed, an updated state message is published with the actions {@code actionStatus} set to
   * finished and if applicable with the corresponding {@code resultDescription}. The action state
   * is kept until a new order is received.
   */
  private List<ActionState> actionStates;
  /**
   * Contains all battery-related information.
   */
  private BatteryState batteryState;
  /**
   * Current operating mode of the AGV.
   */
  private OperatingMode operatingMode;
  /**
   * List of {@link ErrorEntry} objects.
   * <p>
   * All active errors of the AGV should be in this list. An empty array indicates that the AGV has
   * no active errors.
   */
  private List<ErrorEntry> errors;
  /**
   * [Optional] List of {@link InfoEntry} objects.
   * <p>
   * This should only be used for visualization or debugging – it must not be used for logic in
   * master control. An empty list indicates that the AGV has no information.
   */
  private List<InfoEntry> information;
  /**
   * Contains all safety-related information.
   */
  private SafetyState safetyState;

  /**
   * Creates a new instance.
   * <p>
   * Convenient constructor for creating a new instance without explicitly providing information
   * about the {@link Header} content. Instead, defaults (such as e.g. zero values or empty strings)
   * are used. For instances created with this constructor, the header content must therefore be set
   * subsequently.
   *
   * @param orderId Unique order identification of the current order or the previous finished order.
   * @param orderUpdateId Order update identification to identify that an order update has been
   * accepted by the AGV.
   * @param lastNodeId {@code nodeId} of the last reached node or, if AGV is currently on a node,
   * {@code nodeId} of the current node.
   * @param lastNodeSequenceId {@code sequenceId} of the last reached node or, if the AGV is
   * currently on a node,
   * @param nodeStates List of {@link NodeState} objects that need to be traversed for fulfilling
   * the order.
   * @param edgeStates List of {@link EdgeState} objects that need to be traversed for fulfilling
   * the order.
   * @param driving Whether the AGV is driving and/or rotating.
   * @param actionStates List of the current actions and the actions which are yet to be finished.
   * @param batteryState Contains all battery-related information.
   * @param operatingMode Current operating mode of the AGV.
   * @param errors List of {@link ErrorEntry} objects.
   * @param safetyState Contains all safety-related information.
   */
  public State(
      @Nonnull
      String orderId,
      @Nonnull
      Long orderUpdateId,
      @Nonnull
      String lastNodeId,
      @Nonnull
      Long lastNodeSequenceId,
      @Nonnull
      List<NodeState> nodeStates,
      @Nonnull
      List<EdgeState> edgeStates,
      @Nonnull
      Boolean driving,
      @Nonnull
      List<ActionState> actionStates,
      @Nonnull
      BatteryState batteryState,
      @Nonnull
      OperatingMode operatingMode,
      @Nonnull
      List<ErrorEntry> errors,
      @Nonnull
      SafetyState safetyState
  ) {
    this(
        0L, Instant.EPOCH, "", "", "", orderId, orderUpdateId, lastNodeId, lastNodeSequenceId,
        nodeStates, edgeStates, driving, actionStates, batteryState, operatingMode, errors,
        safetyState
    );
  }

  // CHECKSTYLE:OFF (Long lines because some of these parameter declarations are very long.)
  @JsonCreator
  public State(
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
      @JsonProperty(required = true, value = "orderId")
      String orderId,
      @Nonnull
      @JsonProperty(required = true, value = "orderUpdateId")
      Long orderUpdateId,
      @Nonnull
      @JsonProperty(required = true, value = "lastNodeId")
      String lastNodeId,
      @Nonnull
      @JsonProperty(required = true, value = "lastNodeSequenceId")
      Long lastNodeSequenceId,
      @Nonnull
      @JsonProperty(required = true, value = "nodeStates")
      List<NodeState> nodeStates,
      @Nonnull
      @JsonProperty(required = true, value = "edgeStates")
      List<EdgeState> edgeStates,
      @Nonnull
      @JsonProperty(required = true, value = "driving")
      Boolean driving,
      @Nonnull
      @JsonProperty(required = true, value = "actionStates")
      List<ActionState> actionStates,
      @Nonnull
      @JsonProperty(required = true, value = "batteryState")
      BatteryState batteryState,
      @Nonnull
      @JsonProperty(required = true, value = "operatingMode")
      OperatingMode operatingMode,
      @Nonnull
      @JsonProperty(required = true, value = "errors")
      List<ErrorEntry> errors,
      @Nonnull
      @JsonProperty(required = true, value = "safetyState")
      SafetyState safetyState
  ) {
    super(headerId, timestamp, version, manufacturer, serialNumber);
    this.orderId = requireNonNull(orderId, "orderId");
    this.orderUpdateId = checkInRange(
        requireNonNull(orderUpdateId, "orderUpdateId"),
        0, UINT32_MAX_VALUE, "orderUpdateId"
    );
    this.lastNodeId = requireNonNull(lastNodeId, "lastNodeId");
    this.lastNodeSequenceId = checkInRange(
        requireNonNull(lastNodeSequenceId, "lastNodeSequenceId"),
        0, UINT32_MAX_VALUE, "lastNodeSequenceId"
    );
    this.nodeStates = requireNonNull(nodeStates, "nodeStates");
    this.edgeStates = requireNonNull(edgeStates, "edgeStates");
    this.driving = requireNonNull(driving, "driving");
    this.actionStates = requireNonNull(actionStates, "actionStates");
    this.batteryState = requireNonNull(batteryState, "batteryState");
    this.operatingMode = requireNonNull(operatingMode, "operatingMode");
    this.errors = requireNonNull(errors, "errors");
    this.safetyState = requireNonNull(safetyState, "safetyState");
  }
  // CHECKSTYLE:ON

  public String getOrderId() {
    return orderId;
  }

  public State setOrderId(
      @Nonnull
      String orderId
  ) {
    this.orderId = requireNonNull(orderId, "orderId");
    return this;
  }

  public Long getOrderUpdateId() {
    return orderUpdateId;
  }

  public State setOrderUpdateId(
      @Nonnull
      Long orderUpdateId
  ) {
    this.orderUpdateId = checkInRange(
        requireNonNull(orderUpdateId, "orderUpdateId"), 0, UINT32_MAX_VALUE, "orderUpdateId"
    );
    return this;
  }

  public String getLastNodeId() {
    return lastNodeId;
  }

  public State setLastNodeId(
      @Nonnull
      String lastNodeId
  ) {
    this.lastNodeId = requireNonNull(lastNodeId, "lastNodeId");
    return this;
  }

  public String getZoneSetId() {
    return zoneSetId;
  }

  public State setZoneSetId(String zoneSetId) {
    this.zoneSetId = zoneSetId;
    return this;
  }

  public Long getLastNodeSequenceId() {
    return lastNodeSequenceId;
  }

  public State setLastNodeSequenceId(
      @Nonnull
      Long lastNodeSequenceId
  ) {
    this.lastNodeSequenceId = checkInRange(
        requireNonNull(lastNodeSequenceId, "lastNodeSequenceId"),
        0, UINT32_MAX_VALUE, "lastNodeSequenceId"
    );
    return this;
  }

  public Boolean isDriving() {
    return driving;
  }

  public State setDriving(
      @Nonnull
      Boolean driving
  ) {
    this.driving = requireNonNull(driving, "driving");
    return this;
  }

  public Boolean isPaused() {
    return paused;
  }

  public State setPaused(
      @Nullable
      Boolean paused
  ) {
    this.paused = paused;
    return this;
  }

  public Boolean isNewBaseRequest() {
    return newBaseRequest;
  }

  public State setNewBaseRequest(Boolean newBaseRequest) {
    this.newBaseRequest = newBaseRequest;
    return this;
  }

  public Double getDistanceSinceLastNode() {
    return distanceSinceLastNode;
  }

  public State setDistanceSinceLastNode(Double distanceSinceLastNode) {
    this.distanceSinceLastNode = distanceSinceLastNode;
    return this;
  }

  public OperatingMode getOperatingMode() {
    return operatingMode;
  }

  public State setOperatingMode(
      @Nonnull
      OperatingMode operatingMode
  ) {
    this.operatingMode = requireNonNull(operatingMode, "operatingMode");
    return this;
  }

  public List<NodeState> getNodeStates() {
    return nodeStates;
  }

  public State setNodeStates(
      @Nonnull
      List<NodeState> nodeStates
  ) {
    this.nodeStates = requireNonNull(nodeStates, "nodeStates");
    return this;
  }

  public List<EdgeState> getEdgeStates() {
    return edgeStates;
  }

  public State setEdgeStates(
      @Nonnull
      List<EdgeState> edgeStates
  ) {
    this.edgeStates = requireNonNull(edgeStates, "edgeStates");
    return this;
  }

  public AgvPosition getAgvPosition() {
    return agvPosition;
  }

  public State setAgvPosition(AgvPosition agvPosition) {
    this.agvPosition = agvPosition;
    return this;
  }

  public Velocity getVelocity() {
    return velocity;
  }

  public State setVelocity(Velocity velocity) {
    this.velocity = velocity;
    return this;
  }

  public List<Load> getLoads() {
    return loads;
  }

  public State setLoads(List<Load> loads) {
    this.loads = loads;
    return this;
  }

  public List<ActionState> getActionStates() {
    return actionStates;
  }

  public State setActionStates(
      @Nonnull
      List<ActionState> actionStates
  ) {
    this.actionStates = requireNonNull(actionStates, "actionStates");
    return this;
  }

  public BatteryState getBatteryState() {
    return batteryState;
  }

  public State setBatteryState(
      @Nonnull
      BatteryState batteryState
  ) {
    this.batteryState = requireNonNull(batteryState, "batteryState");
    return this;
  }

  public List<ErrorEntry> getErrors() {
    return errors;
  }

  public State setErrors(
      @Nonnull
      List<ErrorEntry> errors
  ) {
    this.errors = requireNonNull(errors, "errors");
    return this;
  }

  public List<InfoEntry> getInformation() {
    return information;
  }

  public State setInformation(List<InfoEntry> information) {
    this.information = information;
    return this;
  }

  public SafetyState getSafetyState() {
    return safetyState;
  }

  public State setSafetyState(
      @Nonnull
      SafetyState safetyState
  ) {
    this.safetyState = requireNonNull(safetyState, "safetyState");
    return this;
  }

  @Override
  public String toString() {
    return "State{"
        + "header=" + super.toString()
        + ", orderId=" + orderId
        + ", orderUpdateId=" + orderUpdateId
        + ", zoneSetId=" + zoneSetId
        + ", lastNodeId=" + lastNodeId
        + ", lastNodeSequenceId=" + lastNodeSequenceId
        + ", nodeStates=" + nodeStates
        + ", edgeStates=" + edgeStates
        + ", agvPosition=" + agvPosition
        + ", velocity=" + velocity
        + ", loads=" + loads
        + ", driving=" + driving
        + ", paused=" + paused
        + ", newBaseRequest=" + newBaseRequest
        + ", distanceSinceLastNode=" + distanceSinceLastNode
        + ", actionStates=" + actionStates
        + ", batteryState=" + batteryState
        + ", operatingMode=" + operatingMode
        + ", errors=" + errors
        + ", information=" + information
        + ", safetyState=" + safetyState
        + '}';
  }
}
