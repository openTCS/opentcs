/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.util;

import java.util.ArrayList;
import java.util.List;
import org.jhotdraw.draw.Figure;
import org.opentcs.guing.model.CompositeModelComponent;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.SystemModel;

/**
 * Provides utility methods for {@link ModelComponent}s.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class ModelComponentUtil {

  /**
   * Prevent instantiation.
   */
  private ModelComponentUtil() {
  }

  public static List<Figure> getChildFigures(CompositeModelComponent parent,
                                             SystemModel systemModel) {
    List<Figure> figures = new ArrayList<>();

    List<ModelComponent> childComps = parent.getChildComponents();
    synchronized (childComps) {
      for (ModelComponent component : childComps) {
        Figure figure = systemModel.getFigure(component);
        if (figure != null) {
          figures.add(figure);
        }
      }
    }

    return figures;
  }
}
