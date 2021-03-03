/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.components.tree.elements;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.opentcs.guing.application.GuiManager;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.components.dialogs.EditStaticRoutePanel;
import org.opentcs.guing.components.dialogs.StandardContentDialog;
import org.opentcs.guing.components.drawing.OpenTCSDrawingView;
import org.opentcs.guing.model.elements.StaticRouteModel;
import org.opentcs.guing.util.IconToolkit;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * Eine statische Route in der Baumansicht.
 * <p>
 * <b>Entwurfsmuster:</b> Befehl.
 * StaticRouteUserObject ist ein konkreter Befehl.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class StaticRouteUserObject
    extends AbstractUserObject {

  /**
   * Erzeugt ein neues Objekt von StaticRouteUserObject.
   *
   * @param modelComponent das Datenmodell
   */
  public StaticRouteUserObject(StaticRouteModel modelComponent) {
    super(modelComponent);
  }

  @Override
  public StaticRouteModel getModelComponent() {
    return (StaticRouteModel) super.getModelComponent();
  }

  @Override // AbstractUserObject
  public JPopupMenu getPopupMenu() {
    ResourceBundleUtil labels = ResourceBundleUtil.getBundle();
    JPopupMenu menu = new JPopupMenu();
    JMenuItem item = new JMenuItem(labels.getString("staticRouteUserObject.edit"));
    item.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent event) {
        EditStaticRoutePanel panel
            = new EditStaticRoutePanel(getModelComponent(),
                                       OpenTCSView.instance().getSystemModel().getPointModels());
        StandardContentDialog dialog = new StandardContentDialog(OpenTCSView.instance(), panel);
        dialog.setVisible(true);
      }
    });

    item.setEnabled(OpenTCSView.instance().getOperationMode() == GuiManager.OperationMode.MODELLING);
    menu.add(item);

    menu.addSeparator();

    item = new JMenuItem(labels.getString("staticRouteUserObject.selectAll"));
    item.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent event) {
        OpenTCSDrawingView drawingView = OpenTCSView.instance().getDrawingView();
        drawingView.highlightStaticRoute(getModelComponent());
      }
    });
    menu.add(item);

    return menu;
  }

  @Override // AbstractUserObject
  public void doubleClicked() {
    OpenTCSView.instance().blockSelected(getModelComponent());
  }

  @Override // AbstractUserObject
  public ImageIcon getIcon() {
    return IconToolkit.instance().createImageIcon("tree/staticRoute.18x18.png");
  }
}
