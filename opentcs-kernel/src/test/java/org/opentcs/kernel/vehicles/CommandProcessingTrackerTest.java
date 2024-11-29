// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.vehicles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.Route;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.drivers.vehicle.MovementCommand;

/**
 * Tests for {@link CommandProcessingTracker}.
 */
class CommandProcessingTrackerTest {

  private Point pointA;
  private Point pointB;
  private Point pointC;
  private Point pointC2;
  private Point pointD;
  private Path pathAB;
  private Path pathBC;
  private Path pathCD;
  private Path pathC2D;

  private CommandProcessingTracker commandProcessingTracker;

  @BeforeEach
  void setUp() {
    pointA = new Point("A");
    pointB = new Point("B");
    pointC = new Point("C");
    pointC2 = new Point("C2");
    pointD = new Point("D");
    pathAB = new Path("A --- B", pointA.getReference(), pointB.getReference());
    pathBC = new Path("B --- C", pointB.getReference(), pointC.getReference());
    pathBC = new Path("B --- C2", pointB.getReference(), pointC2.getReference());
    pathCD = new Path("C --- D", pointC.getReference(), pointD.getReference());
    pathC2D = new Path("C2 --- D", pointC2.getReference(), pointD.getReference());

    commandProcessingTracker = new CommandProcessingTracker();
  }

  @Test
  void initiallyEmpty() {
    assertThat(commandProcessingTracker.getClaimedResources()).isEmpty();
    assertThat(commandProcessingTracker.getAllocatedResources()).isEmpty();
    assertThat(commandProcessingTracker.getAllocatedResourcesAhead()).isEmpty();
    assertThat(commandProcessingTracker.getAllocationPendingResources()).isEmpty();
  }

