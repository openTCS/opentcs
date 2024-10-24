// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.common.components.tree.elements;

import org.opentcs.guing.base.model.CompositeModelComponent;
import org.opentcs.guing.base.model.elements.BlockModel;
import org.opentcs.guing.base.model.elements.LayoutModel;
import org.opentcs.guing.base.model.elements.LinkModel;
import org.opentcs.guing.base.model.elements.LocationModel;
import org.opentcs.guing.base.model.elements.LocationTypeModel;
import org.opentcs.guing.base.model.elements.PathModel;
import org.opentcs.guing.base.model.elements.PointModel;
import org.opentcs.guing.base.model.elements.VehicleModel;

/**
 */
public interface UserObjectFactory {

  BlockUserObject createBlockUserObject(BlockModel model, UserObjectContext context);

  LayoutUserObject createLayoutUserObject(LayoutModel model);

  LinkUserObject createLinkUserObject(LinkModel model);

  LocationTypeUserObject createLocationTypeUserObject(LocationTypeModel model);

  LocationUserObject createLocationUserObject(LocationModel model, UserObjectContext context);

  PathUserObject createPathUserObject(PathModel model, UserObjectContext context);

  PointUserObject createPointUserObject(PointModel model, UserObjectContext context);

  SimpleFolderUserObject createSimpleFolderUserObject(CompositeModelComponent model);

  VehicleUserObject createVehicleUserObject(VehicleModel model);

  ComponentContext createComponentContext();

  BlockContext createBlockContext();

  NullContext createNullContext();
}
