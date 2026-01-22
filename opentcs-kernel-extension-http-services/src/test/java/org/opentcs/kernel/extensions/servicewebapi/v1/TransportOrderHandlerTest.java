// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.from;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.theInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.opentcs.access.to.order.DestinationCreationTO;
import org.opentcs.access.to.order.OrderSequenceCreationTO;
import org.opentcs.access.to.order.TransportOrderCreationTO;
import org.opentcs.components.kernel.services.InternalTransportOrderService;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.kernel.extensions.servicewebapi.KernelExecutorWrapper;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.GetOrderSequenceResponseTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.GetTransportOrderResponseTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.PostOrderSequenceRequestTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.PostTransportOrderRequestTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.posttransportorder.Destination;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.Property;
import org.opentcs.kernel.extensions.servicewebapi.v1.converter.OrderSequenceConverter;
import org.opentcs.kernel.extensions.servicewebapi.v1.converter.TransportOrderConverter;

/**
 * Unit tests for {@link TransportOrderHandler}.
 */
class TransportOrderHandlerTest {

  private InternalTransportOrderService orderService;
  private KernelExecutorWrapper executorWrapper;
  private TransportOrderHandler handler;
  private OrderSequenceConverter orderSequenceConverter;
  private TransportOrderConverter transportOrderConverter;

  @BeforeEach
  void setUp() {
    orderService = mock();
    executorWrapper = new KernelExecutorWrapper(Executors.newSingleThreadExecutor());
    orderSequenceConverter = new OrderSequenceConverter();
    transportOrderConverter = new TransportOrderConverter();

    handler = new TransportOrderHandler(
        orderService, executorWrapper, orderSequenceConverter, transportOrderConverter
    );
  }

  @Test
  void createTransportOrder() {
    // Arrange
    TransportOrder transportOrder = new TransportOrder("some-order", List.of());
    given(orderService.createTransportOrder(any(TransportOrderCreationTO.class)))
        .willReturn(transportOrder);

    // Act
    TransportOrder result = handler.createOrder(
        "some-order",
        new PostTransportOrderRequestTO(
            false,
            false,
            Instant.MAX,
            null,
            null,
            null,
            null,
            List.of(
                new Destination(
                    "some-location",
                    "some-operation",
                    List.of(
                        new Property("some-dest-key", "some-dest-value")
                    )
                )
            ),
            List.of(
                new Property("some-key", "some-value")
            ),
            null
        )
    );

    // Assert
    assertThat(result, is(theInstance(transportOrder)));

    ArgumentCaptor<TransportOrderCreationTO> captor
        = ArgumentCaptor.forClass(TransportOrderCreationTO.class);
    then(orderService).should().createTransportOrder(captor.capture());
    assertThat(captor.getValue())
        .returns("some-order", from(TransportOrderCreationTO::getName))
        .returns(false, from(TransportOrderCreationTO::hasIncompleteName))
        .returns(false, from(TransportOrderCreationTO::isDispensable))
        .returns(Instant.MAX, from(TransportOrderCreationTO::getDeadline))
        .returns(null, from(TransportOrderCreationTO::getWrappingSequence))
        .returns(null, from(TransportOrderCreationTO::getPeripheralReservationToken))
        .returns(Set.of(), from(TransportOrderCreationTO::getDependencyNames))
        .returns(Map.of("some-key", "some-value"), from(TransportOrderCreationTO::getProperties));
    assertThat(captor.getValue().getDestinations()).hasSize(1);
    assertThat(captor.getValue().getDestinations().get(0))
        .returns("some-location", from(DestinationCreationTO::getDestLocationName))
        .returns("some-operation", from(DestinationCreationTO::getDestOperation))
        .returns(
            Map.of("some-dest-key", "some-dest-value"),
            from(DestinationCreationTO::getProperties)
        );
  }

  @Test
  void setTransportOrderIntendedVehicle() {
    // Arrange
    TransportOrder transportOrder = new TransportOrder("some-order", List.of());
    Vehicle vehicle = new Vehicle("some-vehicle");

    given(orderService.fetch(TransportOrder.class, "some-order"))
        .willReturn(Optional.of(transportOrder));
    given(orderService.fetch(Vehicle.class, "some-vehicle"))
        .willReturn(Optional.of(vehicle));

    // Act & Assert: set to vehicle
    handler.updateTransportOrderIntendedVehicle("some-order", "some-vehicle");
    then(orderService).should().updateTransportOrderIntendedVehicle(
        transportOrder.getReference(),
        vehicle.getReference()
    );

    // Act & Assert: set to null
    handler.updateTransportOrderIntendedVehicle("some-order", null);
    then(orderService).should().updateTransportOrderIntendedVehicle(
        transportOrder.getReference(),
        null
    );

    // Act & Assert: nonexistent transport order
    assertThatExceptionOfType(ObjectUnknownException.class)
        .isThrownBy(() -> handler.updateTransportOrderIntendedVehicle("some-unknown-order", null));

    // Act & Assert: nonexistent vehicle
    assertThatExceptionOfType(ObjectUnknownException.class)
        .isThrownBy(
            () -> handler.updateTransportOrderIntendedVehicle("some-order", "some-unknown-vehicle")
        );
  }

