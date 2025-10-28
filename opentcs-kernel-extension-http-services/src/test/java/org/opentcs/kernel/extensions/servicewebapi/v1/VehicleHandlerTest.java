// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.stream.Stream;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.opentcs.components.kernel.services.InternalVehicleService;
import org.opentcs.components.kernel.services.RouterService;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.model.AcceptableOrderType;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.VehicleCommAdapterDescription;
import org.opentcs.drivers.vehicle.management.VehicleAttachmentInformation;
import org.opentcs.kernel.extensions.servicewebapi.KernelExecutorWrapper;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.GetVehicleResponseTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.PostVehicleRoutesRequestTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.PutVehicleAcceptableOrderTypesTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.AcceptableOrderTypeTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.converter.VehicleConverter;

/**
 * Unit tests for {@link VehicleHandler}.
 */
class VehicleHandlerTest {

  private InternalVehicleService vehicleService;
  private RouterService routerService;
  private VehicleConverter vehicleConverter;
  private KernelExecutorWrapper executorWrapper;

  private VehicleHandler handler;

  private Vehicle vehicle;
  private VehicleCommAdapterDescription adapterDescriptionMock;
  private VehicleAttachmentInformation attachmentInfo;

  @BeforeEach
  void setUp() {
    vehicleService = mock();
    routerService = mock();
    vehicleConverter = mock();
    executorWrapper = new KernelExecutorWrapper(Executors.newSingleThreadExecutor());

    handler = new VehicleHandler(vehicleService, routerService, executorWrapper, vehicleConverter);

    vehicle = new Vehicle("some-vehicle");
    adapterDescriptionMock = new MockVehicleCommAdapterDescription();

    attachmentInfo = new VehicleAttachmentInformation(
        vehicle.getReference(),
        List.of(adapterDescriptionMock),
        adapterDescriptionMock
    );

    given(vehicleService.fetch(Vehicle.class, "some-vehicle"))
        .willReturn(Optional.of(vehicle));
    given(vehicleService.fetchAttachmentInformation(vehicle.getReference()))
        .willReturn(attachmentInfo);
  }

  @Test
  void attachMockVehicleAdapter() {
    // Act
    handler.putVehicleCommAdapter(
        "some-vehicle",
        MockVehicleCommAdapterDescription.class.getName()
    );

    // Assert
    then(vehicleService)
        .should()
        .attachCommAdapter(vehicle.getReference(), adapterDescriptionMock);
  }

  @Test
  void throwOnAttachAdapterForUnknownVehicle() {
    assertThatExceptionOfType(ObjectUnknownException.class)
        .isThrownBy(
            () -> handler.putVehicleCommAdapter(
                "some-unknown-vehicle",
                MockVehicleCommAdapterDescription.class.getName()
            )
        );
  }

