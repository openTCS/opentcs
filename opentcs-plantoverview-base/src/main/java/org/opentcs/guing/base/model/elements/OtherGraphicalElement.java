// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.base.model.elements;

import static org.opentcs.guing.base.I18nPlantOverviewBase.BUNDLE_PATH;

import java.util.ResourceBundle;
import org.opentcs.guing.base.model.AbstractModelComponent;

/**
 * A graphical component with illustrating effect, but without any impact
 * on the driving course.
 */
public class OtherGraphicalElement
    extends
      AbstractModelComponent {

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
