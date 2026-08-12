// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.common;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.data.model.Vehicle;

class UnsupportedPropertiesFilterTest {

  private ObjectNode orderNode;
  private final ObjectMapper objectMapper
      = new ObjectMapper()
          .registerModule(new JavaTimeModule())
          .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

  @BeforeEach
  public void setUp() {
    orderNode = objectMapper.createObjectNode();
    orderNode
        .put("orderId", "some-order")
        .put("orderUpdateId", 1L)
        .put("zoneSetId", "zone-set-1");

    ArrayNode nodesArray = orderNode.putArray("nodes");
    ObjectNode node1 = nodesArray.addObject();
    node1
        .put("nodeId", "node1")
        .put("released", true)
        .put("nodeDescription", "some-description");
    ObjectNode node1Pos = node1.putObject("nodePosition");
    node1Pos
        .put("x", 11)
        .put("y", 22)
        .put("mapId", "map")
        .put("allowedDeviationTheta", 3);
    ObjectNode node2 = nodesArray.addObject();
    node2
        .put("nodeId", "node2")
        .put("released", true)
        .put("nodeDescription", "some-description2");
    ObjectNode node2Pos = node2.putObject("nodePosition");
    node2Pos
        .put("x", 33)
        .put("y", 44)
        .put("mapId", "map")
        .put("allowedDeviationTheta", 2);

    ArrayNode edgesArray = orderNode.putArray("edges");
    ObjectNode edge1 = edgesArray.addObject();
    edge1
        .put("edgeId", "edge1")
        .put("sequenceId", 12L)
        .put("released", true)
        .put("sourceNode", "node1")
        .put("destinationNode", "node2")
        .put("maxSpeed", 133);
  }

  @Test
  public void removeOnlyUnsupportedParameterFromOrderMessage() {
    Map<String, OptionalParameterSupport> vehicleOptionalParameters = new HashMap<>() {
      {
        put("zoneSetId", OptionalParameterSupport.NOT_SUPPORTED);
        put("nodes.nodeDescription", OptionalParameterSupport.NOT_SUPPORTED);
        put("nodes.nodePosition.allowedDeviationTheta", OptionalParameterSupport.NOT_SUPPORTED);
        put("edges.maxSpeed", OptionalParameterSupport.NOT_SUPPORTED);
      }
    };
    @SuppressWarnings("unchecked")
    Function<Vehicle, Map<String, OptionalParameterSupport>> mockedFunction = mock(Function.class);
    when(mockedFunction.apply(any(Vehicle.class))).thenReturn(vehicleOptionalParameters);

    JsonNode result = new UnsupportedPropertiesFilter(mock(Vehicle.class), mockedFunction)
        .apply(objectMapper.valueToTree(orderNode));

    Approvals.verify(result.toPrettyString());
  }
}
