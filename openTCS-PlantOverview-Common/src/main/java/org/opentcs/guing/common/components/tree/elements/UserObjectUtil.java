/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.tree.elements;

import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.opentcs.guing.base.model.CompositeModelComponent;
import org.opentcs.guing.base.model.ModelComponent;
import org.opentcs.guing.base.model.elements.BlockModel;
import org.opentcs.guing.base.model.elements.LayoutModel;
import org.opentcs.guing.base.model.elements.LinkModel;
import org.opentcs.guing.base.model.elements.LocationModel;
import org.opentcs.guing.base.model.elements.LocationTypeModel;
import org.opentcs.guing.base.model.elements.PathModel;
import org.opentcs.guing.base.model.elements.PointModel;
import org.opentcs.guing.base.model.elements.VehicleModel;
import org.opentcs.guing.common.components.tree.elements.UserObjectContext.ContextType;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class UserObjectUtil {

  private final UserObjectFactory factory;

  /**
   * Creates a new instance.
   *
   * @param factory A factory for user objects.
   */
  @Inject
  public UserObjectUtil(UserObjectFactory factory) {
    this.factory = requireNonNull(factory, "factory");
  }

  public UserObject createUserObject(ModelComponent model, UserObjectContext context) {
    requireNonNull(model, "model");

    if (model instanceof BlockModel) {
      return factory.createBlockUserObject((BlockModel) model, context);
    }
    else if (model instanceof LayoutModel) {
      return factory.createLayoutUserObject((LayoutModel) model);
    }
    else if (model instanceof LinkModel) {
      return factory.createLinkUserObject((LinkModel) model);
    }
    else if (model instanceof LocationTypeModel) {
      return factory.createLocationTypeUserObject((LocationTypeModel) model);
    }
    else if (model instanceof LocationModel) {
      return factory.createLocationUserObject((LocationModel) model, context);
    }
    else if (model instanceof PathModel) {
      return factory.createPathUserObject((PathModel) model, context);
    }
    else if (model instanceof PointModel) {
      return factory.createPointUserObject((PointModel) model, context);
    }
    else if (model instanceof VehicleModel) {
      return factory.createVehicleUserObject((VehicleModel) model);
    }
    else if (model instanceof CompositeModelComponent) {
      return factory.createSimpleFolderUserObject((CompositeModelComponent) model);
    }

    throw new IllegalArgumentException("Unhandled component class "
        + model.getClass());
  }

  public UserObjectContext createContext(ContextType type) {
    switch (type) {
      case COMPONENT:
        return factory.createComponentContext();
      case BLOCK:
        return factory.createBlockContext();
      default:
        return factory.createNullContext();
    }
  }
}
