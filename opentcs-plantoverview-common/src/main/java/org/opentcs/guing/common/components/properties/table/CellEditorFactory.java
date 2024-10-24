// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.common.components.properties.table;

import javax.swing.JPanel;

/**
 * A factory for cell editors.
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
