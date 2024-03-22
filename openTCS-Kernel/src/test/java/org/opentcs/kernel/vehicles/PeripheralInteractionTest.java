/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.vehicles;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.opentcs.components.kernel.services.PeripheralJobService;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.Route;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.data.peripherals.PeripheralJob;
import org.opentcs.data.peripherals.PeripheralOperation;
import org.opentcs.data.peripherals.PeripheralOperation.ExecutionTrigger;
import org.opentcs.drivers.vehicle.MovementCommand;

/**
 * Defines test cases for {@link PeripheralInteraction}.
 */
class PeripheralInteractionTest {

  /**
   * The vehicle instance to use for the tests.
   */
  private static final Vehicle VEHICLE = new Vehicle("Vehicle-01");
  /**
   * The transport order to use for the tests.
   */
  private static final TransportOrder ORDER = new TransportOrder("TransportOrder-01", List.of());
  /**
   * The reservation token to use for the tests.
   */
  private static final String RESERVATION_TOKEN = "SomeToken";
  /**
   * A (mocked) peripheral job service.
   */
  private PeripheralJobService peripheralJobService;
  /**
   * A (mocked) callback to be executed when interactions succeeded.
   */
  private Runnable succeededCallback;
  /**
   * A (mocked) callback to be executed when interactions failed.
   */
  private Runnable failedCallback;
  /**
   * The instance to test.
   */
  private PeripheralInteraction peripheralInteraction;

  PeripheralInteractionTest() {
  }

  @BeforeEach
  void setUp() {
    peripheralJobService = mock(PeripheralJobService.class);
    succeededCallback = mock(Runnable.class);
    failedCallback = mock(Runnable.class);
  }

  @Test
  void shouldNotWaitForOperationCompletion() {
    PeripheralOperation operation = new PeripheralOperation(createLocation().getReference(),
                                                            RESERVATION_TOKEN,
                                                            ExecutionTrigger.AFTER_ALLOCATION,
                                                            false);

    peripheralInteraction = new PeripheralInteraction(VEHICLE.getReference(),
                                                      ORDER.getReference(),
                                                      new DummyMovementCommand(),
                                                      Arrays.asList(operation),
                                                      peripheralJobService,
                                                      RESERVATION_TOKEN);

    verify(succeededCallback, times(0)).run();
    assertThat(peripheralInteraction.hasState(PeripheralInteraction.State.PRISTINE), is(true));

    peripheralInteraction.start(succeededCallback, failedCallback);
    verify(peripheralJobService, times(1)).createPeripheralJob(any());
    verify(succeededCallback, times(1)).run();
    assertThat(peripheralInteraction.isFinished(), is(true));

    verify(succeededCallback, times(1)).run();
    verify(failedCallback, times(0)).run();
  }

  @Test
  void shouldWaitForOperationCompletion() {
    PeripheralOperation operation = new PeripheralOperation(createLocation().getReference(),
                                                            RESERVATION_TOKEN,
                                                            ExecutionTrigger.AFTER_ALLOCATION,
                                                            true);

    PeripheralJob peripheralJob = new PeripheralJob("SomeJob", RESERVATION_TOKEN, operation);
    when(peripheralJobService.createPeripheralJob(any())).thenReturn(peripheralJob);

    peripheralInteraction = new PeripheralInteraction(VEHICLE.getReference(),
                                                      ORDER.getReference(),
                                                      new DummyMovementCommand(),
                                                      Arrays.asList(operation),
                                                      peripheralJobService,
                                                      RESERVATION_TOKEN);

    verify(succeededCallback, times(0)).run();
    assertThat(peripheralInteraction.hasState(PeripheralInteraction.State.PRISTINE), is(true));

    peripheralInteraction.start(succeededCallback, failedCallback);
    verify(peripheralJobService, times(1)).createPeripheralJob(any());
    verify(succeededCallback, times(0)).run();
    assertThat(peripheralInteraction.isFinished(), is(false));

    peripheralInteraction.onPeripheralJobFinished(peripheralJob);
    verify(succeededCallback, times(1)).run();
    assertThat(peripheralInteraction.isFinished(), is(true));

    verify(failedCallback, times(0)).run();
  }

  @Test
  void shouldCallbackOnFailedRequiredInteraction() {
    PeripheralOperation operation = new PeripheralOperation(createLocation().getReference(),
                                                            RESERVATION_TOKEN,
                                                            ExecutionTrigger.AFTER_ALLOCATION,
                                                            true);

    PeripheralJob peripheralJob = new PeripheralJob("SomeJob", RESERVATION_TOKEN, operation);
    when(peripheralJobService.createPeripheralJob(any())).thenReturn(peripheralJob);

    peripheralInteraction = new PeripheralInteraction(VEHICLE.getReference(),
                                                      ORDER.getReference(),
                                                      new DummyMovementCommand(),
                                                      Arrays.asList(operation),
                                                      peripheralJobService,
                                                      RESERVATION_TOKEN);

    verify(succeededCallback, times(0)).run();
    assertThat(peripheralInteraction.hasState(PeripheralInteraction.State.PRISTINE), is(true));

    peripheralInteraction.start(succeededCallback, failedCallback);
    verify(peripheralJobService, times(1)).createPeripheralJob(any());
    verify(failedCallback, times(0)).run();
    assertThat(peripheralInteraction.isFailed(), is(false));

    peripheralInteraction.onPeripheralJobFailed(peripheralJob);
    verify(failedCallback, times(1)).run();
    assertThat(peripheralInteraction.isFailed(), is(true));

    verify(succeededCallback, times(0)).run();
  }

  private Location createLocation() {
    LocationType locationType = new LocationType("LocationType-01");
    return new Location("Location-01", locationType.getReference());
  }

  private class DummyMovementCommand
      implements MovementCommand {

    private final Route.Step dummyStep;

    DummyMovementCommand() {
      Point srcPoint = new Point("Point-01");
      Point destPoint = new Point("Point-02");
      Path path = new Path("Point-01 --- Point-02",
                           srcPoint.getReference(),
                           destPoint.getReference());
      dummyStep = new Route.Step(path, srcPoint, destPoint, Vehicle.Orientation.FORWARD, 0);
    }

    @Override
    public TransportOrder getTransportOrder() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DriveOrder getDriveOrder() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Route.Step getStep() {
      return dummyStep;
    }

    @Override
    public String getOperation() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    @Deprecated
    public boolean isWithoutOperation() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean hasEmptyOperation() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Location getOpLocation() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isFinalMovement() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Point getFinalDestination() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Location getFinalDestinationLocation() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getFinalOperation() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean equalsInMovement(MovementCommand command) {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Map<String, String> getProperties() {
      throw new UnsupportedOperationException("Not supported yet.");
    }
  }
}
