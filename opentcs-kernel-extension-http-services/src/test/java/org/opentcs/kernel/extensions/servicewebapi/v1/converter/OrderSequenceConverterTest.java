// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.converter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.GetOrderSequenceResponseTO;

/**
 * Tests for {@link OrderSequenceConverter}.
 */
public class OrderSequenceConverterTest {
  private final OrderSequenceConverter converter = new OrderSequenceConverter();

  @Test
  public void checkToGetOrderSequenceResponseTO() {
    OrderSequence sequence = new OrderSequence("OS1")
        .withOrder(new TransportOrder("T1", List.of()).getReference())
        .withComplete(true)
        .withFailureFatal(true)
        .withFinished(false)
        .withFinishedIndex(0)
        .withCreationTime(Instant.EPOCH)
        .withFinishedTime(Instant.EPOCH)
        .withType("some-type")
        .withIntendedVehicle(new Vehicle("V1").getReference())
        .withProcessingVehicle(new Vehicle("V2").getReference())
        .withProperties(Map.of("some-key", "some-value"));

    GetOrderSequenceResponseTO response = converter.toGetOrderSequenceResponseTO(sequence);

    assertThat(response.getName(), is("OS1"));
    assertThat(response.isComplete(), is(true));
    assertThat(response.isFailureFatal(), is(true));
    assertThat(response.isFinished(), is(false));
    assertThat(response.getFinishedIndex(), is(0));
    assertThat(response.getCreationTime(), is(Instant.EPOCH));
    assertThat(response.getFinishedTime(), is(Instant.EPOCH));
    assertThat(response.getType(), is("some-type"));
    assertThat(response.getIntendedVehicle(), is("V1"));
    assertThat(response.getOrders().size(), is(1));
    assertThat(response.getOrders().getFirst(), is("T1"));
    assertThat(response.getProperties().size(), is(1));
    assertThat(response.getProperties().getFirst().getKey(), is("some-key"));
    assertThat(response.getProperties().getFirst().getValue(), is("some-value"));
  }
}
