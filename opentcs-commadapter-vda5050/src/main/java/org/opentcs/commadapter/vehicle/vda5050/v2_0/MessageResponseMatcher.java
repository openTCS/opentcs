// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v2_0;

import static java.util.Objects.requireNonNull;

import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Queue;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.action.CancelOrder;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.Action;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.instantactions.InstantActions;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.order.Order;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.ActionStatus;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.OperatingMode;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.State;
import org.opentcs.drivers.vehicle.MovementCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Matches a state messages with sent order messages to confirm their delivery.
 */
public class MessageResponseMatcher {

  private static final Logger LOG = LoggerFactory.getLogger(MessageResponseMatcher.class);
  /**
   * The comm adapter.
   */
  private final String commAdapterName;
  /**
   * Queue for requests that need to be sent to the vehicle.
   */
  private final Queue<Object> requests = new ArrayDeque<>();
  /**
   * The callback for sending the next order.
   */
  private final Consumer<Order> sendOrderCallback;
  /**
   * The callback for sending the next instant actions.
   */
  private final Consumer<InstantActions> sendInstantActionsCallback;
  /**
   * The callback for when an order is accepted by the vehicle.
   */
  private final Consumer<OrderAssociation> orderAcceptedCallback;
  /**
   * The maximum number of consecutive state messages that indicate a rejection of the current
   * order/message before we consider the rejection to be permanent and stop retrying.
   */
  private final int maxIgnoredRejectionsCount;
  /**
   * The number of consecutive state messages that indicate a rejection of the current order/message
   * we have received so far.
   */
  private int consecutiveRejectionsCount;
  /**
   * Flag indicating whether this comm adapter may currently send requests to the vehicle.
   * If false, all enqueued requests will stay in the queue until the flag becomes true.
   */
  private boolean sendingAllowed;

  /**
   * Creates a new OrderResponseMatcher.
   *
   * @param commAdapterName The name of the comm adapter
   * @param sendOrderCallback The callback for sending the next order.
   * @param sendInstantActionsCallback The callback for sending instant actions.
   * @param orderAcceptedCallback The callback for when the order is accepted by the vehicle.
   * @param maxIgnoredRejectionsCount The maximum number of consecutive state messages that
   * indicate a rejection of the current order/message before we consider the rejection to be
   * permanent and stop retrying.
   */
  public MessageResponseMatcher(
      @Nonnull
      String commAdapterName,
      @Nonnull
      Consumer<Order> sendOrderCallback,
      @Nonnull
      Consumer<InstantActions> sendInstantActionsCallback,
      @Nonnull
      Consumer<OrderAssociation> orderAcceptedCallback,
      int maxIgnoredRejectionsCount
  ) {
    this.commAdapterName = requireNonNull(commAdapterName, "commAdapterName");
    this.sendOrderCallback = requireNonNull(sendOrderCallback, "sendOrderCallback");
    this.sendInstantActionsCallback
        = requireNonNull(sendInstantActionsCallback, "sendInstantActionsCallback");
    this.orderAcceptedCallback = requireNonNull(orderAcceptedCallback, "orderAcceptedCallback");
    this.maxIgnoredRejectionsCount = maxIgnoredRejectionsCount;
  }

  public void enqueueCommand(Order order, MovementCommand command) {
    LOG.debug("{}: Enqueuing order: {}", commAdapterName, order);
    enqueueRequest(new OrderAssociation(order, command));
  }

  public void enqueueAction(InstantActions action) {
    LOG.debug("{}: Enqueuing instant action: {}", commAdapterName, action);
    enqueueRequest(action);
  }

  private void enqueueRequest(Object request) {
    requests.add(request);

    if (requests.size() > 1) {
      LOG.debug(
          "{}: Not sending enqueued request yet, due to unacknowledged previous request.",
          commAdapterName
      );
      return;
    }

    sendNextOrder();
  }

  /**
   * Clears all orders for which the {@link MessageResponseMatcher} is waiting for acknowledgement
   * from the vehicle.
   */
  public void clear() {
    requests.clear();
    consecutiveRejectionsCount = 0;
  }

