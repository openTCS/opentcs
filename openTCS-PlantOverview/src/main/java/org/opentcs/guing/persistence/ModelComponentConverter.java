/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.opentcs.guing.components.properties.type.Property;
import org.opentcs.guing.model.AbstractModelComponent;
import org.opentcs.guing.model.ModelComponent;
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
import org.opentcs.guing.persistence.CourseElement.Block;
import org.opentcs.guing.persistence.CourseElement.Group;
import org.opentcs.guing.persistence.CourseElement.Layout;
import org.opentcs.guing.persistence.CourseElement.Link;
import org.opentcs.guing.persistence.CourseElement.Location;
import org.opentcs.guing.persistence.CourseElement.LocationType;
import org.opentcs.guing.persistence.CourseElement.Path;
import org.opentcs.guing.persistence.CourseElement.Point;
import org.opentcs.guing.persistence.CourseElement.StaticRoute;
import org.opentcs.guing.persistence.CourseElement.Vehicle;
import org.opentcs.guing.util.Comparators;

/**
 * Converts <code>ModelComponents</code> to JAXB classes.
 *
 * @author Philipp Seifert (Philipp.Seifert@iml.fraunhofer.de)
 */
public class ModelComponentConverter {

  private final PropertyConverter propertyConverter = new PropertyConverter();
  /**
   * A list that contains the possible values for location models.
   * LocationTypeModels are deparsed before LocationModels and we have
   * to set the possible values for the location.
   */
  private final List<String> locationTypeNames = new ArrayList<>();

  public ModelComponentConverter() {
  }

  public ModelComponent revertCourseElement(CourseElement element)
      throws IllegalArgumentException {
    if (element instanceof Block) {
      return revertBlock((Block) element);
    }
    if (element instanceof Group) {
      return revertGroup((Group) element);
    }
    if (element instanceof Layout) {
      return revertLayout((Layout) element);
    }
    if (element instanceof Link) {
      return revertLink((Link) element);
    }
    if (element instanceof Location) {
      return revertLocation((Location) element);
    }
    if (element instanceof LocationType) {
      return revertLocationType((LocationType) element);
    }
    if (element instanceof CourseElement.OtherGraphicalElement) {
      return revertOtherGraphicalElement((CourseElement.OtherGraphicalElement) element);
    }
    if (element instanceof Path) {
      return revertPath((Path) element);
    }
    if (element instanceof Point) {
      return revertPoint((Point) element);
    }
    if (element instanceof StaticRoute) {
      return revertStaticRoute((StaticRoute) element);
    }
    if (element instanceof Vehicle) {
      return revertVehicle((Vehicle) element);
    }

    return null;
  }

  private VehicleModel revertVehicle(Vehicle vehicle)
      throws IllegalArgumentException {
    VehicleModel model = new VehicleModel();
    revertProperties(model, vehicle);
    return model;
  }

  private StaticRouteModel revertStaticRoute(StaticRoute staticRoute)
      throws IllegalArgumentException {
    StaticRouteModel model = new StaticRouteModel();
    revertProperties(model, staticRoute);
    return model;
  }

  private PointModel revertPoint(Point point)
      throws IllegalArgumentException {
    PointModel model = new PointModel();
    revertProperties(model, point);
    return model;
  }

  private PathModel revertPath(Path path)
      throws IllegalArgumentException {
    PathModel model = new PathModel();
    revertProperties(model, path);
    return model;
  }

  private OtherGraphicalElement revertOtherGraphicalElement(
      CourseElement.OtherGraphicalElement element)
      throws IllegalArgumentException {
    OtherGraphicalElement model = new OtherGraphicalElement();
    revertProperties(model, element);
    return model;
  }

  private LocationTypeModel revertLocationType(LocationType locType)
      throws IllegalArgumentException {
    LocationTypeModel model = new LocationTypeModel();
    revertProperties(model, locType);
    locationTypeNames.add(model.getName());
    return model;
  }

  private LocationModel revertLocation(Location location)
      throws IllegalArgumentException {
    LocationModel model = new LocationModel();
    model.getPropertyType().setPossibleValues(locationTypeNames);
    revertProperties(model, location);
    return model;
  }

  private BlockModel revertBlock(Block block)
      throws IllegalArgumentException {
    BlockModel model = new BlockModel();
    revertProperties(model, block);
    return model;
  }

