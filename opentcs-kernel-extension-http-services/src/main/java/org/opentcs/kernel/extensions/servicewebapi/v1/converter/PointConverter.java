// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.converter;

import static java.util.Objects.requireNonNull;

import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.opentcs.access.to.model.BoundingBoxCreationTO;
import org.opentcs.access.to.model.CoupleCreationTO;
import org.opentcs.access.to.model.PointCreationTO;
import org.opentcs.data.model.Couple;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Pose;
import org.opentcs.data.model.Triple;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.PointTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.BoundingBoxTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.CoupleTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.TripleTO;

/**
 * Includes the conversion methods for all Point classes.
 */
public class PointConverter {

  private final PropertyConverter pConverter;
  private final EnvelopeConverter envelopeConverter;

  @Inject
  public PointConverter(PropertyConverter pConverter, EnvelopeConverter envelopeConverter) {
    this.pConverter = requireNonNull(pConverter, "pConverter");
    this.envelopeConverter = requireNonNull(envelopeConverter, "envelopeConverter");
  }

  public List<PointTO> toPointTOs(Set<Point> points) {
    return points.stream()
        .map(
            point -> new PointTO(point.getName())
                .setPosition(
                    new TripleTO(
                        point.getPose().getPosition().getX(),
                        point.getPose().getPosition().getY(),
                        point.getPose().getPosition().getZ()
                    )
                )
                .setType(point.getType().name())
                .setVehicleOrientationAngle(point.getPose().getOrientationAngle())
                .setVehicleEnvelopes(envelopeConverter.toEnvelopeTOs(point.getVehicleEnvelopes()))
                .setMaxVehicleBoundingBox(
                    new BoundingBoxTO(
                        point.getMaxVehicleBoundingBox().getLength(),
                        point.getMaxVehicleBoundingBox().getWidth(),
                        point.getMaxVehicleBoundingBox().getHeight(),
                        new CoupleTO(
                            point.getMaxVehicleBoundingBox().getReferenceOffset().getX(),
                            point.getMaxVehicleBoundingBox().getReferenceOffset().getY()
                        )
                    )
                )
                .setProperties(pConverter.toPropertyTOs(point.getProperties()))
                .setLayout(
                    new PointTO.Layout()
                        .setLabelOffset(
                            new CoupleTO(
                                point.getLayout().getLabelOffset().getX(),
                                point.getLayout().getLabelOffset().getY()
                            )
                        )
                        .setPosition(
                            new CoupleTO(
                                point.getLayout().getPosition().getX(),
                                point.getLayout().getPosition().getY()
                            )
                        )
                        .setLayerId(point.getLayout().getLayerId())
                )
        )
        .sorted(Comparator.comparing(PointTO::getName))
        .collect(Collectors.toCollection(ArrayList::new));
  }

  public List<PointCreationTO> toPointCreationTOs(List<PointTO> points) {
    return points.stream()
        .map(
            point -> new PointCreationTO(point.getName())
                .withProperties(pConverter.toPropertyMap(point.getProperties()))
                .withPose(
                    new Pose(
                        new Triple(
                            point.getPosition().getX(),
                            point.getPosition().getY(),
                            point.getPosition().getZ()
                        ),
                        point.getVehicleOrientationAngle()
                    )
                )
                .withType(Point.Type.valueOf(point.getType()))
                .withLayout(
                    new PointCreationTO.Layout(
                        new Couple(
                            point.getLayout().getPosition().getX(),
                            point.getLayout().getPosition().getY()
                        ),
                        new Couple(
                            point.getLayout().getLabelOffset().getX(),
                            point.getLayout().getLabelOffset().getY()
                        ),
                        point.getLayout().getLayerId()
                    )
                )
                .withVehicleEnvelopes(
                    envelopeConverter
                        .toVehicleEnvelopeMap(point.getVehicleEnvelopes())
                )
                .withMaxVehicleBoundingBox(
                    new BoundingBoxCreationTO(
                        point.getMaxVehicleBoundingBox().getLength(),
                        point.getMaxVehicleBoundingBox().getWidth(),
                        point.getMaxVehicleBoundingBox().getHeight()
                    )
                        .withReferenceOffset(
                            new CoupleCreationTO(
                                point.getMaxVehicleBoundingBox().getReferenceOffset().getX(),
                                point.getMaxVehicleBoundingBox().getReferenceOffset().getY()
                            )
                        )
                )
        )
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
