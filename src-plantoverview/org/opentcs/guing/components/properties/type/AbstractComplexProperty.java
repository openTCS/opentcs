/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.components.properties.type;

import javax.swing.JPanel;
import org.opentcs.guing.components.dialogs.DetailsDialogContent;
import org.opentcs.guing.components.dialogs.DialogContent;
import org.opentcs.guing.model.ModelComponent;

/**
 * Abstrakte Oberklasse für Attribute, für deren Bearbeitung günstigerweise
 * immer ein DetailsDialog eingesetzt werden sollte. Eine Bearbeitung über
 * Textfeld, ComboBox oder CheckBox ist nicht oder nur schwer möglich.
 * <p>
 * AbstractComplexProperty verwaltet die Klasse eines {
 *
 * @see DialogContent}, mit dem die Bearbeitung durch den Benutzer erfolgen
 * kann. Bei Bedarf, d.h. wenn der Benutzer das Attribut bearbeiten möchte, wird
 * ein Objekt dieser Klasse erzeugt.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public abstract class AbstractComplexProperty
    extends AbstractProperty {

  /**
   * The class of the panel to be used for editing the property. Must implement
   * {@link DetailsDialogContent}.
   */
  private Class<? extends JPanel> fPropertyEditorPanel;

  /**
   * Creates a new instance.
   *
   * @param model
   * @param editorPanel The class of the panel for editing the property.
   */
  public AbstractComplexProperty(ModelComponent model, Class<? extends JPanel> editorPanel) {
    super(model);
    this.fPropertyEditorPanel = editorPanel;
  }


  /**
   * Setzt die Klasse des DialogContent, mit dem die Bearbeitung des Attributs
   * erfolgt. Wird nur verwendet, wenn ein anderer DialogContent als der mit {
   *
   * @param editorPanel
   * @see setDefaultPropertyEditor(Class)} standardmäßig gesetzte verwendet
   * werden soll.
   */
  public void setPropertyEditor(Class<? extends JPanel> editorPanel) {
    fPropertyEditorPanel = editorPanel;
  }

  /**
   * Returns the class of the panel to be used for editing the property.
   *
   * @return The class of the panel to be used for editing the property.
   */
  public Class<? extends JPanel> getPropertyEditorPanel() {
    return fPropertyEditorPanel;
  }
}