  @Test
  void regularProcessingOfDriveOrder() {
    List<MovementCommand> movementCommands = createMovementCommandList(
        List.of(
            new Route.Step(pathAB, pointA, pointB, Vehicle.Orientation.FORWARD, 0, 1),
            new Route.Step(pathBC, pointB, pointC, Vehicle.Orientation.FORWARD, 1, 1),
            new Route.Step(pathCD, pointC, pointD, Vehicle.Orientation.FORWARD, 2, 1)
        )
    );

    // Initially, a vehicle reports the position it's located at and corresponding resources are
    // allocated
    commandProcessingTracker.allocationReset(Set.of(pointA));
    assertThat(commandProcessingTracker.hasCommandsToBeSent()).isFalse();
    assertThat(commandProcessingTracker.getClaimedResources()).isEmpty();
    assertThat(commandProcessingTracker.getAllocatedResources()).containsExactly(Set.of(pointA));
    assertThat(commandProcessingTracker.getAllocatedResourcesAhead()).isEmpty();
    assertThat(commandProcessingTracker.getAllocationPendingCommand()).isEmpty();
    assertThat(commandProcessingTracker.getSendingPendingCommand()).isEmpty();
    assertThat(commandProcessingTracker.getSentCommands()).isEmpty();
    assertThat(commandProcessingTracker.getLastCommandExecuted()).isEmpty();
    assertThat(commandProcessingTracker.getNextAllocationCommand()).isEmpty();
    assertThat(commandProcessingTracker.isWaitingForAllocation()).isFalse();

    // Then, a transport order / drive order is assigned to the vehicle and the resources for the
    // current drive order and its movement commands are claimed
    commandProcessingTracker.driveOrderUpdated(movementCommands);
    assertThat(commandProcessingTracker.hasCommandsToBeSent()).isTrue();
    assertThat(commandProcessingTracker.getClaimedResources()).containsExactly(
        Set.of(pathAB, pointB),
        Set.of(pathBC, pointC),
        Set.of(pathCD, pointD)
    );
    assertThat(commandProcessingTracker.getAllocatedResources()).containsExactly(Set.of(pointA));
    assertThat(commandProcessingTracker.getAllocatedResourcesAhead()).isEmpty();
    assertThat(commandProcessingTracker.getAllocationPendingCommand()).isEmpty();

    // Then, allocation for the first set of resources is requested
    commandProcessingTracker.allocationRequested(Set.of(pathAB, pointB));
    assertThat(commandProcessingTracker.getClaimedResources()).containsExactly(
        Set.of(pathAB, pointB),
        Set.of(pathBC, pointC),
        Set.of(pathCD, pointD)
    );
    assertThat(commandProcessingTracker.getAllocatedResources()).containsExactly(Set.of(pointA));
    assertThat(commandProcessingTracker.getAllocatedResourcesAhead()).isEmpty();
    assertThat(commandProcessingTracker.getAllocationPendingCommand())
        .contains(movementCommands.get(0));
    assertThat(commandProcessingTracker.isWaitingForAllocation()).isTrue();

    // Then, allocation for the requested resources is confirmed
    commandProcessingTracker.allocationConfirmed(Set.of(pathAB, pointB));
    assertThat(commandProcessingTracker.getClaimedResources()).containsExactly(
        Set.of(pathBC, pointC),
        Set.of(pathCD, pointD)
    );
    assertThat(commandProcessingTracker.getAllocatedResources()).containsExactly(
        Set.of(pointA),
        Set.of(pathAB, pointB)
    );
    assertThat(commandProcessingTracker.getAllocatedResourcesAhead())
        .containsExactly(Set.of(pathAB, pointB));
    assertThat(commandProcessingTracker.getAllocationPendingCommand()).isEmpty();
    assertThat(commandProcessingTracker.getSendingPendingCommand())
        .contains(movementCommands.get(0));
    assertThat(commandProcessingTracker.getSentCommands()).isEmpty();
    assertThat(commandProcessingTracker.isWaitingForAllocation()).isFalse();

    // Then, the movement command for which resources have been allocated is sent to the vehicle
    commandProcessingTracker.commandSent(movementCommands.get(0));
    assertThat(commandProcessingTracker.hasCommandsToBeSent()).isTrue();
    assertThat(commandProcessingTracker.getClaimedResources()).containsExactly(
        Set.of(pathBC, pointC),
        Set.of(pathCD, pointD)
    );
    assertThat(commandProcessingTracker.getAllocatedResources()).containsExactly(
        Set.of(pointA),
        Set.of(pathAB, pointB)
    );
    assertThat(commandProcessingTracker.getAllocatedResourcesAhead())
        .containsExactly(Set.of(pathAB, pointB));
    assertThat(commandProcessingTracker.getSendingPendingCommand()).isEmpty();
    assertThat(commandProcessingTracker.getSentCommands()).containsExactly(movementCommands.get(0));
    assertThat(commandProcessingTracker.getLastCommandExecuted()).isEmpty();

    // Then, the movement command is reported as executed and resources for that command are still
    // allocated but no longer ahead of the vehicle and split into the command's path, and its
    // point & location
    commandProcessingTracker.commandExecuted(movementCommands.get(0));
    assertThat(commandProcessingTracker.getClaimedResources()).containsExactly(
        Set.of(pathBC, pointC),
        Set.of(pathCD, pointD)
    );
    assertThat(commandProcessingTracker.getAllocatedResources()).containsExactly(
        Set.of(pointA),
        Set.of(pathAB),
        Set.of(pointB)
    );
    assertThat(commandProcessingTracker.getAllocatedResourcesAhead()).isEmpty();
    assertThat(commandProcessingTracker.getSentCommands()).isEmpty();
    assertThat(commandProcessingTracker.getLastCommandExecuted()).contains(movementCommands.get(0));

    // Then, allocation for the resources that are no longer needed is released
    commandProcessingTracker.allocationReleased(Set.of(pointA));
    assertThat(commandProcessingTracker.getClaimedResources()).containsExactly(
        Set.of(pathBC, pointC),
        Set.of(pathCD, pointD)
    );
    assertThat(commandProcessingTracker.getAllocatedResources()).containsExactly(
        Set.of(pathAB),
        Set.of(pointB)
    );
    assertThat(commandProcessingTracker.getAllocatedResourcesAhead()).isEmpty();

    // Then, the next movement command is processed
    commandProcessingTracker.allocationRequested(Set.of(pathBC, pointC));
    commandProcessingTracker.allocationConfirmed(Set.of(pathBC, pointC));
    commandProcessingTracker.commandSent(movementCommands.get(1));
    commandProcessingTracker.commandExecuted(movementCommands.get(1));
    commandProcessingTracker.allocationReleased(Set.of(pathAB));
    commandProcessingTracker.allocationReleased(Set.of(pointB));
    assertThat(commandProcessingTracker.hasCommandsToBeSent()).isTrue();
    assertThat(commandProcessingTracker.getClaimedResources())
        .containsExactly(Set.of(pathCD, pointD));
    assertThat(commandProcessingTracker.getAllocatedResources())
        .containsExactly(Set.of(pathBC), Set.of(pointC));
    assertThat(commandProcessingTracker.getAllocatedResourcesAhead()).isEmpty();
    assertThat(commandProcessingTracker.getLastCommandExecuted()).contains(movementCommands.get(1));

    // Then, the next (and last) movement command is processed
    commandProcessingTracker.allocationRequested(Set.of(pathCD, pointD));
    commandProcessingTracker.allocationConfirmed(Set.of(pathCD, pointD));
    commandProcessingTracker.commandSent(movementCommands.get(2));
    commandProcessingTracker.commandExecuted(movementCommands.get(2));
    commandProcessingTracker.allocationReleased(Set.of(pathBC));
    commandProcessingTracker.allocationReleased(Set.of(pointC));
    assertThat(commandProcessingTracker.hasCommandsToBeSent()).isFalse();
    assertThat(commandProcessingTracker.getClaimedResources()).isEmpty();
    assertThat(commandProcessingTracker.getAllocatedResources())
        .containsExactly(Set.of(pathCD), Set.of(pointD));
    assertThat(commandProcessingTracker.getAllocatedResourcesAhead()).isEmpty();
    assertThat(commandProcessingTracker.getLastCommandExecuted()).contains(movementCommands.get(2));

    // At this point, the drive order is considered finished
    assertThat(commandProcessingTracker.isDriveOrderFinished()).isTrue();
  }

