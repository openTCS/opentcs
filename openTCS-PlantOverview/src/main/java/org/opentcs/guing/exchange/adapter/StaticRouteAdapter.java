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

import com.google.inject.assistedinject.Assisted;
import java.util.LinkedList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import org.opentcs.access.CredentialsException;
import org.opentcs.access.Kernel;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.to.model.ModelLayoutElementCreationTO;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.access.to.model.StaticRouteCreationTO;
import org.opentcs.access.to.model.VisualLayoutCreationTO;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.StaticRoute;
import org.opentcs.data.model.visualization.ElementPropKeys;
import org.opentcs.data.model.visualization.ModelLayoutElement;
import org.opentcs.guing.components.properties.type.ColorProperty;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.exchange.EventDispatcher;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.elements.PointModel;
import org.opentcs.guing.model.elements.StaticRouteModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An adapter for static routes.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class StaticRouteAdapter
    extends AbstractProcessAdapter {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(StaticRouteAdapter.class);

  /**
   * Creates a new instance.
   *
   * @param model The corresponding model component.
   * @param eventDispatcher The event dispatcher.
   */
  @Inject
  public StaticRouteAdapter(@Assisted StaticRouteModel model,
                            @Assisted EventDispatcher eventDispatcher) {
    super(model, eventDispatcher);
  }

  @Override
  public StaticRouteModel getModel() {
    return (StaticRouteModel) super.getModel();
  }

  @Override // OpenTCSProcessAdapter
  public void updateModelProperties(Kernel kernel,
                                    TCSObject<?> tcsObject,
                                    @Nullable ModelLayoutElement layoutElement) {
    StaticRoute route = requireNonNull((StaticRoute) tcsObject, "tcsObject");

    try {
      StringProperty name
          = (StringProperty) getModel().getProperty(ModelComponent.NAME);
      name.setText(route.getName());

      getModel().removeAllPoints();

      for (TCSObjectReference<Point> pointRef : route.getHops()) {
        ProcessAdapter adapter
            = getEventDispatcher().findProcessAdapter(pointRef);
        getModel().addPoint((PointModel) adapter.getModel());
      }

      updateMiscModelProperties(route);
    }
    catch (CredentialsException e) {
      LOG.warn("", e);
    }
  }

  @Override // OpenTCSProcessAdapter
  public void storeToPlantModel(PlantModelCreationTO plantModel) {
    try {
      plantModel.getStaticRoutes().add(
          new StaticRouteCreationTO(getModel().getName())
              .setHopNames(getHopNames())
              .setProperties(getKernelProperties()));

      for (VisualLayoutCreationTO layout : plantModel.getVisualLayouts()) {
        updateLayoutElement(layout);
      }
    }
    catch (KernelRuntimeException e) {
      LOG.warn("", e);
    }
  }

  private List<String> getHopNames() {
    List<String> result = new LinkedList<>();
    for (ModelComponent model : getModel().getChildComponents()) {
      result.add(model.getName());
    }
    return result;
  }

  private void updateLayoutElement(VisualLayoutCreationTO layout) {
    ColorProperty pColor = (ColorProperty) getModel().getProperty(ElementPropKeys.BLOCK_COLOR);
    int rgb = pColor.getColor().getRGB() & 0x00FFFFFF;  // mask alpha bits

    layout.getModelElements().add(
        new ModelLayoutElementCreationTO(getModel().getName())
            .setProperty(ElementPropKeys.BLOCK_COLOR, String.format("#%06X", rgb))
    );
  }

}
