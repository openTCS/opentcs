/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.properties;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import org.opentcs.guing.base.components.properties.event.NullAttributesChangeListener;
import org.opentcs.guing.base.components.properties.type.Property;

/**
 * An undo action for a property change.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class PropertyUndoActivity
    extends javax.swing.undo.AbstractUndoableEdit {

  /**
   * The property that changes.
   */
  protected Property fProperty;
  /**
   * The property before the change.
   */
  protected Property fBeforeModification;
  /**
   * The property after the change.
   */
  protected Property fAfterModification;
  /**
   * Indicates whether this change has been performed.
   * Defaults to true; becomes false if this edit is undone, true
   * again if it is redone.
   */
  private boolean hasBeenDone = true;
  /**
   * True if this edit has not received <code>die</code>; defaults
   * to <code>true</code>.
   */
  private boolean alive = true;

  /**
   * Creates a new instance of PropertiesUndoActivity
   *
   * @param property
   */
  public PropertyUndoActivity(Property property) {
    fProperty = property;
  }

  /**
   * Creates a snapshot of the property before the change.
   */
  public void snapShotBeforeModification() {
    fBeforeModification = createMemento();
  }

  /**
   * Creates a snapshot of the property after the change.
   */
  public void snapShotAfterModification() {
    fAfterModification = createMemento();
  }

  /**
   * Creates a copy of the current state of the property.
   *
   * @return A copy of the current state of the property.
   */
  protected Property createMemento() {
    return (Property) fProperty.clone();
  }

  @Override
  public String getPresentationName() {
    return fProperty.getDescription();
  }

  @Override
  public void die() {
    alive = false;
  }

  @Override
  public void undo()
      throws CannotUndoException {
    fProperty.copyFrom(fBeforeModification);
    fProperty.markChanged();
    fProperty.getModel().propertiesChanged(new NullAttributesChangeListener());
    hasBeenDone = false;
  }

  @Override
  public void redo()
      throws CannotRedoException {
    fProperty.copyFrom(fAfterModification);
    fProperty.markChanged();
    fProperty.getModel().propertiesChanged(new NullAttributesChangeListener());
    hasBeenDone = true;
  }

  @Override
  public boolean canUndo() {
    return alive && hasBeenDone;
  }

  @Override
  public boolean canRedo() {
    return alive && !hasBeenDone;
  }
}
