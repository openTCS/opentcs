/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.tree;

import org.opentcs.guing.components.EditableComponent;

/**
 * A JTree which delegates actions to it's parent.
 *
 * @author Heinz Huber (Fraunhofer IML)
 */
public class StandardActionTree
    extends javax.swing.JTree
    implements EditableComponent {

  private final EditableComponent parent;

  /**
   * Creates a new instance.
   *
   * @param parent The parent editable component
   */
  public StandardActionTree(EditableComponent parent) {
    this.parent = parent;
  }

  @Override
  public void cutSelectedItems() {
    parent.cutSelectedItems();
  }

  @Override
  public void copySelectedItems() {
    parent.copySelectedItems();
  }

  @Override
  public void pasteBufferedItems() {
    parent.pasteBufferedItems();
  }

  @Override
  public void delete() {
    parent.delete();
  }

  @Override
  public void duplicate() {
    parent.duplicate();
  }

  @Override
  public void selectAll() {
    parent.selectAll();
  }

  /**
   * Note: EditableComponent.clearSelection() must _not_ be overridden
   * since the method JTree.clearSelection() is called in JTree's constructor.
   */
}
