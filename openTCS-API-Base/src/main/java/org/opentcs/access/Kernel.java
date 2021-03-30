/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access;

/**
 * Declares the methods an openTCS kernel implements.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface Kernel {

  /**
   * The default name used for the empty model created on startup.
   */
  String DEFAULT_MODEL_NAME = "unnamed";

  /**
   * Returns the current state of the kernel.
   *
   * @return The current state of the kernel.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  State getState()
      throws CredentialsException;

  /**
   * Sets the current state of the kernel.
   * <p>
   * Note: This method should only be used internally by the Kernel application.
   * </p>
   *
   * @param newState The state the kernel is to be set to.
   * @throws IllegalArgumentException If setting the new state is not possible,
   * e.g. because a transition from the current to the new state is not allowed.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void setState(State newState)
      throws IllegalArgumentException, CredentialsException;

  /**
   * The various states a kernel instance may be running in.
   */
  enum State {

    /**
     * The state in which the model/topology is created and parameters are set.
     */
    MODELLING,
    /**
     * The normal mode of operation in which transport orders may be accepted
     * and dispatched to vehicles.
     */
    OPERATING,
    /**
     * A transitional state the kernel is in while shutting down.
     */
    SHUTDOWN
  }
}
