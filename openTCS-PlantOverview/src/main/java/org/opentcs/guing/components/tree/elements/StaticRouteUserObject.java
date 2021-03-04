/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.tree.elements;

import com.google.inject.assistedinject.Assisted;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.opentcs.guing.application.ApplicationState;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.application.OperationMode;
import org.opentcs.guing.components.dialogs.DialogsFactory;
import org.opentcs.guing.components.dialogs.EditStaticRoutePanel;
import org.opentcs.guing.components.dialogs.StandardContentDialog;
import org.opentcs.guing.components.drawing.OpenTCSDrawingEditor;
import org.opentcs.guing.components.drawing.OpenTCSDrawingView;
import org.opentcs.guing.model.ModelManager;
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
 * @author Stefan Walter (Fraunhofer IML)
 */
public class StaticRouteUserObject
    extends AbstractUserObject {

  /**
   * Stores the application's current state.
   */
  private final ApplicationState appState;
  /**
   * A factory for dialog-related components.
   */
  private final DialogsFactory dialogsFactory;

  /**
   * Creates a new instance.
   *
   * @param modelComponent The corresponding model component.
   * @param appState Stores the application's current state.
   * @param view The openTCS view.
   * @param editor The application's drawing editor.
   * @param modelManager Provides the currently loaded system model.
   * @param dialogsFactory A factory for dialogs-related components.
   */
  @Inject
  public StaticRouteUserObject(@Assisted StaticRouteModel modelComponent,
                               ApplicationState appState,
                               OpenTCSView view,
                               OpenTCSDrawingEditor editor,
                               ModelManager modelManager,
                               DialogsFactory dialogsFactory) {
    super(modelComponent, view, editor, modelManager);
    this.appState = requireNonNull(appState, "appState");
    this.dialogsFactory = requireNonNull(dialogsFactory, "dialogsFactory");
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
            = dialogsFactory.createEditStaticRoutePanel(
                getModelComponent(), getModelManager().getModel().getPointModels());
        StandardContentDialog dialog = new StandardContentDialog(getView(), panel);
        dialog.setVisible(true);
      }
    });

    item.setEnabled(appState.hasOperationMode(OperationMode.MODELLING));
    menu.add(item);

    menu.addSeparator();

    item = new JMenuItem(labels.getString("staticRouteUserObject.selectAll"));
    item.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent event) {
        OpenTCSDrawingView drawingView = getEditor().getActiveView();
        drawingView.highlightStaticRoute(getModelComponent());
      }
    });
    menu.add(item);

    return menu;
  }

  @Override // AbstractUserObject
  public void doubleClicked() {
    getView().blockSelected(getModelComponent());
  }

  @Override // AbstractUserObject
  public ImageIcon getIcon() {
    return IconToolkit.instance().createImageIcon("tree/staticRoute.18x18.png");
  }
}