  @Test
  void throwOnAttachUnknownAdapter() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(
            () -> handler.putVehicleCommAdapter(
                "some-vehicle",
                "some-unknown-adapter-class-name"
            )
        );
  }

  @Test
  void enableCommAdapter() {
    handler.putVehicleCommAdapterEnabled("some-vehicle", "true");

    then(vehicleService).should().enableCommAdapter(vehicle.getReference());
  }

  @ParameterizedTest
  @ValueSource(strings = {"false", "flase", "some-value-that-is-not-true"})
  void disableCommAdapterOnAnyNontrueValue(String value) {
    handler.putVehicleCommAdapterEnabled("some-vehicle", value);

    then(vehicleService).should().disableCommAdapter(vehicle.getReference());
  }

  @ParameterizedTest
  @ValueSource(strings = {"true ", "false"})
  void throwOnEnableUnknownVehicle(String value) {
    assertThatExceptionOfType(ObjectUnknownException.class)
        .isThrownBy(() -> handler.putVehicleCommAdapterEnabled("some-unknown-vehicle", value));
  }

  @Test
  void fetchAttachmentInformation() {
    assertThat(handler.getVehicleCommAdapterAttachmentInformation("some-vehicle"))
        .isSameAs(attachmentInfo);
  }

  @Test
  void throwOnFetchInfoForUnknownLocation() {
    assertThatExceptionOfType(ObjectUnknownException.class)
        .isThrownBy(
            () -> handler.getVehicleCommAdapterAttachmentInformation("some-unknown-vehicle")
        );
  }

  @ParameterizedTest
  @EnumSource(Vehicle.ProcState.class)
  void retrieveVehiclesByProcState(Vehicle.ProcState procState) {
    // Arrange
    Vehicle vehicleWithProcState = vehicle.withProcState(procState);
    given(vehicleService.stream(Vehicle.class))
        .willReturn(Stream.of(vehicleWithProcState));

    // Act & Assert
    List<GetVehicleResponseTO> result = handler.getVehiclesState(procState.name());
    MatcherAssert.assertThat(result, hasSize(1));
    then(vehicleService).should().stream(Vehicle.class);
  }

  @Test
  void throwOnRetrieveVehiclesForUnknownProcState() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> handler.getVehiclesState("some-unknown-proc-state"));
  }

  @Test
  void retrieveVehicleByName() {
    when(vehicleConverter.toGetVehicleResponseTO(any(Vehicle.class))).thenReturn(
        new GetVehicleResponseTO()
    );

    // Act & Assert: happy path
    GetVehicleResponseTO result = handler.getVehicleStateByName("some-vehicle");
    MatcherAssert.assertThat(result, is(notNullValue()));
    then(vehicleService).should().fetch(Vehicle.class, "some-vehicle");

    // Act & Assert: nonexistent vehicle
    assertThatExceptionOfType(ObjectUnknownException.class)
        .isThrownBy(() -> handler.getVehicleStateByName("some-other-vehicle"));
  }

  @ParameterizedTest
  @EnumSource(Vehicle.IntegrationLevel.class)
  void updateVehicleIntegrationLevel(Vehicle.IntegrationLevel integrationLevel) {
    handler.putVehicleIntegrationLevel("some-vehicle", integrationLevel.name());
    then(vehicleService)
        .should()
        .updateVehicleIntegrationLevel(vehicle.getReference(), integrationLevel);
  }

  @Test
  void throwOnUpdateIntegrationLevelForUnknownVehicleOrIntegrationLevel() {
    // Act & Assert: nonexistent vehicle
    assertThatExceptionOfType(ObjectUnknownException.class)
        .isThrownBy(
            () -> handler.putVehicleIntegrationLevel(
                "some-unknown-vehicle",
                "TO_BE_UTILIZED"
            )
        );

    // Act & Assert: unknown integration level
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(
            () -> handler.putVehicleIntegrationLevel(
                "some-vehicle",
                "some-unknown-integration-level"
            )
        );
  }

  @Test
  void pauseVehicle() {
    handler.putVehiclePaused("some-vehicle", "true");

    then(vehicleService).should().updateVehiclePaused(vehicle.getReference(), true);
  }

  @ParameterizedTest
  @ValueSource(strings = {"false", "flase", "some-value-that-is-not-true"})
  void unpauseVehicleOnAnyNontrueValue(String value) {
    handler.putVehiclePaused("some-vehicle", value);

    then(vehicleService).should().updateVehiclePaused(vehicle.getReference(), false);
  }

  @ParameterizedTest
  @ValueSource(strings = {"true ", "false"})
  void throwOnPauseUnknownVehicle(String value) {
    assertThatExceptionOfType(ObjectUnknownException.class)
        .isThrownBy(() -> handler.putVehiclePaused("some-unknown-vehicle", value));
  }

  @Test
  void setVehicleEnvelopeKey() {
    handler.putVehicleEnvelopeKey("some-vehicle", "some-key");

    then(vehicleService).should().updateVehicleEnvelopeKey(vehicle.getReference(), "some-key");
  }

  @Test
  void nullVehicleEnvelopeKey() {
    handler.putVehicleEnvelopeKey("some-vehicle", null);

    then(vehicleService).should().updateVehicleEnvelopeKey(vehicle.getReference(), null);
  }

  @Test
  void throwOnSetEnvelopeUnknownVehicle() {
    assertThatExceptionOfType(ObjectUnknownException.class)
        .isThrownBy(() -> handler.putVehicleEnvelopeKey("some-unknown-vehicle", "some-key"));
  }

  @Test
  void updateVehicleAcceptableOrderTypes() {
    // Act
    handler.putVehicleAcceptableOrderTypes(
        "some-vehicle",
        new PutVehicleAcceptableOrderTypesTO(
            List.of(
                new AcceptableOrderTypeTO("some-order-type", 0),
                new AcceptableOrderTypeTO("some-other-order-type", 1)
            )
        )
    );

    // Assert
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Set<AcceptableOrderType>> captor = ArgumentCaptor.forClass(Set.class);
    then(vehicleService)
        .should()
        .updateVehicleAcceptableOrderTypes(eq(vehicle.getReference()), captor.capture());
    assertThat(captor.getValue())
        .hasSize(2)
        .contains(
            new AcceptableOrderType("some-order-type", 0),
            new AcceptableOrderType("some-other-order-type", 1)
        );
  }

  @Test
  void throwOnUpdateAcceptableOrderTypesForUnknownVehicle() {
    assertThatExceptionOfType(ObjectUnknownException.class)
        .isThrownBy(
            () -> handler.putVehicleAcceptableOrderTypes(
                "some-unknown-vehicle",
                new PutVehicleAcceptableOrderTypesTO(List.of())
            )
        );
  }

  @Test
  void retrieveVehicleRoutesForCurrentPosition() {
    // Arrange
    Point vehiclePosition = new Point("some-point");
    Point destinationPoint1 = new Point("some-destination-point");
    Point destinationPoint2 = new Point("some-destination-point-2");
    Vehicle vehicleWithPosition = vehicle.withCurrentPosition(vehiclePosition.getReference());
    given(vehicleService.fetch(Point.class, "some-point"))
        .willReturn(Optional.of(vehiclePosition));
    given(vehicleService.fetch(Point.class, "some-destination-point"))
        .willReturn(Optional.of(destinationPoint1));
    given(vehicleService.fetch(Point.class, "some-destination-point-2"))
        .willReturn(Optional.of(destinationPoint2));
    given(vehicleService.fetch(Vehicle.class, "some-vehicle"))
        .willReturn(Optional.of(vehicleWithPosition));

    // Act & Assert: happy path
    handler.getVehicleRoutes(
        "some-vehicle",
        2,
        new PostVehicleRoutesRequestTO(
            List.of("some-destination-point", "some-destination-point-2")
        )
    );

    then(routerService)
        .should()
        .computeRoutes(
            vehicle.getReference(),
            vehiclePosition.getReference(),
            Set.of(destinationPoint1.getReference(), destinationPoint2.getReference()),
            Set.of(),
            2
        );

    // Act & Assert: nonexistent vehicle
    assertThatExceptionOfType(ObjectUnknownException.class)
        .isThrownBy(
            () -> handler.getVehicleRoutes(
                "some-unknown-vehicle",
                1,
                new PostVehicleRoutesRequestTO(List.of("some-destination-point"))
            )
        );

    // Act & Assert: nonexistent destination point
    assertThatExceptionOfType(ObjectUnknownException.class)
        .isThrownBy(
            () -> handler.getVehicleRoutes(
                "some-vehicle",
                1,
                new PostVehicleRoutesRequestTO(List.of("some-unknown-destination-point"))
            )
        );

    // Act & Assert: unknown vehicle position
    given(vehicleService.fetch(Vehicle.class, "some-vehicle"))
        .willReturn(Optional.of(vehicle));
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(
            () -> handler.getVehicleRoutes(
                "some-vehicle",
                1,
                new PostVehicleRoutesRequestTO(List.of("some-destination-point"))
            )
        );
  }

  @Test
  void retrieveVehicleRoutesForPositionProvidedInRequest() {
    // Arrange
    Point sourcePoint = new Point("some-source-point");
    Point destinationPoint1 = new Point("some-destination-point");
    Point destinationPoint2 = new Point("some-destination-point-2");
    given(vehicleService.fetch(Point.class, "some-source-point"))
        .willReturn(Optional.of(sourcePoint));
    given(vehicleService.fetch(Point.class, "some-destination-point"))
        .willReturn(Optional.of(destinationPoint1));
    given(vehicleService.fetch(Point.class, "some-destination-point-2"))
        .willReturn(Optional.of(destinationPoint2));

    // Act & Assert: happy path
    handler.getVehicleRoutes(
        "some-vehicle",
        1,
        new PostVehicleRoutesRequestTO(
            List.of("some-destination-point", "some-destination-point-2")
        ).setSourcePoint("some-source-point")
    );

    then(routerService)
        .should()
        .computeRoutes(
            vehicle.getReference(),
            sourcePoint.getReference(),
            Set.of(destinationPoint1.getReference(), destinationPoint2.getReference()),
            Set.of(),
            1
        );

    // Act & Assert: nonexistent source point
    assertThatExceptionOfType(ObjectUnknownException.class)
        .isThrownBy(
            () -> handler.getVehicleRoutes(
                "some-vehicle",
                1,
                new PostVehicleRoutesRequestTO(List.of("some-destination-point"))
                    .setSourcePoint("some-unknown-source-point")
            )
        );
  }

  @Test
  void retrieveVehicleRoutesForResourcesToAvoid() {
    // Arrange
    Point sourcePoint = new Point("some-source-point");
    Point destinationPoint1 = new Point("some-destination-point");
    Point destinationPoint2 = new Point("some-destination-point-2");
    Point pointToAvoid = new Point("some-point-to-avoid");
    Path pathToAvoid = new Path(
        "some-path",
        sourcePoint.getReference(),
        destinationPoint1.getReference()
    );
    Location locationToAvoid = new Location(
        "some-location",
        new LocationType("some-locType").getReference()
    );
    given(vehicleService.fetch(Point.class, "some-source-point"))
        .willReturn(Optional.of(sourcePoint));
    given(vehicleService.fetch(Point.class, "some-destination-point"))
        .willReturn(Optional.of(destinationPoint1));
    given(vehicleService.fetch(Point.class, "some-destination-point-2"))
        .willReturn(Optional.of(destinationPoint2));
    given(vehicleService.fetch(Point.class, "some-point-to-avoid"))
        .willReturn(Optional.of(pointToAvoid));
    given(vehicleService.fetch(Path.class, "some-path"))
        .willReturn(Optional.of(pathToAvoid));
    given(vehicleService.fetch(Location.class, "some-location"))
        .willReturn(Optional.of(locationToAvoid));

    // Act & Assert: happy path
    handler.getVehicleRoutes(
        "some-vehicle",
        1,
        new PostVehicleRoutesRequestTO(
            List.of("some-destination-point", "some-destination-point-2")
        )
            .setSourcePoint("some-source-point")
            .setResourcesToAvoid(
                List.of("some-point-to-avoid", "some-path", "some-location")
            )
    );

    then(routerService)
        .should()
        .computeRoutes(
            vehicle.getReference(),
            sourcePoint.getReference(),
            Set.of(destinationPoint1.getReference(), destinationPoint2.getReference()),
            Set.of(
                pointToAvoid.getReference(),
                pathToAvoid.getReference(),
                locationToAvoid.getReference()
            ),
            1
        );

    // Act & Assert: nonexistent resource to avoid
    assertThatExceptionOfType(ObjectUnknownException.class)
        .isThrownBy(
            () -> handler.getVehicleRoutes(
                "some-vehicle",
                1,
                new PostVehicleRoutesRequestTO(List.of("some-destination-point"))
                    .setSourcePoint("some-source-point")
                    .setResourcesToAvoid(List.of("some-unknown-resource"))
            )
        );
  }

  static class MockVehicleCommAdapterDescription
      extends
        VehicleCommAdapterDescription {

    @Override
    public String getDescription() {
      return "some-vehicle-comm-adapter";
    }

    @Override
    public boolean isSimVehicleCommAdapter() {
      return false;
    }
  }
}
