/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.components.properties;

import javax.swing.JComponent;
import org.opentcs.guing.application.action.edit.UndoRedoManager;
import org.opentcs.guing.model.ModelComponent;

/**
 * Basisimplementierung für Inhalte zur Darstellung von Eigenschaften eines
 * ModelComponent-Objekts.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public abstract class AbstractAttributesContent
    implements AttributesContent {

  /**
   * Das Model, dessen Eigenschaften angezeigt werden sollen.
   */
  protected ModelComponent fModel;
  /**
   * Der Undo-Manager.
   */
  protected UndoRedoManager fUndoRedoManager;
  /**
   * Die eigene Swing-Komponente.
   */
  protected JComponent fComponent;

  /**
   * Creates a new instance of AbstractAttributesContent
   */
  public AbstractAttributesContent() {
  }

  @Override // AttributesContent
  public void setModel(ModelComponent model) {
    fModel = model;
  }

  @Override // AttributesContent
  public abstract void reset();

  @Override // AttributesContent
  public JComponent getComponent() {
    return fComponent;
  }

  @Override // AttributesContent
  public String getDescription() {
    return fModel.getDescription();
  }

  @Override // AttributesContent
  public void setup(UndoRedoManager undoManager) {
    fUndoRedoManager = undoManager;
    fComponent = createComponent();
  }

  /**
   * Fabrikmethode zur Erzeugung der Gesamtkomponente.
   *
   * @return
   */
  protected abstract JComponent createComponent();
}
