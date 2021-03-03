/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.components.tree.elements;

import javax.swing.JComponent;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.model.ModelComponent;

/**
 * Ein einfacher Ordner in der Baumansicht, dem keine weitere Funktionalität
 * zugeordnet ist.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class SimpleFolderUserObject
    extends AbstractUserObject {

  /**
   * @param dataObject
   */
  public SimpleFolderUserObject(ModelComponent dataObject) {
    super(dataObject);
  }

  @Override // AbstractUserObject
  public boolean removed() {
    return false;
  }

  @Override // AbstractUserObject
  public void selected() {
    OpenTCSView.instance().selectModelComponent(getModelComponent());
  }

  @Override // AbstractUserObject
  public void rightClicked(JComponent component, int x, int y) {
    // Empty - no popup menu to be displayed.
  }
}
