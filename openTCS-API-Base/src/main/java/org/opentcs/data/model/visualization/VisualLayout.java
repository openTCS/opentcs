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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.TreeMap;
import org.opentcs.data.TCSObject;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * Describes the visual attributes of a model.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
@ScheduledApiChange(when = "5.0", details = "Will not implement Cloneable any more")
public class VisualLayout
    extends TCSObject<VisualLayout>
    implements Serializable,
               Cloneable {

  /**
   * This layout's scale on the X axis (in mm/pixel).
   */
  private double scaleX = 50.0;
  /**
   * This layout's scale on the Y axis (in mm/pixel).
   */
  private double scaleY = 50.0;
  /**
   * A pool of named colours that can be referenced in layout elements' properties.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  private Map<String, Color> colors = new TreeMap<>();
  /**
   * VisualLayout elements describing the visualization of a model and additional
   * elements that need to be displayed.
   */
  private Set<LayoutElement> layoutElements = new HashSet<>();
  /**
   * A list of views on the layout/model that the user has bookmarked.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  private List<ViewBookmark> viewBookmarks = new LinkedList<>();

  /**
   * Creates a new VisualLayout.
   *
   * @param objectID This visual layout's object ID.
   * @param name This visual layout's name.
   * @deprecated Will be removed.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public VisualLayout(int objectID, String name) {
    super(objectID, name);
    this.scaleX = 50.0;
    this.scaleY = 50.0;
    this.layoutElements = new HashSet<>();
    this.viewBookmarks = new ArrayList<>();
  }

  /**
   * Creates a new VisualLayout.
   *
   * @param name This visual layout's name.
   */
  public VisualLayout(String name) {
    super(name);
    this.scaleX = 50.0;
    this.scaleY = 50.0;
    this.colors = new TreeMap<>();
    this.layoutElements = new HashSet<>();
    this.viewBookmarks = new ArrayList<>();
  }

  /**
   * Creates a new VisualLayout.
   *
   * @param objectID This visual layout's object ID.
   * @param name This visual layout's name.
   */
  @SuppressWarnings("deprecation")
  private VisualLayout(int objectID,
                       String name,
                       Map<String, String> properties,
                       double scaleX,
                       double scaleY,
                       Map<String, Color> colors,
                       Set<LayoutElement> layoutElements,
                       List<ViewBookmark> viewBookmarks) {
    super(objectID, name, properties);
    this.scaleX = scaleX;
    this.scaleY = scaleY;
    this.colors = new TreeMap<>(mapWithoutNullValues(colors));
    this.layoutElements = new HashSet<>(requireNonNull(layoutElements, "layoutElements"));
    this.viewBookmarks = new ArrayList<>(requireNonNull(viewBookmarks, "viewBookmarks"));
  }

  @Override
  public VisualLayout withProperty(String key, String value) {
    return new VisualLayout(getIdWithoutDeprecationWarning(),
                            getName(),
                            propertiesWith(key, value),
                            scaleX,
                            scaleY,
                            colors,
                            layoutElements,
                            viewBookmarks);
  }

  @Override
  public VisualLayout withProperties(Map<String, String> properties) {
    return new VisualLayout(getIdWithoutDeprecationWarning(),
                            getName(),
                            properties,
                            scaleX,
                            scaleY,
                            colors,
                            layoutElements,
                            viewBookmarks);
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
   * @deprecated Will become immutable.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public void setScaleX(double scaleX) {
    this.scaleX = scaleX;
  }

  /**
   * Creates a copy of this object, with the given scaleX.
   *
   * @param scaleX The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public VisualLayout withScaleX(double scaleX) {
    return new VisualLayout(getIdWithoutDeprecationWarning(),
                            getName(),
                            getProperties(),
                            scaleX,
                            scaleY,
                            colors,
                            layoutElements,
                            viewBookmarks);
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
   * @deprecated Will become immutable.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public void setScaleY(double scaleY) {
    this.scaleY = scaleY;
  }

  /**
   * Creates a copy of this object, with the given scaleY.
   *
   * @param scaleY The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public VisualLayout withScaleY(double scaleY) {
    return new VisualLayout(getIdWithoutDeprecationWarning(),
                            getName(),
                            getProperties(),
                            scaleX,
                            scaleY,
                            colors,
                            layoutElements,
                            viewBookmarks);
  }

  /**
   * Returns this layout's pool of named colours that can be referenced (by
   * their names) in layout elements' properties.
   *
   * @return This layout's pool of named colours.
   * @deprecated Will be removed.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public Map<String, Color> getColors() {
    return colors;
  }

  /**
   * Sets this layout's pool of named colours.
   *
   * @param colors The new colours.
   * @deprecated Will be removed.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
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
   * @deprecated Will become immutable.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public void setLayoutElements(Set<LayoutElement> layoutElements) {
    this.layoutElements = Objects.requireNonNull(layoutElements,
                                                 "layoutElements is null");
  }

  /**
   * Creates a copy of this object, with the given layoutElements.
   *
   * @param layoutElements The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public VisualLayout withLayoutElements(Set<LayoutElement> layoutElements) {
    return new VisualLayout(getIdWithoutDeprecationWarning(),
                            getName(),
                            getProperties(),
                            scaleX,
                            scaleY,
                            colors,
                            layoutElements,
                            viewBookmarks);
  }

  /**
   * Returns a list of views on the layout/model that the user has bookmarked.
   *
   * @return A list of views on the layout/model that the user has bookmarked.
   * @deprecated Will be removed.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public List<ViewBookmark> getViewBookmarks() {
    return viewBookmarks;
  }

  /**
   * Sets a list of views on the layout/model that the user has bookmarked.
   *
   * @param viewBookmarks The new list of views.
   * @deprecated Will be removed.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public void setViewBookmarks(List<ViewBookmark> viewBookmarks) {
    this.viewBookmarks = Objects.requireNonNull(viewBookmarks,
                                                "viewBookmarks is null");
  }

  /**
   * {@inheritDoc}
   *
   * @deprecated Will become immutable and not implement Cloneable any more.
   */
  @Override
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public VisualLayout clone() {
    return new VisualLayout(getIdWithoutDeprecationWarning(),
                            getName(),
                            getProperties(),
                            scaleX,
                            scaleY,
                            colors,
                            layoutElements,
                            viewBookmarks);
  }

  @SuppressWarnings("deprecation")
  private int getIdWithoutDeprecationWarning() {
    return getId();
  }

}
