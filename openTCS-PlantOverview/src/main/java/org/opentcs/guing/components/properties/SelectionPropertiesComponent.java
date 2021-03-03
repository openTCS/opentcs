/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.properties;

import javax.inject.Inject;
import net.engio.mbassy.listener.Handler;
import org.opentcs.guing.application.action.edit.UndoRedoManager;
import org.opentcs.guing.event.OperationModeChangeEvent;
import org.opentcs.guing.event.SystemModelTransitionEvent;

/**
 * An AttributesComponent intended to be shown permanently to display the
 * properties of the currently selected driving course components.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class SelectionPropertiesComponent
    extends AttributesComponent {

  /**
   * Creates a new instance.
   *
   * @param undoManager Manages undo/redo actions.
   */
  @Inject
  public SelectionPropertiesComponent(UndoRedoManager undoManager) {
    super(undoManager);
  }

  @Handler
  public void handleSystemModelTransition(SystemModelTransitionEvent evt) {
    switch (evt.getStage()) {
      case UNLOADING:
        reset();
        break;
      default:
      // Do nada.
    }
  }

  @Handler
  public void handleOperationModeChange(OperationModeChangeEvent evt) {
    reset();
  }
}
