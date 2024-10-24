// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.common.components.properties;

import javax.swing.JComponent;
import org.opentcs.guing.base.model.ModelComponent;
import org.opentcs.thirdparty.guing.common.jhotdraw.application.action.edit.UndoRedoManager;

/**
 * Interface for Swing components that allow editing and viewing a model component attribute.
 */
public interface AttributesContent {

  /**
   * Sets the model component whose properties are to be displayed.
   *
   * @param model The model component whose properties are to be displayed.
   */
  void setModel(ModelComponent model);

  /**
   * Resets this content to no longer display the model component properties.
   */
  void reset();

  /**
   * Returns the content as a Swing component.
   *
   * @return The content as a Swing component.
   */
  JComponent getComponent();

  /**
   * Return a description of the content.
   *
   * @return A description of the content.
   */
  String getDescription();

  /**
   * Initialises the content with the undo manager.
   *
   * @param undoRedoManager The content with the undo manager.
   */
  void setup(UndoRedoManager undoRedoManager);
}
