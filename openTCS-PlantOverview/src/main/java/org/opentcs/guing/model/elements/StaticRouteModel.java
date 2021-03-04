/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.model.elements;

import com.google.common.collect.Lists;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jhotdraw.draw.Figure;
import org.opentcs.data.model.visualization.ElementPropKeys;
import org.opentcs.guing.components.properties.event.AttributesChangeListener;
import org.opentcs.guing.components.properties.type.ColorProperty;
import org.opentcs.guing.components.properties.type.KeyValueSetProperty;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.components.properties.type.StringSetProperty;
import org.opentcs.guing.event.StaticRouteChangeEvent;
import org.opentcs.guing.event.StaticRouteChangeListener;
import org.opentcs.guing.model.FiguresFolder;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * A static route.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class StaticRouteModel
    extends FiguresFolder {

  /**
   * Key for the elements.
   */
  public static final String ELEMENTS = "staticRouteElements";
  /**
   * The listeners that get informed on changes.
   */
  private List<StaticRouteChangeListener> fListeners = new ArrayList<>();

  /**
   * Creates a new instance.
   */
  public StaticRouteModel() {
    createProperties();
  }

  /**
   * Returns the first point of this static route.
   *
   * @return The first Point of this route.
   */
  public PointModel getStartPoint() {
    if (getChildComponents().isEmpty()) {
      return null;
    }
    return (PointModel) getChildComponents().get(0);
  }

  /**
   * Returns the last point of this static route.
   *
   * @return The last Point of this route.
   */
  public PointModel getEndPoint() {
    if (getChildComponents().isEmpty()) {
      return null;
    }
    return (PointModel) getChildComponents().get(getChildComponents().size() - 1);
  }

  @Override // FiguresFolder
  public Iterator<Figure> figures() {
    List<Figure> figures = new ArrayList<>();

    Iterator<ModelComponent> ePoints = getChildComponents().iterator();
    if (ePoints.hasNext()) {
      PointModel startPoint = (PointModel) ePoints.next();
      figures.add(startPoint.getFigure());

      while (ePoints.hasNext()) {
        PointModel nextPoint = (PointModel) ePoints.next();
        AbstractConnection path = startPoint.getConnectionTo(nextPoint);
        if (path == null) {
          path = nextPoint.getConnectionTo(startPoint);
        }
        if (path != null) {
          figures.add(path.getFigure());
        }

        figures.add(nextPoint.getFigure());
        startPoint = nextPoint;
      }
    }

    return figures.iterator();
  }

  /**
   * Removes a point from this static route.
   *
   * @param point The model to remove.
   */
  public void removePoint(PointModel point) {
    if (contains(point)) {
      remove(point);
      getPropertyElements().getItems().remove(point.getName());
    }
  }

  /**
   * Adds a point to this static route.
   *
   * @param point The model to add.
   */
  public void addPoint(PointModel point) {
    add(point);
    if (!getPropertyElements().getItems().contains(point.getName())) {
      getPropertyElements().addItem(point.getName());
    }
  }

  /**
   * Removes all points from this static route.
   */
  public void removeAllPoints() {
    for (Object o : new ArrayList<>(Lists.reverse(getChildComponents()))) {
      remove((ModelComponent) o);
    }
    getPropertyElements().getItems().clear();
  }

  /**
   * Returns the color of this static route.
   *
   * @return The color.
   */
  public Color getColor() {
    return getPropertyColor().getColor();
  }

  /**
   * Informs all listeners that the color has changed.
   */
  public void colorChanged() {
    for (StaticRouteChangeListener listener : fListeners) {
      listener.colorChanged(new StaticRouteChangeEvent(this));
    }
  }

  @Override  // AbstractModelComponent
  public void propertiesChanged(AttributesChangeListener l) {
    if (getPropertyColor().hasChanged()) {
      colorChanged();
    }

    super.propertiesChanged(l);
  }

  @Override  // AbstractModelComponent
  public String getTreeViewName() {
    return getName();
  }

  @Override  // AbstractModelComponent
  public String getDescription() {
    return ResourceBundleUtil.getBundle().getString("staticRoute.description.text");
  }

  /**
   * Removes a listener.
   *
   * @param listener The listener to remove.
   */
  public void removeStaticRouteChangeListener(StaticRouteChangeListener listener) {
    fListeners.remove(listener);
  }

  /**
   * Adds a listener.
   *
   * @param listener The listener to add.
   */
  public void addStaticRouteChangeListener(StaticRouteChangeListener listener) {
    if (fListeners == null) {
      fListeners = new ArrayList<>();
    }

    if (!fListeners.contains(listener)) {
      fListeners.add(listener);
    }
  }

  /**
   * Informs all listeners that the points of this static route have changed.
   */
  public void pointsChanged() {
    for (StaticRouteChangeListener listener : fListeners) {
      listener.pointsChanged(new StaticRouteChangeEvent(this));
    }
  }

  public ColorProperty getPropertyColor() {
    return (ColorProperty) getProperty(ElementPropKeys.BLOCK_COLOR);
  }

  public StringSetProperty getPropertyElements() {
    return (StringSetProperty) getProperty(ELEMENTS);
  }

  public KeyValueSetProperty getPropertyMiscellaneous() {
    return (KeyValueSetProperty) getProperty(MISCELLANEOUS);
  }

  /**
   * The properties of a Static Route:
   * - The name shown in the "Components" tree
   * - The color used to decorate the hop-points in the DrawingView.
   */
  private void createProperties() {
    ResourceBundleUtil bundle = ResourceBundleUtil.getBundle();

    StringProperty pName = new StringProperty(this);
    pName.setDescription(bundle.getString("staticRoute.name.text"));
    pName.setHelptext(bundle.getString("staticRoute.name.helptext"));
    setProperty(NAME, pName);

    ColorProperty pColor = new ColorProperty(this, Color.red);
    pColor.setDescription(bundle.getString("element.staticRouteColor.text"));
    pColor.setHelptext(bundle.getString("element.staticRouteColor.helptext"));
    setProperty(ElementPropKeys.BLOCK_COLOR, pColor);

    StringSetProperty pElements = new StringSetProperty(this);
    pElements.setDescription(bundle.getString("staticroute.elements.text"));
    pElements.setModellingEditable(false);
    pElements.setOperatingEditable(false);
    setProperty(ELEMENTS, pElements);

    KeyValueSetProperty pMiscellaneous = new KeyValueSetProperty(this);
    pMiscellaneous.setDescription(bundle.getString("staticRoute.miscellaneous.text"));
    pMiscellaneous.setHelptext(bundle.getString("staticRoute.miscellaneous.helptext"));
    setProperty(MISCELLANEOUS, pMiscellaneous);
  }
}
