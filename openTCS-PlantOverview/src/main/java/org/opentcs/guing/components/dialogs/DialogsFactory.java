/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.dialogs;

import java.util.List;
import org.opentcs.guing.model.elements.PointModel;
import org.opentcs.guing.model.elements.StaticRouteModel;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface DialogsFactory {

  EditStaticRoutePanel createEditStaticRoutePanel(StaticRouteModel staticRoute,
                                                  List<PointModel> allPoints);

  AddNodesToStaticRoutePanel createAddNodesToStaticRoutePanel(
      PointModel point, List<PointModel> allPoints);
}
