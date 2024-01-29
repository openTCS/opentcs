/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.documentation.developers_guide;

import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import org.opentcs.components.kernel.services.DispatcherService;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.TransportOrder;

/**
 * Test for the developers asciidoc documentation to show how a tranport order is withdrawn.
 * This test has no meaning and it just exists for the documentation to refer to and to guarantee
 * an example that compiles.
 */
class WithdrawTransportOrderTest {

  private DispatcherService dispService;

  @BeforeEach
  void setUp() {
    dispService = mock(DispatcherService.class);
  }

  @Test
  void shouldWithdrawTransportOrder() {
    TransportOrder someOrder = getSomeTransportOrder();

    // tag::documentation_withdrawTransportOrder[]
    DispatcherService dispatcherService = getADispatcherService();
    dispatcherService.withdrawByTransportOrder(someOrder.getReference(), true);
    // end::documentation_withdrawTransportOrder[]
  }

  @Test
  void shouldWithdrawTransportOrderByVehicle() {
    Vehicle curVehicle = getSomeVehicle();

    // tag::documentation_withdrawTransportOrderByVehicle[]
    DispatcherService dispatcherService = getADispatcherService();
    dispatcherService.withdrawByVehicle(curVehicle.getReference(), true);
    // end::documentation_withdrawTransportOrderByVehicle[]
  }

  private Location getSampleDestinationLocation() {
    return new Location("Location", new LocationType("LocationType").getReference());
  }

  private DispatcherService getADispatcherService() {
    return dispService;
  }

  private TransportOrder getSomeTransportOrder() {
    return new TransportOrder(
        "Transportorder",
        Collections.singletonList(
            new DriveOrder(
                new DriveOrder.Destination(getSampleDestinationLocation().getReference())
                    .withOperation("some-operation"))
        )
    );
  }

  private Vehicle getSomeVehicle() {
    return new Vehicle("some-vehicle");
  }
}
