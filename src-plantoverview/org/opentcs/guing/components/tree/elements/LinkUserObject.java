/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.components.tree.elements;

import javax.swing.ImageIcon;
import org.opentcs.guing.model.elements.LinkModel;
import org.opentcs.guing.util.IconToolkit;

/**
 * Die Repräsentation eines Links in der Baumansicht.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class LinkUserObject
    extends FigureUserObject {

  /**
   * Creates a new instance of LinkUserObject
   *
   * @param modelComponent
   */
  public LinkUserObject(LinkModel modelComponent) {
    super(modelComponent);
  }

  @Override
  public LinkModel getModelComponent() {
    return (LinkModel) super.getModelComponent();
  }

  @Override
  public ImageIcon getIcon() {
    return IconToolkit.instance().createImageIcon("tree/link.18x18.png");
  }
}
