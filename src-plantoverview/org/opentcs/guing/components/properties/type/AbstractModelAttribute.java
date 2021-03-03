/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.components.properties.type;

import org.opentcs.guing.model.ModelComponent;

/**
 * Basisimplementierung für Attribute von ModelComponent-Objekten.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public abstract class AbstractModelAttribute
    implements ModelAttribute {

  /**
   * Das Model, zu dem dieses Attribut gehört.
   */
  private ModelComponent fModel;
  /**
   * Zeigt an, ob sich das Attribut geändert hat.
   */
  protected ChangeState fChangeState;
  /**
   * Die Bezeichnung des Attributs.
   */
  protected String fDescription;
  /**
   * Der Hilfetext.
   */
  protected String fHelptext;
  /**
   * Zeigt an, ob das Attribut gemeinsam mit gleichnamigen Attributen anderer
   * ModelComponent-Objekte bearbeitet werden kann.
   */
  protected boolean fCollectiveEditable;
  /**
   * Zeigt an, ob das Attribut im Modus "Modelling" verändert werden kann.
   */
  protected boolean fModellingEditable;
  /**
   * Zeigt an, ob das Attribut im Modus "Operating" verändert werden kann.
   */
  protected boolean fOperatingEditable;

  /**
   * Creates a new instance of AbstractModelAttribute
   *
   * @param model
   */
  public AbstractModelAttribute(ModelComponent model) {
    fModel = model;
    fChangeState = ChangeState.NOT_CHANGED;
    fDescription = "";
    fHelptext = "";
    fCollectiveEditable = false;
    fModellingEditable = true;
    fOperatingEditable = false;
  }

  @Override // ModelAttribute
  public ModelComponent getModel() {
    return fModel;
  }

  @Override // ModelAttribute
  public void setModel(ModelComponent model) {
    fModel = model;
  }

  @Override // ModelAttribute
  public void markChanged() {
    fChangeState = ChangeState.CHANGED;
  }

  @Override // ModelAttribute
  public void unmarkChanged() {
    fChangeState = ChangeState.NOT_CHANGED;
  }

  @Override // ModelAttribute
  public void setChangeState(ChangeState state) {
    fChangeState = state;
  }

  /**
   *
   * @return
   */
  public ChangeState getChangeState() {
    return fChangeState;
  }

  @Override // ModelAttribute
  public boolean hasChanged() {
    return (fChangeState != ChangeState.NOT_CHANGED);
  }

  @Override // ModelAttribute
  public void setDescription(String description) {
    fDescription = description;
  }

  @Override // ModelAttribute
  public String getDescription() {
    return fDescription;
  }

  @Override // ModelAttribute
  public void setHelptext(String helptext) {
    fHelptext = helptext;
  }

  @Override // ModelAttribute
  public String getHelptext() {
    return fHelptext;
  }

  @Override // ModelAttribute
  public void setCollectiveEditable(boolean collectiveEditable) {
    fCollectiveEditable = collectiveEditable;
  }

  @Override // ModelAttribute
  public boolean isCollectiveEditable() {
    return fCollectiveEditable;
  }

  @Override // ModelAttribute
  public void setModellingEditable(boolean editable) {
    fModellingEditable = editable;
  }

  @Override // ModelAttribute
  public boolean isModellingEditable() {
    return fModellingEditable;
  }

  @Override // ModelAttribute
  public void setOperatingEditable(boolean editable) {
    fOperatingEditable = editable;
  }

  @Override // ModelAttribute
  public boolean isOperatingEditable() {
    return fOperatingEditable;
  }
}
