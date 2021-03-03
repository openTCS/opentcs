/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.components.tree.elements;

import javax.swing.ImageIcon;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.model.elements.LocationTypeModel;
import org.opentcs.guing.util.IconToolkit;

/**
 * Die Repräsentation einer Station in der Baumansicht.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class LocationTypeUserObject
    extends AbstractUserObject {

  /**
   * Creates a new instance of StationUserObject
   *
   * @param modelComponent
   */
  public LocationTypeUserObject(LocationTypeModel modelComponent) {
    super(modelComponent);
  }

  @Override
  public LocationTypeModel getModelComponent() {
    return (LocationTypeModel) super.getModelComponent();
  }

  @Override
  public ImageIcon getIcon() {
    return IconToolkit.instance().createImageIcon("tree/locationType.18x18.png");
  }

  @Override
  public void doubleClicked() {
    OpenTCSView.instance().figureSelected(getModelComponent());
  }
}
