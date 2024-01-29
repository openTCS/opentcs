/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.binding;

import java.util.List;
import java.util.Set;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.kernel.extensions.servicewebapi.JsonBinder;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.BlockTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.CoupleTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.LayerGroupTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.LayerTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.LocationTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.LocationTypeTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.PathTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.PeripheralOperationTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.PointTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.EnvelopeTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.PropertyTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.TripleTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.VehicleTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.VisualLayoutTO;

/**
 */
class PlantModelTOTest {

  private JsonBinder jsonBinder;

  @BeforeEach
  void setUp() {
    jsonBinder = new JsonBinder();
  }

  @Test
  void jsonSample() {
    PlantModelTO to = new PlantModelTO("some-name")
        .setPoints(List.of(
            new PointTO("some-point")
                .setProperties(List.of(new PropertyTO("point-prop", "point-value")))
                .setPosition(new TripleTO(25000, -15000, 0))
                .setVehicleOrientationAngle(73.3)
                .setType("PARK_POSITION")
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
                .setLayout(new PointTO.Layout()
                    .setPosition(new CoupleTO(25000, -15000))
                    .setLabelOffset(new CoupleTO(-10, -20))
                    .setLayerId(0)),
            new PointTO("some-point2")
                .setProperties(List.of(new PropertyTO("point-prop", "point-value")))
                .setPosition(new TripleTO(18000, -15000, 0))
                .setLayout(new PointTO.Layout()
                    .setPosition(new CoupleTO(18000, -15000))
                    .setLabelOffset(new CoupleTO(-10, -20))
                    .setLayerId(0)),
            new PointTO("some-point3")
                .setProperties(List.of(new PropertyTO("point-prop", "point-value")))
                .setPosition(new TripleTO(25000, -9000, 0))
                .setLayout(new PointTO.Layout()
                    .setPosition(new CoupleTO(25000, -9000))
                    .setLabelOffset(new CoupleTO(-10, -20))
                    .setLayerId(0))))
        .setPaths(List.of(
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
                .setPeripheralOperations(List.of(new PeripheralOperationTO("some-op",
                                                                           "some-location")
                    .setExecutionTrigger("AFTER_ALLOCATION")
                    .setCompletionRequired(true)))
                .setLayout(new PathTO.Layout()
                    .setConnectionType("SLANTED")
                    .setLayerId(0)
                    .setControlPoints(List.of(new CoupleTO(43000, 30000),
                                              new CoupleTO(44000, 31000),
                                              new CoupleTO(45000, 32000)))),
            new PathTO("another-path", "some-point2", "some-point3")))
        .setLocationTypes(List.of(
            new LocationTypeTO("some-locationType")
                .setProperties(List.of(new PropertyTO("locType-prop", "locType-value")))
                .setAllowedOperations(List.of("some-operation", "another-operation", "operation3"))
                .setAllowedPeripheralOperations(List.of("some-perOp", "another-perOp", "perOp3"))
                .setLayout(
                    new LocationTypeTO.Layout()
                        .setLocationRepresentation("WORKING_GENERIC"))))
        .setLocations(List.of(
            new LocationTO("some-location",
                           "some-locationType",
                           new TripleTO(30000, -15000, 0))
                .setLocked(true)
                .setLayout(
                    new LocationTO.Layout()
                        .setPosition(new CoupleTO(30000, -15000))
                        .setLabelOffset(new CoupleTO(-10, -20))
                        .setLocationRepresentation("LOAD_TRANSFER_GENERIC"))))
        .setBlocks(List.of(
            new BlockTO("some-block")
                .setProperties(List.of(new PropertyTO("block-prop", "block-value")))
                .setType("SAME_DIRECTION_ONLY")
                .setMemberNames(Set.of("some-point2"))
                .setLayout(new BlockTO.Layout())))
        .setVehicles(List.of(
            new VehicleTO("some-vehicle")
                .setProperties(List.of(new PropertyTO("vehicle-prop", "vehicle-value")))
                .setLength(1456)
                .setEnergyLevelCritical(10)
                .setEnergyLevelGood(30)
                .setEnergyLevelSufficientlyRecharged(60)
                .setMaxVelocity(2000)
                .setMaxReverseVelocity(733)
                .setLayout(new VehicleTO.Layout().setRouteColor("#123456"))))
        .setVisualLayout(
            new VisualLayoutTO("some-visualLayout")
                .setProperties(List.of(new PropertyTO("vLayout-prop", "vLayout-value")))
                .setScaleX(65)
                .setScaleY(65)
                .setLayers(List.of(new LayerTO(0, 0, true, "layer0", 0)))
                .setLayerGroups(List.of(new LayerGroupTO(0, "layerGroup0", true))))
        .setProperties(List.of(new PropertyTO("plantModel-prop", "value")));

    Approvals.verify(jsonBinder.toJson(to));
  }

}
