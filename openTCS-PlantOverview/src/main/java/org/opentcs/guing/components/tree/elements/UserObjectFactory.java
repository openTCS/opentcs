/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.tree.elements;

import org.opentcs.guing.model.CompositeModelComponent;
import org.opentcs.guing.model.elements.BlockModel;
import org.opentcs.guing.model.elements.GroupModel;
import org.opentcs.guing.model.elements.LayoutModel;
import org.opentcs.guing.model.elements.LinkModel;
import org.opentcs.guing.model.elements.LocationModel;
import org.opentcs.guing.model.elements.LocationTypeModel;
import org.opentcs.guing.model.elements.PathModel;
import org.opentcs.guing.model.elements.PointModel;
import org.opentcs.guing.model.elements.StaticRouteModel;
import org.opentcs.guing.model.elements.VehicleModel;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface UserObjectFactory {

  BlockUserObject createBlockUserObject(BlockModel model, UserObjectContext context);

//  FigureUserObject createFigureUserObject(AbstractFigureComponent model);
  GroupUserObject createGroupUserObject(GroupModel model);

  LayoutUserObject createLayoutUserObject(LayoutModel model);

  LinkUserObject createLinkUserObject(LinkModel model);

  LocationTypeUserObject createLocationTypeUserObject(LocationTypeModel model);

  LocationUserObject createLocationUserObject(LocationModel model, UserObjectContext context);

  PathUserObject createPathUserObject(PathModel model, UserObjectContext context);

  PointUserObject createPointUserObject(PointModel model, UserObjectContext context);

  SimpleFolderUserObject createSimpleFolderUserObject(CompositeModelComponent model);

  StaticRouteUserObject createStaticRouteUserObject(StaticRouteModel model);

  VehicleUserObject createVehicleUserObject(VehicleModel model);

  ComponentContext createComponentContext();

  BlockContext createBlockContext();

  GroupContext createGroupContext();

  NullContext createNullContext();
}
