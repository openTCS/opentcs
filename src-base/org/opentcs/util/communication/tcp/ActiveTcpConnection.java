/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.communication.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.opentcs.util.math.ByteConversions;

/**
 * A TCP connection to a peer with the peer being the server.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public final class ActiveTcpConnection
    extends TcpConnection {

  /**
   * This class's Logger.
   */
  private static final Logger log =
      Logger.getLogger(ActiveTcpConnection.class.getName());
  /**
   * The timeout for connection attempts (in ms).
   */
  private static final int connectTimeout = 10000;
  /**
   * The time to wait after a failed (re)connection attempt (in ms).
   */
  private static final int reconnectDelay = 1000;
  /**
   * The communication adapter.
   */
  private final IncomingTelegramHandler telegramHandler;
  /**
   * The tokenizer for incoming telegrams.
   */
  private final ByteSequenceDissector<? extends IncomingTelegram> telegramTokenizer;
  /**
   * The host to connect to.
   */
  private final String remoteHost;
  /**
   * The port to connect to.
   */
  private final int remotePort;
  /**
   * The timeout for reading data from the socket's input stream (in ms).
   */
  private final int receiveTimeout;
  /**
   * The task polling for incoming data and re-establishing lost connections.
   */
  private final TelegramReceiverTask receiverTask;
  /**
   * An object to synchronize on when accessing/modifying the socket.
   */
  private final Object syncObject = new Object();
  /**
   * A socket for communicating with the peer.
   */
  private Socket clientSocket;

  /**
   * Creates a new ActiveTCPConnection.
   *
   * @param handler The handler we send incoming telegrams to.
   * @param tokenizer A tokenizer to construct telegram objects from incoming
   * bytes for us.
   * @param host The peer host name or IP address.
   * @param port The port number with the peer host.
   * @param readTimeout A timeout for reading from the socket's input stream (in
   * ms). If no data is read after this amount of time, the connection will be
   * considered broken, the socket closed and a new connection attempted. A
   * value of zero disables the timeout/is interpreted as an infinite timeout.
   * @param reconnectDelay A delay (in ms) to wait before re-establishing the
   * connection.
   */
  public ActiveTcpConnection(IncomingTelegramHandler handler,
                             ByteSequenceDissector<? extends IncomingTelegram> tokenizer,
                             String host,
                             int port,
                             int readTimeout,
                             int reconnectDelay) {
    super(reconnectDelay);
    if (handler == null) {
      throw new NullPointerException("handler is null");
    }
    if (tokenizer == null) {
      throw new NullPointerException("tokenizer is null");
    }
    if (host == null) {
      throw new NullPointerException("host is null");
    }
    if (port < 1) {
      throw new IllegalArgumentException("illegal port number: " + port);
    }
    if (readTimeout < 0) {
      throw new IllegalArgumentException("illegal read timeout: " + readTimeout);
    }
    telegramHandler = handler;
    telegramTokenizer = tokenizer;
    remoteHost = host;
    remotePort = port;
    receiveTimeout = readTimeout;
    receiverTask = new TelegramReceiverTask();
    final Thread receiverThread =
        new Thread(receiverTask,
                   "TelegramReceiverTask-" + remoteHost + ":" + remotePort);
    receiverThread.start();
  }

  /**
   * Creates a new ActiveTCPConnection with a reconnectDelay of zero.
   *
   * @param handler The handler we send incoming telegrams to.
   * @param tokenizer A tokenizer to construct telegram objects from incoming
   * bytes for us.
   * @param host The peer host name or IP address.
   * @param port The port number with the peer host.
   * @param readTimeout A timeout for reading from the socket's input stream (in
   * ms). If no data is read after this amount of time, the connection will be
   * considered broken, the socket closed and a new connection attempted. A
   * value of zero disables the timeout/is interpreted as an infinite timeout.
   */
  public ActiveTcpConnection(IncomingTelegramHandler handler,
                             ByteSequenceDissector<? extends IncomingTelegram> tokenizer,
                             String host,
                             int port,
                             int readTimeout) {
    this(handler, tokenizer, host, port, readTimeout, 0);
  }

  @Override
  public boolean isConnected() {
    log.fine("method entry");
    synchronized (syncObject) {
      return clientSocket != null && clientSocket.isConnected();
    }
  }

  @Override
  public void disconnect() {
    log.fine("method entry");
    deactivate();
    synchronized (syncObject) {
      receiverTask.terminate();
      if (clientSocket != null) {
        // Close the socket.
        if (!clientSocket.isClosed()) {
          try {
            clientSocket.close();
          }
          catch (IOException exc) {
            log.log(Level.WARNING, "IOException closing socket", exc);
          }
        }
        log.fine("Disposing socket");
        clientSocket = null;
      }
    }
  }

  @Override
  public void sendTelegram(byte[] telegram)
      throws IOException {
    if (telegram == null) {
      throw new NullPointerException("telegram is null");
    }
    try {
      synchronized (syncObject) {
        if (clientSocket == null) {
          throw new IOException("Not connected (client socket null)");
        }
        OutputStream out = clientSocket.getOutputStream();
        log.fine("Sending telegram to vehicle: " +
            ByteConversions.byteArrayToHexString(telegram));
        out.write(telegram);
        out.flush();
      }
    }
    catch (IOException exc) {
      closeCurrentConnection();
      throw exc;
    }
  }

  @Override
  protected void establishConnection() {
    log.fine("Establishing new connection to " + remoteHost + ":" +
        remotePort + "...");
    try {
      SocketAddress addr = new InetSocketAddress(remoteHost, remotePort);
      Socket socket = new Socket();
      socket.bind(null);
      socket.setTcpNoDelay(true);
      socket.setKeepAlive(true);
      socket.setSoTimeout(receiveTimeout);
      socket.connect(addr, connectTimeout);
      synchronized (syncObject) {
        clientSocket = socket;
        // Reset the VehicleProtocol instance.
        telegramTokenizer.reset();
      }
    }
    catch (IOException exc) {
      if (isActive()) {
        log.warning("Couldn't connect to peer: " + exc.getMessage());
      }
      closeCurrentConnection();
    }
    if (isConnected()) {
      log.fine("Connection established.");
    }
    else if (isActive()) {
      log.fine("Establishing connection failed, sleeping " +
          reconnectDelay / 1000 + " seconds");
      // Wait a bit before trying again.
      try {
        Thread.sleep(reconnectDelay);
      }
      catch (InterruptedException exc) {
        log.log(Level.WARNING, "Unexpectedly interrupted", exc);
      }
    }
  }

  @Override
  protected void processVehicleTelegrams() {
    try {
      byte[] buffer = new byte[RECEIVE_BUFFER_SIZE];
      InputStream in;
      synchronized (syncObject) {
        if (isConnected()) {
          in = clientSocket.getInputStream();
        }
        else {
          // If we're not connected, there's nothing to read from the stream.
          return;
        }
      }
      log.fine("Reading data from socket...");
      int bytesRead = in.read(buffer);
      log.fine("Returned from reading data from socket...");
      if (bytesRead == -1) {
        log.warning("Connection closed");
        closeCurrentConnection();
      }
      else {
        byte[] telegramData = Arrays.copyOf(buffer, bytesRead);
        telegramTokenizer.addIncomingBytes(telegramData);
        while (telegramTokenizer.hasObjects()) {
          // Send the telegram object to the handler.
          telegramHandler.handleTelegram(telegramTokenizer.getNextObject());
        }
      }
    }
    catch (IOException exc) {
      if (isActive()) {
        log.log(Level.WARNING, "Exception reading from socket", exc);
      }
      // Try to close the socket properly so we can reconnect to the vehicle
      // in an orderly way.
      closeCurrentConnection();
    }
  }

  /**
   * Closes and discards the socket currently used.
   */
  private void closeCurrentConnection() {
    synchronized (syncObject) {
      if (clientSocket != null) {
        log.fine("Closing connection");
        try {
          clientSocket.close();
        }
        catch (IOException exc) {
          log.log(Level.WARNING, "Exception closing socket - ignored", exc);
        }
        finally {
          clientSocket = null;
        }
      }
    }
  }
}
