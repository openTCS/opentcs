/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.model;

import java.util.List;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.Figure;
import org.opentcs.guing.base.components.properties.type.KeyValueSetProperty;
import org.opentcs.guing.base.model.ModelComponent;
import org.opentcs.guing.base.model.elements.BlockModel;
import org.opentcs.guing.base.model.elements.LayoutModel;
import org.opentcs.guing.base.model.elements.LinkModel;
import org.opentcs.guing.base.model.elements.LocationModel;
import org.opentcs.guing.base.model.elements.LocationTypeModel;
import org.opentcs.guing.base.model.elements.OtherGraphicalElement;
import org.opentcs.guing.base.model.elements.PathModel;
import org.opentcs.guing.base.model.elements.PointModel;
import org.opentcs.guing.base.model.elements.VehicleModel;
import org.opentcs.guing.common.components.drawing.course.DrawingMethod;

/**
 * Interface for the date model of the whole system model.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public interface SystemModel
    extends ModelComponent {

  /**
   * Returns this model's set of miscellaneous properties.
   *
   * @return This model's set of miscellaneous properties.
   */
  KeyValueSetProperty getPropertyMiscellaneous();

  /**
   * Add a main component to the system model.
   *
   * @param key The folder key
   * @param component The model component to be added to the folder
   */
  void addMainFolder(FolderKey key, ModelComponent component);

  /**
   * Return the main model component for the folder key.
   *
   * @param key The folder key
   * @return The model component that represents the main folder
   */
  ModelComponent getMainFolder(FolderKey key);

  /**
   * Returns the parent folder for a specified model component.
   *
   * @param item The model component where a parent folder should be found from
   * @return The parent folder of the item
   */
  ModelComponent getFolder(ModelComponent item);

  /**
   * Return all object of a specified class in a folder.
   *
   * @param foldername Key of the folder in which to search.
   * @param classType Tha class of the objects to find.
   * @return List of all object of specified type in the folder.
   */
  <T> List<T> getAll(FolderKey foldername, Class<T> classType);

  /**
   * Return all model components in the system model.
   *
   * @return List of all model components in the system model.
   */
  List<ModelComponent> getAll();

  /**
   * Notifies the model that all elements have been restored.
   */
  void onRestorationComplete();

  /**
   * Registers the given figure and associates it with the given model component.
   *
   * @param component The model component.
   * @param figure The figure to register.
   */
  void registerFigure(ModelComponent component, Figure figure);

  /**
   * Returns the figure for the given model component.
   *
   * @param component The model component.
   * @return The figure for the given model component.
   */
  Figure getFigure(ModelComponent component);

  /**
   * Return the drawing.
   *
   * @return The drawing.
   */
  Drawing getDrawing();

  /**
   * Return the drawing method.
   *
   * @return The drawing method
   */
  DrawingMethod getDrawingMethod();

  /**
   * Returns the component with the given name, if it exists.
   *
   * @param name The name.
   * @return The component with the given name, or {@code null}, if it does not exist.
   */
  ModelComponent getModelComponent(String name);

  /**
   * Return a list of all vehicles.
   *
   * @return The list of vehicle models
   */
  List<VehicleModel> getVehicleModels();

  /**
   * Return a vehicle with a specified name.
   *
   * @param name The name of the vehicle to search.
   * @return The vehicle or null if vehicle is not found.
   */
  VehicleModel getVehicleModel(String name);

  /**
   * Returns the layout model.
   *
   * @return The layout model.
   */
  LayoutModel getLayoutModel();

  /**
   * Return a list of all points.
   *
   * @return The list of points
   */
  List<PointModel> getPointModels();

  /**
   * Return a point with the specified name.
   *
   * @param name The name of the point to return.
   * @return The point or null if the name is not found.
   */
  PointModel getPointModel(String name);

  /**
   * Return all locations.
   *
   * @return List of all locations.
   */
  List<LocationModel> getLocationModels();

  /**
   * Return all locations with a specified location type.
   *
   * @param locationType The location type.
   * @return List of locations with the location type.
   */
  List<LocationModel> getLocationModels(LocationTypeModel locationType);

  /**
   * Return a location with the specified name.
   *
   * @param name The name of the location to return.
   * @return The location or null if the name is not found.
   */
  LocationModel getLocationModel(String name);

  /**
   * Return all paths.
   *
   * @return A list of all paths.
   */
  List<PathModel> getPathModels();

  /**
   * Return the PathModel with the given name.
   *
   * @param name Name of the path.
   * @return The PathModel.
   */
  PathModel getPathModel(String name);

  /**
   * Return all links in the model.
   *
   * @return A list of all links.
   */
  List<LinkModel> getLinkModels();

  /**
   * Return all links attached to a location with a specified location type.
   *
   * @param locationType The location type.
   * @return A list of all links attached to a location with a specified location type.
   */
  List<LinkModel> getLinkModels(LocationTypeModel locationType);

  /**
   * Return all location types.
   *
   * @return List of all location types.
   */
  List<LocationTypeModel> getLocationTypeModels();

  /**
   * Return the location type with the specified name.
   *
   * @param name The name of the location type to return.
   * @return The location type.
   */
  LocationTypeModel getLocationTypeModel(String name);

  /**
   * Returns the block model for the given name.
   *
   * @param name The block's name.
   * @return The block model.
   */
  BlockModel getBlockModel(String name);

  /**
   * Return all block models.
   *
   * @return A list of all block models.
   */
  List<BlockModel> getBlockModels();

  /**
   * Return all figures that are used for visualisation purposes.
   *
   * @return A list of all figures that are used for visualisation purposes.
   */
  List<OtherGraphicalElement> getOtherGraphicalElements();

  /**
   * Keys for the folders in a SystemModel.
   */
  public static enum FolderKey {

    VEHICLES,
    LAYOUT,
    POINTS,
    LOCATIONS,
    PATHS,
    LINKS,
    LOCATION_TYPES,
    BLOCKS,
    OTHER_GRAPHICAL_ELEMENTS
  }
}
