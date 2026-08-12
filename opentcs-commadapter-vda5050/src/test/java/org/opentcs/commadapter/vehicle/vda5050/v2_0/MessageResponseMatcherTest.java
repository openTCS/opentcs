// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v2_0;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ErrorTypes.NO_ROUTE_ERROR;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ErrorTypes.ORDER_ERROR;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ErrorTypes.ORDER_UPDATE_ERROR;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ErrorTypes.VALIDATION_ERROR;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.action.CancelOrder;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.action.Drop;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.action.Pick;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.action.StartCharging;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.Action;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.BlockingType;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.instantactions.InstantActions;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.order.Order;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.ActionState;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.ActionStatus;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.BatteryState;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.EStop;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.ErrorEntry;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.ErrorLevel;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.OperatingMode;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.SafetyState;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.State;
import org.opentcs.drivers.vehicle.MovementCommand;

/**
 * Unit tests for {@link MessageResponseMatcher}.
 */
public class MessageResponseMatcherTest {

  private MessageResponseMatcher messageResponseMatcher;

  private Consumer<Order> sendOrderCallback;
  private Consumer<InstantActions> sendInstantActionsCallback;
  private Consumer<OrderAssociation> orderAcceptedCallback;

  private MovementCommand dummyCommand;

  @BeforeEach
  @SuppressWarnings("unchecked")
  public void setUp() {
    sendOrderCallback = mock(Consumer.class);
    sendInstantActionsCallback = mock(Consumer.class);
    orderAcceptedCallback = mock(Consumer.class);
    messageResponseMatcher = new MessageResponseMatcher(
        "test",
        sendOrderCallback,
        sendInstantActionsCallback,
        orderAcceptedCallback,
        0
    );
    messageResponseMatcher.onStateMessage(newState());
    dummyCommand = mock(MovementCommand.class);
  }

  @Test
  public void waitForAcknowledgementBeforeSendingNextOrder() {
    Order order1 = new Order("order1", 0L, List.of(), List.of());
    Order order2 = new Order("order2", 0L, List.of(), List.of());
    Order order3 = new Order("order3", 0L, List.of(), List.of());

    messageResponseMatcher.enqueueCommand(order1, dummyCommand);

    verify(sendOrderCallback, times(1)).accept(order1);

    messageResponseMatcher.enqueueCommand(order2, dummyCommand);
    messageResponseMatcher.enqueueCommand(order3, dummyCommand);

    verify(sendOrderCallback, times(1)).accept(order1);
    verify(sendOrderCallback, never()).accept(order2);
    verify(sendOrderCallback, never()).accept(order3);

    messageResponseMatcher.onStateMessage(stateAcceptingOrder(order1));

    verify(sendOrderCallback, times(1)).accept(order1);
    verify(sendOrderCallback, times(1)).accept(order2);
    verify(sendOrderCallback, never()).accept(order3);

    messageResponseMatcher.onStateMessage(stateAcceptingOrder(order2));

    verify(sendOrderCallback, times(1)).accept(order1);
    verify(sendOrderCallback, times(1)).accept(order2);
    verify(sendOrderCallback, times(1)).accept(order3);
  }

  @Test
  public void callOrderAcceptedCallbackWhenOrderWasAccepted() {
    Order order1 = new Order("order1", 0L, List.of(), List.of());

    messageResponseMatcher.enqueueCommand(order1, dummyCommand);
    messageResponseMatcher.onStateMessage(stateAcceptingOrder(order1));

    ArgumentCaptor<OrderAssociation> callbackCapture
        = ArgumentCaptor.forClass(OrderAssociation.class);
    verify(orderAcceptedCallback, times(1)).accept(callbackCapture.capture());

    OrderAssociation orderAssociation = callbackCapture.getValue();
    assertThat(orderAssociation.getOrder(), is(order1));
    assertThat(orderAssociation.getCommand(), is(dummyCommand));
  }

  @Test
  public void retrySendingOrderIfItIsNotAcknowledged() {
    Order orderNone = new Order("", 0L, List.of(), List.of());
    Order order1 = new Order("order1", 0L, List.of(), List.of());

    messageResponseMatcher.enqueueCommand(order1, dummyCommand);
    verify(sendOrderCallback, times(1)).accept(order1);

    messageResponseMatcher.onStateMessage(stateAcceptingOrder(orderNone));

    verify(sendOrderCallback, times(2)).accept(order1);
    verify(orderAcceptedCallback, never()).accept(any());
  }