  @Test
  void processingOfDriveOrderWithNewRoute() {
    List<MovementCommand> movementCommands = createMovementCommandList(
        List.of(
            new Route.Step(pathAB, pointA, pointB, Vehicle.Orientation.FORWARD, 0, 1),
            new Route.Step(pathBC, pointB, pointC, Vehicle.Orientation.FORWARD, 1, 1),
            new Route.Step(pathCD, pointC, pointD, Vehicle.Orientation.FORWARD, 2, 1)
        )
    );

    // Regular processing of the first movement command up to the point where the command is sent
    commandProcessingTracker.allocationReset(Set.of(pointA));
    commandProcessingTracker.driveOrderUpdated(movementCommands);
    commandProcessingTracker.allocationRequested(Set.of(pathAB, pointB));
    commandProcessingTracker.allocationConfirmed(Set.of(pathAB, pointB));
    commandProcessingTracker.commandSent(movementCommands.get(0));
    assertThat(commandProcessingTracker.hasCommandsToBeSent()).isTrue();
    assertThat(commandProcessingTracker.getClaimedResources()).containsExactly(
        Set.of(pathBC, pointC),
        Set.of(pathCD, pointD)
    );
    assertThat(commandProcessingTracker.getAllocatedResources()).containsExactly(
        Set.of(pointA),
        Set.of(pathAB, pointB)
    );
    assertThat(commandProcessingTracker.getAllocatedResourcesAhead())
        .containsExactly(Set.of(pathAB, pointB));
    assertThat(commandProcessingTracker.getAllocationPendingCommand()).isEmpty();
    assertThat(commandProcessingTracker.getSentCommands()).containsExactly(movementCommands.get(0));
    assertThat(commandProcessingTracker.getNextAllocationCommand())
        .contains(movementCommands.get(1));

    // Then, a drive order update (with a new route) is received
    movementCommands = createMovementCommandList(
        List.of(
            new Route.Step(pathAB, pointA, pointB, Vehicle.Orientation.FORWARD, 0, 1),
            new Route.Step(pathBC, pointB, pointC2, Vehicle.Orientation.FORWARD, 1, 1),
            new Route.Step(pathC2D, pointC2, pointD, Vehicle.Orientation.FORWARD, 2, 1)
        )
    );
    commandProcessingTracker.driveOrderUpdated(movementCommands);
    assertThat(commandProcessingTracker.getClaimedResources()).containsExactly(
        Set.of(pathBC, pointC2),
        Set.of(pathC2D, pointD)
    );
    assertThat(commandProcessingTracker.getAllocatedResources()).containsExactly(
        Set.of(pointA),
        Set.of(pathAB, pointB)
    );
    assertThat(commandProcessingTracker.getAllocatedResourcesAhead())
        .containsExactly(Set.of(pathAB, pointB));
    assertThat(commandProcessingTracker.getAllocationPendingCommand()).isEmpty();
    assertThat(commandProcessingTracker.getSentCommands()).containsExactly(movementCommands.get(0));
    assertThat(commandProcessingTracker.getNextAllocationCommand())
        .contains(movementCommands.get(1));

    // Then, the first movement command is reported as executed and allocation of corresponding
    // resources is released
    commandProcessingTracker.commandExecuted(movementCommands.get(0));
    commandProcessingTracker.allocationReleased(Set.of(pointA));
    assertThat(commandProcessingTracker.getAllocatedResources())
        .containsExactly(Set.of(pathAB), Set.of(pointB));
    assertThat(commandProcessingTracker.getLastCommandExecuted()).contains(movementCommands.get(0));

    // Then, the next movement command is processed (the one for the new route)
    commandProcessingTracker.allocationRequested(Set.of(pathBC, pointC2));
    commandProcessingTracker.allocationConfirmed(Set.of(pathBC, pointC2));
    commandProcessingTracker.commandSent(movementCommands.get(1));
    commandProcessingTracker.commandExecuted(movementCommands.get(1));
    commandProcessingTracker.allocationReleased(Set.of(pathAB));
    commandProcessingTracker.allocationReleased(Set.of(pointB));
    assertThat(commandProcessingTracker.hasCommandsToBeSent()).isTrue();
    assertThat(commandProcessingTracker.getClaimedResources())
        .containsExactly(Set.of(pathC2D, pointD));
    assertThat(commandProcessingTracker.getAllocatedResources())
        .containsExactly(Set.of(pathBC), Set.of(pointC2));
    assertThat(commandProcessingTracker.getAllocatedResourcesAhead()).isEmpty();
    assertThat(commandProcessingTracker.getLastCommandExecuted()).contains(movementCommands.get(1));

    // Then, the next (and last) movement command is processed (the one for the new route)
    commandProcessingTracker.allocationRequested(Set.of(pathC2D, pointD));
    commandProcessingTracker.allocationConfirmed(Set.of(pathC2D, pointD));
    commandProcessingTracker.commandSent(movementCommands.get(2));
    commandProcessingTracker.commandExecuted(movementCommands.get(2));
    commandProcessingTracker.allocationReleased(Set.of(pathBC));
    commandProcessingTracker.allocationReleased(Set.of(pointC2));
    assertThat(commandProcessingTracker.hasCommandsToBeSent()).isFalse();
    assertThat(commandProcessingTracker.getClaimedResources()).isEmpty();
    assertThat(commandProcessingTracker.getAllocatedResources())
        .containsExactly(Set.of(pathC2D), Set.of(pointD));
    assertThat(commandProcessingTracker.getAllocatedResourcesAhead()).isEmpty();
    assertThat(commandProcessingTracker.getLastCommandExecuted()).contains(movementCommands.get(2));

    // At this point, the drive order is considered finished
    assertThat(commandProcessingTracker.isDriveOrderFinished()).isTrue();
  }

