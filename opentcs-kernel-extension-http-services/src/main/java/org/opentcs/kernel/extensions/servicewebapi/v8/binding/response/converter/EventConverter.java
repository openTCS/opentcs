// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.converter;

import static java.util.Objects.requireNonNull;
import static org.opentcs.util.Assertions.checkArgument;

import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import org.opentcs.access.NotificationPublicationEvent;
import org.opentcs.data.TCSObjectEvent;
import org.opentcs.data.model.EnvironmentalEntity;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.data.peripherals.PeripheralJob;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.UserNotificationTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.sse.EnvironmentalEntityEventTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.sse.LocationEventTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.sse.OrderSequenceEventTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.sse.PathEventTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.sse.PeripheralJobEventTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.sse.TransportOrderEventTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.sse.VehicleEventTO;

/**
 * Converts application events to their SSE representation.
 */
public class EventConverter {

  private final VehicleConverter vehicleConverter;
  private final TransportOrderConverter transportOrderConverter;
  private final OrderSequenceConverter orderSequenceConverter;
  private final PeripheralJobConverter peripheralJobConverter;
  private final PathConverter pathConverter;
  private final LocationConverter locationConverter;
  private final EnvironmentalEntityConverter environmentalEntityConverter;
  private final UserNotificationConverter userNotificationConverter;

  /**
   * Creates a new instance.
   *
   * @param vehicleConverter Provides methods to convert vehicles to their SSE representation.
   * @param transportOrderConverter Provides methods to convert transport orders to their SSE
   * representation.
   * @param orderSequenceConverter Provides methods to convert order sequences to their SSE
   * representation.
   * @param peripheralJobConverter Provides methods to convert peripheral jobs to their SSE
   * representation.
   * @param pathConverter Provides methods to convert paths to their SSE representation.
   * @param locationConverter Provides methods to convert locations to their SSE representation.
   * @param environmentalEntityConverter Provides methods to convert environmental entities to their
   * SSE representation.
   * @param userNotificationConverter Provides methods to convert user notifications to their SSE
   * representation.
   */
  @Inject
  public EventConverter(
      VehicleConverter vehicleConverter,
      TransportOrderConverter transportOrderConverter,
      OrderSequenceConverter orderSequenceConverter,
      PeripheralJobConverter peripheralJobConverter,
      PathConverter pathConverter,
      LocationConverter locationConverter,
      EnvironmentalEntityConverter environmentalEntityConverter,
      UserNotificationConverter userNotificationConverter
  ) {
    this.vehicleConverter = requireNonNull(vehicleConverter, "vehicleConverter");
    this.transportOrderConverter = requireNonNull(
        transportOrderConverter,
        "transportOrderConverter"
    );
    this.orderSequenceConverter = requireNonNull(orderSequenceConverter, "orderSequenceConverter");
    this.peripheralJobConverter = requireNonNull(peripheralJobConverter, "peripheralJobConverter");
    this.pathConverter = requireNonNull(pathConverter, "pathConverter");
    this.locationConverter = requireNonNull(locationConverter, "locationConverter");
    this.environmentalEntityConverter = requireNonNull(
        environmentalEntityConverter,
        "environmentalEntityConverter"
    );
    this.userNotificationConverter = requireNonNull(
        userNotificationConverter,
        "userNotificationConverter"
    );
  }

  /**
   * Converts the given event to its SSE representation.
   *
   * @param event The event to convert.
   * @return The converted event.
   * @throws IllegalArgumentException If the event's object is not of type {@link Vehicle}.
   */
  public VehicleEventTO convertVehicleEvent(
      @Nonnull
      TCSObjectEvent event
  ) {
    requireNonNull(event, "event");
    checkArgument(
        event.getCurrentOrPreviousObjectState() instanceof Vehicle,
        "Event object must be of type Vehicle"
    );

    return new VehicleEventTO(
        event.getCurrentObjectState() == null
            ? null
            : vehicleConverter.convert((Vehicle) event.getCurrentObjectState()),
        event.getPreviousObjectState() == null
            ? null
            : vehicleConverter.convert((Vehicle) event.getPreviousObjectState())
    );
  }

  /**
   * Converts the given event to its SSE representation.
   *
   * @param event The event to convert.
   * @return The converted event.
   * @throws IllegalArgumentException If the event's object is not of type {@link TransportOrder}.
   */
  public TransportOrderEventTO convertTransportOrderEvent(
      @Nonnull
      TCSObjectEvent event
  ) {
    requireNonNull(event, "event");
    checkArgument(
        event.getCurrentOrPreviousObjectState() instanceof TransportOrder,
        "Event object must be of type TransportOrder"
    );

    return new TransportOrderEventTO(
        event.getCurrentObjectState() == null
            ? null
            : transportOrderConverter.convert((TransportOrder) event.getCurrentObjectState()),
        event.getPreviousObjectState() == null
            ? null
            : transportOrderConverter.convert((TransportOrder) event.getPreviousObjectState())
    );
  }

