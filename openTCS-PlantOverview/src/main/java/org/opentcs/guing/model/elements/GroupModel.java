/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.model.elements;

import java.util.HashMap;
import java.util.Map;
import org.opentcs.guing.components.properties.type.KeyValueSetProperty;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.components.properties.type.StringSetProperty;
import org.opentcs.guing.model.ModelComponent;
import static org.opentcs.guing.model.ModelComponent.MISCELLANEOUS;
import static org.opentcs.guing.model.ModelComponent.NAME;
import org.opentcs.guing.model.SimpleFolder;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * A folder class that manages the visible state of its members.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class GroupModel
    extends SimpleFolder {

  /**
   * Key for the elements.
   */
  public static final String ELEMENTS = "groupElements";
  private boolean groupVisibleInAllDrawingViews = true;
  private final Map<String, Boolean> drawingViewVisibilityMap = new HashMap<>();

  public GroupModel() {
    this("");
  }

  public GroupModel(String name) {
    super(name);
    createProperties();
  }

  public boolean isGroupVisible() {
    return groupVisibleInAllDrawingViews;
  }

  /**
   * Sets the visibility status this group in all drawing views to
   * <code>isGroupVisible</code>.
   *
   * @param isGroupVisible If this group should be shown or hidden
   * in all drawing views.
   */
  public void setGroupVisible(boolean isGroupVisible) {
    this.groupVisibleInAllDrawingViews = isGroupVisible;

    for (String key : drawingViewVisibilityMap.keySet()) {
      drawingViewVisibilityMap.put(key, isGroupVisible);
    }
  }

  /**
   * Removes a drawing view from this group folder.
   *
   * @param title The title of the drawing view.
   */
  public void removeDrawingView(String title) {
    drawingViewVisibilityMap.remove(title);
    evaluateVisibilityInAllDrawingViews();
  }

  /**
   * Returns whether this group is visible in a drawing view.
   *
   * @param title The title of the drawing view.
   * @return Wehther this group is visible or not.
   */
  public boolean isGroupInDrawingViewVisible(String title) {
    if (drawingViewVisibilityMap.containsKey(title)) {
      return drawingViewVisibilityMap.get(title);
    }

    return true;
  }

  /**
   * Sets the visibility status of a drawing view. If it isn't known the
   * drawing view will be added.
   *
   * @param title The title of the drawing view.
   * @param visible If it is visible or not.
   */
  public void setDrawingViewVisible(String title, boolean visible) {
    drawingViewVisibilityMap.put(title, visible);

    if (!visible) {
      groupVisibleInAllDrawingViews = false;
    }
    else {
      evaluateVisibilityInAllDrawingViews();
    }
  }

  /**
   * Evaluates if <code>groupVisibleInAllDrawingViews</code> should
   * be set to true or false, depending on the states of every
   * drawing view variable.
   */
  private void evaluateVisibilityInAllDrawingViews() {
    groupVisibleInAllDrawingViews = true;

    for (Boolean visible : drawingViewVisibilityMap.values()) {
      if (!visible) {
        groupVisibleInAllDrawingViews = false;
        break;
      }
    }
  }

  @Override
  public void add(ModelComponent component) {
    super.add(component);
    if (!getPropertyElements().getItems().contains(component.getName())) {
      getPropertyElements().addItem(component.getName());
    }
  }

  @Override
  public void remove(ModelComponent component) {
    super.remove(component);
    getPropertyElements().getItems().remove(component.getName());
  }

  @Override  // AbstractModelComponent
  public String getTreeViewName() {
    String treeViewName = getDescription() + " " + getName();

    return treeViewName;
  }

  @Override  // AbstractModelComponent
  public String getDescription() {
    return ResourceBundleUtil.getBundle().getString("group.description");
  }

  public StringSetProperty getPropertyElements() {
    return (StringSetProperty) getProperty(ELEMENTS);
  }

  public KeyValueSetProperty getPropertyMiscellaneous() {
    return (KeyValueSetProperty) getProperty(MISCELLANEOUS);
  }

  private void createProperties() {
    ResourceBundleUtil bundle = ResourceBundleUtil.getBundle();

    StringProperty pName = new StringProperty(this);
    pName.setDescription(bundle.getString("group.name.text"));
    pName.setHelptext(bundle.getString("group.name.helptext"));
    setProperty(NAME, pName);

    StringSetProperty pElements = new StringSetProperty(this);
    pElements.setDescription(bundle.getString("group.elements.text"));
    pElements.setModellingEditable(false);
    pElements.setOperatingEditable(false);
    setProperty(ELEMENTS, pElements);

    KeyValueSetProperty pMiscellaneous = new KeyValueSetProperty(this);
    pMiscellaneous.setDescription(bundle.getString("group.miscellaneous.text"));
    pMiscellaneous.setHelptext(bundle.getString("group.miscellaneous.helptext"));
    setProperty(MISCELLANEOUS, pMiscellaneous);
  }
}
