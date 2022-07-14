/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.base.components.properties.type;

import org.opentcs.guing.base.model.ModelComponent;

/**
 * Attribute of a {@link ModelComponent}.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public abstract class AbstractModelAttribute
    implements ModelAttribute {

  /**
   * The model this attribute is attached to.
   */
  private ModelComponent fModel;
  /**
   * Indicates that this attribute has changed.
   */
  private ChangeState fChangeState = ChangeState.NOT_CHANGED;
  /**
   * Description of this attribute.
   */
  private String fDescription = "";
  /**
   * Tooltip text.
   */
  private String fHelptext = "";
  /**
   * Indicates whether or not this attribute can simultaneously be edited with other
   * attributes of the same name of other model components.
   */
  private boolean fCollectiveEditable;
  /**
   * Indicates whether or not this attribute can be changed in modeling mode.
   */
  private boolean fModellingEditable = true;
  /**
   * Indicates whether or not this attribute can be changed in operating mode.
   */
  private boolean fOperatingEditable;

  /**
   * Creates a new instance.
   *
   * @param model The model component.
   */
  public AbstractModelAttribute(ModelComponent model) {
    fModel = model;
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
   * Returns the change state of this attribute.
   * @return The change state.
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
