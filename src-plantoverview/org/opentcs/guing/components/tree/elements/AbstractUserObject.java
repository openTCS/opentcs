/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.components.tree.elements;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.model.ModelComponent;

/**
 * Defaultimplementierung des UserObject-Interfaces. Ein UserObject ist das
 * Objekt, das ein DefaultMutableTreeNode in einem JTree mit verwaltet. Ein
 * UserObject hat hier die Funktion des Befehls im Befehlsmuster. Die
 * ModelingApplication ist der Befehlsempfänger. Ein UserObject hält darüber
 * hinaus eine Referenz auf ein Datenobjekt aus dem Systemmodell.
 * <p>
 * <b>Entwurfsmuster:</b> Befehl. AbstractUserObject ist der abstrakte Befehl.
 * Klient ist der TreeView und Empfänger die Applikation.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public abstract class AbstractUserObject
    implements UserObject {

  /**
   * Das Datenobjekt aus dem Systemmodell.
   */
  private final ModelComponent fModelComponent;
  /**
   * The parent object.
   */
  private ModelComponent parent;

  /**
   * Creates a new instance of AbstractUserObject
   *
   * @param modelComponent
   */
  public AbstractUserObject(ModelComponent modelComponent) {
    this.fModelComponent = modelComponent;
  }

  @Override // Object
  public String toString() {
    return fModelComponent.getTreeViewName();
  }

  @Override // UserObject
  public ModelComponent getModelComponent() {
    return fModelComponent;
  }

  @Override // UserObject
  public void selected() {
    OpenTCSView.instance().selectModelComponent(getModelComponent());
  }

  @Override // UserObject
  public boolean removed() {
    return OpenTCSView.instance().treeComponentRemoved(fModelComponent);
  }

  @Override // UserObject
  public void rightClicked(JComponent component, int x, int y) {
    JPopupMenu popupMenu = getPopupMenu();

    if (popupMenu != null) {
      popupMenu.show(component, x, y);
    }
  }

  @Override // UserObject
  public void doubleClicked() {
  }

  @Override // UserObject
  public JPopupMenu getPopupMenu() {
    return null;
  }

  @Override // UserObject
  public ImageIcon getIcon() {
    return null;
  }

  /**
   * Called when an object in the tree view is right clicked. Additionally
   * this method calls a seperate popup menu depending on if this object
   * belongs to a group.
   *
   * @param component
   * @param x
   * @param y
   * @param isGroupView
   */
  public void rightClicked(JComponent component, int x, int y, boolean isGroupView) {
    rightClicked(component, x, y);
  }

  /**
   * Wird aufgerufen, wenn mehrere Objekte im Baum selektiert werden sollen.
   */
  public void selectMultipleObjects() {
    OpenTCSView.instance().addSelectedModelComponent(getModelComponent());
  }

  @Override
  public ModelComponent getParent() {
    return parent;
  }

  @Override
  public void setParent(ModelComponent parent) {
    this.parent = parent;
  }
}
