/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.model;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jhotdraw.draw.Drawing;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Block;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.model.visualization.ModelLayoutElement;
import org.opentcs.data.model.visualization.VisualLayout;
import org.opentcs.guing.components.drawing.course.DrawingMethod;
import org.opentcs.guing.components.properties.type.KeyValueSetProperty;
import org.opentcs.guing.model.elements.BlockModel;
import org.opentcs.guing.model.elements.GroupModel;
import org.opentcs.guing.model.elements.LayoutModel;
import org.opentcs.guing.model.elements.LinkModel;
import org.opentcs.guing.model.elements.LocationModel;
import org.opentcs.guing.model.elements.LocationTypeModel;
import org.opentcs.guing.model.elements.OtherGraphicalElement;
import org.opentcs.guing.model.elements.PathModel;
import org.opentcs.guing.model.elements.PointModel;
import org.opentcs.guing.model.elements.StaticRouteModel;
import org.opentcs.guing.model.elements.VehicleModel;

/**
 * Interface für das Datenmodell des gesamten modellierten Systems. Besteht aus
 * den Fahrzeugen und dem Fahrkurslayout. Das Systemmodell verwaltet
 * Komposita-Komponenten, die unbedingt vorhanden sein müssen.
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
   * Fügt dem Systemmodell eine Hauptkomponente hinzu.
   *
   * @param key The folder key
   * @param component The model component to be added to the folder
   */
  void addMainFolder(FolderKey key, ModelComponent component);

  /**
   * Liefert die zum Schlüssel passende Hauptkomponente.
   *
   * @param key The folder key
   * @return The model component that represents the main folder
   */
  ModelComponent getMainFolder(FolderKey key);

  /**
   * Liefert das Elternobjekt zu einem ModelComponent-Objekt. Die Zuordnung wird
   * über das Class-Objekt von item vorgenommen.
   *
   * @param item The model component where a parent folder should be found from
   * @return The parent folder of the item
   */
  ModelComponent getFolder(ModelComponent item);

  /**
   * Liefert alle Objekt, die zu einer bestimmten Klasse gehören.
   *
   * @param foldername der Name des Ordners, in dem gesucht werden soll
   * @param classType die Klasse, von der die Objekte sein müssen
   * @return alle Objekte einer Klasse
   */
  <T> List<T> getAll(FolderKey foldername, Class<T> classType);

  /**
   * Liefert alle Objekte in allen Ordnern.
   *
   * @return Liste aller Objekte
   */
  List<ModelComponent> getAll();

  /**
   * Liefert die Zeichnung.
   *
   * @return die Zeichnung
   */
  Drawing getDrawing();

  /**
   * Liefert die Zeichenmethode. Hier sind beispielsweise möglich "symbolisch"
   * und auf "Koordinaten basierend".
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
   * Liefert eine Liste aller Fahrzeuge.
   *
   * @return The list of vehicle models
   */
  List<VehicleModel> getVehicleModels();

  /**
   * Sucht ein Fahrzeug mit einem bestimmten Namen.
   *
   * @param name der Name des gesuchten Fahrzeugs
   * @return das gefundene Fahrzeug oder
   * <code> null </code>, wenn kein entsprechendes Fahrzeug gefunden werden
   * konnte
   */
  VehicleModel getVehicleModel(String name);

  /**
   * Returns the list of layout models
   *
   * @return The list of layout models
   */
  List<LayoutModel> getLayoutModels();

  /**
   * Searches a layout model with a specific name.
   *
   * @param name The name of the layout model
   * @return The layout model or <code>null</code> if no layout model with the name could be found
   */
  LayoutModel getLayoutModel(String name);

  /**
   * Liefert eine Liste aller Knoten.
   *
   * @return The list of points
   */
  List<PointModel> getPointModels();

  /**
   * Sucht einen Knoten mit einem bestimmten Namen.
   *
   * @param name der Name des gesuchten Knotens
   * @return den gefundenen Knoten oder
   * <code> null </code>, wenn kein entsprechender Knoten gefunden werden konnte
   */
  PointModel getPointModel(String name);

  /**
   * Liefert alle Stationen (Übergabestationen, Batterieladestation,
   * Arbeitsstationen).
   *
   * @return eine Liste aller Stationen
   */
  List<LocationModel> getLocationModels();

  /**
   * Liefert alle Stationen, die zu einem bestimmten Typ gehören.
   *
   * @param locationType der Stationstyp
   * @return die Stationen
   */
  List<LocationModel> getLocationModels(LocationTypeModel locationType);

  /**
   * Sucht eine Station mit einem bestimmten Namen.
   *
   * @param name der Name der gesuchten Station
   * @return die gefundene Station oder
   * <code> null </code>, wenn keine entsprechende Station gefunden werden
   * konnte
   */
  LocationModel getLocationModel(String name);

  /**
   * Liefert alle Kanten zwischen zwei Punkten.
   *
   * @return eine Liste mit allen Kanten, die jeweils zwei Punkte miteinander
   * verbinden
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
   * Liefert alle ´Links, die jeweils einen Punkt mit einer Station verbinden.
   *
   * @return eine Liste aller Referenzen
   */
  List<LinkModel> getLinkModels();

  /**
   * Liefert alle Links auf Stationen eines bestimmten Typs.
   *
   * @param locationType der Stationstyp
   * @return eine Liste aller Links
   */
  List<LinkModel> getLinkModels(LocationTypeModel locationType);

  /**
   * Liefert alle Stationstypen.
   *
   * @return alle Stationstypen
   */
  List<LocationTypeModel> getLocationTypeModels();

  /**
   * Liefert den Stationstyp mit einem bestimmten Namen.
   *
   * @param name der Name des gesuchten Stationstyps
   * @return der gesuchte Stationstyp
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
   * Liefert alle Blockbereiche.
   *
   * @return eine Liste aller Blockbereiche
   */
  List<BlockModel> getBlockModels();

  /**
   * Returns the group model for the given name.
   *
   * @param name The group's name.
   * @return The group model.
   */
  GroupModel getGroupModel(String name);

  List<GroupModel> getGroupModels();

  /**
   * Returns the static route model for the given name.
   *
   * @param name The static route's name.
   * @return The static route model.
   */
  StaticRouteModel getStaticRouteModel(String name);

  /**
   * Liefert alle statischen Routen.
   *
   * @return eine Liste aller statischen Routen
   */
  List<StaticRouteModel> getStaticRouteModels();

  /**
   * Liefert alle grafischen Objekte, die lediglich eine illustrierende Wirkung
   * haben. Diese Objekte sind für den Fahrkurs irrelevant.
   *
   * @return eine Liste aller sonstigen grafischen Objekte, die mit dem Fahrkurs
   * direkt nichts zu tun haben
   */
  List<OtherGraphicalElement> getOtherGraphicalElements();

  /**
   * Adds a reference to a ModelLayoutElement to every object in the pool.
   *
   * @param layout The visual layout
   * @param points The points
   * @param paths The paths
   * @param locations The locations
   * @param blocks The blocks
   * @param vehicles The vehicles
   */
  void createLayoutMap(VisualLayout layout,
                       Set<Point> points,
                       Set<Path> paths,
                       Set<Location> locations,
                       Set<Block> blocks,
                       Set<Vehicle> vehicles);

  /**
   * Returns the LayoutMap.
   *
   * @return The layout map.
   */
  Map<TCSObjectReference<?>, ModelLayoutElement> getLayoutMap();

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
    GROUPS,
    STATIC_ROUTES,
    OTHER_GRAPHICAL_ELEMENTS
  }
}