  @Test
  void processingOfRegularDriveOrderAbortion() {
    List<MovementCommand> movementCommands = createMovementCommandList(
        List.of(
            new Route.Step(pathAB, pointA, pointB, Vehicle.Orientation.FORWARD, 0, 1),
            new Route.Step(pathBC, pointB, pointC, Vehicle.Orientation.FORWARD, 1, 1),
            new Route.Step(pathCD, pointC, pointD, Vehicle.Orientation.FORWARD, 2, 1)
        )
    );

    // Regular processing of the first movement command up to the point where the allocation is
    // confirmed
    commandProcessingTracker.allocationReset(Set.of(pointA));
    commandProcessingTracker.driveOrderUpdated(movementCommands);
    commandProcessingTracker.allocationRequested(Set.of(pathAB, pointB));
    commandProcessingTracker.allocationConfirmed(Set.of(pathAB, pointB));
    assertThat(commandProcessingTracker.hasCommandsToBeSent()).isTrue();
    assertThat(commandProcessingTracker.getClaimedResources()).containsExactly(
        Set.of(pathBC, pointC),
        Set.of(pathCD, pointD)
    );
    assertThat(commandProcessingTracker.getAllocatedResources()).containsExactly(
        Set.of(pointA),
        Set.of(pathAB, pointB)
    );
    assertThat(commandProcessingTracker.getAllocatedResourcesAhead())
        .containsExactly(Set.of(pathAB, pointB));

    // Then, the drive order is aborted regularly
    commandProcessingTracker.driveOrderAborted(false);
    assertThat(commandProcessingTracker.hasCommandsToBeSent()).isTrue();
    assertThat(commandProcessingTracker.getClaimedResources()).isEmpty();
    assertThat(commandProcessingTracker.getAllocatedResources()).containsExactly(
        Set.of(pointA),
        Set.of(pathAB, pointB)
    );
    assertThat(commandProcessingTracker.getAllocatedResourcesAhead())
        .containsExactly(Set.of(pathAB, pointB));
    assertThat(commandProcessingTracker.getSendingPendingCommand())
        .contains(movementCommands.get(0));

    // Then, the movement command for which resources have already been allocated (and which is
    // pending to be sent) is sent and reported as executed
    commandProcessingTracker.commandSent(movementCommands.get(0));
    assertThat(commandProcessingTracker.hasCommandsToBeSent()).isFalse();
    commandProcessingTracker.commandExecuted(movementCommands.get(0));
    commandProcessingTracker.allocationReleased(Set.of(pointA));

    // At this point, the drive order is considered finished and further processing should result
    // in an exception
    assertThat(commandProcessingTracker.isDriveOrderFinished()).isTrue();
    assertThatThrownBy(() -> commandProcessingTracker.allocationRequested(Set.of(pathBC, pointC)))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void processingOfImmediateDriveOrderAbortion() {
    List<MovementCommand> movementCommands = createMovementCommandList(
        List.of(
            new Route.Step(pathAB, pointA, pointB, Vehicle.Orientation.FORWARD, 0, 1),
            new Route.Step(pathBC, pointB, pointC, Vehicle.Orientation.FORWARD, 1, 1),
            new Route.Step(pathCD, pointC, pointD, Vehicle.Orientation.FORWARD, 2, 1)
        )
    );

    // Regular processing of the first movement command up to the point where the allocation is
    // confirmed
    commandProcessingTracker.allocationReset(Set.of(pointA));
    commandProcessingTracker.driveOrderUpdated(movementCommands);
    commandProcessingTracker.allocationRequested(Set.of(pathAB, pointB));
    commandProcessingTracker.allocationConfirmed(Set.of(pathAB, pointB));
    assertThat(commandProcessingTracker.hasCommandsToBeSent()).isTrue();
    assertThat(commandProcessingTracker.getClaimedResources()).containsExactly(
        Set.of(pathBC, pointC),
        Set.of(pathCD, pointD)
    );
    assertThat(commandProcessingTracker.getAllocatedResources()).containsExactly(
        Set.of(pointA),
        Set.of(pathAB, pointB)
    );
    assertThat(commandProcessingTracker.getAllocatedResourcesAhead())
        .containsExactly(Set.of(pathAB, pointB));

    // Then, the drive order is aborted immediately
    commandProcessingTracker.driveOrderAborted(true);
    assertThat(commandProcessingTracker.hasCommandsToBeSent()).isFalse();
    assertThat(commandProcessingTracker.getClaimedResources()).isEmpty();
    assertThat(commandProcessingTracker.getAllocatedResources()).containsExactly(Set.of(pointA));
    assertThat(commandProcessingTracker.getAllocatedResourcesAhead()).isEmpty();

    // At this point, the drive order is considered finished and further processing should result
    // in an exception
    assertThat(commandProcessingTracker.isDriveOrderFinished()).isTrue();
    assertThatThrownBy(() -> commandProcessingTracker.commandSent(movementCommands.get(0)))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void processingOfDriveOrderWithRevokedAllocation() {
    List<MovementCommand> movementCommands = createMovementCommandList(
        List.of(
            new Route.Step(pathAB, pointA, pointB, Vehicle.Orientation.FORWARD, 0, 1),
            new Route.Step(pathBC, pointB, pointC, Vehicle.Orientation.FORWARD, 1, 1),
            new Route.Step(pathCD, pointC, pointD, Vehicle.Orientation.FORWARD, 2, 1)
        )
    );

    // Regular processing of the first movement command up to the point where the allocation is
    // confirmed
    commandProcessingTracker.allocationReset(Set.of(pointA));
    commandProcessingTracker.driveOrderUpdated(movementCommands);
    commandProcessingTracker.allocationRequested(Set.of(pathAB, pointB));
    commandProcessingTracker.allocationConfirmed(Set.of(pathAB, pointB));
    assertThat(commandProcessingTracker.getClaimedResources()).containsExactly(
        Set.of(pathBC, pointC),
        Set.of(pathCD, pointD)
    );
    assertThat(commandProcessingTracker.getAllocatedResources()).containsExactly(
        Set.of(pointA),
        Set.of(pathAB, pointB)
    );
    assertThat(commandProcessingTracker.getAllocatedResourcesAhead())
        .containsExactly(Set.of(pathAB, pointB));
    assertThat(commandProcessingTracker.getSendingPendingCommand())
        .contains(movementCommands.get(0));

    // Then, allocation for the resources that have been allocated last is revoked
    commandProcessingTracker.allocationRevoked(Set.of(pathAB, pointB));
    assertThat(commandProcessingTracker.getClaimedResources()).containsExactly(
        Set.of(pathBC, pointC),
        Set.of(pathCD, pointD)
    );
    assertThat(commandProcessingTracker.getAllocatedResources()).containsExactly(Set.of(pointA));
    assertThat(commandProcessingTracker.getAllocatedResourcesAhead()).isEmpty();
    assertThat(commandProcessingTracker.getSendingPendingCommand()).isEmpty();

    // At this point, the drive order is still considered being processed but further processing
    // should result in an exception
    assertThat(commandProcessingTracker.isDriveOrderFinished()).isFalse();
    assertThatThrownBy(() -> commandProcessingTracker.commandSent(movementCommands.get(0)))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void processingOfDriveOrderWhereSendingIsAborted() {
    List<MovementCommand> movementCommands = createMovementCommandList(
        List.of(
            new Route.Step(pathAB, pointA, pointB, Vehicle.Orientation.FORWARD, 0, 1),
            new Route.Step(pathBC, pointB, pointC, Vehicle.Orientation.FORWARD, 1, 1),
            new Route.Step(pathCD, pointC, pointD, Vehicle.Orientation.FORWARD, 2, 1)
        )
    );

    // Regular processing of the first movement command up to the point where the allocation is
    // confirmed
    commandProcessingTracker.allocationReset(Set.of(pointA));
    commandProcessingTracker.driveOrderUpdated(movementCommands);
    commandProcessingTracker.allocationRequested(Set.of(pathAB, pointB));
    commandProcessingTracker.allocationConfirmed(Set.of(pathAB, pointB));
    assertThat(commandProcessingTracker.getClaimedResources()).containsExactly(
        Set.of(pathBC, pointC),
        Set.of(pathCD, pointD)
    );
    assertThat(commandProcessingTracker.getAllocatedResources()).containsExactly(
        Set.of(pointA),
        Set.of(pathAB, pointB)
    );
    assertThat(commandProcessingTracker.getAllocatedResourcesAhead())
        .containsExactly(Set.of(pathAB, pointB));
    assertThat(commandProcessingTracker.getSendingPendingCommand())
        .contains(movementCommands.get(0));

    // Then, sending of the first movement command is aborted, but allocations don't change
    commandProcessingTracker.commandSendingStopped(movementCommands.get(0));
    assertThat(commandProcessingTracker.getClaimedResources()).containsExactly(
        Set.of(pathBC, pointC),
        Set.of(pathCD, pointD)
    );
    assertThat(commandProcessingTracker.getAllocatedResources()).containsExactly(
        Set.of(pointA),
        Set.of(pathAB, pointB)
    );
    assertThat(commandProcessingTracker.getAllocatedResourcesAhead())
        .containsExactly(Set.of(pathAB, pointB));
    assertThat(commandProcessingTracker.getSendingPendingCommand()).isEmpty();

    // At this point, the drive order is still considered being processed but further processing
    // should result in an exception
    assertThat(commandProcessingTracker.isDriveOrderFinished()).isFalse();
    assertThatThrownBy(() -> commandProcessingTracker.commandSent(movementCommands.get(0)))
        .isInstanceOf(IllegalArgumentException.class);
  }

  private List<MovementCommand> createMovementCommandList(List<Route.Step> steps) {
    Point finalDestinationPoint = steps.getLast().getDestinationPoint();
    DriveOrder driveOrder = new DriveOrder(
        new DriveOrder.Destination(finalDestinationPoint.getReference())
    ).withRoute(new Route(steps));
    TransportOrder transportOrder = new TransportOrder(
        String.format(
            "%s-to-%s",
            steps.getFirst().getSourcePoint().getName(),
            steps.getLast().getDestinationPoint().getName()
        ),
        List.of(driveOrder)
    ).withProcessingVehicle(new Vehicle("vehicle").getReference());

    List<MovementCommand> movementCommands = new ArrayList<>();
    for (Route.Step step : steps) {
      movementCommands.add(
          new MovementCommand(
              transportOrder,
              driveOrder,
              step,
              MovementCommand.MOVE_OPERATION,
              null,
              true,
              null,
              finalDestinationPoint,
              MovementCommand.MOVE_OPERATION,
              Map.of()
          )
      );
    }

    return movementCommands;
  }
}
