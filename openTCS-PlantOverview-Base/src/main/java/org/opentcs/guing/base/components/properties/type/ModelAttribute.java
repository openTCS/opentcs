/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.base.components.properties.type;

import java.io.Serializable;
import org.opentcs.guing.base.model.ModelComponent;

/**
 * Interface to specify how an attribute of a model component must appear.
 */
public interface ModelAttribute
    extends Serializable {

  /**
   * Potential change states of the attribute.
   */
  enum ChangeState {
    /**
     * The attribute has not changed.
     */
    NOT_CHANGED,
    /**
     * The attribute has changed.
     */
    CHANGED,
    /**
     * (Only) A detail of the attribute has changed.
     */
    DETAIL_CHANGED,
  };

  /**
   * Returns the model component this attribute is attached to.
   *
   * @return The model component.
   */
  ModelComponent getModel();

  /**
   * Sets the model component this attribute is attached to.
   *
   * @param model The model component.
   */
  void setModel(ModelComponent model);

  /**
   * Marks the attribute as changed.
   */
  void markChanged();

  /**
   * Marks the attribute as not changed.
   */
  void unmarkChanged();

  /**
   * Sets the change state for this attribute.
   *
   * @param changeState The new change state.
   */
  void setChangeState(AbstractModelAttribute.ChangeState changeState);

  /**
   * Returns whether or not the attribute has changed.
   *
   * @return {@code true}, if the state of the model attribute has changed, otherwiese
   * {@code false}.
   */
  boolean hasChanged();

  /**
   * Sets the description of the attribute.
   *
   * @param description The description.
   */
  void setDescription(String description);

  /**
   * Returns the description of the attribute.
   *
   * @return The description.
   */
  String getDescription();

  /**
   * Sets the tooltip text for this attribute.
   *
   * @param helptext The tooltip text.
   */
  void setHelptext(String helptext);

  /**
   * Returns the tooltip text for this attribute.
   *
   * @return The helptext.
   */
  String getHelptext();

  /**
   *
   * Sets whether or not the attribute is collectively editable with attributes
   * of the same name of other model components.
   *
   * @param collectiveEditable Whether the attribute is collectively editable with attributes
   * of the same name of other model components.
   */
  void setCollectiveEditable(boolean collectiveEditable);

  /**
   * Returns whether or not the attribute is collectively editable with attributes of the same name
   * of other model components.
   *
   * @return Whether the attribute is collectively editable with attributes of the same name
   * of other model components.
   */
  boolean isCollectiveEditable();

  /**
   * Sets whether or not the attribute can be changed in modelling mode.
   *
   * @param editable True if the attribute can be changed in modelling mode.
   */
  void setModellingEditable(boolean editable);

  /**
   * Returns whether or not the attribute can be changed in modelling mode.
   *
   * @return True if the attribute can be changed in modelling mode.
   */
  boolean isModellingEditable();

  /**
   * Sets whether or not the attribute can be changed in operating mode.
   *
   * @param editable True if the attribute can be changed in operating mode.
   */
  void setOperatingEditable(boolean editable);

  /**
   * Returns whether or not the attribute can be changed in operating mode.
   *
   * @return True if the attribute can be changed in operating mode.
   */
  boolean isOperatingEditable();

  /**
   * Whether this attribute should be included when persisting the model to a file.
   *
   * @return true, if this attribute should be included when persisting the model to a file.
   */
  default boolean isPersistent() {
    return true;
  }
}
