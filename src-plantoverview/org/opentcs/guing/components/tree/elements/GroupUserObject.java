/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.components.tree.elements;

import org.opentcs.guing.model.elements.GroupModel;

/**
 * A folder for a Group in the "Group" tree.
 *
 * @author Heinz Huber (Fraunhofer IML)
 */
public class GroupUserObject
    extends AbstractUserObject {

  /**
   * Creates a new instance.
   *
   * @param model The corresponding model object.
   */
  public GroupUserObject(GroupModel model) {
    super(model);
  }

  @Override
  public GroupModel getModelComponent() {
    return (GroupModel) super.getModelComponent();
  }
}
