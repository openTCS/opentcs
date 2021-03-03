/*
 * openTCS copyright information:
 * Copyright (c) 2008 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.xmlstatus;

import com.google.inject.BindingAnnotation;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import static java.util.Objects.requireNonNull;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import org.opentcs.access.Kernel;
import org.opentcs.access.LocalKernel;
import org.opentcs.access.xmlstatus.OrderStatusMessage;
import org.opentcs.access.xmlstatus.StatusMessage;
import org.opentcs.access.xmlstatus.TCSStatusMessageSet;
import org.opentcs.access.xmlstatus.VehicleStatusMessage;
import org.opentcs.algorithms.KernelExtension;
import org.opentcs.data.TCSObjectEvent;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.util.eventsystem.AcceptingTCSEventFilter;
import org.opentcs.util.eventsystem.EventListener;
import org.opentcs.util.eventsystem.EventSource;
import org.opentcs.util.eventsystem.TCSEvent;

/**
 * An instance of this class accepts TCP connections from clients that wish to
 * be informed of generic status changes. XML messages are sent to all clients
 * connected whenever something interesting happens. Interesting events can be:
 * <ul>
 * <li>Changes of a transport order's state.</li>
 * <li>Changes of a vehicle's state.</li>
 * </ul>
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class StatusMessageDispatcher
    implements KernelExtension {

  /**
   * This class's Logger.
   */
  private static final Logger log
      = Logger.getLogger(StatusMessageDispatcher.class.getName());
  /**
   * The port on which to listen for connections.
   */
  private final int listenPort;
  /**
   * A string indicating the end of a status message/separating status messages
   * in the stream.
   */
  private final String messageSeparator;
  /**
   * The local kernel.
   */
  private final Kernel localKernel;
  /**
   * This dispatcher's listener task.
   */
  private ConnectionListener connectionListener;
  /**
   * This dispatcher's <em>enabled</em> flag.
   */
  private volatile boolean enabled;

  /**
   * Creates a new StatusMessageDispatcher.
   *
   * @param kernel The local kernel.
   * @param listenPort The port on which to listen for connections.
   * @param messageSeparator A string indicating the end of a status
   * message/separating status messages in the stream.
   */
  @Inject
  public StatusMessageDispatcher(LocalKernel kernel,
                                 @ListenPort int listenPort,
                                 @MessageSeparator String messageSeparator) {
    this.localKernel = requireNonNull(kernel, "kernel");
    this.listenPort = listenPort;
    this.messageSeparator = requireNonNull(messageSeparator, "messageSeparator");
  }

  @Override
  public boolean isPluggedIn() {
    return enabled;
  }

  @Override
  public void plugIn() {
    if (enabled) {
      return;
    }
    connectionListener = new ConnectionListener();
    Thread connectionListenerThread
        = new Thread(connectionListener, "statusMessageListenerThread");
    connectionListenerThread.start();
    enabled = true;
    log.fine("StatusMessageDispatcher enabled");
  }

  @Override
  public void plugOut() {
    if (!enabled) {
      return;
    }
    connectionListener.terminate();
    connectionListener = null;
  }

  /**
   * Annotation type for injecting the port to listen on.
   */
  @BindingAnnotation
  @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  static @interface ListenPort {
    // Nothing here.
  }

  /**
   * Annotation type for injecting the message separator.
   */
  @BindingAnnotation
  @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  static @interface MessageSeparator {
    // Nothing here.
  }

  /**
   * The task listening for new client connections.
   */
  private final class ConnectionListener
      implements Runnable {

    /**
     * This listener's server socket.
     */
    private volatile ServerSocket serverSocket;
    /**
     * This task's termination flag.
     */
    private volatile boolean terminated;
    /**
     * A set of connection handlers that have been created.
     */
    private final Set<ConnectionHandler> runningHandlers = new HashSet<>();

    /**
     * Creates a new ConnectionListener.
     */
    private ConnectionListener() {
    }

    /**
     * Flags this task for termination.
     */
    private void terminate() {
      terminated = true;
      if (serverSocket != null && !serverSocket.isClosed()) {
        try {
          serverSocket.close();
        }
        catch (IOException exc) {
          log.log(Level.WARNING, "Exception closing server socket", exc);
        }
      }
    }

    @Override
    public void run() {
      ExecutorService clientExecutor = Executors.newCachedThreadPool();
      // Set up a server socket and wait for connections to handle.
      try {
        serverSocket = new ServerSocket(listenPort);
        terminated = false;
        while (!terminated) {
          Socket clientSocket = serverSocket.accept();
          log.fine("Connection from "
              + clientSocket.getInetAddress().getHostAddress() + ":"
              + clientSocket.getPort());
          ConnectionHandler newHandler = new ConnectionHandler(clientSocket,
                                                               localKernel);
          localKernel.addEventListener(newHandler,
                                       new AcceptingTCSEventFilter());
          clientExecutor.execute(newHandler);
          runningHandlers.add(newHandler);
          // Forget any handlers that have terminated since the last run.
          Iterator<ConnectionHandler> iter = runningHandlers.iterator();
          while (iter.hasNext()) {
            ConnectionHandler curHandler = iter.next();
            if (curHandler.isTerminated()) {
              iter.remove();
            }
          }
        }
      }
      catch (SocketException exc) {
        // Check if we're supposed to terminate.
        if (terminated) {
          log.fine("Received termination signal.");
        }
        else {
          log.log(Level.WARNING,
                  "SocketException without termination flag set",
                  exc);
        }
      }
      catch (IOException exc) {
        log.log(Level.WARNING, "IOException handling server socket", exc);
      }
      finally {
        clientExecutor.shutdown();
        if (serverSocket != null && !serverSocket.isClosed()) {
          try {
            serverSocket.close();
          }
          catch (IOException exc) {
            log.log(Level.SEVERE, "Couldn't close server socket", exc);
          }
        }
        // Terminate all handlers that may still be running.
        for (ConnectionHandler handler : runningHandlers) {
          localKernel.removeEventListener(handler);
          handler.terminate();
        }
      }
      log.fine("Terminated connection listener.");
    }
  }

  /**
   * The task handling client connections.
   */
  private final class ConnectionHandler
      implements Runnable, EventListener<TCSEvent> {

    /**
     * The connection to the client.
     */
    private final Socket socket;
    /**
     * The source of status events.
     */
    private final EventSource<TCSEvent> eventSource;
    /**
     * The queue for incoming events to be processed.
     */
    private final Queue<TCSEvent> eventQueue = new LinkedList<>();
    /**
     * This connectio handler's <em>terminated</em> flag.
     */
    private volatile boolean terminated;

    /**
     * Creates a new ConnectionHandler.
     *
     * @param clientSocket The socket for communication with the client.
     * @param evtSource The source of the status events with which the handler
     * is supposed to register.
     */
    private ConnectionHandler(Socket clientSocket,
                              EventSource<TCSEvent> evtSource) {
      socket = requireNonNull(clientSocket, "clientSocket");
      eventSource = requireNonNull(evtSource, "evtSource");
      if (!clientSocket.isConnected()) {
        throw new IllegalArgumentException("clientSocket is not connected");
      }
    }

    /**
     * Terminates this listener.
     */
    private void terminate() {
      terminated = true;
      synchronized (eventQueue) {
        eventQueue.notify();
      }
    }

    /**
     * Returns whether this listener has terminated.
     *
     * @return True if yes, false if not.
     */
    private boolean isTerminated() {
      return terminated;
    }

    /**
     * Adds an event to this handler's queue.
     *
     * @param event The event to be processed.
     */
    @Override
    public void processEvent(TCSEvent event) {
      if (event == null) {
        throw new NullPointerException("event is null");
      }
      synchronized (eventQueue) {
        eventQueue.add(event);
        eventQueue.notify();
      }
    }

    @Override
    public void run() {
      try {
        OutputStream outStream = socket.getOutputStream();
        while (!terminated) {
          TCSObjectEvent event = getNextEventFromQueue();
          if (!terminated && event != null) {
            Class<?> eventObjectClass
                = event.getCurrentOrPreviousObjectState().getClass();
            TCSStatusMessageSet messageSet = new TCSStatusMessageSet();
            StatusMessage message = null;
            if (eventObjectClass.equals(TransportOrder.class)) {
              TransportOrder order
                  = (TransportOrder) event.getCurrentOrPreviousObjectState();
              OrderStatusMessage orderMessage = new OrderStatusMessage();
              orderMessage.setOrderName(order.getName());
              orderMessage.setOrderState(order.getState());
              for (DriveOrder curDriveOrder : order.getAllDriveOrders()) {
                OrderStatusMessage.Destination dest
                    = new OrderStatusMessage.Destination();
                dest.setLocationName(
                    curDriveOrder.getDestination().getLocation().getName());
                dest.setOperation(curDriveOrder.getDestination().getOperation());
                dest.setState(curDriveOrder.getState());
                orderMessage.getDestinations().add(dest);
                message = orderMessage;
              }
            }
            else if (eventObjectClass.equals(Vehicle.class)) {
              Vehicle vehicle
                  = (Vehicle) event.getCurrentOrPreviousObjectState();
              VehicleStatusMessage vehicleMessage = new VehicleStatusMessage();
              // Set vehicle name
              vehicleMessage.setVehicleName(vehicle.getName());
              // Set position
              TCSObjectReference<Point> posRef = vehicle.getCurrentPosition();
              if (posRef != null) {
                vehicleMessage.setPosition(posRef.getName());
              }
              // Set vehicle state
              Vehicle.State state = vehicle.getState();
              vehicleMessage.setState(state);
              // Set vehciel processing state
              Vehicle.ProcState procState = vehicle.getProcState();
              vehicleMessage.setProcState(procState);
              // Set presice position
              Triple precisePos = vehicle.getPrecisePosition();
              if (precisePos != null) {
                VehicleStatusMessage.PrecisePosition precisePosElement;
                precisePosElement = new VehicleStatusMessage.PrecisePosition(
                    precisePos.getX(), precisePos.getY(), precisePos.getZ());
                vehicleMessage.setPrecisePosition(precisePosElement);
              }
              message = vehicleMessage;
            }
            if (message != null) {
              messageSet.getStatusMessages().add(message);
              outStream.write(messageSet.toXml().getBytes());
              outStream.write(messageSeparator.getBytes());
              outStream.flush();
            }
          }
        }
        log.fine("Terminating connection handler");
      }
      catch (IOException exc) {
        log.log(Level.WARNING, "Exception terminates connection handler", exc);
        terminated = true;
      }
      finally {
        cleanup();
      }
    }

    /**
     * Returns the next event from the queue.
     *
     * @return the next event from the queue, or <code>null</code>, if the
     * connection handler has been terminated while waiting for an event to
     * arrive.
     */
    private TCSObjectEvent getNextEventFromQueue() {
      TCSObjectEvent result;
      // Get the next event from the queue.
      synchronized (eventQueue) {
        // Wait until there's something in the queue or we're terminated.
        while (!terminated && eventQueue.isEmpty()) {
          try {
            eventQueue.wait();
          }
          catch (InterruptedException exc) {
            log.log(Level.WARNING, "Unexpectedly interrupted, ignoring", exc);
          }
        }
        TCSEvent event = eventQueue.poll();
        // XXX This is necessary until we have changed the caller of this method
        // to handle TCSEvents in general.
        if (event instanceof TCSObjectEvent) {
          result = (TCSObjectEvent) event;
        }
        else {
          result = null;
        }
      }
      return result;
    }

    /**
     * Cleanup, free resources etc. when the connection handler has been
     * terminated.
     */
    private void cleanup() {
      if (!socket.isClosed()) {
        try {
          socket.close();
        }
        catch (IOException exc) {
          log.log(Level.WARNING, "Exception closing socket, ignored", exc);
        }
      }
      log.fine("Unregistering from event source");
      eventSource.removeEventListener(this);
    }
  }
}
