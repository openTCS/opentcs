// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.request;

import java.util.List;
import java.util.Set;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.kernel.extensions.servicewebapi.common.JsonBinder;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.BlockTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.LayerGroupTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.LayerTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.LocationRepresentationTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.LocationTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.LocationTypeTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.PathTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.PeripheralOperationTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.PointTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.VehicleTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.VisualLayoutTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.shared.BoundingBoxTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.shared.CoupleTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.shared.EnvelopeTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.shared.PropertyTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.shared.TripleTO;

/**
 */
class PutPlantModelRequestTOTest {

  private JsonBinder jsonBinder;

  @BeforeEach
  void setUp() {
    jsonBinder = new JsonBinder();
  }

  @Test
  void jsonSample() {
    PutPlantModelRequestTO to = new PutPlantModelRequestTO("some-name")
        .setPoints(
            List.of(
                new PointTO("some-point")
                    .setProperties(List.of(new PropertyTO("point-prop", "point-value")))
                    .setPosition(new TripleTO(25000, -15000, 0))
                    .setVehicleOrientationAngle(73.3)
                    .setType(PointTO.Type.PARK_POSITION)
                    .setVehicleEnvelopes(
                        List.of(
                            new EnvelopeTO(
                                "envelopeType-01",
                                List.of(
                                    new CoupleTO(25500, -15500),
                                    new CoupleTO(25500, -14500),
                                    new CoupleTO(24500, -14500),
                                    new CoupleTO(24500, -15500)
                                )
                            )
                        )
                    )
                    .setMaxVehicleBoundingBox(
                        new BoundingBoxTO(1500, 2000, 3000, new CoupleTO(10, 20))
                    )
                    .setLayout(
                        new PointTO.Layout()
                            .setPosition(new CoupleTO(25000, -15000))
                            .setLabelOffset(new CoupleTO(-10, -20))
                            .setLayerId(0)
                    ),
                new PointTO("some-point2")
                    .setProperties(List.of(new PropertyTO("point-prop", "point-value")))
                    .setPosition(new TripleTO(18000, -15000, 0))
                    .setMaxVehicleBoundingBox(
                        new BoundingBoxTO(1500, 2000, 3000, new CoupleTO(10, 20))
                    )
                    .setLayout(
                        new PointTO.Layout()
                            .setPosition(new CoupleTO(18000, -15000))
                            .setLabelOffset(new CoupleTO(-10, -20))
                            .setLayerId(0)
                    ),
                new PointTO("some-point3")
                    .setProperties(List.of(new PropertyTO("point-prop", "point-value")))
                    .setPosition(new TripleTO(25000, -9000, 0))
                    .setMaxVehicleBoundingBox(
                        new BoundingBoxTO(1500, 2000, 3000, new CoupleTO(10, 20))
                    )
                    .setLayout(
                        new PointTO.Layout()
                            .setPosition(new CoupleTO(25000, -9000))
                            .setLabelOffset(new CoupleTO(-10, -20))
                            .setLayerId(0)
                    )
            )
        )
        .setPaths(
            List.of(
                new PathTO("some-path", "some-point", "some-point2")
                    .setProperties(List.of(new PropertyTO("path-prop", "path-value")))
                    .setLength(3)
                    .setMaxVelocity(13)
                    .setMaxReverseVelocity(3)
                    .setLocked(true)
                    .setVehicleEnvelopes(
                        List.of(
                            new EnvelopeTO(
                                "envelopeType-01",
                                List.of(
                                    new CoupleTO(25500, -15500),
                                    new CoupleTO(25500, -14500),
                                    new CoupleTO(17500, -14500),
                                    new CoupleTO(17500, -15500)
                                )
                            )
                        )
                    )
                    .setPeripheralOperations(
                        List.of(
                            new PeripheralOperationTO(
                                "some-op",
                                "some-location"
                            )
                                .setExecutionTrigger(
                                    PeripheralOperationTO.ExecutionTrigger.AFTER_ALLOCATION
                                )
                                .setCompletionRequired(true)
                        )
                    )
                    .setLayout(
                        new PathTO.Layout()
                            .setConnectionType(PathTO.Layout.ConnectionType.SLANTED)
                            .setLayerId(0)
                            .setControlPoints(
                                List.of(
                                    new CoupleTO(43000, 30000),
                                    new CoupleTO(44000, 31000),
                                    new CoupleTO(45000, 32000)
                                )
                            )
                    ),
                new PathTO("another-path", "some-point2", "some-point3")
            )
        )
        .setLocationTypes(
            List.of(
                new LocationTypeTO("some-locationType")
                    .setProperties(List.of(new PropertyTO("locType-prop", "locType-value")))
                    .setAllowedOperations(
                        List.of("some-operation", "another-operation", "operation3")
                    )
                    .setAllowedPeripheralOperations(
                        List.of("some-perOp", "another-perOp", "perOp3")
                    )
                    .setLayout(
                        new LocationTypeTO.Layout()
                            .setLocationRepresentation(LocationRepresentationTO.WORKING_GENERIC)
                    )
            )
        )
        .setLocations(
            List.of(
                new LocationTO(
                    "some-location",
                    "some-locationType",
                    new TripleTO(30000, -15000, 0)
                )
                    .setLocked(true)
                    .setLayout(
                        new LocationTO.Layout()
                            .setPosition(new CoupleTO(30000, -15000))
                            .setLabelOffset(new CoupleTO(-10, -20))
                            .setLocationRepresentation(
                                LocationRepresentationTO.LOAD_TRANSFER_GENERIC
                            )
                    )
            )
        )
        .setBlocks(
            List.of(
                new BlockTO("some-block")
                    .setProperties(List.of(new PropertyTO("block-prop", "block-value")))
                    .setType(BlockTO.Type.SAME_DIRECTION_ONLY)
                    .setMemberNames(Set.of("some-point2"))
                    .setLayout(new BlockTO.Layout())
            )
        )
        .setVehicles(
            List.of(
                new VehicleTO("some-vehicle")
                    .setProperties(List.of(new PropertyTO("vehicle-prop", "vehicle-value")))
                    .setBoundingBox(new BoundingBoxTO(150, 200, 300, new CoupleTO(10, 20)))
                    .setEnergyLevelCritical(10)
                    .setEnergyLevelGood(30)
                    .setEnergyLevelSufficientlyRecharged(60)
                    .setMaxVelocity(2000)
                    .setMaxReverseVelocity(733)
                    .setLayout(new VehicleTO.Layout().setRouteColor("#123456"))
            )
        )
        .setVisualLayout(
            new VisualLayoutTO("some-visualLayout")
                .setProperties(List.of(new PropertyTO("vLayout-prop", "vLayout-value")))
                .setScaleX(65)
                .setScaleY(65)
                .setLayers(List.of(new LayerTO(0, 0, true, "layer0", 0)))
                .setLayerGroups(List.of(new LayerGroupTO(0, "layerGroup0", true)))
        )
        .setProperties(List.of(new PropertyTO("plantModel-prop", "value")));

    Approvals.verify(jsonBinder.toJson(to));
  }

}
