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
import javax.annotation.Nullable;
import javax.inject.Inject;
import org.opentcs.access.CredentialsException;
import org.opentcs.access.Kernel;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.StaticRoute;
import org.opentcs.data.model.visualization.ElementPropKeys;
import org.opentcs.data.model.visualization.ModelLayoutElement;
import org.opentcs.data.model.visualization.VisualLayout;
import org.opentcs.guing.components.properties.type.ColorProperty;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.exchange.EventDispatcher;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.elements.PointModel;
import org.opentcs.guing.model.elements.StaticRouteModel;
import org.opentcs.guing.storage.PlantModelCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static java.util.Objects.requireNonNull;

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
  private static final Logger log
      = LoggerFactory.getLogger(StaticRouteAdapter.class);

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
      log.warn("", e);
    }
  }

  @Override // OpenTCSProcessAdapter
  public void updateProcessProperties(Kernel kernel, PlantModelCache plantModel) {
    StaticRoute staticRoute = kernel.createStaticRoute();
    TCSObjectReference<StaticRoute> reference = staticRoute.getReference();

    StringProperty pName = (StringProperty) getModel().getProperty(
        ModelComponent.NAME);
    String name = pName.getText();

    try {
      kernel.renameTCSObject(reference, name);

      kernel.clearStaticRouteHops(reference);

      for (ModelComponent model : getModel().getChildComponents()) {
        Point hop = plantModel.getPoints().get(model.getName());
        kernel.addStaticRouteHop(reference, hop.getReference());
      }

      for (VisualLayout layout : plantModel.getVisualLayouts()) {
        updateLayoutElement(layout, reference);
      }

      updateMiscProcessProperties(kernel, reference);
    }
    catch (KernelRuntimeException e) {
      log.warn("", e);
    }
  }

  private void updateLayoutElement(VisualLayout layout,
                                   TCSObjectReference<?> ref) {
    ModelLayoutElement layoutElement = new ModelLayoutElement(ref);

    ColorProperty pColor = (ColorProperty) getModel().getProperty(ElementPropKeys.BLOCK_COLOR);
    int rgb = pColor.getColor().getRGB() & 0x00FFFFFF;  // mask alpha bits
    layoutElement.getProperties().put(ElementPropKeys.BLOCK_COLOR, String.format("#%06X", rgb));

    layout.getLayoutElements().add(layoutElement);
  }
}
