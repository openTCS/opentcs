/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.exchange.adapter;

import org.opentcs.guing.exchange.EventDispatcher;
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
public interface ProcessAdapterFactory {

  PointAdapter createPointAdapter(PointModel model, EventDispatcher dispatcher);

  PathAdapter createPathAdapter(PathModel model, EventDispatcher dispatcher);

  LocationTypeAdapter createLocTypeAdapter(LocationTypeModel model,
                                           EventDispatcher dispatcher);

  LocationAdapter createLocationAdapter(LocationModel model,
                                        EventDispatcher dispatcher);

  LinkAdapter createLinkAdapter(LinkModel model, EventDispatcher dispatcher);

  VehicleAdapter createVehicleAdapter(VehicleModel model,
                                      EventDispatcher dispatcher);

  BlockAdapter createBlockAdapter(BlockModel model, EventDispatcher dispatcher);

  GroupAdapter createGroupAdapter(GroupModel model, EventDispatcher dispatcher);

  StaticRouteAdapter createStaticRouteAdapter(StaticRouteModel model,
                                              EventDispatcher dispatcher);

  LayoutAdapter createLayoutAdapter(LayoutModel model,
                                    EventDispatcher dispatcher);
}
