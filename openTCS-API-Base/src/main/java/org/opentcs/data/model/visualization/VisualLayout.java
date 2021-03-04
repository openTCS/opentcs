/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data.model.visualization;

import java.awt.Color;
import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import org.opentcs.data.TCSObject;

/**
 * Describes the visual attributes of a model.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class VisualLayout
    extends TCSObject<VisualLayout>
    implements Serializable, Cloneable {

  /**
   * This layout's scale on the X axis (in mm/pixel).
   */
  private double scaleX = 50.0;
  /**
   * This layout's scale on the Y axis (in mm/pixel).
   */
  private double scaleY = 50.0;
  /**
   * A pool of named colours that can be referenced in layout elements'
   * properties.
   */
  private Map<String, Color> colors = new TreeMap<>();
  /**
   * VisualLayout elements describing the visualization of a model and additional
   * elements that need to be displayed.
   */
  private Set<LayoutElement> layoutElements = new HashSet<>();
  /**
   * A list of views on the layout/model that the user has bookmarked.
   */
  private List<ViewBookmark> viewBookmarks = new LinkedList<>();

  /**
   * Creates a new VisualLayout.
   * 
   * @param objectID This visual layout's object ID.
   * @param name This visual layout's name.
   */
  public VisualLayout(int objectID, String name) {
    super(objectID, name);
  }

  /**
   * Returns this layout's scale on the X axis (in mm/pixel).
   *
   * @return This layout's scale on the X axis.
   */
  public double getScaleX() {
    return scaleX;
  }

  /**
   * Sets this layout's scale on the X axis (in mm/pixel).
   *
   * @param scaleX The new scale.
   */
  public void setScaleX(double scaleX) {
    this.scaleX = scaleX;
  }

  /**
   * Returns this layout's scale on the Y axis (in mm/pixel).
   *
   * @return This layout's scale on the Y axis.
   */
  public double getScaleY() {
    return scaleY;
  }

  /**
   * Sets this layout's scale on the Y axis (in mm/pixel).
   *
   * @param scaleY The new scale.
   */
  public void setScaleY(double scaleY) {
    this.scaleY = scaleY;
  }

  /**
   * Returns this layout's pool of named colours that can be referenced (by
   * their names) in layout elements' properties.
   *
   * @return This layout's pool of named colours.
   */
  public Map<String, Color> getColors() {
    return colors;
  }

  /**
   * Sets this layout's pool of named colours.
   *
   * @param colors The new colours.
   */
  public void setColors(Map<String, Color> colors) {
    this.colors = Objects.requireNonNull(colors, "colors is null");
  }

  /**
   * Returns the layout elements describing the visualization of a model.
   *
   * @return The layout elements describing the visualization of a model.
   */
  public Set<LayoutElement> getLayoutElements() {
    return layoutElements;
  }

  /**
   * Sets the layout elements describing the visualization of a model.
   *
   * @param layoutElements The new layout elements.
   */
  public void setLayoutElements(Set<LayoutElement> layoutElements) {
    this.layoutElements = Objects.requireNonNull(layoutElements,
                                                 "layoutElements is null");
  }

  /**
   * Returns a list of views on the layout/model that the user has bookmarked.
   *
   * @return A list of views on the layout/model that the user has bookmarked.
   */
  public List<ViewBookmark> getViewBookmarks() {
    return viewBookmarks;
  }

  /**
   * Sets a list of views on the layout/model that the user has bookmarked.
   *
   * @param viewBookmarks The new list of views.
   */
  public void setViewBookmarks(List<ViewBookmark> viewBookmarks) {
    this.viewBookmarks = Objects.requireNonNull(viewBookmarks,
                                                "viewBookmarks is null");
  }

  @Override
  public VisualLayout clone() {
    VisualLayout clone = (VisualLayout) super.clone();
    clone.colors = new TreeMap<>(colors);
    clone.layoutElements = new HashSet<>(layoutElements);
    clone.viewBookmarks = new LinkedList<>(viewBookmarks);
    return clone;
  }
}
