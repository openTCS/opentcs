/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.communication.tcp;

/**
 * A dummy TCP connection, not really connecting to any peer.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public final class DummyTcpConnection
    extends TcpConnection {

  /**
   * Creates a new DummyVehicleConnection.
   */
  public DummyTcpConnection() {
  }

  @Override
  public void disconnect() {
    // Do nada
  }

  @Override
  public boolean isConnected() {
    return false;
  }

  @Override
  public void sendTelegram(byte[] telegram) {
    // Do nada
  }

  @Override
  protected void establishConnection() {
    // Do nada
  }

  @Override
  protected void processVehicleTelegrams() {
    // Do nada
  }
}
