// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v1_1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.CommAdapterMessages.SEND_INSTANT_ACTION_PARAM_ACTION_DESCRIPTION;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.CommAdapterMessages.SEND_INSTANT_ACTION_PARAM_ACTION_ID;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.CommAdapterMessages.SEND_INSTANT_ACTION_PARAM_ACTION_TYPE;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.CommAdapterMessages.SEND_INSTANT_ACTION_PARAM_BLOCKING_TYPE;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.CommAdapterMessages.SEND_INSTANT_ACTION_PARAM_PARAMETER_PREFIX;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.CommAdapterMessages.SEND_ORDER_PARAM_DESTINATION_NODE;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.CommAdapterMessages.SEND_ORDER_PARAM_DESTINATION_NODE_ACTION_BLOCKING_TYPE;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.CommAdapterMessages.SEND_ORDER_PARAM_DESTINATION_NODE_ACTION_DESCRIPTION;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.CommAdapterMessages.SEND_ORDER_PARAM_DESTINATION_NODE_ACTION_ID;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.CommAdapterMessages.SEND_ORDER_PARAM_DESTINATION_NODE_ACTION_PARAMETER_PREFIX;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.CommAdapterMessages.SEND_ORDER_PARAM_DESTINATION_NODE_ACTION_TYPE;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.CommAdapterMessages.SEND_ORDER_PARAM_EDGE;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.CommAdapterMessages.SEND_ORDER_PARAM_ORDER_ID;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.CommAdapterMessages.SEND_ORDER_PARAM_ORDER_UPDATE_ID;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.CommAdapterMessages.SEND_ORDER_PARAM_SOURCE_NODE;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.action.InitPosition;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.common.Action;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.common.ActionParameter;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.common.BlockingType;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.order.Edge;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.order.Node;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.ordermapping.NodeMapping;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.VehicleCommAdapterMessage;
import org.opentcs.util.MapValueExtractor;

/**
 * Tests for {@link CommAdapterMessageMapper}.
 */
class CommAdapterMessageMapperTest {

  private TCSObjectService objectService;
  private CommAdapterMessageMapper mapper;

  @BeforeEach
  void setUp() {
    objectService = mock(TCSObjectService.class);
    mapper = new CommAdapterMessageMapper(
        new Vehicle("vehicle-1"),
        new MapValueExtractor(),
        objectService,
        mock(NodeMapping.class)
    );
  }

  @Test
  void mapToOrder() {
    VehicleCommAdapterMessage message = new VehicleCommAdapterMessage(
        CommAdapterMessages.SEND_ORDER_TYPE,
        Map.ofEntries(
            Map.entry(SEND_ORDER_PARAM_ORDER_ID, "order-id"),
            Map.entry(SEND_ORDER_PARAM_ORDER_UPDATE_ID, "7"),
            Map.entry(SEND_ORDER_PARAM_SOURCE_NODE, "source-node"),
            Map.entry(SEND_ORDER_PARAM_DESTINATION_NODE, "destination-node"),
            Map.entry(SEND_ORDER_PARAM_DESTINATION_NODE_ACTION_TYPE, "action-type"),
            Map.entry(SEND_ORDER_PARAM_DESTINATION_NODE_ACTION_ID, "action-id"),
            Map.entry(SEND_ORDER_PARAM_DESTINATION_NODE_ACTION_DESCRIPTION, "action-description"),
            Map.entry(
                SEND_ORDER_PARAM_DESTINATION_NODE_ACTION_BLOCKING_TYPE,
                BlockingType.HARD.name()
            ),
            Map.entry(
                SEND_ORDER_PARAM_DESTINATION_NODE_ACTION_PARAMETER_PREFIX + "action-param-1",
                "value-1"
            ),
            Map.entry(
                SEND_ORDER_PARAM_DESTINATION_NODE_ACTION_PARAMETER_PREFIX + "action-param-2",
                "value-2"
            ),
            Map.entry(SEND_ORDER_PARAM_EDGE, "edge")
        )
    );

    when(objectService.fetch(Point.class, "source-node"))
        .thenReturn(Optional.of(new Point("source-node")));
    when(objectService.fetch(Point.class, "destination-node"))
        .thenReturn(Optional.of(new Point("destination-node")));

    var result = mapper.toOrder(message);

    assertThat(result)
        .hasValueSatisfying(order -> {
          assertThat(order.getOrderId()).isEqualTo("order-id");
          assertThat(order.getOrderUpdateId()).isEqualTo(7L);
          assertThat(order.getNodes())
              .hasSize(2)
              .extracting(Node::getNodeId, Node::getSequenceId, Node::isReleased)
              .containsExactly(
                  tuple("source-node", 0L, true),
                  tuple("destination-node", 1L, true)
              );
          assertThat(order.getEdges())
              .hasSize(1)
              .extracting(Edge::getEdgeId, Edge::getSequenceId, Edge::isReleased)
              .containsExactly(tuple("edge", 0L, true));

          assertThat(order.getNodes().getLast().getActions()).hasSize(1);
          Action destinationAction = order.getNodes().getLast().getActions().getFirst();
          assertThat(destinationAction.getActionType()).isEqualTo("action-type");
          assertThat(destinationAction.getActionId()).isEqualTo("action-id");
          assertThat(destinationAction.getActionDescription()).isEqualTo("action-description");
          assertThat(destinationAction.getBlockingType()).isEqualTo(BlockingType.HARD);
          assertThat(destinationAction.getActionParameters())
              .hasSize(2)
              .extracting(ActionParameter::getKey, ActionParameter::getValue)
              .contains(
                  tuple("action-param-1", "value-1"),
                  tuple("action-param-2", "value-2")
              );
        });
  }

