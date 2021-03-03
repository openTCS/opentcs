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
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.opentcs.util.CyclicTask;

/**
 * A connection to a peer.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public abstract class TcpConnection {

  /**
   * Buffer size for reading data from the TCP stream.
   */
  protected static final int RECEIVE_BUFFER_SIZE = 8192;
  /**
   * This class's Logger.
   */
  private static final Logger log =
      Logger.getLogger(TcpConnection.class.getName());
  /**
   * A delay (in ms) to wait before re-establishing the connection after it was
   * disconnected.
   */
  private final int reconnectDelay;
  /**
   * A set of listeners to notify when this connection's state changes.
   */
  private final Set<ConnectionStateListener> stateListeners =
      new HashSet<>();
  /**
   * A flag indicating whether a connection is desired or not.
   */
  private volatile boolean active = true;

  /**
   * Creates a new TcpConnection.
   */
  protected TcpConnection() {
    reconnectDelay = 0;
  }

  /**
   * Creates a new TcpConnection.
   *
   * @param reconnectDelay A delay (in ms) to wait before re-establishing the
   * connection after it was disconnected.
   */
  protected TcpConnection(int reconnectDelay) {
    if (reconnectDelay < 0) {
      throw new IllegalArgumentException("reconnectDelay < 0: "
          + reconnectDelay);
    }
    this.reconnectDelay = reconnectDelay;
  }

  /**
   * Checks whether this connection <em>should</em> be up, i.e. if it should
   * re-connect automatically on connection loss. This does not indicate the
   * actual connection state, which can be down at least temporarily.
   *
   * @return <code>true</code> if, and only if, this connection should be up.
   */
  protected final boolean isActive() {
    return active;
  }

  /**
   * Deactivates this connection, indicating that it is not going to be used any
   * more.
   */
  protected final void deactivate() {
    active = false;
  }

  /**
   * Registers a state listener with this connection.
   *
   * @param newListener The listener to be registered.
   */
  public final void addStateListener(ConnectionStateListener newListener) {
    if (newListener == null) {
      throw new NullPointerException("newListener is null");
    }
    stateListeners.add(newListener);
  }

  /**
   * Unregisters a state listener from this connection.
   *
   * @param rmListener The listener to be unregistered.
   */
  public final void removeStateListener(ConnectionStateListener rmListener) {
    if (rmListener == null) {
      throw new NullPointerException("rmListener is null");
    }
    stateListeners.remove(rmListener);
  }

  /**
   * Checks whether the connection to the vehicle is currently up, i.e. whether
   * communication is possible.
   *
   * @return <code>true</code> if, and only if, the connection to the vehicle is
   * currently up.
   */
  public abstract boolean isConnected();

  /**
   * Disconnects from the peer and deactivates this connection.
   * After disconnecting, this <code>VehicleConnection</code> cannot be used for
   * another connection to peer; instead, a new instance must be created. If
   * already disconnected, this method does nothing.
   */
  public abstract void disconnect();

  /**
   * Sends the given sequence of bytes to the peer.
   *
   * @param telegram The sequence of bytes to be sent to the peer.
   * @throws IOException If an I/O error occurs trying to send the given data,
   * or if the connection currently isn't up.
   */
  public abstract void sendTelegram(byte[] telegram)
      throws IOException;

  /**
   * (Re-)establish a connection to the vehicle.
   * This method is called by the receiver task if the connection should be up
   * but is currently down.
   */
  protected abstract void establishConnection();

  /**
   * Read what the vehicle has sent to us.
   * This method is called by the receiver task to wait for incoming data from
   * the vehicle and process it.
   */
  protected abstract void processVehicleTelegrams();

  /**
   * Notifies listeners about a connection state change.
   *
   * @param connected <code>true</code> if the connection went up,
   * <code>false</code> if it went down.
   */
  private void notifyStateListeners(boolean connected) {
    for (ConnectionStateListener listener : stateListeners) {
      listener.connectionStateChanged(this, connected);
    }
  }

  /**
   * A task that continuously waits for incoming data from the vehicle and
   * processes it. If the connection to the peer goes down unexpectedly, it's
   * re-established automatically.
   */
  protected final class TelegramReceiverTask
      extends CyclicTask {

    /**
     * A flag indicating whether the connection was up during the last run of
     * this task.
     */
    private boolean connectedLastRound;

    /**
     * Creates a new TelegramReceiverTask.
     */
    TelegramReceiverTask() {
      super(0);
    }

    @Override
    protected void runActualTask() {
      log.fine("method entry");
      if (isActive()) {
        if (isConnected()) {
          if (!connectedLastRound) {
            // Let listeners know we're connected now.
            notifyStateListeners(true);
            connectedLastRound = true;
          }
          // If we're connected, read from the connection and process telegrams.
          processVehicleTelegrams();
        }
        else {
          if (connectedLastRound) {
            // Let listeners know we're disconnected now.
            notifyStateListeners(false);
            connectedLastRound = false;
          }
          // If we're not connected, establish a new connection.
          if (reconnectDelay > 0) {
            try {
              Thread.sleep(reconnectDelay);
            }
            catch (InterruptedException exc) {
              log.log(Level.WARNING, "Unexpectedly interrupted", exc);
            }
          }
          establishConnection();
        }
      }
      else {
        // If were up during the last round, notify listeners we're down now.
        if (connectedLastRound) {
          notifyStateListeners(false);
        }
        // If we're not active any more, terminate this task.
        terminate();
      }
    }
  }
}
