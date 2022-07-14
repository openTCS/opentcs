/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.modeleditor.application.toolbar;

import com.google.inject.assistedinject.Assisted;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.swing.Action;
import javax.swing.JMenuItem;
import org.jhotdraw.draw.tool.DragTracker;
import org.jhotdraw.draw.tool.SelectAreaTracker;
import org.opentcs.guing.common.application.ApplicationState;
import org.opentcs.modeleditor.application.menus.MenuFactory;
import org.opentcs.thirdparty.guing.common.jhotdraw.application.toolbar.AbstractMultipleSelectionTool;

/**
 * The default selection tool.
 *
 * @author Heinz Huber (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class MultipleSelectionTool
    extends AbstractMultipleSelectionTool {

  /**
   * A factory for menu items.
   */
  private final MenuFactory menuFactory;
  
  /**
   * Creates a new instance.
   *
   * @param appState Stores the application's current state.
   * @param menuFactory A factory for menu items in popup menus created by this tool.
   * @param selectAreaTracker The tracker to be used for area selections in the drawing.
   * @param dragTracker The tracker to be used for dragging figures.
   * @param drawingActions Drawing-related actions for the popup menus created by this tool.
   * @param selectionActions Selection-related actions for the popup menus created by this tool.
   */
  @Inject
  public MultipleSelectionTool(ApplicationState appState,
                               MenuFactory menuFactory,
                               SelectAreaTracker selectAreaTracker,
                               DragTracker dragTracker,
                               @Assisted("drawingActions") Collection<Action> drawingActions,
                               @Assisted("selectionActions") Collection<Action> selectionActions) {
    super(appState, selectAreaTracker, dragTracker, drawingActions, selectionActions);
    this.menuFactory = requireNonNull(menuFactory, "menuFactory");
  }

  @Override
  public List<JMenuItem> customPopupMenuItems() {
    // Points and Locations get two additional entries
    List<JMenuItem> actions = new ArrayList<>();
    actions.add(menuFactory.createModelToLayoutMenuItem(false));
    actions.add(menuFactory.createLayoutToModelMenuItem(false));
    return actions;
  }
}
