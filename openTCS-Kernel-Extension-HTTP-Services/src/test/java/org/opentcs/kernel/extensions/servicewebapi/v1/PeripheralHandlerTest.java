/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1;

import java.util.List;
import java.util.concurrent.Executors;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import org.opentcs.common.peripherals.NullPeripheralCommAdapterDescription;
import org.opentcs.components.kernel.services.PeripheralService;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.drivers.peripherals.PeripheralCommAdapterDescription;
import org.opentcs.drivers.peripherals.management.PeripheralAttachmentInformation;
import org.opentcs.kernel.extensions.servicewebapi.KernelExecutorWrapper;

/**
 * Unit tests for {@link PeripheralJobHandler}.
 */
class PeripheralHandlerTest {

  private PeripheralService peripheralService;
  private KernelExecutorWrapper executorWrapper;

  private PeripheralHandler handler;

  private Location location;
  private PeripheralCommAdapterDescription adapterDescriptionNull;
  private PeripheralCommAdapterDescription adapterDescriptionMock;
  private PeripheralAttachmentInformation attachmentInfo;

  @BeforeEach
  void setUp() {
    peripheralService = mock();
    executorWrapper = new KernelExecutorWrapper(Executors.newSingleThreadExecutor());

    handler = new PeripheralHandler(peripheralService, executorWrapper);

    location = new Location("some-location", new LocationType("some-location-type").getReference());
    adapterDescriptionNull = new NullPeripheralCommAdapterDescription();
    adapterDescriptionMock = new MockPeripheralCommAdapterDescription();
    attachmentInfo = new PeripheralAttachmentInformation(
        location.getReference(),
        List.of(
            adapterDescriptionNull,
            adapterDescriptionMock
        ),
        adapterDescriptionNull
    );

    given(peripheralService.fetchObject(Location.class, "some-location"))
        .willReturn(location);
    given(peripheralService.fetchAttachmentInformation(location.getReference()))
        .willReturn(attachmentInfo);
  }

  @Test
  void attachNullPeripheralAdapter() {
    handler.putPeripheralCommAdapter(
        "some-location",
        NullPeripheralCommAdapterDescription.class.getName()
    );

    then(peripheralService)
        .should()
        .attachCommAdapter(location.getReference(), adapterDescriptionNull);
  }

  @Test
  void attachMockPeripheralAdapter() {
    handler.putPeripheralCommAdapter(
        "some-location",
        MockPeripheralCommAdapterDescription.class.getName()
    );

    then(peripheralService)
        .should()
        .attachCommAdapter(location.getReference(), adapterDescriptionMock);
  }

  @Test
  void throwOnAttachAdapterForUnknownLocation() {
    assertThatExceptionOfType(ObjectUnknownException.class)
        .isThrownBy(
            () -> handler.putPeripheralCommAdapter(
                "some-unknown-location",
                NullPeripheralCommAdapterDescription.class.getName()
            )
        );
  }

  @Test
  void throwOnAttachUnknownAdapter() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(
            () -> handler.putPeripheralCommAdapter(
                "some-location",
                "some-unknown-adapter-class-name"
            )
        );
  }

  @Test
  void enableCommAdapter() {
    handler.putPeripheralCommAdapterEnabled("some-location", "true");

    then(peripheralService).should().enableCommAdapter(location.getReference());
  }

  @ParameterizedTest
  @ValueSource(strings = {"false", "flase", "some-value-that-is-not-true"})
  void disableCommAdapterOnAnyNontrueValue(String value) {
    handler.putPeripheralCommAdapterEnabled("some-location", value);

    then(peripheralService).should().disableCommAdapter(location.getReference());
  }

  @ParameterizedTest
  @ValueSource(strings = {"true ", "false"})
  void throwOnEnableUnknownLocation(String value) {
    assertThatExceptionOfType(ObjectUnknownException.class)
        .isThrownBy(() -> handler.putPeripheralCommAdapterEnabled("some-unknown-location", value));
  }

  @Test
  void fetchAttachmentInformation() {
    assertThat(handler.getPeripheralCommAdapterAttachmentInformation("some-location"))
        .isSameAs(attachmentInfo);
  }

  @Test
  void throwOnFetchInfoForUnknownLocation() {
    assertThatExceptionOfType(ObjectUnknownException.class)
        .isThrownBy(
            () -> handler.getPeripheralCommAdapterAttachmentInformation("some-unknown-location")
        );
  }

  static class MockPeripheralCommAdapterDescription
      extends PeripheralCommAdapterDescription {

    @Override
    public String getDescription() {
      return "some-peripheral-comm-adapter";
    }
  }
}
