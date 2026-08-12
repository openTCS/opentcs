// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state;

import java.time.Instant;
import java.util.List;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.commadapter.vehicle.vda5050.ResourceLoader;
import org.opentcs.commadapter.vehicle.vda5050.common.JsonBinder;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.MessageValidator;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.AgvPosition;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.ControlPoint;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.NodePosition;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.Trajectory;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.Velocity;

/**
 * Unit tests for {@link State}.
 */
public class StateTest {

  private static final String RESOURCE_DIR
      = "/org/opentcs/commadapter/vehicle/vda5050/v2_0/message/state/";

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
        jsonBinder.toJson(createStateMinimal()),
        State.class
    );
  }

  @Test
  public void validateAgainstJsonSchemaFull() {
    messageValidator.validate(
        jsonBinder.toJson(createStateFull()),
        State.class
    );
  }

  @Test
  public void validateJsonWithNullForOptionalFieldsAgainstSchema() {
    messageValidator.validate(
        ResourceLoader.load(RESOURCE_DIR + "stateMessageWithNullForOptionalFields.json"),
        State.class
    );
  }

  @Test
  public void deserializeJsonWithNullForOptionalFields() {
    Approvals.verify(
        jsonBinder.toJson(
            jsonBinder.fromJson(
                ResourceLoader.load(RESOURCE_DIR + "stateMessageWithNullForOptionalFields.json"),
                State.class
            )
        )
    );
  }

  @Test
  public void validateJsonWithNullForOptionalRootFieldsAgainstSchema() {
    messageValidator.validate(
        ResourceLoader.load(RESOURCE_DIR + "stateMessageWithNullForOptionalRootFields.json"),
        State.class
    );
  }

  @Test
  public void deserializeJsonWithNullForOptionalRootFields() {
    Approvals.verify(
        jsonBinder.toJson(
            jsonBinder.fromJson(
                ResourceLoader.load(
                    RESOURCE_DIR + "stateMessageWithNullForOptionalRootFields.json"
                ),
                State.class
            )
        )
    );
  }

  @Test
  public void jsonSampleMinimal() {
    Approvals.verify(jsonBinder.toJson(createStateMinimal()));
  }

  @Test
  public void jsonSampleFull() {
    Approvals.verify(jsonBinder.toJson(createStateFull()));
  }

  private static State createStateMinimal() {
    return new State(
        0L,
        Instant.EPOCH,
        "version",
        "manufacturer",
        "serial-number",
        "some-order",
        0L,
        "some-node",
        0L,
        List.of(),
        List.of(),
        true,
        List.of(),
        new BatteryState(50.0, false),
        OperatingMode.SEMIAUTOMATIC,
        List.of(),
        new SafetyState(EStop.AUTOACK, false)
    );
  }

  private static State createStateFull() {
    return new State(
        0L,
        Instant.EPOCH,
        "version",
        "manufacturer",
        "serial-number",
        "some-order",
        0L,
        "some-node",
        0L,
        List.of(
            new NodeState("some-node", 0L, true)
                .setNodeDescription("some-node-description")
                .setReleased(true)
                .setNodePosition(
                    new NodePosition(1.2, 3.4, "some-map-id")
                        .setTheta(2.7)
                        .setAllowedDeviationXY(0.2)
                        .setAllowedDeviationTheta(0.5)
                        .setMapDescription("some-map-description")
                )
        ),
        List.of(
            new EdgeState("some-edge", 0L, true)
                .setEdgeDescription("some-description")
                .setTrajectory(
                    new Trajectory(
                        1.0,
                        List.of(0.0),
                        List.of(
                            new ControlPoint(9.8, 7.6)
                                .setWeight(2.0)
                        )
                    )
                )
        ),
        true,
        List.of(
            new ActionState("some-action-id", ActionStatus.FAILED)
                .setActionType("some-type")
                .setActionDescription("some-action-description")
                .setResultDescription("some-result-description")
        ),
        new BatteryState(50.0, false)
            .setBatteryVoltage(12.0)
            .setBatteryHealth(70L)
            .setReach(70L),
        OperatingMode.SEMIAUTOMATIC,
        List.of(
            new ErrorEntry(
                "some-error",
                ErrorLevel.FATAL
            )
                .setErrorDescription("some-error-description")
                .setErrorReferences(
                    List.of(
                        new ErrorReference("some-ref-key", "some-ref-value")
                    )
                )
        ),
        new SafetyState(EStop.AUTOACK, false)
    )
        .setZoneSetId("some-zone-set")
        .setAgvPosition(
            new AgvPosition(2.1, 5.4, 0.3, "some-map-id", true)
                .setDeviationRange(12.0)
                .setLocalizationScore(0.5)
                .setMapDescription("some-map-description")
        )
        .setVelocity(
            new Velocity()
                .setVx(0.1)
                .setVy(0.3)
                .setOmega(0.0)
        )
        .setLoads(
            List.of(
                new Load()
                    .setLoadId("some-load")
                    .setLoadType("some-load-type")
                    .setLoadPosition("some-load-position")
                    .setBoundingBoxReference(
                        new BoundingBoxReference(0.0, 0.0, 0.0)
                            .setTheta(0.0)
                    )
                    .setLoadDimensions(
                        new LoadDimensions(0.0, 0.0)
                            .setHeight(1.4)
                    )
                    .setWeight(0L)
            )
        )
        .setInformation(
            List.of(
                new InfoEntry("some-info", InfoLevel.DEBUG)
                    .setInfoDescription("some-info-description")
                    .setInfoReferences(
                        List.of(
                            new InfoReference("some-ref-key", "some-ref-value")
                        )
                    )
            )
        )
        .setPaused(Boolean.FALSE)
        .setNewBaseRequest(Boolean.FALSE)
        .setDistanceSinceLastNode(12.0);
  }
}
