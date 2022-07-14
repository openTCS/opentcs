/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
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
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface UserObjectFactory {

  BlockUserObject createBlockUserObject(BlockModel model, UserObjectContext context);

//  FigureUserObject createFigureUserObject(AbstractFigureComponent model);

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
