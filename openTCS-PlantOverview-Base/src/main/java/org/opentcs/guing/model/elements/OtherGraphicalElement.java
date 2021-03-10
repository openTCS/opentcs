/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.model.elements;

import java.util.ResourceBundle;
import static org.opentcs.guing.I18nPlantOverviewBase.BUNDLE_PATH;
import org.opentcs.guing.model.AbstractModelComponent;

/**
 * A graphical component with illustrating effect, but without any impact
 * on the driving course.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class OtherGraphicalElement
    extends AbstractModelComponent {

  /**
   * Creates a new instance of OtherGraphicalElement.
   */
  public OtherGraphicalElement() {
    super();
  }

  @Override
  public String getDescription() {
    return ResourceBundle.getBundle(BUNDLE_PATH)
        .getString("otherGraphicalElement.description");
  }
}