  public void onStateMessage(
      @Nonnull
      State state
  ) {
    requireNonNull(state, "state");

    sendingAllowed = state.getOperatingMode() == OperatingMode.AUTOMATIC
        || state.getOperatingMode() == OperatingMode.SEMIAUTOMATIC;

    Object currentRequest = requests.peek();
    if (currentRequest == null) {
      return;
    }

    boolean rejected = updateRejectionState(state);
    boolean accepted = !rejected && requestAccepted(currentRequest, state);
    boolean complete = accepted && requestComplete(currentRequest, state);

    if (rejected) {
      // Don't do anything - the vehicle cannot continue processing the drive order. We will wait
      // for this to be resolved via order withdrawal and a new initial order message.
      LOG.debug(
          "{}: Vehicle rejected request. Waiting for resolution: {}",
          commAdapterName,
          currentRequest
      );
    }
    else if (complete) {
      requests.poll();
      if (currentRequest instanceof OrderAssociation orderAssociation) {
        LOG.debug("{}: Vehicle acknowledged order: {}", commAdapterName, orderAssociation);
        orderAcceptedCallback.accept(orderAssociation);
      }
      else if (currentRequest instanceof InstantActions actions) {
        LOG.debug("{}: Vehicle acknowledged instant actions: {}", commAdapterName, actions);
      }
      // Send the next order, if any.
      sendNextOrder();
    }
    else if (accepted) {
      // The vehicle reflects the request in its state but has not completed it yet (e.g. a
      // cancelOrder that is still being processed). We do not resend the request, but keep it at
      // the head of the queue so that subsequent requests remain blocked until it completes.
      LOG.debug(
          "{}: Request accepted but not yet completed. Waiting without resending: {}",
          commAdapterName,
          currentRequest
      );
    }
    else {
      // The vehicle neither rejected nor accepted the current request - resend it.
      sendNextOrder();
    }
  }

  private boolean updateRejectionState(State state) {
    if (StateMappings.vehicleRejectsOrder(state)) {
      consecutiveRejectionsCount++;
    }
    else {
      consecutiveRejectionsCount = 0;
    }

    return consecutiveRejectionsCount > maxIgnoredRejectionsCount;
  }

  private boolean requestAccepted(Object request, State state) {
    if (request instanceof OrderAssociation orderAssociation) {
      return orderAccepted(orderAssociation.getOrder(), state);
    }
    else if (request instanceof InstantActions actions) {
      return instantActionsAccepted(actions, state);
    }
    else {
      LOG.warn(
          "{}: Unrecognized request of type {}.",
          commAdapterName,
          request.getClass().getName()
      );
      return false;
    }
  }

  private boolean requestComplete(Object request, State state) {
    if (request instanceof OrderAssociation orderAssociation) {
      return orderAccepted(orderAssociation.getOrder(), state);
    }
    else if (request instanceof InstantActions actions) {
      return instantActionsCompleted(actions, state);
    }
    else {
      LOG.warn(
          "{}: Unrecognized request of type {}.",
          commAdapterName,
          request.getClass().getName()
      );
      return false;
    }
  }

  /**
   * Send the first request in the queue to the vehicle.
   */
  private void sendNextOrder() {
    if (!sendingAllowed) {
      LOG.debug("{}: Cannot send next order. Sending is currently disallowed", commAdapterName);
      return;
    }

    if (requests.isEmpty()) {
      LOG.debug("{}: Cannot send next order. No request to send", commAdapterName);
      return;
    }

    Object request = requests.peek();
    LOG.debug("{}: Sending order to comm adapter: {}", commAdapterName, request);
    if (request instanceof OrderAssociation) {
      sendOrderCallback.accept(((OrderAssociation) request).getOrder());
    }
    else if (request instanceof InstantActions) {
      sendInstantActionsCallback.accept((InstantActions) request);
    }
    else {
      LOG.warn(
          "{}: Cannot send request. Unrecognized request of type {}.",
          commAdapterName,
          request.getClass().getName()
      );
    }
  }

  private boolean orderAccepted(Order order, State state) {
    return Objects.equals(state.getOrderId(), order.getOrderId())
        && Objects.equals(state.getOrderUpdateId(), order.getOrderUpdateId());
  }

  private boolean instantActionsCompleted(InstantActions instantAction, State state) {
    return instantAction.getActions().stream()
        .allMatch(action -> {
          // In case of a cancelOrder action, we actually wait for the vehicle to accept AND
          // COMPLETE the action. Not doing this can lead to situations in which we send another
          // order while the vehicle is still processing the cancelOrder, and the vehicle then
          // immediately cancelling that new order.
          if (Objects.equals(action.getActionType(), CancelOrder.ACTION_TYPE)) {
            return cancelOrderAcceptedAndCompleted(action, state);
          }
          else {
            return actionAccepted(action, state);
          }
        });
  }

  /**
   * Checks whether the vehicle reflects all actions of the given instant actions message in its
   * state, regardless of their (possibly non-terminal) action status.
   *
   * @param instantAction The instant actions message.
   * @param state The vehicle's state.
   * @return {@code true} if every action is present in the vehicle's {@code actionStates}.
   */
  private boolean instantActionsAccepted(InstantActions instantAction, State state) {
    return instantAction.getActions().stream()
        .allMatch(action -> actionAccepted(action, state));
  }

  private boolean actionAccepted(Action action, State state) {
    return state.getActionStates().stream()
        .anyMatch(actionState -> actionState.getActionId().equals(action.getActionId()));
  }

  private boolean cancelOrderAcceptedAndCompleted(Action action, State state) {
    return state.getActionStates().stream()
        .filter(actionState -> actionState.getActionId().equals(action.getActionId()))
        .anyMatch(
            actionState -> actionState.getActionStatus() == ActionStatus.FINISHED
                || actionState.getActionStatus() == ActionStatus.FAILED
        );
  }
}
