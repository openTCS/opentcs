/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.xmlorders;

import com.google.inject.BindingAnnotation;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import org.opentcs.access.CredentialsException;
import org.opentcs.access.Kernel;
import org.opentcs.access.LocalKernel;
import org.opentcs.access.to.order.DestinationCreationTO;
import org.opentcs.access.to.order.TransportOrderCreationTO;
import org.opentcs.components.kernel.KernelExtension;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.kernel.xmlorders.binding.Destination;
import org.opentcs.kernel.xmlorders.binding.ScriptResponse;
import org.opentcs.kernel.xmlorders.binding.TCSOrder;
import org.opentcs.kernel.xmlorders.binding.TCSOrderSet;
import org.opentcs.kernel.xmlorders.binding.TCSResponse;
import org.opentcs.kernel.xmlorders.binding.TCSResponseSet;
import org.opentcs.kernel.xmlorders.binding.TCSScriptFile;
import org.opentcs.kernel.xmlorders.binding.Transport;
import org.opentcs.kernel.xmlorders.binding.TransportResponse;
import org.opentcs.kernel.xmlorders.binding.TransportScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Instances of this class accept general orders for the openTCS system encoded
 * in XML via TCP connections.
 * <p>
 * Setting up the server socket is done in a separate thread that is started
 * when {@link #initialize() initialize()} is called and stopped when
 * {@link #terminate() terminate()} is called. Incoming client connections will be
 * handled concurrently in separate threads.
 * </p>
 *
 * <p>
 * Input data is read from the socket until the client shuts down its output
 * stream. The input is then parsed, actions like the creation of transport
 * orders are performed and a response sent back to the client via the same
 * connection.
 * </p>
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class XMLTelegramOrderReceiver
    implements KernelExtension {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(XMLTelegramOrderReceiver.class);
  /**
   * The sequence of bytes marking the end of an incoming telegram.
   */
  private static final String END_OF_TELEGRAM = "\r\n\r\n";
  /**
   * The local kernel that this <code>OrderReceiver</code> receives transport
   * order requests for.
   */
  private final Kernel localKernel;
  /**
   * The port on which to listen for connections.
   */
  private final int listenPort;
  /**
   * The time in ms for which the socket may wait for input from the client
   * before aborting and closing the connection.
   */
  private final int inputTimeout;
  /**
   * The maximum length of input data (in bytes) read from sockets. If the
   * client sends more, the connection is closed immediately.
   */
  private final int maxInputLength;
  /**
   * A script file manager to help us with script files.
   */
  private final ScriptFileManager scriptFileManager;
  /**
   * This receiver's <em>enabled</em> flag.
   */
  private boolean enabled;
  /**
   * The listener task.
   */
  private ConnectionListener connectionListener;
  /**
   * The thread in which the listener task is running.
   */
  private Thread connectionListenerThread;

  /**
   * Creates a new instance.
   *
   * @param kernel The local kernel this instance receives transport orders for.
   * @param scriptFileManager The manager for script files to be used.
   * @param listenPort The port on which to listen for connections.
   * @param inputTimeout The time in ms for which the socket may wait for input
   * from the client before aborting and closing the connection.
   * @param maxInputLength The maximum length of input data (in bytes) read from
   * sockets. If the client sends more, the connection is closed immediately.
   */
  @Inject
  public XMLTelegramOrderReceiver(LocalKernel kernel,
                                  ScriptFileManager scriptFileManager,
                                  @ListenPort int listenPort,
                                  @InputTimeout int inputTimeout,
                                  @MaxInputLength int maxInputLength) {
    this.localKernel = requireNonNull(kernel, "kernel");
    this.scriptFileManager = requireNonNull(scriptFileManager, "scriptFileManager");
    this.listenPort = listenPort;
    this.inputTimeout = inputTimeout;
    this.maxInputLength = maxInputLength;
  }

  @Override
  public boolean isInitialized() {
    return enabled;
  }

  @Override
  public void initialize() {
    // Only react if the state really changes.
    if (enabled) {
      return;
    }
    connectionListener = new ConnectionListener();
    connectionListenerThread = new Thread(connectionListener, "xmlOrderListenerThread");
    connectionListenerThread.start();
    enabled = true;
    LOG.debug("XMLTelegramOrderReceiver initialized");
  }

  @Override
  public void terminate() {
    // Only react if the state really changes.
    if (!enabled) {
      return;
    }
    LOG.info("Terminating connection listener...");
    connectionListener.terminate();
    try {
      connectionListenerThread.join();
      LOG.info("Connection listener thread has terminated.");
    }
    catch (InterruptedException exc) {
      LOG.warn("Interrupted while waiting for connection listener to die.");
    }
    finally {
      connectionListenerThread = null;
      connectionListener = null;
      enabled = false;
    }
  }

  /**
   * Returns the number of the port on which this receiver listens for client
   * connections.
   *
   * @return The number of the port on which this receiver listens for client
   * connections.
   */
  public int getListenPort() {
    return listenPort;
  }

  /**
   * Annotation type for injecting the port to listen on.
   */
  @BindingAnnotation
  @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  public static @interface ListenPort {
    // Nothing here.
  }

  /**
   * Annotation type for injecting the input timeout.
   */
  @BindingAnnotation
  @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  public static @interface InputTimeout {
    // Nothing here.
  }

  /**
   * Annotation type for injecting the input timeout.
   */
  @BindingAnnotation
  @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  public static @interface MaxInputLength {
    // Nothing here.
  }

  /**
   * The task listening for new client connections.
   */
  private class ConnectionListener
      implements Runnable {

    /**
     * The socket on which this listener listens for client connections.
     */
    private ServerSocket serverSocket;
    /**
     * This listener's termation flag.
     */
    private volatile boolean terminated;
    /**
     * An executor for managing the ConnectionHandler tasks that process client
     * connections.
     */
    private final Executor clientExecutor = Executors.newCachedThreadPool();

    /**
     * Creates a new ConnectionListener.
     */
    public ConnectionListener() {
      // Do nada.
    }

    /**
     * Signals this listener's working thread it is supposed to terminate.
     */
    public void terminate() {
      terminated = true;
      try {
        if (serverSocket != null && !serverSocket.isClosed()) {
          serverSocket.close();
        }
      }
      catch (IOException exc) {
        LOG.warn("IOException closing server socket", exc);
      }
      finally {
        // Make sure we dispose of the used server socket.
        serverSocket = null;
      }
    }

    @Override
    public void run() {
      // Set up the listening socket.
      try {
        serverSocket = new ServerSocket(listenPort);
      }
      catch (IOException exc) {
        throw new IllegalStateException(
            "IOException trying to create server socket", exc);
      }
      // Accept new connections until terminated from outside.
      while (!terminated) {
        try {
          Socket clientSocket = serverSocket.accept();
          LOG.info("Connection from {}:{}",
                   clientSocket.getInetAddress().getHostAddress(),
                   clientSocket.getPort());
          clientExecutor.execute(new ConnectionHandler(clientSocket));
        }
        catch (SocketException exc) {
          // Check if we're supposed to terminate.
          if (terminated) {
            LOG.info("Received termination signal.");
          }
          else {
            LOG.warn("SocketException without termination flag set");
            throw new IllegalStateException(
                "SocketException without termination flag set", exc);
          }
        }
        catch (IOException exc) {
          throw new IllegalStateException(
              "IOException listening for connections", exc);
        }
      }
      LOG.info("Terminated connection listener.");
    }
  }

  /**
   * The task handling client connections.
   */
  private class ConnectionHandler
      implements Runnable {

    /**
     * Input buffer size for reading from sockets.
     */
    private static final int IN_BUF_SIZE = 16384;
    /**
     * The connection to the client.
     */
    private final Socket clientSocket;

    /**
     * Creates a new ConnectionHandler.
     *
     * @param clientSocket The socket for communication with the client.
     */
    ConnectionHandler(Socket clientSocket) {
      this.clientSocket = requireNonNull(clientSocket, "clientSocket");
    }

    @Override
    public void run() {
      try (Socket socket = clientSocket) {
        if (!socket.isConnected()) {
          throw new IllegalArgumentException("socket is not connected");
        }
        // Set a timeout for read() operations.
        socket.setSoTimeout(inputTimeout);
        InputStream inStream = socket.getInputStream();
        ByteArrayOutputStream bufferStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[IN_BUF_SIZE];
        int bytesRead = inStream.read(buffer);
        int totalBytesRead = 0;
        boolean foundEndOfTelegram = false;
        String telegram = "";
        // Read from socket while we haven't encountered the end of telegram
        // marker and we haven't reached the EOF on the stream.
        while (!foundEndOfTelegram && bytesRead != -1) {
          totalBytesRead += bytesRead;
          if (totalBytesRead > maxInputLength) {
            throw new IllegalStateException("maxAllowedInputLength reached");
          }
          bufferStream.write(buffer, 0, bytesRead);
          telegram = bufferStream.toString();
          // If we did NOT receive the end of telegram marker, yet, read on.
          if (!telegram.contains(END_OF_TELEGRAM)) {
            bytesRead = inStream.read(buffer);
          }
          // If we did find the end of the telegram, stop reading.
          else {
            foundEndOfTelegram = true;
          }
        }
        LOG.debug("Reached end of telegram, processing input");
        TCSOrderSet orderSet = TCSOrderSet.fromXml(telegram);
        LOG.debug("Constructed order set");
        TCSResponseSet responseSet = processOrderSet(orderSet);
        OutputStream outStream = socket.getOutputStream();
        LOG.debug("Sending response");
        outStream.write(responseSet.toXml().getBytes());
        outStream.flush();
        LOG.debug("Sent response, finishing.");
      }
      catch (Exception exc) {
        LOG.warn("Unexpected exception, aborting communication", exc);
      }
    }

    /**
     * Processes the given order set.
     *
     * @param orderSet The order set to be processed.
     * @return The <code>TCSResponseSet</code>.
     */
    private TCSResponseSet processOrderSet(TCSOrderSet orderSet) {
      assert orderSet != null;
      TCSResponseSet responseSet = new TCSResponseSet();
      for (TCSOrder curOrder : orderSet.getOrders()) {
        if (curOrder instanceof Transport) {
          LOG.debug("Processing 'Transport' element");
          TCSResponse response = processTransport((Transport) curOrder);
          responseSet.getResponses().add(response);
        }
        else if (curOrder instanceof TransportScript) {
          LOG.debug("Processing 'TransportScript' element");
          TransportScript curScript = (TransportScript) curOrder;
          ScriptResponse response = processScriptFile(curScript);
          responseSet.getResponses().add(response);
        }
        else {
          LOG.warn("Unhandled order type: " + curOrder.getClass().getName());
          // Create a negative response for this order.
          TransportResponse response = new TransportResponse();
          response.setId(curOrder.getId());
          response.setOrderName("");
          response.setExecutionSuccessful(false);
          responseSet.getResponses().add(response);
        }
      }
      return responseSet;
    }

    /**
     * Processes a transport.
     *
     * @param transport The transport to be processed.
     * @return The <code>TCSResponse</code>.
     */
    private TCSResponse processTransport(Transport transport) {
      requireNonNull(transport, "transport");

      // Create a response for this order.
      TransportResponse response = new TransportResponse();
      response.setId(transport.getId());

      try {
        TransportOrder order = localKernel.createTransportOrder(
            new TransportOrderCreationTO("TOrder-" + UUID.randomUUID(),
                                         createDestinations(transport.getDestinations()))
                .setDeadline(transport.getDeadline() == null
                    ? ZonedDateTime.of(2099, 12, 31, 23, 59, 59, 0, ZoneId.systemDefault())
                    : ZonedDateTime.ofInstant(transport.getDeadline().toInstant(),
                                              ZoneId.systemDefault()))
                .setIntendedVehicleName(transport.getIntendedVehicle())
                .setDependencyNames(new HashSet<>(transport.getDependencies()))
        );

        response.setOrderName(order.getName());

        localKernel.activateTransportOrder(order.getReference());

        // Everything went fine - let the client know.
        response.setExecutionSuccessful(true);
      }
      catch (ObjectUnknownException | CredentialsException exc) {
        LOG.warn("Unexpected exception", exc);
        response.setExecutionSuccessful(false);
      }
      return response;
    }

    /**
     * Processes a script file.
     *
     * @param transportScript The transport script to be processed.
     * @return The <code>ScriptResponse</code>.
     */
    private ScriptResponse processScriptFile(TransportScript transportScript) {
      requireNonNull(transportScript, "transportScript");

      ScriptResponse result = new ScriptResponse();
      result.setId(transportScript.getId());

      // Parse the script file.
      TCSScriptFile scriptFile;
      try {
        scriptFile = scriptFileManager.getScriptFile(transportScript.getFileName());
      }
      catch (IOException exc) {
        LOG.warn("Exception parsing script file", exc);
        result.setParsingSuccessful(false);
        return result;
      }

      // Process all order entries in the script file and create a response entry for each of them.
      String prevOrderName = null;
      for (TCSScriptFile.Order curOrder : scriptFile.getOrders()) {
        TransportResponse response = new TransportResponse();
        response.setId(transportScript.getId());

        try {
          TransportOrderCreationTO orderTO
              = new TransportOrderCreationTO("TOrder-" + UUID.randomUUID(),
                                             createDestinations(curOrder.getDestinations()))
                  .setIntendedVehicleName(curOrder.getIntendedVehicle());
          if (scriptFile.getSequentialDependencies() && prevOrderName != null) {
            orderTO.getDependencyNames().add(prevOrderName);
          }

          TransportOrder order = localKernel.createTransportOrder(orderTO);

          response.setOrderName(order.getName());

          localKernel.activateTransportOrder(order.getReference());

          response.setExecutionSuccessful(true);
          prevOrderName = order.getName();
        }
        catch (ObjectUnknownException | CredentialsException exc) {
          LOG.warn("Unexpected exception", exc);
          response.setExecutionSuccessful(false);
          // XXX With sequential dependencies, we should stop here, not add
          // another order without any dependencies!
          prevOrderName = null;
        }
        result.getTransports().add(response);
      }
      return result;
    }

    private List<DestinationCreationTO> createDestinations(List<Destination> destinations) {
      List<DestinationCreationTO> result = new ArrayList<>();
      for (Destination curDest : destinations) {
        result.add(new DestinationCreationTO(curDest.getLocationName(), curDest.getOperation()));
      }
      return result;
    }
  }
}
