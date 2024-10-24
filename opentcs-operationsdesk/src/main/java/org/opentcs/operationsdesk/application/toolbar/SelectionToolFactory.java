// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.operationsdesk.application.toolbar;

import com.google.inject.assistedinject.Assisted;
import java.util.Collection;
import javax.swing.Action;

/**
 */
public interface SelectionToolFactory {

  MultipleSelectionTool createMultipleSelectionTool(
      @Assisted("drawingActions")
      Collection<Action> drawingActions,
      @Assisted("selectionActions")
      Collection<Action> selectionActions
  );
}
