/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.components.tree.elements;

import org.opentcs.guing.model.elements.LayoutModel;

/**
 * Die Repräsentation eines Point-Objekts in der Baumansicht.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class LayoutUserObject
    extends FigureUserObject {

  /**
   * Creates a new instance.
   *
   * @param model The corresponding data object.
   */
  public LayoutUserObject(LayoutModel model) {
    super(model);
  }

  @Override
  public LayoutModel getModelComponent() {
    return (LayoutModel) super.getModelComponent();
  }
}