  @Test
  void mapToAction() {
    VehicleCommAdapterMessage message = new VehicleCommAdapterMessage(
        CommAdapterMessages.SEND_INSTANT_ACTION_TYPE,
        Map.of(
            SEND_INSTANT_ACTION_PARAM_ACTION_TYPE, "action-type",
            SEND_INSTANT_ACTION_PARAM_ACTION_ID, "action-id",
            SEND_INSTANT_ACTION_PARAM_ACTION_DESCRIPTION, "action-description",
            SEND_INSTANT_ACTION_PARAM_BLOCKING_TYPE, BlockingType.HARD.name(),
            SEND_INSTANT_ACTION_PARAM_PARAMETER_PREFIX + "action-param-1", "value-1",
            SEND_INSTANT_ACTION_PARAM_PARAMETER_PREFIX + "action-param-2", "value-2"
        )
    );

    var result = mapper.toAction(message);

    assertThat(result)
        .hasValueSatisfying(action -> {
          assertThat(action.getActionType()).isEqualTo("action-type");
          assertThat(action.getActionId()).isEqualTo("action-id");
          assertThat(action.getActionDescription()).isEqualTo("action-description");
          assertThat(action.getBlockingType()).isEqualTo(BlockingType.HARD);
          assertThat(action.getActionParameters())
              .extracting(ActionParameter::getKey, ActionParameter::getValue)
              .contains(
                  tuple("action-param-1", "value-1"),
                  tuple("action-param-2", "value-2")
              );
        });
  }

  @Test
  void mapToActionInitPositionXYThetaAreDouble() {
    VehicleCommAdapterMessage message = new VehicleCommAdapterMessage(
        CommAdapterMessages.SEND_INSTANT_ACTION_TYPE,
        Map.of(
            SEND_INSTANT_ACTION_PARAM_ACTION_TYPE, InitPosition.ACTION_TYPE,
            SEND_INSTANT_ACTION_PARAM_ACTION_ID, "1",
            SEND_INSTANT_ACTION_PARAM_BLOCKING_TYPE, BlockingType.NONE.name(),
            SEND_INSTANT_ACTION_PARAM_PARAMETER_PREFIX + InitPosition.PARAMKEY_X, "2.009",
            SEND_INSTANT_ACTION_PARAM_PARAMETER_PREFIX + InitPosition.PARAMKEY_Y, "3.367",
            SEND_INSTANT_ACTION_PARAM_PARAMETER_PREFIX + InitPosition.PARAMKEY_THETA,
            "3.141592653589793",
            SEND_INSTANT_ACTION_PARAM_PARAMETER_PREFIX + InitPosition.PARAMKEY_MAP_ID, "floor1",
            SEND_INSTANT_ACTION_PARAM_PARAMETER_PREFIX + InitPosition.PARAMKEY_LAST_NODE_ID,
            "io-0001"
        )
    );

    var result = mapper.toAction(message);

    assertThat(result).hasValueSatisfying(action -> {
      // Numeric keys must be parsed as Double
      assertThat(action.getActionParameters())
          .extracting(ActionParameter::getKey, ActionParameter::getValue)
          .contains(
              tuple(InitPosition.PARAMKEY_X, 2.009),
              tuple(InitPosition.PARAMKEY_Y, 3.367),
              tuple(InitPosition.PARAMKEY_THETA, 3.141592653589793)
          );
      // String parameters must NOT be converted to numbers
      assertThat(action.getActionParameters())
          .extracting(ActionParameter::getKey, ActionParameter::getValue)
          .contains(
              tuple(InitPosition.PARAMKEY_MAP_ID, "floor1"),
              tuple(InitPosition.PARAMKEY_LAST_NODE_ID, "io-0001")
          );
    });
  }

