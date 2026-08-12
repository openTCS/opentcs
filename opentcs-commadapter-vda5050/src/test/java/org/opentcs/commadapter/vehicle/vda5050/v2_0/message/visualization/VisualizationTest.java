// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v2_0.message.visualization;

import java.time.Instant;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.commadapter.vehicle.vda5050.ResourceLoader;
import org.opentcs.commadapter.vehicle.vda5050.common.JsonBinder;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.MessageValidator;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.AgvPosition;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.Velocity;

/**
 * Unit tests for {@link Visualization}.
 */
public class VisualizationTest {

  private static final String RESOURCE_DIR
      = "/org/opentcs/commadapter/vehicle/vda5050/v2_0/message/visualization/";

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
        jsonBinder.toJson(createVisualizationMinimal()),
        Visualization.class
    );
  }

  @Test
  public void validateAgainstJsonSchemaFull() {
    messageValidator.validate(
        jsonBinder.toJson(createVisualizationFull()),
        Visualization.class
    );
  }

  @Test
  public void validateJsonWithNullForOptionalFieldsAgainstSchema() {
    messageValidator.validate(
        ResourceLoader.load(RESOURCE_DIR + "visualizationMessageWithNullForOptionalFields.json"),
        Visualization.class
    );
  }

  @Test
  public void deserializeJsonWithNullForOptionalFields() {
    Approvals.verify(
        jsonBinder.toJson(
            jsonBinder.fromJson(
                ResourceLoader.load(
                    RESOURCE_DIR + "visualizationMessageWithNullForOptionalFields.json"
                ),
                Visualization.class
            )
        )
    );
  }

  @Test
  public void validateJsonWithNullForOptionalRootFieldsAgainstSchema() {
    messageValidator.validate(
        ResourceLoader.load(
            RESOURCE_DIR + "visualizationMessageWithNullForOptionalRootFields.json"
        ),
        Visualization.class
    );
  }

  @Test
  public void deserializeJsonWithNullForOptionalRootFields() {
    Approvals.verify(
        jsonBinder.toJson(
            jsonBinder.fromJson(
                ResourceLoader.load(
                    RESOURCE_DIR + "visualizationMessageWithNullForOptionalRootFields.json"
                ),
                Visualization.class
            )
        )
    );
  }

  @Test
  public void jsonSampleMinimal() {
    Approvals.verify(jsonBinder.toJson(createVisualizationMinimal()));
  }

  @Test
  public void jsonSampleFull() {
    Approvals.verify(jsonBinder.toJson(createVisualizationFull()));
  }

  private Visualization createVisualizationMinimal() {
    return new Visualization(
        0L,
        Instant.EPOCH,
        "version",
        "manufacturer",
        "serial-number"
    );
  }

  private Visualization createVisualizationFull() {
    return new Visualization(
        0L,
        Instant.EPOCH,
        "version",
        "manufacturer",
        "serial-number"
    )
        .setAgvPosition(
            new AgvPosition(1.2, 3.4, 1.2, "some-map-id", true)
                .setDeviationRange(0.1)
                .setLocalizationScore(0.2)
                .setMapDescription("some-map-description")
        )
        .setVelocity(
            new Velocity()
                .setVx(0.1)
                .setVy(0.2)
                .setOmega(0.3)
        );
  }
}
