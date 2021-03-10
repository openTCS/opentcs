/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.tree.elements;

import com.google.inject.assistedinject.Assisted;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;
import org.opentcs.data.model.Block;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.components.drawing.OpenTCSDrawingEditor;
import org.opentcs.guing.model.elements.BlockModel;
import org.opentcs.guing.persistence.ModelManager;
import org.opentcs.guing.util.BlockSelector;
import org.opentcs.guing.util.IconToolkit;

/**
 * A Block object in the tree view.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Stefan Walter (Fraunhofer IML)
 * @see Block
 */
public class BlockUserObject
    extends AbstractUserObject
    implements ContextObject {

  private final UserObjectContext context;
  /**
   * A helper for selecting blocks/block elements.
   */
  private final BlockSelector blockSelector;

  /**
   * Creates a new instance.
   *
   * @param dataObject The corresponding model component
   * @param context The user object context
   * @param view The openTCS view
   * @param editor The drawing editor
   * @param modelManager The model manager
   * @param blockSelector A helper for selecting blocks/block elements.
   */
  @Inject
  public BlockUserObject(@Assisted BlockModel dataObject,
                         @Assisted UserObjectContext context,
                         OpenTCSView view,
                         OpenTCSDrawingEditor editor,
                         ModelManager modelManager,
                         BlockSelector blockSelector) {
    super(dataObject, view, editor, modelManager);
    this.context = requireNonNull(context, "context");
    this.blockSelector = requireNonNull(blockSelector, "blockSelector");
  }

  @Override
  public JPopupMenu getPopupMenu() {
    JPopupMenu menu = context.getPopupMenu(null);

    return menu;
  }

  @Override
  public BlockModel getModelComponent() {
    return (BlockModel) super.getModelComponent();
  }

  @Override
  public void doubleClicked() {
    blockSelector.blockSelected(getModelComponent());
  }

  @Override
  public ImageIcon getIcon() {
    return IconToolkit.instance().createImageIcon("tree/block.18x18.png");
  }

  @Override
  public UserObjectContext.ContextType getContextType() {
    return context.getType();
  }
}
