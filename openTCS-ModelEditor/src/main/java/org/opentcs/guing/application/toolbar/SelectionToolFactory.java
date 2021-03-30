/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application.toolbar;

import com.google.inject.assistedinject.Assisted;
import java.util.Collection;
import javax.swing.Action;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface SelectionToolFactory {

  MultipleSelectionTool createMultipleSelectionTool(
      @Assisted("drawingActions") Collection<Action> drawingActions,
      @Assisted("selectionActions") Collection<Action> selectionActions);
}