  private GroupModel revertGroup(Group group)
      throws IllegalArgumentException {
    GroupModel model = new GroupModel();
    revertProperties(model, group);
    return model;
  }

  private void revertProperties(ModelComponent model, CourseElement element)
      throws IllegalArgumentException {
    try {
      propertyConverter.revertProperties(model, element);
    }
    catch (ClassCastException e) {
      throw new IllegalArgumentException(e);
    }
  }

  private LayoutModel revertLayout(Layout layout)
      throws IllegalArgumentException {
    LayoutModel layoutModel = new LayoutModel();
    revertProperties(layoutModel, layout);
    return layoutModel;
  }

  private LinkModel revertLink(Link link)
      throws IllegalArgumentException {
    LinkModel model = new LinkModel();
    revertProperties(model, link);
    return model;
  }

  public CourseElement convertModel(ModelComponent model) {
    if (model instanceof PointModel) {
      return convertPoint((PointModel) model);
    }
    if (model instanceof PathModel) {
      return convertPath((PathModel) model);
    }
    if (model instanceof PathModel) {
      return convertPath((PathModel) model);
    }
    if (model instanceof BlockModel) {
      return convertBlock((BlockModel) model);
    }
    if (model instanceof GroupModel) {
      return convertGroup((GroupModel) model);
    }
    if (model instanceof LayoutModel) {
      return convertLayout((LayoutModel) model);
    }
    if (model instanceof LinkModel) {
      return convertLink((LinkModel) model);
    }
    if (model instanceof LocationModel) {
      return convertLocation((LocationModel) model);
    }
    if (model instanceof LocationTypeModel) {
      return convertLocationType((LocationTypeModel) model);
    }
    if (model instanceof OtherGraphicalElement) {
      return convertOtherGraphical((OtherGraphicalElement) model);
    }
    if (model instanceof StaticRouteModel) {
      return convertStaticRoute((StaticRouteModel) model);
    }
    if (model instanceof VehicleModel) {
      return convertVehicle((VehicleModel) model);
    }

    return null;
  }

  private CourseElement convertPoint(PointModel model) {
    CourseElement point = new Point();
    convert(point, model);

    return point;
  }

  private CourseElement convertPath(PathModel model) {
    CourseElement path = new Path();
    convert(path, model);

    return path;
  }

  private CourseElement convertBlock(BlockModel model) {
    CourseElement block = new Block();
    convert(block, model);

    return block;
  }

  private CourseElement convertGroup(GroupModel model) {
    CourseElement group = new Group();
    convert(group, model);

    return group;
  }

  private CourseElement convertLayout(LayoutModel model) {
    CourseElement layout = new Layout();
    convert(layout, model);

    return layout;
  }

  private CourseElement convertLink(LinkModel model) {
    CourseElement link = new Link();
    convert(link, model);

    return link;
  }

  private CourseElement convertLocation(LocationModel model) {
    CourseElement location = new Location();
    convert(location, model);

    return location;
  }

  private CourseElement convertLocationType(LocationTypeModel model) {
    CourseElement locationType = new LocationType();
    convert(locationType, model);

    return locationType;
  }

  private CourseElement convertOtherGraphical(OtherGraphicalElement model) {
    CourseElement otherGraphical = new CourseElement.OtherGraphicalElement();
    convert(otherGraphical, model);

    return otherGraphical;
  }

  private CourseElement convertStaticRoute(StaticRouteModel model) {
    CourseElement staticRoute = new StaticRoute();
    convert(staticRoute, model);

    return staticRoute;
  }

  private CourseElement convertVehicle(VehicleModel model) {
    CourseElement vehicle = new Vehicle();
    convert(vehicle, model);

    return vehicle;
  }

  private void convert(CourseElement element, AbstractModelComponent model) {
    element.setTreeViewName(model.getTreeViewName());
    Set<CourseObjectProperty> courseProperties = convertProperties(model.getProperties());
    element.setProperties(courseProperties);
  }

  private Set<CourseObjectProperty> convertProperties(Map<String, Property> modelProperties) {
    Set<CourseObjectProperty> courseProperties
        = new TreeSet<>(Comparators.courseObjectPropertiesByName());
    for (String key : modelProperties.keySet()) {
      if (modelProperties.get(key).isPersistent()) {
        CourseObjectProperty property = propertyConverter.convert(key, modelProperties.get(key));
        courseProperties.add(property);
      }
    }

    return courseProperties;
  }
}
