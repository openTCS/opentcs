/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.xmlstatus;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import static java.util.Objects.requireNonNull;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectEvent;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.kernel.xmlstatus.binding.OrderStatusMessage;
import org.opentcs.kernel.xmlstatus.binding.StatusMessage;
import org.opentcs.kernel.xmlstatus.binding.TCSStatusMessageSet;
import org.opentcs.kernel.xmlstatus.binding.VehicleStatusMessage;
import org.opentcs.util.Assertions;
import org.opentcs.util.eventsystem.EventListener;
import org.opentcs.util.eventsystem.EventSource;
import org.opentcs.util.eventsystem.TCSEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The task handling client connections.
 */
class ConnectionHandler
    implements Runnable,
               EventListener<TCSEvent> {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ConnectionHandler.class);
  /**
   * The connection to the client.
   */
  private final Socket socket;
  /**
   * The source of status events.
   */
  private final EventSource<TCSEvent> eventSource;
  /**
   * A string indicating the end of a status message/separating status messages
   * in the stream.
   */
  private final String messageSeparator;
  /**
   * Commands to be processed.
   */
  private final BlockingQueue<ConnectionCommand> commands = new PriorityBlockingQueue<>();
  /**
   * This connection handler's <em>terminated</em> flag.
   */
  private volatile boolean terminated;

  /**
   * Creates a new ConnectionHandler.
   *
   * @param clientSocket The socket for communication with the client.
   * @param evtSource The source of the status events with which the handler
   * is supposed to register.
   */
  ConnectionHandler(Socket clientSocket,
                    EventSource<TCSEvent> evtSource,
                    String messageSeparator) {
    this.socket = requireNonNull(clientSocket, "clientSocket");
    this.eventSource = requireNonNull(evtSource, "evtSource");
    Assertions.checkArgument(clientSocket.isConnected(), "clientSocket is not connected");
    this.messageSeparator = requireNonNull(messageSeparator, "messageSeparator");
  }

  /**
   * Adds an event to this handler's queue.
   *
   * @param event The event to be processed.
   */
  @Override
  public void processEvent(TCSEvent event) {
    requireNonNull(event, "event");
    if (event instanceof TCSObjectEvent) {
      commands.offer(new ConnectionCommand.ProcessObjectEvent((TCSObjectEvent) event));
    }
  }

  @Override
  public void run() {
    try (final OutputStream outStream = socket.getOutputStream()) {
      while (!terminated) {
        consume(commands.take(), outStream);
      }
      LOG.debug("Terminating connection handler.");
    }
    catch (IOException | InterruptedException exc) {
      LOG.warn("Exception terminates connection handler.", exc);
      terminated = true;
    }
    finally {
      LOG.debug("Unregistering from event source");
      eventSource.removeEventListener(this);
    }
  }

  /**
   * Terminates this listener.
   */
  public void terminate() {
    commands.offer(new ConnectionCommand.PoisonPill());
  }

  /**
   * Returns whether this listener has terminated.
   *
   * @return True if yes, false if not.
   */
  public boolean isTerminated() {
    return terminated;
  }

  private void consume(ConnectionCommand command, OutputStream outStream)
      throws IOException {
    if (command instanceof ConnectionCommand.PoisonPill) {
      terminated = true;
    }
    else if (command instanceof ConnectionCommand.ProcessObjectEvent) {
      processObjectEvent(((ConnectionCommand.ProcessObjectEvent) command).getEvent(), outStream);
    }
  }

  private void processObjectEvent(TCSObjectEvent event, OutputStream outStream)
      throws IOException {
    TCSObject<?> eventObject = event.getCurrentOrPreviousObjectState();
    if (eventObject instanceof TransportOrder) {
      sendMessage(OrderStatusMessage.fromTransportOrder((TransportOrder) eventObject), outStream);
    }
    else if (eventObject instanceof Vehicle) {
      sendMessage(VehicleStatusMessage.fromVehicle((Vehicle) eventObject), outStream);
    }
  }

  private void sendMessage(StatusMessage message, OutputStream outStream)
      throws IOException {
    TCSStatusMessageSet messageSet = new TCSStatusMessageSet();
    messageSet.getStatusMessages().add(message);
    outStream.write(messageSet.toXml().getBytes());
    outStream.write(messageSeparator.getBytes());
    outStream.flush();
  }
}
