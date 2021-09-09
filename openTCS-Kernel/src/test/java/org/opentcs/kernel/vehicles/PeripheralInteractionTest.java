/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.vehicles;

import java.util.Arrays;
import java.util.Map;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.junit.*;
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
import org.opentcs.data.order.Route;
import org.opentcs.data.peripherals.PeripheralJob;
import org.opentcs.data.peripherals.PeripheralOperation;
import org.opentcs.data.peripherals.PeripheralOperation.ExecutionTrigger;
import org.opentcs.drivers.vehicle.MovementCommand;

/**
 * Defines test cases for {@link PeripheralInteraction}.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class PeripheralInteractionTest {

  /**
   * The vehicle instance to use for the tests.
   */
  private static final Vehicle VEHICLE = new Vehicle("Vehicle-01");
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

  public PeripheralInteractionTest() {
  }

  @Before
  public void setUp() {
    peripheralJobService = mock(PeripheralJobService.class);
    succeededCallback = mock(Runnable.class);
    failedCallback = mock(Runnable.class);
  }

  @Test
  public void shouldNotWaitForOperationCompletion() {
    PeripheralOperation operation = new PeripheralOperation(createLocation().getReference(),
                                                            RESERVATION_TOKEN,
                                                            ExecutionTrigger.BEFORE_MOVEMENT,
                                                            false);

    peripheralInteraction = new PeripheralInteraction(VEHICLE.getReference(),
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
  public void shouldWaitForOperationCompletion() {
    PeripheralOperation operation = new PeripheralOperation(createLocation().getReference(),
                                                            RESERVATION_TOKEN,
                                                            ExecutionTrigger.BEFORE_MOVEMENT,
                                                            true);

    PeripheralJob peripheralJob = new PeripheralJob("SomeJob", RESERVATION_TOKEN, operation);
    when(peripheralJobService.createPeripheralJob(any())).thenReturn(peripheralJob);

    peripheralInteraction = new PeripheralInteraction(VEHICLE.getReference(),
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
  public void shouldCallbackOnFailedRequiredInteraction() {
    PeripheralOperation operation = new PeripheralOperation(createLocation().getReference(),
                                                            RESERVATION_TOKEN,
                                                            ExecutionTrigger.BEFORE_MOVEMENT,
                                                            true);

    PeripheralJob peripheralJob = new PeripheralJob("SomeJob", RESERVATION_TOKEN, operation);
    when(peripheralJobService.createPeripheralJob(any())).thenReturn(peripheralJob);

    peripheralInteraction = new PeripheralInteraction(VEHICLE.getReference(),
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

    public DummyMovementCommand() {
      Point srcPoint = new Point("Point-01");
      Point destPoint = new Point("Point-02");
      Path path = new Path("Point-01 --- Point-02",
                           srcPoint.getReference(),
                           destPoint.getReference());
      dummyStep = new Route.Step(path, srcPoint, destPoint, Vehicle.Orientation.FORWARD, 0);
    }

    @Override
    public Route getRoute() {
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
    public boolean isWithoutOperation() {
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
    public Map<String, String> getProperties() {
      throw new UnsupportedOperationException("Not supported yet.");
    }
  }
}