  @Test
  void retrieveTransportOrdersUnfiltered() {
    // Arrange
    TransportOrder transportOrder1 = new TransportOrder("some-order", List.of());
    TransportOrder transportOrder2 = new TransportOrder("some-order-2", List.of());

    given(orderService.stream(TransportOrder.class))
        .willReturn(Set.of(transportOrder1, transportOrder2).stream());

    // Act
    List<GetTransportOrderResponseTO> result = handler.getTransportOrders(null);

    // Assert
    assertThat(result, hasSize(2));
    then(orderService).should().stream(TransportOrder.class);
  }

  @Test
  void retrieveTransportOrdersFilteredByIntendedVehicle() {
    // Arrange
    Vehicle vehicle = new Vehicle("some-vehicle");
    TransportOrder transportOrder1 = new TransportOrder("some-order", List.of())
        .withIntendedVehicle(vehicle.getReference());
    TransportOrder transportOrder2 = new TransportOrder("some-order-2", List.of())
        .withIntendedVehicle(vehicle.getReference());

    given(orderService.fetch(Vehicle.class, "some-vehicle"))
        .willReturn(Optional.of(vehicle));
    given(orderService.stream(TransportOrder.class))
        .willReturn(Stream.of(transportOrder1, transportOrder2));

    // Act & Assert: happy path
    List<GetTransportOrderResponseTO> result = handler.getTransportOrders("some-vehicle");
    assertThat(result, hasSize(2));
    then(orderService).should().stream(TransportOrder.class);

    // Act & Assert: nonexistent vehicle
    assertThatExceptionOfType(ObjectUnknownException.class)
        .isThrownBy(() -> handler.getTransportOrders("some-other-vehicle"));
  }

  @Test
  void retrieveTransportOrderByName() {
    // Arrange
    TransportOrder transportOrder = new TransportOrder("some-order", List.of());

    given(orderService.fetch(TransportOrder.class, "some-order"))
        .willReturn(Optional.of(transportOrder));

    // Act & Assert: happy path
    GetTransportOrderResponseTO result = handler.getTransportOrderByName("some-order");
    assertThat(result, is(notNullValue()));
    then(orderService).should().fetch(TransportOrder.class, "some-order");

    // Act & Assert: nonexistent order
    assertThatExceptionOfType(ObjectUnknownException.class)
        .isThrownBy(() -> handler.getTransportOrderByName("some-other-order"));
  }

  @Test
  void createOrderSequence() {
    // Arrange
    OrderSequence orderSequence = new OrderSequence("some-sequence");
    given(orderService.createOrderSequence(any(OrderSequenceCreationTO.class)))
        .willReturn(orderSequence);

    // Act
    OrderSequence result = handler.createOrderSequence(
        "some-sequence",
        new PostOrderSequenceRequestTO()
            .setIncompleteName(false)
            .setFailureFatal(false)
            .setIntendedVehicle(null)
            .setOrderTypes(List.of("some-type"))
            .setProperties(
                List.of(
                    new Property("some-key", "some-value")
                )
            )
    );

    // Assert
    assertThat(result, is(theInstance(orderSequence)));

    ArgumentCaptor<OrderSequenceCreationTO> captor
        = ArgumentCaptor.forClass(OrderSequenceCreationTO.class);
    then(orderService).should().createOrderSequence(captor.capture());
    assertThat(captor.getValue())
        .returns(false, from(OrderSequenceCreationTO::hasIncompleteName))
        .returns(null, from(OrderSequenceCreationTO::getIntendedVehicleName))
        .returns(false, from(OrderSequenceCreationTO::isFailureFatal))
        .returns(Set.of("some-type"), from(OrderSequenceCreationTO::getOrderTypes))
        .returns(
            Map.of("some-key", "some-value"),
            from(OrderSequenceCreationTO::getProperties)
        );
  }

