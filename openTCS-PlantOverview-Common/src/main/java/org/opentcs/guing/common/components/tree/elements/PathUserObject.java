/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.tree.elements;

import com.google.inject.assistedinject.Assisted;
import javax.inject.Inject;
import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;
import org.opentcs.guing.base.model.elements.PathModel;
import org.opentcs.guing.common.application.GuiManager;
import org.opentcs.guing.common.persistence.ModelManager;
import org.opentcs.guing.common.util.IconToolkit;

/**
 * Represents a path in the TreeView.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class PathUserObject
    extends FigureUserObject
    implements ContextObject {

  private final UserObjectContext context;

  /**
   * Creates a new instance.
   *
   * @param model The corresponding model object
   * @param context The user object context
   * @param guiManager The gui manager.
   * @param modelManager The model manager
   */
  @Inject
  public PathUserObject(@Assisted PathModel model,
                        @Assisted UserObjectContext context,
                        GuiManager guiManager,
                        ModelManager modelManager) {
    super(model, guiManager, modelManager);
    this.context = context;
  }

  @Override // AbstractUserObject
  public JPopupMenu getPopupMenu() {
    JPopupMenu menu = context.getPopupMenu(userObjectItems);

    return menu;
  }

  @Override // FigureUserObject
  public ImageIcon getIcon() {
    return IconToolkit.instance().createImageIcon("tree/path.18x18.png");
  }

  @Override
  public boolean removed() {
    return context.removed(this);
  }

  @Override
  public UserObjectContext.ContextType getContextType() {
    return context.getType();
  }
}
