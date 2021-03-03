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

import static java.util.Objects.requireNonNull;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.components.drawing.OpenTCSDrawingEditor;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.ModelManager;

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
   * The view.
   */
  private final OpenTCSView view;
  /**
   * The drawing editor.
   */
  private final OpenTCSDrawingEditor editor;
  /**
   * The model manager.
   */
  private final ModelManager modelManager;
  /**
   * The parent object.
   */
  private ModelComponent parent;

  /**
   * Creates a new instance.
   *
   * @param modelComponent The corresponding model component.
   * @param view The application's main view.
   * @param editor The application's drawing editor.
   * @param modelManager Provides access to the currently loaded system model.
   */
  public AbstractUserObject(ModelComponent modelComponent,
                            OpenTCSView view,
                            OpenTCSDrawingEditor editor,
                            ModelManager modelManager) {
    this.fModelComponent = requireNonNull(modelComponent, "modelComponent");
    this.view = requireNonNull(view, "view");
    this.editor = requireNonNull(editor, "editor");
    this.modelManager = requireNonNull(modelManager, "modelManager");
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
    getView().selectModelComponent(getModelComponent());
  }

  @Override // UserObject
  public boolean removed() {
    return getView().treeComponentRemoved(fModelComponent);
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
    return new JPopupMenu();
  }

  @Override // UserObject
  public ImageIcon getIcon() {
    return null;
  }

  /**
   * Wird aufgerufen, wenn mehrere Objekte im Baum selektiert werden sollen.
   */
  public void selectMultipleObjects() {
    getView().addSelectedModelComponent(getModelComponent());
  }

  @Override
  public ModelComponent getParent() {
    return parent;
  }

  @Override
  public void setParent(ModelComponent parent) {
    this.parent = parent;
  }

  protected OpenTCSView getView() {
    return view;
  }

  protected OpenTCSDrawingEditor getEditor() {
    return editor;
  }

  protected ModelManager getModelManager() {
    return modelManager;
  }
}
