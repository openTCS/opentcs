/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.event;

import java.util.EventObject;
import static java.util.Objects.requireNonNull;
import org.opentcs.guing.common.model.SystemModel;

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
   * The system model to which this event refers.
   */
  private final SystemModel model;

  /**
   * Creates a new instance.
   *
   * @param source The originator of this event.
   * @param stage The current state of the transition.
   * @param model The system model to which this event refers.
   */
  public SystemModelTransitionEvent(Object source, Stage stage, SystemModel model) {
    super(source);
    this.stage = stage;
    this.model = requireNonNull(model, "model");
  }

  /**
   * Returns the current stage of the transition.
   *
   * @return The current stage of the transition.
   */
  public Stage getStage() {
    return stage;
  }

  /**
   * Returns the system model to which this event refers.
   *
   * @return The system model to which this event refers.
   */
  public SystemModel getModel() {
    return model;
  }

  @Override
  public String toString() {
    return "SystemModelTransitionEvent{"
        + "source=" + getSource() + ", "
        + "stage=" + stage + ", "
        + "model=" + model
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