  /**
   * Converts the given event to its SSE representation.
   *
   * @param event The event to convert.
   * @return The converted event.
   * @throws IllegalArgumentException If the event's object is not of type {@link OrderSequence}.
   */
  public OrderSequenceEventTO convertOrderSequenceEvent(
      @Nonnull
      TCSObjectEvent event
  ) {
    requireNonNull(event, "event");
    checkArgument(
        event.getCurrentOrPreviousObjectState() instanceof OrderSequence,
        "Event object must be of type OrderSequence"
    );

    return new OrderSequenceEventTO(
        event.getCurrentObjectState() == null
            ? null
            : orderSequenceConverter.convert((OrderSequence) event.getCurrentObjectState()),
        event.getPreviousObjectState() == null
            ? null
            : orderSequenceConverter.convert((OrderSequence) event.getPreviousObjectState())
    );
  }

  /**
   * Converts the given event to its SSE representation.
   *
   * @param event The event to convert.
   * @return The converted event.
   * @throws IllegalArgumentException If the event's object is not of type {@link PeripheralJob}.
   */
  public PeripheralJobEventTO convertPeripheralJobEvent(
      @Nonnull
      TCSObjectEvent event
  ) {
    requireNonNull(event, "event");
    checkArgument(
        event.getCurrentOrPreviousObjectState() instanceof PeripheralJob,
        "Event object must be of type PeripheralJob"
    );

    return new PeripheralJobEventTO(
        event.getCurrentObjectState() == null
            ? null
            : peripheralJobConverter.convert((PeripheralJob) event.getCurrentObjectState()),
        event.getPreviousObjectState() == null
            ? null
            : peripheralJobConverter.convert((PeripheralJob) event.getPreviousObjectState())
    );
  }

  /**
   * Converts the given event to its SSE representation.
   *
   * @param event The event to convert.
   * @return The converted event.
   * @throws IllegalArgumentException If the event's object is not of type {@link Path}.
   */
  public PathEventTO convertPathEvent(
      @Nonnull
      TCSObjectEvent event
  ) {
    requireNonNull(event, "event");
    checkArgument(
        event.getCurrentOrPreviousObjectState() instanceof Path,
        "Event object must be of type Path"
    );

    return new PathEventTO(
        event.getCurrentObjectState() == null
            ? null
            : pathConverter.convert((Path) event.getCurrentObjectState()),
        event.getPreviousObjectState() == null
            ? null
            : pathConverter.convert((Path) event.getPreviousObjectState())
    );
  }

  /**
   * Converts the given event to its SSE representation.
   *
   * @param event The event to convert.
   * @return The converted event.
   * @throws IllegalArgumentException If the event's object is not of type {@link Location}.
   */
  public LocationEventTO convertLocationEvent(
      @Nonnull
      TCSObjectEvent event
  ) {
    requireNonNull(event, "event");
    checkArgument(
        event.getCurrentOrPreviousObjectState() instanceof Location,
        "Event object must be of type Location"
    );

    return new LocationEventTO(
        event.getCurrentObjectState() == null
            ? null
            : locationConverter.convert((Location) event.getCurrentObjectState()),
        event.getPreviousObjectState() == null
            ? null
            : locationConverter.convert((Location) event.getPreviousObjectState())
    );
  }

  public EnvironmentalEntityEventTO convertEnvironmentalEntityEvent(
      @Nonnull
      TCSObjectEvent event
  ) {
    requireNonNull(event, "event");
    checkArgument(
        event.getCurrentOrPreviousObjectState() instanceof EnvironmentalEntity,
        "Event object must be of type EnvironmentalEntity"
    );
    return new EnvironmentalEntityEventTO(
        event.getCurrentObjectState() == null
            ? null
            : environmentalEntityConverter.convert(
                (EnvironmentalEntity) event.getCurrentObjectState()
            ),
        event.getPreviousObjectState() == null
            ? null
            : environmentalEntityConverter.convert(
                (EnvironmentalEntity) event.getPreviousObjectState()
            )
    );
  }

  /**
   * Converts the given event to its SSE representation.
   *
   * @param event The event to convert.
   * @return The converted user notification.
   */
  public UserNotificationTO convertUserNotificationEvent(
      @Nonnull
      NotificationPublicationEvent event
  ) {
    requireNonNull(event, "event");

    return userNotificationConverter.convert(event.getNotification());
  }
}