  @SuppressWarnings("deprecation")
  @Test
  void createOrderSequenceWithType() {
    // Arrange
    OrderSequence orderSequence = new OrderSequence("some-sequence");
    given(orderService.createOrderSequence(any(OrderSequenceCreationTO.class)))
        .willReturn(orderSequence);

    // Act
    OrderSequence result = handler.createOrderSequence(
        "some-sequence",
        new PostOrderSequenceRequestTO()
            .setIncompleteName(false)
            .setFailureFatal(false)
            .setIntendedVehicle(null)
            .setType("some-type")
            .setProperties(
                List.of(
                    new Property("some-key", "some-value")
                )
            )
    );

    // Assert
    assertThat(result, is(theInstance(orderSequence)));

    ArgumentCaptor<OrderSequenceCreationTO> captor
        = ArgumentCaptor.forClass(OrderSequenceCreationTO.class);
    then(orderService).should().createOrderSequence(captor.capture());
    assertThat(captor.getValue())
        .returns(false, from(OrderSequenceCreationTO::hasIncompleteName))
        .returns(null, from(OrderSequenceCreationTO::getIntendedVehicleName))
        .returns(false, from(OrderSequenceCreationTO::isFailureFatal))
        .returns("some-type", from(OrderSequenceCreationTO::getType))
        .returns(Set.of(), from(OrderSequenceCreationTO::getOrderTypes))
        .returns(
            Map.of("some-key", "some-value"),
            from(OrderSequenceCreationTO::getProperties)
        );
  }

  @Test
  void createOrderSequenceWithTypeAndOrderTypesFails() {
    // Arrange
    OrderSequence orderSequence = new OrderSequence("some-sequence");
    given(orderService.createOrderSequence(any(OrderSequenceCreationTO.class)))
        .willReturn(orderSequence);

    // Assert
    assertThrows(
        IllegalArgumentException.class, () -> handler.createOrderSequence(
            "some-sequence",
            new PostOrderSequenceRequestTO()
                .setIncompleteName(false)
                .setFailureFatal(false)
                .setIntendedVehicle(null)
                .setType("some-type")
                .setOrderTypes(List.of("some-type"))
                .setProperties(
                    List.of(
                        new Property("some-key", "some-value")
                    )
                )
        )
    );
  }

  @Test
  void setOrderSequenceComplete() {
    // Arrange
    OrderSequence orderSequence = new OrderSequence("some-sequence");

    given(orderService.fetch(OrderSequence.class, "some-sequence"))
        .willReturn(Optional.of(orderSequence));

    // Act & Assert: happy path
    handler.putOrderSequenceComplete("some-sequence");
    then(orderService).should().markOrderSequenceComplete(orderSequence.getReference());

    // Act & Assert: nonexistent order sequence
    assertThatExceptionOfType(ObjectUnknownException.class)
        .isThrownBy(() -> handler.putOrderSequenceComplete("some-other-sequence"));
  }

  @Test
  void retrieveOrderSequencesUnfiltered() {
    // Arrange
    OrderSequence sequence1 = new OrderSequence("some-sequence");
    OrderSequence sequence2 = new OrderSequence("some-sequence-2");

    given(orderService.stream(OrderSequence.class))
        .willReturn(Stream.of(sequence1, sequence2));

    // Act
    List<GetOrderSequenceResponseTO> result = handler.getOrderSequences(null);

    // Assert
    assertThat(result, hasSize(2));
    then(orderService).should().stream(OrderSequence.class);
  }

  @Test
  void retrieveOrderSequencesFilteredByIntendedVehicle() {
    // Arrange
    Vehicle vehicle = new Vehicle("some-vehicle");
    OrderSequence sequence1 = new OrderSequence("some-sequence")
        .withIntendedVehicle(vehicle.getReference());
    OrderSequence sequence2 = new OrderSequence("some-sequence-2")
        .withIntendedVehicle(vehicle.getReference());

    given(orderService.fetch(Vehicle.class, "some-vehicle"))
        .willReturn(Optional.of(vehicle));
    given(orderService.stream(OrderSequence.class))
        .willReturn(Stream.of(sequence1, sequence2));

    // Act & Assert: happy path
    List<GetOrderSequenceResponseTO> result = handler.getOrderSequences("some-vehicle");
    assertThat(result, hasSize(2));
    then(orderService).should().stream(OrderSequence.class);

    // Act & Assert: nonexistent vehicle
    assertThatExceptionOfType(ObjectUnknownException.class)
        .isThrownBy(() -> handler.getOrderSequences("some-other-vehicle"));
  }

  @Test
  void retrieveOrderSequenceByName() {
    // Arrange
    OrderSequence orderSequence = new OrderSequence("some-sequence");

    given(orderService.fetch(OrderSequence.class, "some-sequence"))
        .willReturn(Optional.of(orderSequence));

    // Act & Assert: happy path
    GetOrderSequenceResponseTO result = handler.getOrderSequenceByName("some-sequence");
    assertThat(result, is(notNullValue()));
    then(orderService).should().fetch(OrderSequence.class, "some-sequence");

    // Act & Assert: nonexistent order
    assertThatExceptionOfType(ObjectUnknownException.class)
        .isThrownBy(() -> handler.getOrderSequenceByName("some-other-sequence"));
  }

}
