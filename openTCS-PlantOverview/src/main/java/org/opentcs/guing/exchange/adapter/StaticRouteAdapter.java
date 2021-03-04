/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.exchange.adapter;

import java.util.LinkedList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nullable;
import org.opentcs.access.to.model.ModelLayoutElementCreationTO;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.access.to.model.VisualLayoutCreationTO;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.visualization.ElementPropKeys;
import org.opentcs.data.model.visualization.ModelLayoutElement;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.SystemModel;
import org.opentcs.guing.model.elements.PointModel;
import org.opentcs.guing.model.elements.StaticRouteModel;
import org.opentcs.util.Colors;

/**
 * An adapter for static routes.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Stefan Walter (Fraunhofer IML)
 */
@SuppressWarnings("deprecation")
public class StaticRouteAdapter
    extends AbstractProcessAdapter {

  @Override // OpenTCSProcessAdapter
  public void updateModelProperties(TCSObject<?> tcsObject,
                                    ModelComponent modelComponent,
                                    SystemModel systemModel,
                                    TCSObjectService objectService,
                                    @Nullable ModelLayoutElement layoutElement) {
    org.opentcs.data.model.StaticRoute route
        = requireNonNull((org.opentcs.data.model.StaticRoute) tcsObject, "tcsObject");
    StaticRouteModel model = (StaticRouteModel) modelComponent;

    model.getPropertyName().setText(route.getName());

    model.removeAllPoints();

    for (TCSObjectReference<Point> pointRef : route.getHops()) {
      PointModel hop = systemModel.getPointModel(pointRef.getName());
      model.addPoint(hop);
    }

    updateMiscModelProperties(model, route);

    if (layoutElement != null) {
      updateModelLayoutProperties(model, layoutElement);
    }
  }

  @Override // OpenTCSProcessAdapter
  public PlantModelCreationTO storeToPlantModel(ModelComponent modelComponent,
                                                SystemModel systemModel,
                                                PlantModelCreationTO plantModel) {
    return plantModel
        .withStaticRoute(
            new org.opentcs.access.to.model.StaticRouteCreationTO(modelComponent.getName())
                .withHopNames(getHopNames((StaticRouteModel) modelComponent))
                .withProperties(getKernelProperties(modelComponent))
        )
        .withVisualLayouts(updatedLayouts(modelComponent, plantModel.getVisualLayouts()));
  }

  private void updateModelLayoutProperties(StaticRouteModel model,
                                           ModelLayoutElement layoutElement) {
    String sBlockColor = layoutElement.getProperties().get(ElementPropKeys.BLOCK_COLOR);
    if (sBlockColor != null) {
      model.getPropertyColor().setColor(Colors.decodeFromHexRGB(sBlockColor));
    }
  }

  private List<String> getHopNames(StaticRouteModel staticRouteModel) {
    List<String> result = new LinkedList<>();
    for (ModelComponent model : staticRouteModel.getChildComponents()) {
      result.add(model.getName());
    }
    return result;
  }

  @Override
  protected VisualLayoutCreationTO updatedLayout(ModelComponent model,
                                                 VisualLayoutCreationTO layout) {
    StaticRouteModel staticRouteModel = (StaticRouteModel) model;

    return layout.withModelElement(
        new ModelLayoutElementCreationTO(staticRouteModel.getName())
            .withProperty(ElementPropKeys.BLOCK_COLOR,
                          Colors.encodeToHexRGB(staticRouteModel.getPropertyColor().getColor()))
    );
  }

}
