// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v1_1.message.instantactions;

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

/**
 * Unit tests for {@link InstantActions}.
 */
public class InstantActionsTest {

  private static final String RESOURCE_DIR
      = "/org/opentcs/commadapter/vehicle/vda5050/v1_1/message/instantactions/";

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
        jsonBinder.toJson(createInstantActionsMinimal()),
        InstantActions.class
    );
  }

  @Test
  public void validateAgainstJsonSchemaFull() {
    messageValidator.validate(
        jsonBinder.toJson(createInstantActionsFull()),
        InstantActions.class
    );
  }

  @Test
  public void validateJsonWithNullForOptionalFieldsAgainstSchema() {
    messageValidator.validate(
        ResourceLoader.load(RESOURCE_DIR + "instantActionsMessageWithNullForOptionalFields.json"),
        InstantActions.class
    );
  }

  @Test
  public void jsonSampleMinimal() {
    Approvals.verify(jsonBinder.toJson(createInstantActionsMinimal()));
  }

  @Test
  public void jsonSampleFull() {
    Approvals.verify(jsonBinder.toJson(createInstantActionsFull()));
  }

  private InstantActions createInstantActionsMinimal() {
    return new InstantActions(
        0L,
        Instant.EPOCH,
        "version",
        "manufacturer",
        "serial-number",
        List.of(new Action("some-action-type", "action1", BlockingType.HARD))
    );
  }

  private InstantActions createInstantActionsFull() {
    return new InstantActions(
        0L,
        Instant.EPOCH,
        "version",
        "manufacturer",
        "serial-number",
        List.of(
            new Action("some-action-type", "action1", BlockingType.NONE)
                .setActionDescription("action-description")
                .setActionParameters(
                    List.of(
                        new ActionParameter("some-key", "some-value"),
                        new ActionParameter("some-other-key", "some-other-value")
                    )
                ),
            new Action("some-other-action-type", "action2", BlockingType.SOFT)
                .setActionDescription("action-description")
                .setActionParameters(
                    List.of(
                        new ActionParameter("some-key", "some-value"),
                        new ActionParameter("some-other-key", "some-other-value")
                    )
                )
        )
    );
  }
}
