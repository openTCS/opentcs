// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.converter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.GetTransportOrderResponseTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.getevents.OrderStatusMessage;

/**
 * Tests for {@link TransportOrderConverter}.
 */
public class TransportOrderConverterTest {
  private final TransportOrderConverter transportOrderConverter = new TransportOrderConverter();

  @Test
  void checkToOrderStatusMessage() {
    TransportOrder t1
        = new TransportOrder(
            "T1", List.of(
                new DriveOrder(new DriveOrder.Destination(new Point("P1").getReference()))
            )
        )
            .withDispensable(true)
            .withPeripheralReservationToken("some token")
            .withWrappingSequence(new OrderSequence("Os1").getReference())
            .withType("some type")
            .withIntendedVehicle(new Vehicle("V1").getReference())
            .withProcessingVehicle(new Vehicle("V2").getReference())
            .withState(TransportOrder.State.DISPATCHABLE)
            .withProperties(Map.of("some-key", "some-value"));

    OrderStatusMessage statusMessage = transportOrderConverter.toOrderStatusMessage(
        t1, 555, Instant.EPOCH
    );

    assertThat(statusMessage.getOrderName(), is("T1"));
    assertThat(statusMessage.getDestinations().size(), is(1));
    assertThat(statusMessage.getDestinations().getFirst().getLocationName(), is("P1"));
    assertThat(statusMessage.isDispensable(), is(true));
    assertThat(statusMessage.getPeripheralReservationToken(), is("some token"));
    assertThat(statusMessage.getWrappingSequence(), is("Os1"));
    assertThat(statusMessage.getOrderType(), is("some type"));
    assertThat(statusMessage.getIntendedVehicle(), is("V1"));
    assertThat(statusMessage.getProcessingVehicleName(), is("V2"));
    assertThat(statusMessage.getOrderState(), is(OrderStatusMessage.OrderState.DISPATCHABLE));
    assertThat(statusMessage.getProperties().size(), is(1));
    assertThat(statusMessage.getProperties().getFirst().getKey(), is("some-key"));
    assertThat(statusMessage.getProperties().getFirst().getValue(), is("some-value"));
  }

  @Test
  void checkToGetTransportOrderResponse() {
    TransportOrder t1
        = new TransportOrder(
            "T1", List.of(
                new DriveOrder(new DriveOrder.Destination(new Point("P1").getReference()))
            )
        )
            .withDispensable(true)
            .withPeripheralReservationToken("some token")
            .withWrappingSequence(new OrderSequence("Os1").getReference())
            .withType("some type")
            .withIntendedVehicle(new Vehicle("V1").getReference())
            .withProcessingVehicle(new Vehicle("V2").getReference())
            .withState(TransportOrder.State.DISPATCHABLE)
            .withProperties(Map.of("some-key", "some-value"));

    GetTransportOrderResponseTO response = transportOrderConverter.toGetTransportOrderResponse(t1);

    assertThat(response.getName(), is("T1"));
    assertThat(response.getDestinations().size(), is(1));
    assertThat(response.getDestinations().getFirst().getLocationName(), is("P1"));
    assertThat(response.isDispensable(), is(true));
    assertThat(response.getPeripheralReservationToken(), is("some token"));
    assertThat(response.getWrappingSequence(), is("Os1"));
    assertThat(response.getType(), is("some type"));
    assertThat(response.getIntendedVehicle(), is("V1"));
    assertThat(response.getProcessingVehicle(), is("V2"));
    assertThat(response.getState(), is(TransportOrder.State.DISPATCHABLE));
    assertThat(response.getProperties().size(), is(1));
    assertThat(response.getProperties().getFirst().getKey(), is("some-key"));
    assertThat(response.getProperties().getFirst().getValue(), is("some-value"));
  }
}
