/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.model.elements;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import org.opentcs.data.model.visualization.ElementPropKeys;
import static org.opentcs.guing.I18nPlantOverviewBase.BUNDLE_PATH;
import org.opentcs.guing.components.properties.event.AttributesChangeListener;
import org.opentcs.guing.components.properties.type.ColorProperty;
import org.opentcs.guing.components.properties.type.KeyValueSetProperty;
import org.opentcs.guing.components.properties.type.SelectionProperty;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.components.properties.type.StringSetProperty;
import org.opentcs.guing.event.BlockChangeEvent;
import org.opentcs.guing.event.BlockChangeListener;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.SimpleFolder;

/**
 * A block area. Contains figure components that are part of this block
 * area.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class BlockModel
    extends SimpleFolder {

  public static final String TYPE = "Type";
  /**
   * Key for the elements property.
   */
  public static final String ELEMENTS = "blockElements";
  /**
   * This class's resource bundle.
   */
  private final ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_PATH);
  /**
   * A list of change listeners for this object.
   */
  private List<BlockChangeListener> fListeners = new LinkedList<>();

  /**
   * Creates a new instance.
   */
  public BlockModel() {
    super("");
    createProperties();
  }

  @Override  // AbstractModelComponent
  public String getTreeViewName() {
    String treeViewName = getDescription() + " " + getName();

    return treeViewName;
  }

  @Override  // AbstractModelComponent
  public String getDescription() {
    return bundle.getString("block.description");
  }

  @Override  // AbstractModelComponent
  public void propertiesChanged(AttributesChangeListener listener) {
    if (getPropertyColor().hasChanged()) {
      colorChanged();
    }

    super.propertiesChanged(listener);
  }

  /**
   * Returns the color of this block area.
   *
   * @return The color.
   */
  public Color getColor() {
    return getPropertyColor().getColor();
  }

  /**
   * Adds an element to this block area.
   *
   * @param model The component to add.
   */
  public void addCourseElement(ModelComponent model) {
    if (!contains(model)) {
      getChildComponents().add(model);
      String addedModelName = model.getName();
      if (!getPropertyElements().getItems().contains(addedModelName)) {
        getPropertyElements().addItem(addedModelName);
      }
    }
  }

  /**
   * Removes an element from this block area.
   *
   * @param model The component to remove.
   */
  public void removeCourseElement(ModelComponent model) {
    if (contains(model)) {
      remove(model);
      getPropertyElements().getItems().remove(model.getName());
    }
  }

  /**
   * Removes all elements in this block area.
   */
  public void removeAllCourseElements() {
    for (Object o : new ArrayList<>(getChildComponents())) {
      remove((ModelComponent) o);
    }
  }

  /**
   * Adds a listeners that gets informed when this block members change.
   *
   * @param listener The new listener.
   */
  public void addBlockChangeListener(BlockChangeListener listener) {
    if (fListeners == null) {
      fListeners = new ArrayList<>();
    }

    if (!fListeners.contains(listener)) {
      fListeners.add(listener);
    }
  }

  /**
   * Removes a listener.
   *
   * @param listener The listener to remove.
   */
  public void removeBlockChangeListener(BlockChangeListener listener) {
    fListeners.remove(listener);
  }

  /**
   * Informs all listeners that the block elements have changed.
   */
  public void courseElementsChanged() {
    for (BlockChangeListener listener : fListeners) {
      listener.courseElementsChanged(new BlockChangeEvent(this));
    }
  }

  /**
   * Informs all listeners that the color of this block has changed.
   */
  public void colorChanged() {
    for (BlockChangeListener listener : fListeners) {
      listener.colorChanged(new BlockChangeEvent(this));
    }
  }

  /**
   * Informs all listeners that this block was removed.
   */
  public void blockRemoved() {
    for (BlockChangeListener listener : new ArrayList<>(fListeners)) {
      listener.blockRemoved(new BlockChangeEvent(this));
    }
  }

  public ColorProperty getPropertyColor() {
    return (ColorProperty) getProperty(ElementPropKeys.BLOCK_COLOR);
  }

  @SuppressWarnings("unchecked")
  public SelectionProperty<BlockType> getPropertyType() {
    return (SelectionProperty<BlockType>) getProperty(TYPE);
  }

  public StringSetProperty getPropertyElements() {
    return (StringSetProperty) getProperty(ELEMENTS);
  }

  public KeyValueSetProperty getPropertyMiscellaneous() {
    return (KeyValueSetProperty) getProperty(MISCELLANEOUS);
  }

  private void createProperties() {
    StringProperty pName = new StringProperty(this);
    pName.setDescription(bundle.getString("block.name.text"));
    pName.setHelptext(bundle.getString("block.name.helptext"));
    setProperty(NAME, pName);

    ColorProperty pColor = new ColorProperty(this, Color.red);
    pColor.setDescription(bundle.getString("element.blockColor.text"));
    pColor.setHelptext(bundle.getString("element.blockColor.helptext"));
    setProperty(ElementPropKeys.BLOCK_COLOR, pColor);

    SelectionProperty<BlockType> pType = new SelectionProperty<>(this,
                                                                 Arrays.asList(BlockType.values()),
                                                                 BlockType.values()[0]);
    pType.setDescription(bundle.getString("block.type.text"));
    pType.setHelptext(bundle.getString("block.type.helptext"));
    pType.setCollectiveEditable(true);
    setProperty(TYPE, pType);

    StringSetProperty pElements = new StringSetProperty(this);
    pElements.setDescription(bundle.getString("block.elements.text"));
    pElements.setHelptext(bundle.getString("block.elements.helptext"));
    pElements.setModellingEditable(false);
    pElements.setOperatingEditable(false);
    setProperty(ELEMENTS, pElements);

    KeyValueSetProperty pMiscellaneous = new KeyValueSetProperty(this);
    pMiscellaneous.setDescription(bundle.getString("block.miscellaneous.text"));
    pMiscellaneous.setHelptext(bundle.getString("block.miscellaneous.helptext"));
    setProperty(MISCELLANEOUS, pMiscellaneous);
  }

  /**
   * The supported point types.
   */
  public enum BlockType {

    /**
     * Single vehicle only allowed.
     */
    SINGLE_VEHICLE_ONLY,
    /**
     * Same direction only allowed.
     */
    SAME_DIRECTION_ONLY;

    @Override
    public String toString() {
      return ResourceBundle.getBundle(BUNDLE_PATH).getString("block.type." + name() + ".text");
    }
  }
}
