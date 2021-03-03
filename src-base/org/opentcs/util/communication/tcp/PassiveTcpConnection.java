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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.opentcs.util.math.ByteConversions;

/**
 * A TCP connection to a peer with the peer being the client.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public final class PassiveTcpConnection
    extends TcpConnection {

  /**
   * This class's Logger.
   */
  private static final Logger log =
      Logger.getLogger(PassiveTcpConnection.class.getName());
  /**
   * The communication adapter.
   */
  private final IncomingTelegramHandler telegramHandler;
  /**
   * The tokenizer for incoming telegrams.
   */
  private final ByteSequenceDissector<? extends IncomingTelegram> telegramTokenizer;
  /**
   * The actual socket listening for incoming connections.
   */
  private final ServerSocket serverSocket;
  /**
   * The timeout for reading data from the socket's input stream (in ms).
   */
  private final int receiveTimeout;
  /**
   * The task polling for incoming data and re-establishing lost connections.
   */
  private final TelegramReceiverTask receiverTask;
  /**
   * The connection socket.
   */
  private volatile Socket clientSocket;

  /**
   * Creates a new PassiveTCPVehicleConnection.
   *
   * @param handler The handler we send incoming telegrams to.
   * @param tokenizer A tokenizer to construct telegram objects from incoming
   * bytes for us.
   * @param port The port on which to listen for incoming connections.
   * @param readTimeout A timeout for reading from the socket's input stream (in
   * ms). If no data is read after this amount of time, the connection will be
   * considered broken, the socket closed and a new connection accepted. A
   * value of zero disables the timeout/is interpreted as an infinite timeout.
   */
  public PassiveTcpConnection(IncomingTelegramHandler handler,
                              ByteSequenceDissector<? extends IncomingTelegram> tokenizer,
                              int port,
                              int readTimeout) {
    if (handler == null) {
      throw new NullPointerException("handler is null");
    }
    if (tokenizer == null) {
      throw new NullPointerException("tokenizer is null");
    }
    if (port <= 0) {
      throw new IllegalArgumentException("port <= 0: " + port);
    }
    if (readTimeout < 0) {
      throw new IllegalArgumentException("illegal read timeout: " + readTimeout);
    }
    telegramHandler = handler;
    telegramTokenizer = tokenizer;
    receiveTimeout = readTimeout;
    try {
      serverSocket = new ServerSocket(port);
      receiverTask = new TelegramReceiverTask();
      final Thread clientThread =
          new Thread(receiverTask,
                     "TelegramReceiverTask at port " + port);
      clientThread.start();
    }
    catch (IOException exc) {
      throw new IllegalStateException("Failed to init server socket", exc);
    }
  }

  @Override
  public boolean isConnected() {
    synchronized (serverSocket) {
      return clientSocket != null && clientSocket.isConnected();
    }
  }

  @Override
  public void disconnect() {
    deactivate();
    synchronized (serverSocket) {
      receiverTask.terminate();
      try {
        serverSocket.close();
      }
      catch (IOException exc) {
        log.log(Level.WARNING, "serverSocket.close() failed", exc);
      }
      try {
        if (clientSocket != null && !clientSocket.isClosed()) {
          clientSocket.close();
        }
      }
      catch (IOException exc) {
        log.log(Level.WARNING, "clientSocket.close() failed", exc);
      }
      finally {
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
      synchronized (serverSocket) {
        if (clientSocket == null) {
          throw new IOException("Not connected (client socket null)");
        }
        OutputStream out = clientSocket.getOutputStream();
        log.fine("Sending telegram to peer: "
            + ByteConversions.byteArrayToHexString(telegram));
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
    try {
      log.fine("Waiting for new connection on port "
          + serverSocket.getLocalPort());
      Socket socket = serverSocket.accept();
      log.fine("Accepted incoming connection from " + socket.getInetAddress());
      socket.setTcpNoDelay(true);
      socket.setKeepAlive(true);
      socket.setSoTimeout(receiveTimeout);
      synchronized (serverSocket) {
        clientSocket = socket;
      }
    }
    catch (IOException exc) {
      if (isActive()) {
        log.log(Level.WARNING, "Exception establishing connection", exc);
      }
      closeCurrentConnection();
    }
  }

  @Override
  protected void processVehicleTelegrams() {
    try {
      byte[] buffer = new byte[RECEIVE_BUFFER_SIZE];
      InputStream in;
      synchronized (serverSocket) {
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
        // If the returned argument is less than zero, the end of the stream
        // is reached. - Close the connection so we can accept a new one.
        closeCurrentConnection();
      }
      else {
        byte[] receivedData = Arrays.copyOf(buffer, bytesRead);
        telegramTokenizer.addIncomingBytes(receivedData);
        while (telegramTokenizer.hasObjects()) {
          // Hand over the telegram to the communication adapter.
          telegramHandler.handleTelegram(telegramTokenizer.getNextObject());
        }
      }
    }
    catch (IOException exc) {
      if (isActive()) {
        log.log(Level.WARNING, "Exception communicating with peer", exc);
      }
      closeCurrentConnection();
    }
  }

  /**
   * Closes and discards the socket currently used.
   */
  private void closeCurrentConnection() {
    synchronized (serverSocket) {
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
