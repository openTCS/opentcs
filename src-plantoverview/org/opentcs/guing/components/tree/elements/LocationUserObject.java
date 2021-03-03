/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.components.tree.elements;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.elements.LocationModel;
import org.opentcs.guing.util.IconToolkit;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * Die Repräsentation einer Station in der Baumansicht.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class LocationUserObject
    extends FigureUserObject {

  /**
   * Creates a new instance.
   *
   * @param model The corresponding model object.
   */
  public LocationUserObject(LocationModel model) {
    super(model);
  }

  @Override
  public LocationModel getModelComponent() {
    return (LocationModel) super.getModelComponent();
  }

  @Override // UserObject
  public void rightClicked(JComponent component, int x, int y) {
    userObjectItems = getSelectedUserObjects(((JTree) component));
    JPopupMenu popupMenu = getPopupMenu();

    if (popupMenu != null) {
      popupMenu.show(component, x, y);
    }
  }

  @Override // AbstractUserObject
  public void rightClicked(JComponent component, int x, int y, boolean isGroupView) {
    userObjectItems = getSelectedUserObjects(((JTree) component));
    JPopupMenu menu = getGroupPopupMenu();
    menu.show(component, x, y);
  }

  @Override // AbstractUserObject
  public JPopupMenu getPopupMenu() {
    JPopupMenu menu = new JPopupMenu();
    ResourceBundleUtil labels = ResourceBundleUtil.getBundle();

    JMenuItem item = new JMenuItem(labels.getString("tree.createGroup"));
    item.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent event) {
        Set<ModelComponent> items = new HashSet<>();
        Iterator<UserObject> it = userObjectItems.iterator();

        while (it.hasNext()) {
          UserObject next = it.next();
          ModelComponent dataObject = next.getModelComponent();
          items.add(dataObject);
        }
        OpenTCSView.instance().createGroup(items);
      }
    });

    menu.add(item);

    return menu;
  }

  @Override // FigureUserObject
  public ImageIcon getIcon() {
    return IconToolkit.instance().createImageIcon("tree/location.18x18.png");
  }

  private JPopupMenu getGroupPopupMenu() {
    JPopupMenu menu = new JPopupMenu();
    ResourceBundleUtil labels = ResourceBundleUtil.getBundle();

    JMenuItem item = new JMenuItem(labels.getString("tree.removeFromGroup"));
    item.addActionListener(new ActionListener() {

      @Override // ActionListener
      public void actionPerformed(ActionEvent e) {
        OpenTCSView.instance().removeGroupMembers(userObjectItems);
      }
    });

    menu.add(item);

    return menu;
  }
}
