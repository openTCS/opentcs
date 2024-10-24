// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.common.components.properties;

import javax.swing.JComponent;
import org.opentcs.guing.base.model.ModelComponent;
import org.opentcs.thirdparty.guing.common.jhotdraw.application.action.edit.UndoRedoManager;

/**
 * Base implementation for visualisation of model component properties.
 */
public abstract class AbstractAttributesContent
    implements
      AttributesContent {

  /**
   * The model component to show the properties of.
   */
  protected ModelComponent fModel;
  /**
   * The undo manager.
   */
  protected UndoRedoManager fUndoRedoManager;
  /**
   * The swing component.
   */
  protected JComponent fComponent;

  /**
   * Creates a new instance of AbstractAttributesContent
   */
  public AbstractAttributesContent() {
  }

  @Override // AttributesContent
  public void setModel(ModelComponent model) {
    fModel = model;
  }

  @Override // AttributesContent
  public abstract void reset();

  @Override // AttributesContent
  public JComponent getComponent() {
    return fComponent;
  }

  @Override // AttributesContent
  public String getDescription() {
    return fModel.getDescription();
  }

  @Override // AttributesContent
  public void setup(UndoRedoManager undoManager) {
    fUndoRedoManager = undoManager;
    fComponent = createComponent();
  }

  /**
   * Creates the component.
   *
   * @return The created component.
   */
  protected abstract JComponent createComponent();
}
