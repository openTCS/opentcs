/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.drivers;

/**
 * Describes an answer to a call of
 * {@link CommunicationAdapter#canProcess(java.util.List)
 * CommunicationAdapter#canProcess()}.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class Processability {

  /**
   * Indicates whether the communication adapter can process the given list of
   * operations or not.
   */
  private final boolean canProcess;
  /**
   * A reason given by the communication adapter if <code>canProcess</code> is
   * <code>false</code>. May not be <code>null</code>.
   */
  private final String reason;

  /**
   * Creates a new Processability.
   *
   * @param canProcess Indicates whether the communication adapter can process
   * the given list of operations or not.
   * @param reason A reason given by the communication adapter if
   * <code>canProcess</code> is <code>false</code>. May not be
   * <code>null</code>.
   */
  public Processability(boolean canProcess, String reason) {
    if (reason == null) {
      throw new NullPointerException("reason is null");
    }
    this.canProcess = canProcess;
    this.reason = reason;
  }

  /**
   * Returns whether the communication adapter can process the given list
   * of operations or not.
   *
   * @return True if it can, false otherwise
   */
  public boolean isCanProcess() {
    return canProcess;
  }

  /**
   * Returns a reason given by the communication adapter
   * if <code>canProcess</code> is <code>false</code>.
   * May not be <code>null</code>.
   *
   * @return The reason
   */
  public String getReason() {
    return reason;
  }
}