  @Test
  void mapToActionInitPositionMapIdLookingNumericStaysString() {
    // mapId of "1.0" must not be converted to Double even though it parses as a number
    VehicleCommAdapterMessage message = new VehicleCommAdapterMessage(
        CommAdapterMessages.SEND_INSTANT_ACTION_TYPE,
        Map.of(
            SEND_INSTANT_ACTION_PARAM_ACTION_TYPE, InitPosition.ACTION_TYPE,
            SEND_INSTANT_ACTION_PARAM_ACTION_ID, "1",
            SEND_INSTANT_ACTION_PARAM_BLOCKING_TYPE, BlockingType.NONE.name(),
            SEND_INSTANT_ACTION_PARAM_PARAMETER_PREFIX + InitPosition.PARAMKEY_X, "1.0",
            SEND_INSTANT_ACTION_PARAM_PARAMETER_PREFIX + InitPosition.PARAMKEY_Y, "2.0",
            SEND_INSTANT_ACTION_PARAM_PARAMETER_PREFIX + InitPosition.PARAMKEY_THETA, "0.0",
            SEND_INSTANT_ACTION_PARAM_PARAMETER_PREFIX + InitPosition.PARAMKEY_MAP_ID, "1.0",
            SEND_INSTANT_ACTION_PARAM_PARAMETER_PREFIX + InitPosition.PARAMKEY_LAST_NODE_ID,
            "io-0001"
        )
    );

    var result = mapper.toAction(message);

    assertThat(result).hasValueSatisfying(action -> {
      assertThat(action.getActionParameters())
          .extracting(ActionParameter::getKey, ActionParameter::getValue)
          .contains(
              tuple(InitPosition.PARAMKEY_X, 1.0),
              tuple(InitPosition.PARAMKEY_Y, 2.0),
              tuple(InitPosition.PARAMKEY_THETA, 0.0),
              // "1.0" as mapId must remain a String, not become Double(1.0)
              tuple(InitPosition.PARAMKEY_MAP_ID, "1.0")
          );
    });
  }

  @Test
  void mapToActionInitPositionWithInvalidNumericIsNotMapped() {
    VehicleCommAdapterMessage message = new VehicleCommAdapterMessage(
        CommAdapterMessages.SEND_INSTANT_ACTION_TYPE,
        Map.of(
            SEND_INSTANT_ACTION_PARAM_ACTION_TYPE, InitPosition.ACTION_TYPE,
            SEND_INSTANT_ACTION_PARAM_ACTION_ID, "1",
            SEND_INSTANT_ACTION_PARAM_BLOCKING_TYPE, BlockingType.NONE.name(),
            SEND_INSTANT_ACTION_PARAM_PARAMETER_PREFIX + InitPosition.PARAMKEY_X, "not-a-number",
            SEND_INSTANT_ACTION_PARAM_PARAMETER_PREFIX + InitPosition.PARAMKEY_Y, "3.367",
            SEND_INSTANT_ACTION_PARAM_PARAMETER_PREFIX + InitPosition.PARAMKEY_THETA, "0.0"
        )
    );

    assertThat(mapper.toAction(message)).isEmpty();
  }

  @Test
  void mapToOrderWithInvalidInitPositionNumericIsNotMapped() {
    VehicleCommAdapterMessage message = new VehicleCommAdapterMessage(
        CommAdapterMessages.SEND_ORDER_TYPE,
        Map.ofEntries(
            Map.entry(SEND_ORDER_PARAM_ORDER_ID, "order-id"),
            Map.entry(SEND_ORDER_PARAM_ORDER_UPDATE_ID, "7"),
            Map.entry(SEND_ORDER_PARAM_SOURCE_NODE, "source-node"),
            Map.entry(SEND_ORDER_PARAM_DESTINATION_NODE, "destination-node"),
            Map.entry(SEND_ORDER_PARAM_EDGE, "edge"),
            Map.entry(SEND_ORDER_PARAM_DESTINATION_NODE_ACTION_TYPE, InitPosition.ACTION_TYPE),
            Map.entry(SEND_ORDER_PARAM_DESTINATION_NODE_ACTION_ID, "action-id"),
            Map.entry(
                SEND_ORDER_PARAM_DESTINATION_NODE_ACTION_BLOCKING_TYPE,
                BlockingType.NONE.name()
            ),
            Map.entry(
                SEND_ORDER_PARAM_DESTINATION_NODE_ACTION_PARAMETER_PREFIX + InitPosition.PARAMKEY_X,
                "not-a-number"
            )
        )
    );

    when(objectService.fetch(Point.class, "source-node"))
        .thenReturn(Optional.of(new Point("source-node")));
    when(objectService.fetch(Point.class, "destination-node"))
        .thenReturn(Optional.of(new Point("destination-node")));

    assertThat(mapper.toOrder(message)).isEmpty();
  }
}
