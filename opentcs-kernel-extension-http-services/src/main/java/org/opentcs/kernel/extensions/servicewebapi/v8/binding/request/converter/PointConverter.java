// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.converter;

import static java.util.Objects.requireNonNull;

import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.opentcs.access.to.model.BoundingBoxCreationTO;
import org.opentcs.access.to.model.CoupleCreationTO;
import org.opentcs.access.to.model.PointCreationTO;
import org.opentcs.access.to.model.PoseCreationTO;
import org.opentcs.access.to.model.TripleCreationTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.PointTO;

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

  public List<PointCreationTO> toPointCreationTOs(List<PointTO> points) {
    return points.stream()
        .map(
            point -> new PointCreationTO(point.getName())
                .withProperties(pConverter.toPropertyMap(point.getProperties()))
                .withPose(
                    new PoseCreationTO(
                        new TripleCreationTO(
                            point.getPosition().getX(),
                            point.getPosition().getY(),
                            point.getPosition().getZ()
                        ),
                        point.getVehicleOrientationAngle()
                    )
                )
                .withType(toPointType(point.getType()))
                .withLayout(
                    new PointCreationTO.Layout(
                        new CoupleCreationTO(
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

  private PointCreationTO.Type toPointType(PointTO.Type type) {
    return switch (type) {
      case HALT_POSITION -> PointCreationTO.Type.HALT_POSITION;
      case PARK_POSITION -> PointCreationTO.Type.PARK_POSITION;
    };
  }
}
