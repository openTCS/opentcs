/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.tree;

import java.util.Objects;
import static java.util.Objects.requireNonNull;
import java.util.function.Predicate;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * A filter for model components that does not accept the block folder.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class BlockFolderFilter
    implements Predicate<ModelComponent> {

  /**
   * Creates a new instance.
   */
  public BlockFolderFilter() {
  }

  @Override
  public boolean test(ModelComponent component) {
    requireNonNull(component, "component");

    return !Objects.equals(component.getTreeViewName(),
                           ResourceBundleUtil.getBundle().getString("tree.blocks.text"));
  }
}
