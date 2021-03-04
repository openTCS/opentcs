/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.event;

import java.util.EventObject;

/**
 * Informs listeners about the current system model being replaced with a
 * different one.
 * For every stage of the transition from the current/old system model to the
 * new one, a separate event is emitted.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class SystemModelTransitionEvent
    extends EventObject {

  /**
   * The current stage of the transition.
   */
  private final Stage stage;

  /**
   * Creates a new instance.
   *
   * @param source The originator of this event.
   * @param stage The current state of the transition.
   */
  public SystemModelTransitionEvent(Object source, Stage stage) {
    super(source);
    this.stage = stage;
  }

  /**
   * Returns the current stage of the transition.
   *
   * @return The current stage of the transition.
   */
  public Stage getStage() {
    return stage;
  }

  @Override
  public String toString() {
    return "SystemModelTransitionEvent{" 
        + "stage=" + stage 
        + ", source=" + getSource()
        + '}';
  }

  public static enum Stage {

    /**
     * Indicates the current system model is currently being unloaded.
     */
    UNLOADING,
    /**
     * Indicates the current system model has been unloaded.
     */
    UNLOADED,
    /**
     * Indicates the new system model is being loaded.
     */
    LOADING,
    /**
     * Indicates the new system model has been loaded.
     */
    LOADED;
  }
}
