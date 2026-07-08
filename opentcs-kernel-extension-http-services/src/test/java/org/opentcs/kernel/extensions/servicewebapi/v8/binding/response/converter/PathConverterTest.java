// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.converter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.opentcs.data.model.Couple;
import org.opentcs.data.model.Envelope;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.peripherals.PeripheralOperation;
import org.opentcs.kernel.extensions.servicewebapi.common.JsonBinder;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.PathTO;

/**
 * Tests for {@link PathConverter}.
 */
class PathConverterTest {

  private JsonBinder jsonBinder;
  private PathConverter pathConverter;

  @BeforeEach
  void setUp() {
    jsonBinder = new JsonBinder();
    pathConverter = new PathConverter();
  }

  @Test
  void convert() {
    Path path = new Path(
        "path-1",
        new Point("point-1").getReference(),
        new Point("point-2").getReference()
    )
        .withProperties(Map.of("key-1", "value-1"))
        .withLength(1)
        .withMaxVelocity(2)
        .withMaxReverseVelocity(3)
        .withPeripheralOperations(
            List.of(
                new PeripheralOperation(
                    new Location(
                        "location-1",
                        new LocationType("location-type-1").getReference()
                    ).getReference(),
                    "operation-1",
                    PeripheralOperation.ExecutionTrigger.AFTER_ALLOCATION,
                    true
                )
            )
        )
        .withLocked(true)
        .withVehicleEnvelopes(
            Map.of("envelope-key-1", new Envelope(List.of(new Couple(4, 5))))
        )
        .withLayout(
            new Path.Layout(
                Path.Layout.ConnectionType.POLYPATH,
                List.of(new Couple(6, 7)),
                8
            )
        );

    PathTO result = pathConverter.convert(path);

    Approvals.verify(jsonBinder.toJson(result));
  }

  @ParameterizedTest
  @EnumSource(PeripheralOperation.ExecutionTrigger.class)
  void convertsExecutionTriggers(PeripheralOperation.ExecutionTrigger executionTrigger) {
    Path path = new Path(
        "path-1",
        new Point("point-1").getReference(),
        new Point("point-2").getReference()
    ).withPeripheralOperations(
        List.of(
            new PeripheralOperation(
                new Location(
                    "location-1",
                    new LocationType("location-type-1").getReference()
                ).getReference(),
                "operation-1",
                executionTrigger,
                true
            )
        )
    );

    PathTO result = pathConverter.convert(path);

    PathTO.PeripheralOperationTO.ExecutionTriggerTO expectedExecutionTrigger
        = switch (executionTrigger) {
          case IMMEDIATE -> PathTO.PeripheralOperationTO.ExecutionTriggerTO.IMMEDIATE;
          case AFTER_ALLOCATION -> PathTO.PeripheralOperationTO.ExecutionTriggerTO.AFTER_ALLOCATION;
          case AFTER_MOVEMENT -> PathTO.PeripheralOperationTO.ExecutionTriggerTO.AFTER_MOVEMENT;
        };
    assertThat(result.getPeripheralOperations().getFirst().getExecutionTrigger())
        .isEqualTo(expectedExecutionTrigger);
  }

  @ParameterizedTest
  @EnumSource(Path.Layout.ConnectionType.class)
  void convertsConnectionTypes(Path.Layout.ConnectionType connectionType) {
    Path path = new Path(
        "path-1",
        new Point("point-1").getReference(),
        new Point("point-2").getReference()
    ).withLayout(
        new Path.Layout()
            .withConnectionType(connectionType)
    );

    PathTO result = pathConverter.convert(path);

    PathTO.LayoutTO.ConnectionTypeTO expectedConnectionType
        = switch (connectionType) {
          case DIRECT -> PathTO.LayoutTO.ConnectionTypeTO.DIRECT;
          case ELBOW -> PathTO.LayoutTO.ConnectionTypeTO.ELBOW;
          case SLANTED -> PathTO.LayoutTO.ConnectionTypeTO.SLANTED;
          case POLYPATH -> PathTO.LayoutTO.ConnectionTypeTO.POLYPATH;
          case BEZIER -> PathTO.LayoutTO.ConnectionTypeTO.BEZIER;
          case BEZIER_3 -> PathTO.LayoutTO.ConnectionTypeTO.BEZIER_3;
        };
    assertThat(result.getLayout().getConnectionType()).isEqualTo(expectedConnectionType);
  }
}
