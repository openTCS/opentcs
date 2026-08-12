// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v1_1.message.order;

import java.time.Instant;
import java.util.List;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.commadapter.vehicle.vda5050.ResourceLoader;
import org.opentcs.commadapter.vehicle.vda5050.common.JsonBinder;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.MessageValidator;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.common.Action;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.common.ActionParameter;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.common.BlockingType;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.common.ControlPoint;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.common.NodePosition;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.common.Trajectory;

/**
 * Unit tests for {@link Order}.
 */
public class OrderTest {

  private static final String RESOURCE_DIR
      = "/org/opentcs/commadapter/vehicle/vda5050/v1_1/message/order/";

  private MessageValidator messageValidator;
  private JsonBinder jsonBinder;

  @BeforeEach
  public void setUp() {
    messageValidator = new MessageValidator();
    jsonBinder = new JsonBinder();
  }

  @Test
  public void validateAgainstJsonSchemaMinimal() {
    messageValidator.validate(
        jsonBinder.toJson(createOrderMinimal()),
        Order.class
    );
  }

  @Test
  public void validateAgainstJsonSchemaFull() {
    messageValidator.validate(
        jsonBinder.toJson(createOrderFull()),
        Order.class
    );
  }

  @Test
  public void validateJsonWithNullForOptionalFieldsAgainstSchema() {
    messageValidator.validate(
        ResourceLoader.load(RESOURCE_DIR + "orderMessageWithNullForOptionalFields.json"),
        Order.class
    );
  }

  @Test
  public void jsonSampleMinimal() {
    Approvals.verify(jsonBinder.toJson(createOrderMinimal()));
  }

  @Test
  public void jsonSampleFull() {
    Approvals.verify(jsonBinder.toJson(createOrderFull()));
  }

  private static Order createOrderMinimal() {
    return new Order(
        0L,
        Instant.EPOCH,
        "version",
        "manufacture",
        "serial-number",
        "some-order-id",
        0L,
        List.of(
            new Node(
                "source-node",
                0L,
                true,
                List.of()
            )
        ),
        List.of()
    );
  }

  private static Order createOrderFull() {
    return new Order(
        0L,
        Instant.EPOCH,
        "version",
        "manufacture",
        "serial-number",
        "some-order-id",
        0L,
        List.of(
            new Node(
                "source-node",
                0L,
                true,
                List.of(
                    new Action("some-action-type", "some-action-id", BlockingType.NONE)
                        .setActionDescription("some-action-description")
                        .setActionParameters(
                            List.of(
                                new ActionParameter("some-key", "some-value"),
                                new ActionParameter("some-other-key", "some-other-value")
                            )
                        )
                )
            )
                .setNodeDescription("some-node-description")
                .setNodePosition(
                    new NodePosition(1.2, 3.4, "some-map-id")
                        .setTheta(1.3)
                        .setMapDescription("some-map-description")
                        .setAllowedDeviationXY(0.1)
                        .setAllowedDeviationTheta(0.1)
                ),
            new Node(
                "destination-node",
                0L,
                true,
                List.of(
                    new Action("some-action-type", "some-action-id", BlockingType.NONE)
                        .setActionDescription("some-action-description")
                        .setActionParameters(
                            List.of(
                                new ActionParameter("some-key", "some-value"),
                                new ActionParameter("some-other-key", "some-other-value")
                            )
                        )
                )
            )
                .setNodeDescription("some-node-description")
                .setNodePosition(
                    new NodePosition(5.6, 7.8, "some-map-id")
                        .setTheta(0.0)
                        .setMapDescription("some-map-description")
                        .setAllowedDeviationXY(0.2)
                        .setAllowedDeviationTheta(0.3)
                )
        ),
        List.of(
            new Edge(
                "some-edge-id",
                0L,
                true,
                "source-node",
                "destination-node",
                List.of(
                    new Action("some-action-type", "some-action-id", BlockingType.NONE)
                        .setActionDescription("some-action-description")
                        .setActionParameters(
                            List.of(
                                new ActionParameter("some-key", "some-value"),
                                new ActionParameter("some-other-key", "some-other-value")
                            )
                        )
                )
            )
                .setDirection("some-direction")
                .setLength(123.0)
                .setEdgeDescription("some-edge-description")
                .setMaxHeight(3.0)
                .setMaxRotationSpeed(44.0)
                .setMaxSpeed(123.0)
                .setMinHeight(1.0)
                .setOrientation(0.0)
                .setRotationAllowed(true)
                .setTrajectory(
                    new Trajectory(
                        1.0,
                        List.of(0.1, 0.2, 0.3),
                        List.of(
                            new ControlPoint(1.2, 2.3, 1.0)
                                .setOrientation(3.14)
                        )
                    )
                )
        )
    )
        .setZoneSetId("some-zone-set-id");
  }

}
