// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v2_0.message.order;

import static java.util.Objects.requireNonNull;
import static org.opentcs.commadapter.vehicle.vda5050.common.Limits.UINT32_MAX_VALUE;
import static org.opentcs.util.Assertions.checkInRange;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;
import javax.annotation.Nonnull;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.Header;

/**
 * Defines an order sent from master control to the AGV.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Order
    extends
      Header {

  /**
   * The path to the JSON schema file.
   */
  public static final String JSON_SCHEMA_PATH
      = "/org/opentcs/commadapter/vehicle/vda5050/v2_0/schemas/order.schema.json";
  /**
   * Unique order identification.
   * <p>
   * This is to be used to identify multiple order messages that belong to the same order.
   */
  private String orderId;
  /**
   * Order update identification.
   * <p>
   * Is unique per {@code orderId}. If an order update is rejected, this field is to be passed in
   * the rejection message.
   */
  private Long orderUpdateId;
  /**
   * [Optional] Unique identifier of the zone set that the AGV has to use for navigation or that
   * was used by master control for planning.
   * <p>
   * Optional: Some master control systems do not use zones. Some AGVs do not understand zones. Do
   * not add to message if no zones are used.
   */
  private String zoneSetId;
  /**
   * List of {@link Node} objects to be traversed for fulfilling the order.
   * <p>
   * One node is enough for a valid order. Leave edge list empty for that case.
   */
  private List<Node> nodes;
  /**
   * List of {@link Edge} objects to be traversed for fulfilling the order.
   * <p>
   * May be empty in case only one node is used for an order.
   */
  private List<Edge> edges;

  /**
   * Creates a new instance.
   * <p>
   * Convenient constructor for creating a new instance without explicitly providing information
   * about the {@link Header} content. Instead, defaults (such as e.g. zero values or empty strings)
   * are used. For instances created with this constructor, the header content must therefore be set
   * subsequently.
   *
   * @param orderId Unique order identification.
   * @param orderUpdateId Order update identification.
   * @param nodes List of {@link Node} objects to be traversed for fulfilling the order.
   * @param edges List of {@link Edge} objects to be traversed for fulfilling the order.
   */
  public Order(
      @Nonnull
      String orderId,
      @Nonnull
      Long orderUpdateId,
      @Nonnull
      List<Node> nodes,
      @Nonnull
      List<Edge> edges
  ) {
    this(0L, Instant.EPOCH, "", "", "", orderId, orderUpdateId, nodes, edges);
  }

  @JsonCreator
  public Order(
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
      @JsonProperty(required = true, value = "nodes")
      List<Node> nodes,
      @Nonnull
      @JsonProperty(required = true, value = "edges")
      List<Edge> edges
  ) {
    super(headerId, timestamp, version, manufacturer, serialNumber);
    this.orderId = requireNonNull(orderId, "orderId");
    this.orderUpdateId = checkInRange(
        requireNonNull(orderUpdateId, "orderUpdateId"),
        0, UINT32_MAX_VALUE, "orderUpdateId"
    );
    this.nodes = requireNonNull(nodes, "nodes");
    this.edges = requireNonNull(edges, "edges");
  }

  public String getOrderId() {
    return orderId;
  }

  public Order setOrderId(
      @Nonnull
      String orderId
  ) {
    this.orderId = requireNonNull(orderId, "orderId");
    return this;
  }

  public Long getOrderUpdateId() {
    return orderUpdateId;
  }

  public Order setOrderUpdateId(
      @Nonnull
      Long orderUpdateId
  ) {
    this.orderUpdateId = checkInRange(
        requireNonNull(orderUpdateId, "orderUpdateId"),
        0, UINT32_MAX_VALUE, "orderUpdateId"
    );
    return this;
  }

  public String getZoneSetId() {
    return zoneSetId;
  }

  public Order setZoneSetId(String zoneSetId) {
    this.zoneSetId = zoneSetId;
    return this;
  }

  public List<Node> getNodes() {
    return nodes;
  }

  public Order setNodes(
      @Nonnull
      List<Node> nodes
  ) {
    this.nodes = requireNonNull(nodes, "nodes");
    return this;
  }

  public List<Edge> getEdges() {
    return edges;
  }

  public Order setEdges(
      @Nonnull
      List<Edge> edges
  ) {
    this.edges = requireNonNull(edges, "edges");
    return this;
  }

  @Override
  public String toString() {
    return "Order{"
        + "header=" + super.toString()
        + ", orderId=" + orderId
        + ", orderUpdateId=" + orderUpdateId
        + ", zoneSetId=" + zoneSetId
        + ", nodes=" + nodes
        + ", edges=" + edges
        + '}';
  }
}
