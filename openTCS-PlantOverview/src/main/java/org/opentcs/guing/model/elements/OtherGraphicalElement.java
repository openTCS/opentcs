/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.model.elements;

import org.opentcs.guing.model.AbstractFigureComponent;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * A graphical component with illustrating effect, but without any impact
 * on the driving course.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class OtherGraphicalElement
    extends AbstractFigureComponent {

  /**
   * Creates a new instance of OtherGraphicalElement.
   */
  public OtherGraphicalElement() {
    super();
  }

  @Override
  public String getDescription() {
    return ResourceBundleUtil.getBundle().getString("otherGraphicalElement.description");
  }
}
