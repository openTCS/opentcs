/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.properties.table;

import javax.swing.JPanel;

/**
 * A factory for cell editors.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface CellEditorFactory {

  /**
   * Creates a new <code>ComplexPropertyCellEditor</code>.
   *
   * @param dialogParent The component to be used as the parent for dialogs
   * created by the new instance.
   * @return A new cell editor instance.
   */
  ComplexPropertyCellEditor createComplexPropertyCellEditor(JPanel dialogParent);

}