  @ParameterizedTest
  @ValueSource(strings = {VALIDATION_ERROR, NO_ROUTE_ERROR, ORDER_ERROR, ORDER_UPDATE_ERROR})
  public void suppressOrderRepetitionOnOrderRejection(String errorType) {
    Order order = new Order("some-order", 0L, List.of(), List.of());
    State state = newState();
    state.setErrors(List.of(new ErrorEntry(errorType, ErrorLevel.WARNING)));

    messageResponseMatcher.enqueueCommand(order, dummyCommand);
    messageResponseMatcher.onStateMessage(state);

    // The order should have been sent only once - not again as a reaction to the state message.
    verify(sendOrderCallback, times(1)).accept(any());
  }

  @Test
  public void shouldNotSendWhenInManualModeAndOrderRejection() {
    Order order = new Order("some-order", 0L, List.of(), List.of());
    InstantActions action = new InstantActions();
    State state = stateWithOperatingMode(OperatingMode.MANUAL);
    state.setErrors(List.of(new ErrorEntry(VALIDATION_ERROR, ErrorLevel.WARNING)));

    messageResponseMatcher.onStateMessage(state);
    messageResponseMatcher.enqueueAction(action);

    verify(sendInstantActionsCallback, never()).accept(action);
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 1, 5, 10, 20_000})
  public void ignoreOrderRejectionsUntilMaximumIsReached(int ignoredRejectionsCount) {
    // Initialize a new MessageResponseMatcher for this test, configuring it to ignore a specific
    // maximum number of consecutive rejections.
    MessageResponseMatcher ignoringMessageResponseMatcher = new MessageResponseMatcher(
        "test",
        sendOrderCallback,
        sendInstantActionsCallback,
        orderAcceptedCallback,
        ignoredRejectionsCount
    );
    ignoringMessageResponseMatcher.onStateMessage(newState());

    // Enqueue a single order.
    ignoringMessageResponseMatcher.enqueueCommand(
        new Order("some-order", 0L, List.of(), List.of()),
        dummyCommand
    );

    // The order should have been sent once immediately.
    verify(sendOrderCallback, times(1)).accept(any());

    // The order should be resent as a reaction to each state message indicating a rejection.
    for (int i = 0; i < ignoredRejectionsCount; i++) {
      ignoringMessageResponseMatcher.onStateMessage(
          newState().setErrors(List.of(new ErrorEntry(VALIDATION_ERROR, ErrorLevel.WARNING)))
      );
    }
    verify(sendOrderCallback, times(ignoredRejectionsCount + 1)).accept(any());

    // Once the maximum number of ignored rejections is reached, the order should not be resent
    // any more.
    ignoringMessageResponseMatcher.onStateMessage(
        newState().setErrors(List.of(new ErrorEntry(VALIDATION_ERROR, ErrorLevel.WARNING)))
    );
    verify(sendOrderCallback, times(ignoredRejectionsCount + 1)).accept(any());
  }

  @Test
  public void waitForInstantActionAcknowledgementBeforeSendingNextMessage() {
    InstantActions action1 = new InstantActions();
    action1.setHeaderId(1L);
    action1.setActions(List.of(new Action(Pick.ACTION_TYPE, "action1", BlockingType.HARD)));
    InstantActions action2 = new InstantActions();
    action2.setHeaderId(2L);
    action2.setActions(List.of(new Action(Drop.ACTION_TYPE, "action2", BlockingType.HARD)));
    InstantActions action3 = new InstantActions();
    action3.setHeaderId(3L);
    action3.setActions(
        List.of(new Action(StartCharging.ACTION_TYPE, "action3", BlockingType.HARD))
    );

    messageResponseMatcher.enqueueAction(action1);
    messageResponseMatcher.enqueueAction(action2);
    messageResponseMatcher.enqueueAction(action3);

    verify(sendInstantActionsCallback, times(1)).accept(action1);
    verify(sendInstantActionsCallback, never()).accept(action2);
    verify(sendInstantActionsCallback, never()).accept(action3);

    messageResponseMatcher.onStateMessage(newState());

    // The instant action is repeated if the vehicle does not reflect it as accepted in its state.
    verify(sendInstantActionsCallback, times(2)).accept(action1);
    verify(sendInstantActionsCallback, never()).accept(action2);
    verify(sendInstantActionsCallback, never()).accept(action3);

    messageResponseMatcher.onStateMessage(stateAcceptingInstantAction(action1));

    verify(sendInstantActionsCallback, times(2)).accept(action1);
    verify(sendInstantActionsCallback, times(1)).accept(action2);
    verify(sendInstantActionsCallback, never()).accept(action3);

    messageResponseMatcher.onStateMessage(stateAcceptingInstantAction(action2));

    verify(sendInstantActionsCallback, times(2)).accept(action1);
    verify(sendInstantActionsCallback, times(1)).accept(action2);
    verify(sendInstantActionsCallback, times(1)).accept(action3);
  }

  @Test
  public void waitForCancelOrderCompletionBeforeSendingNextMessage() {
    InstantActions action1 = new InstantActions();
    action1.setHeaderId(1L);
    action1.setActions(List.of(new Action(CancelOrder.ACTION_TYPE, "action1", BlockingType.HARD)));
    InstantActions action2 = new InstantActions();
    action2.setHeaderId(2L);
    action2.setActions(List.of(new Action(CancelOrder.ACTION_TYPE, "action2", BlockingType.HARD)));
    InstantActions action3 = new InstantActions();
    action3.setHeaderId(3L);
    action3.setActions(List.of(new Action(CancelOrder.ACTION_TYPE, "action3", BlockingType.HARD)));

    messageResponseMatcher.enqueueAction(action1);
    messageResponseMatcher.enqueueAction(action2);
    messageResponseMatcher.enqueueAction(action3);

    verify(sendInstantActionsCallback, times(1)).accept(action1);
    verify(sendInstantActionsCallback, never()).accept(action2);
    verify(sendInstantActionsCallback, never()).accept(action3);

    messageResponseMatcher.onStateMessage(newState());

    // The cancelOrder is repeated if the vehicle does not reflect it as accepted in its state.
    verify(sendInstantActionsCallback, times(2)).accept(action1);
    verify(sendInstantActionsCallback, never()).accept(action2);
    verify(sendInstantActionsCallback, never()).accept(action3);

    messageResponseMatcher.onStateMessage(stateAcceptingInstantAction(action1));

    // The cancelOrder is not repeated once the vehicle reflects it in its state, even though it
    // has not completed it yet. The next request stays blocked until the cancelOrder completes.
    verify(sendInstantActionsCallback, times(2)).accept(action1);
    verify(sendInstantActionsCallback, never()).accept(action2);
    verify(sendInstantActionsCallback, never()).accept(action3);

    messageResponseMatcher.onStateMessage(stateCompletingInstantAction(action1));

    verify(sendInstantActionsCallback, times(2)).accept(action1);
    verify(sendInstantActionsCallback, times(1)).accept(action2);
    verify(sendInstantActionsCallback, never()).accept(action3);

    messageResponseMatcher.onStateMessage(stateCompletingInstantAction(action2));

    verify(sendInstantActionsCallback, times(2)).accept(action1);
    verify(sendInstantActionsCallback, times(1)).accept(action2);
    verify(sendInstantActionsCallback, times(1)).accept(action3);
  }

  @Test
  public void stopResendingCancelOrderWhenVehicleReportsItRunning() {
    InstantActions cancelAction = new InstantActions();
    cancelAction.setHeaderId(1L);
    cancelAction.setActions(
        List.of(new Action(CancelOrder.ACTION_TYPE, "cancel1", BlockingType.HARD))
    );
    InstantActions nextAction = new InstantActions();
    nextAction.setHeaderId(2L);
    nextAction.setActions(
        List.of(new Action(CancelOrder.ACTION_TYPE, "cancel2", BlockingType.HARD))
    );

    messageResponseMatcher.enqueueAction(cancelAction);
    messageResponseMatcher.enqueueAction(nextAction);

    verify(sendInstantActionsCallback, times(1)).accept(cancelAction);
    verify(sendInstantActionsCallback, never()).accept(nextAction);

    // While the vehicle has not yet reflected the cancelOrder, it is resent.
    messageResponseMatcher.onStateMessage(newState());
    verify(sendInstantActionsCallback, times(2)).accept(cancelAction);

    // Once the vehicle reports the cancelOrder as RUNNING, it must not be resent anymore, and the
    // next request stays blocked.
    messageResponseMatcher.onStateMessage(
        stateWithActionStatus(cancelAction, ActionStatus.RUNNING)
    );
    verify(sendInstantActionsCallback, times(2)).accept(cancelAction);
    verify(sendInstantActionsCallback, never()).accept(nextAction);

    // Further RUNNING reports still do not trigger a resend.
    messageResponseMatcher.onStateMessage(
        stateWithActionStatus(cancelAction, ActionStatus.RUNNING)
    );
    verify(sendInstantActionsCallback, times(2)).accept(cancelAction);
    verify(sendInstantActionsCallback, never()).accept(nextAction);

    // Only when the cancelOrder completes does the matcher advance to the next request.
    messageResponseMatcher.onStateMessage(stateCompletingInstantAction(cancelAction));
    verify(sendInstantActionsCallback, times(2)).accept(cancelAction);
    verify(sendInstantActionsCallback, times(1)).accept(nextAction);
  }

  @ParameterizedTest
  @EnumSource(value = OperatingMode.class, names = {"TEACHIN", "MANUAL", "SERVICE"})
  public void shouldNotSendWhenNotInAutomaticModes(OperatingMode mode) {
    messageResponseMatcher.onStateMessage(stateWithOperatingMode(mode));

    Order order1 = new Order("order1", 0L, List.of(), List.of());
    messageResponseMatcher.enqueueCommand(order1, dummyCommand);

    verify(sendOrderCallback, never()).accept(any());
  }

  @ParameterizedTest
  @EnumSource(value = OperatingMode.class, names = {"AUTOMATIC", "SEMIAUTOMATIC"})
  public void shouldSendWhenInAutomaticModes(OperatingMode mode) {
    messageResponseMatcher.onStateMessage(stateWithOperatingMode(mode));

    Order order1 = new Order("order1", 0L, List.of(), List.of());
    messageResponseMatcher.enqueueCommand(order1, dummyCommand);

    verify(sendOrderCallback, times(1)).accept(order1);
  }

  @Test
  public void sendOrderWhenOperatingModeChanges() {
    messageResponseMatcher.onStateMessage(stateWithOperatingMode(OperatingMode.MANUAL));

    Order order1 = new Order("order1", 0L, List.of(), List.of());
    messageResponseMatcher.enqueueCommand(order1, dummyCommand);

    verify(sendOrderCallback, never()).accept(any());

    messageResponseMatcher.onStateMessage(stateWithOperatingMode(OperatingMode.AUTOMATIC));
    verify(sendOrderCallback, times(1)).accept(order1);
  }

  private State stateWithOperatingMode(OperatingMode mode) {
    State state = newState();
    state.setOperatingMode(mode);
    return state;
  }

  private State stateAcceptingOrder(Order order) {
    State state = newState();
    state.setOrderId(order.getOrderId());
    state.setOrderUpdateId(order.getOrderUpdateId());
    return state;
  }

  private State stateAcceptingInstantAction(InstantActions actions) {
    State state = newState();
    state.getActionStates().addAll(
        actions.getActions().stream()
            .map(
                action -> new ActionState(
                    action.getActionId(),
                    ActionStatus.WAITING
                )
                    .setActionType(action.getActionType())
            )
            .toList()
    );
    return state;
  }

  private State stateCompletingInstantAction(
      InstantActions actions
  ) {
    State state = newState();
    state.getActionStates().addAll(
        actions.getActions().stream()
            .map(
                action -> new ActionState(action.getActionId(), ActionStatus.FINISHED)
                    .setActionType(action.getActionType())
            )
            .toList()
    );
    return state;
  }

  private State stateWithActionStatus(
      InstantActions actions,
      ActionStatus status
  ) {
    State state = newState();
    state.getActionStates().addAll(
        actions.getActions().stream()
            .map(
                action -> new ActionState(action.getActionId(), status)
                    .setActionType(action.getActionType())
            )
            .toList()
    );
    return state;
  }

  private State newState() {
    return new State(
        "",
        0L,
        "",
        0L,
        new ArrayList<>(),
        new ArrayList<>(),
        false,
        new ArrayList<>(),
        new BatteryState(100.0, false),
        OperatingMode.AUTOMATIC,
        new ArrayList<>(),
        new SafetyState(EStop.NONE, false)
    );
  }
}
