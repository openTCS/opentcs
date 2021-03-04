/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.persistence;

import java.awt.Color;
import java.util.List;
import org.opentcs.data.ObjectPropConstants;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.visualization.ElementPropKeys;
import static org.opentcs.data.model.visualization.ElementPropKeys.BLOCK_COLOR;
import org.opentcs.data.model.visualization.LocationRepresentation;
import org.opentcs.guing.components.properties.type.AngleProperty;
import org.opentcs.guing.components.properties.type.BooleanProperty;
import org.opentcs.guing.components.properties.type.ColorProperty;
import org.opentcs.guing.components.properties.type.CoordinateProperty;
import org.opentcs.guing.components.properties.type.CoursePointProperty;
import org.opentcs.guing.components.properties.type.IntegerProperty;
import org.opentcs.guing.components.properties.type.KeyValueProperty;
import org.opentcs.guing.components.properties.type.KeyValueSetProperty;
import org.opentcs.guing.components.properties.type.LengthProperty;
import org.opentcs.guing.components.properties.type.LinkActionsProperty;
import org.opentcs.guing.components.properties.type.LocationTypeProperty;
import org.opentcs.guing.components.properties.type.PercentProperty;
import org.opentcs.guing.components.properties.type.Property;
import org.opentcs.guing.components.properties.type.SelectionProperty;
import org.opentcs.guing.components.properties.type.SpeedProperty;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.components.properties.type.StringSetProperty;
import org.opentcs.guing.components.properties.type.SymbolProperty;
import org.opentcs.guing.components.properties.type.TripleProperty;
import static org.opentcs.guing.model.AbstractFigureComponent.MODEL_X_POSITION;
import static org.opentcs.guing.model.AbstractFigureComponent.MODEL_Y_POSITION;
import org.opentcs.guing.model.ModelComponent;
import static org.opentcs.guing.model.ModelComponent.MISCELLANEOUS;
import static org.opentcs.guing.model.ModelComponent.NAME;
import static org.opentcs.guing.model.elements.AbstractConnection.END_COMPONENT;
import static org.opentcs.guing.model.elements.AbstractConnection.START_COMPONENT;
import org.opentcs.guing.model.elements.BlockModel;
import org.opentcs.guing.model.elements.GroupModel;
import static org.opentcs.guing.model.elements.LayoutModel.SCALE_X;
import static org.opentcs.guing.model.elements.LayoutModel.SCALE_Y;
import static org.opentcs.guing.model.elements.LinkModel.ALLOWED_OPERATIONS;
import org.opentcs.guing.model.elements.LocationModel;
import static org.opentcs.guing.model.elements.PathModel.LENGTH;
import static org.opentcs.guing.model.elements.PathModel.LOCKED;
import static org.opentcs.guing.model.elements.PathModel.MAX_REVERSE_VELOCITY;
import static org.opentcs.guing.model.elements.PathModel.MAX_VELOCITY;
import static org.opentcs.guing.model.elements.PathModel.ROUTING_COST;
import org.opentcs.guing.model.elements.PointModel;
import static org.opentcs.guing.model.elements.PointModel.VEHICLE_ORIENTATION_ANGLE;
import org.opentcs.guing.model.elements.StaticRouteModel;
import org.opentcs.guing.model.elements.VehicleModel;
import static org.opentcs.guing.model.elements.VehicleModel.ENERGY_LEVEL;
import static org.opentcs.guing.model.elements.VehicleModel.ENERGY_LEVEL_CRITICAL;
import static org.opentcs.guing.model.elements.VehicleModel.ENERGY_LEVEL_GOOD;
import static org.opentcs.guing.model.elements.VehicleModel.ENERGY_STATE;
import static org.opentcs.guing.model.elements.VehicleModel.INTEGRATION_LEVEL;
import static org.opentcs.guing.model.elements.VehicleModel.LOADED;
import static org.opentcs.guing.model.elements.VehicleModel.NEXT_POINT;
import static org.opentcs.guing.model.elements.VehicleModel.ORIENTATION_ANGLE;
import static org.opentcs.guing.model.elements.VehicleModel.POINT;
import static org.opentcs.guing.model.elements.VehicleModel.PRECISE_POSITION;
import static org.opentcs.guing.model.elements.VehicleModel.PROC_STATE;
import static org.opentcs.guing.model.elements.VehicleModel.STATE;
import org.opentcs.guing.persistence.CourseObjectProperty.AngleCourseProperty;
import org.opentcs.guing.persistence.CourseObjectProperty.BooleanCourseProperty;
import org.opentcs.guing.persistence.CourseObjectProperty.ColorCourseProperty;
import org.opentcs.guing.persistence.CourseObjectProperty.CoordinateCourseProperty;
import org.opentcs.guing.persistence.CourseObjectProperty.CoursePointCourseProperty;
import org.opentcs.guing.persistence.CourseObjectProperty.IntegerCourseProperty;
import org.opentcs.guing.persistence.CourseObjectProperty.KeyValueCourseProperty;
import org.opentcs.guing.persistence.CourseObjectProperty.KeyValueSetCourseProperty;
import org.opentcs.guing.persistence.CourseObjectProperty.LengthCourseProperty;
import org.opentcs.guing.persistence.CourseObjectProperty.LinkActionsCourseProperty;
import org.opentcs.guing.persistence.CourseObjectProperty.LocationTypeCourseProperty;
import org.opentcs.guing.persistence.CourseObjectProperty.PercentCourseProperty;
import org.opentcs.guing.persistence.CourseObjectProperty.SelectionCourseProperty;
import org.opentcs.guing.persistence.CourseObjectProperty.SpeedCourseProperty;
import org.opentcs.guing.persistence.CourseObjectProperty.StringCourseProperty;
import org.opentcs.guing.persistence.CourseObjectProperty.StringSetCourseProperty;
import org.opentcs.guing.persistence.CourseObjectProperty.SymbolCourseProperty;
import org.opentcs.guing.persistence.CourseObjectProperty.TripleCourseProperty;

/**
 * Converts OpenTCS Properties to JAXB classes.
 *
 * @author Philipp Seifert (Philipp.Seifert@iml.fraunhofer.de)
 */
public class PropertyConverter {

  public CourseObjectProperty convert(String key, Property property) {
    if (property instanceof StringProperty) {
      return convertStringProperty(key, (StringProperty) property);
    }
    if (property instanceof IntegerProperty) {
      return convertIntegerProperty(key, (IntegerProperty) property);
    }
    if (property instanceof AngleProperty) {
      return convertAngleProperty(key, (AngleProperty) property);
    }
    if (property instanceof BooleanProperty) {
      return convertBooleanProperty(key, (BooleanProperty) property);
    }
    if (property instanceof ColorProperty) {
      return convertColorProperty(key, (ColorProperty) property);
    }
    if (property instanceof CoordinateProperty) {
      return convertCoordinateProperty(key, (CoordinateProperty) property);
    }
    if (property instanceof CoursePointProperty) {
      return convertCoursePointProperty(key, (CoursePointProperty) property);
    }
    if (property instanceof KeyValueProperty) {
      return convertKeyValueProperty(key, (KeyValueProperty) property);
    }
    if (property instanceof KeyValueSetProperty) {
      return convertKeyValueSetProperty(key, (KeyValueSetProperty) property);
    }
    if (property instanceof LengthProperty) {
      return convertLengthProperty(key, (LengthProperty) property);
    }
    if (property instanceof LinkActionsProperty) {
      return convertLinkActionsProperty(key, (LinkActionsProperty) property);
    }
    if (property instanceof LocationTypeProperty) {
      return convertLocationTypeProperty(key, (LocationTypeProperty) property);
    }
    if (property instanceof PercentProperty) {
      return convertPercentProperty(key, (PercentProperty) property);
    }
    if (property instanceof SelectionProperty) {
      return convertSelectionProperty(key, (SelectionProperty<?>) property);
    }
    if (property instanceof SpeedProperty) {
      return convertSpeedProperty(key, (SpeedProperty) property);
    }
    if (property instanceof StringSetProperty) {
      return convertStringSetProperty(key, (StringSetProperty) property);
    }
    if (property instanceof SymbolProperty) {
      return convertSymbolProperty(key, (SymbolProperty) property);
    }
    if (property instanceof TripleProperty) {
      return convertTripleProperty(key, (TripleProperty) property);
    }

    return null;
  }

  public void revertProperties(ModelComponent model,
                               CourseElement element)
      throws ClassCastException {
    for (CourseObjectProperty property : element.getProperties()) {
      if (model instanceof LocationModel) {
        // Locations and Points have unique keys with the same name
        // and they would conflict here
        revertLocationProperties(model, property);
      }
      else {
        switch (property.getKey()) {
          case NAME:
            revertStringProperty(model, NAME, property);
            break;
          case SCALE_X:
            revertLengthProperty(model, SCALE_X, property);
            break;
          case SCALE_Y:
            revertLengthProperty(model, SCALE_Y, property);
            break;
          case LENGTH:
            revertLengthProperty(model, LENGTH, property);
            break;
          case VehicleModel.LENGTH:
            revertLengthProperty(model, VehicleModel.LENGTH, property);
            break;
          case ElementPropKeys.VEHICLE_ROUTE_COLOR:
            revertRouteColorProperty(model, ElementPropKeys.VEHICLE_ROUTE_COLOR, property);
            break;
          case ENERGY_LEVEL_CRITICAL:
            revertPercentProperty(model, ENERGY_LEVEL_CRITICAL, property);
            break;
          case ENERGY_LEVEL_GOOD:
            revertPercentProperty(model, ENERGY_LEVEL_GOOD, property);
            break;
          case ENERGY_LEVEL:
            revertPercentProperty(model, ENERGY_LEVEL, property);
            break;
          case ENERGY_STATE:
            revertSelectionProperty(model, ENERGY_STATE, property);
            break;
          case LOADED:
            revertBooleanProperty(model, LOADED, property);
            break;
          case STATE:
            revertSelectionProperty(model, STATE, property);
            break;
          case PROC_STATE:
            revertSelectionProperty(model, PROC_STATE, property);
            break;
          case INTEGRATION_LEVEL:
            revertSelectionProperty(model, INTEGRATION_LEVEL, property);
            break;
          case POINT:
            revertStringProperty(model, POINT, property);
            break;
          case NEXT_POINT:
            revertStringProperty(model, NEXT_POINT, property);
            break;
          case PRECISE_POSITION:
            revertTripleProperty(model, PRECISE_POSITION, property);
            break;
          case ORIENTATION_ANGLE:
            revertAngleProperty(model, ORIENTATION_ANGLE, property);
            break;
          case ROUTING_COST:
            revertIntegerProerty(model, ROUTING_COST, property);
            break;
          case START_COMPONENT:
            revertStringProperty(model, START_COMPONENT, property);
            break;
          case END_COMPONENT:
            revertStringProperty(model, END_COMPONENT, property);
            break;
          case MAX_VELOCITY:
            revertSpeedProperty(model, MAX_VELOCITY, property);
            break;
          case MAX_REVERSE_VELOCITY:
            revertSpeedProperty(model, MAX_REVERSE_VELOCITY, property);
            break;
          case VehicleModel.MAXIMUM_VELOCITY:
            revertSpeedProperty(model, VehicleModel.MAXIMUM_VELOCITY, property);
            break;
          case VehicleModel.MAXIMUM_REVERSE_VELOCITY:
            revertSpeedProperty(model, VehicleModel.MAXIMUM_REVERSE_VELOCITY, property);
            break;
          case ElementPropKeys.PATH_CONN_TYPE:
            revertSelectionProperty(model, ElementPropKeys.PATH_CONN_TYPE, property);
            break;
          case ElementPropKeys.PATH_CONTROL_POINTS:
            revertStringProperty(model, ElementPropKeys.PATH_CONTROL_POINTS, property);
            break;
          case LOCKED:
            revertBooleanProperty(model, LOCKED, property);
            break;
          case ElementPropKeys.POINT_POS_X:
            revertStringProperty(model, ElementPropKeys.POINT_POS_X, property);
            break;
          case ElementPropKeys.POINT_POS_Y:
            revertStringProperty(model, ElementPropKeys.POINT_POS_Y, property);
            break;
          case ElementPropKeys.POINT_LABEL_OFFSET_X:
            revertStringProperty(model, ElementPropKeys.POINT_LABEL_OFFSET_X, property);
            break;
          case ElementPropKeys.POINT_LABEL_OFFSET_Y:
            revertStringProperty(model, ElementPropKeys.POINT_LABEL_OFFSET_Y, property);
            break;
          case ElementPropKeys.POINT_LABEL_ORIENTATION_ANGLE:
            revertStringProperty(model, ElementPropKeys.POINT_LABEL_ORIENTATION_ANGLE, property);
            break;
          case VEHICLE_ORIENTATION_ANGLE:
            revertAngleProperty(model, VEHICLE_ORIENTATION_ANGLE, property);
            break;
          case PointModel.TYPE:
            revertSelectionProperty(model, PointModel.TYPE, property);
            break;
          case MODEL_X_POSITION:
            revertCoordinateProperty(model, MODEL_X_POSITION, property);
            break;
          case MODEL_Y_POSITION:
            revertCoordinateProperty(model, MODEL_Y_POSITION, property);
            break;
          case ObjectPropConstants.LOCTYPE_DEFAULT_REPRESENTATION:
            revertSymbolProperty(model, ObjectPropConstants.LOCTYPE_DEFAULT_REPRESENTATION, property);
            break;
          case ALLOWED_OPERATIONS:
            revertStringSetProperty(model, ALLOWED_OPERATIONS, property);
            break;
          case BLOCK_COLOR:
            revertColorProperty(model, property);
            break;
          case BlockModel.ELEMENTS:
            revertStringSetProperty(model, BlockModel.ELEMENTS, property);
            break;
          case StaticRouteModel.ELEMENTS:
            revertStringSetProperty(model, StaticRouteModel.ELEMENTS, property);
            break;
          case GroupModel.ELEMENTS:
            revertStringSetProperty(model, GroupModel.ELEMENTS, property);
            break;
          case MISCELLANEOUS:
            revertMiscellaneosProperty(model,
                                       (KeyValueSetListWrapper) property.getValue(),
                                       (KeyValueSetProperty) model.getProperty(MISCELLANEOUS));
            break;
          default:
        }
      }
    }
  }

  private void revertLocationProperties(ModelComponent model,
                                        CourseObjectProperty property)
      throws ClassCastException {
    switch (property.getKey()) {
      case NAME:
        revertStringProperty(model, NAME, property);
        break;
      case MODEL_X_POSITION:
        revertCoordinateProperty(model, MODEL_X_POSITION, property);
        break;
      case MODEL_Y_POSITION:
        revertCoordinateProperty(model, MODEL_Y_POSITION, property);
        break;
      case ElementPropKeys.LOC_POS_X:
        revertStringProperty(model, ElementPropKeys.LOC_POS_X, property);
        break;
      case ElementPropKeys.LOC_POS_Y:
        revertStringProperty(model, ElementPropKeys.LOC_POS_Y, property);
        break;
      case ElementPropKeys.LOC_LABEL_OFFSET_X:
        revertStringProperty(model, ElementPropKeys.LOC_LABEL_OFFSET_X, property);
        break;
      case ElementPropKeys.LOC_LABEL_OFFSET_Y:
        revertStringProperty(model, ElementPropKeys.LOC_LABEL_OFFSET_Y, property);
        break;
      case ElementPropKeys.LOC_LABEL_ORIENTATION_ANGLE:
        revertStringProperty(model, ElementPropKeys.LOC_LABEL_ORIENTATION_ANGLE, property);
        break;
      case LocationModel.TYPE:
        revertLocationTypeProperty(model, LocationModel.TYPE, property);
        break;
      case ObjectPropConstants.LOC_DEFAULT_REPRESENTATION:
        revertSymbolProperty(model, ObjectPropConstants.LOC_DEFAULT_REPRESENTATION, property);
        break;
      case MISCELLANEOUS:
        revertMiscellaneosProperty(model,
                                   (KeyValueSetListWrapper) property.getValue(),
                                   (KeyValueSetProperty) model.getProperty(MISCELLANEOUS));
        break;
      default:
    }
  }

  private void revertTripleProperty(ModelComponent model,
                                    String key,
                                    CourseObjectProperty property)
      throws ClassCastException {
    TripleProperty tp = (TripleProperty) model.getProperty(key);
    String[] values = ((String) property.getValue()).split("=");
    Triple triple;
    // values either contains three elements or is empty
    if (values.length == 3) {
      triple = new Triple(values[0].isEmpty() ? 0 : Long.parseLong(values[0]),
                          values[1].isEmpty() ? 0 : Long.parseLong(values[1]),
                          values[2].isEmpty() ? 0 : Long.parseLong(values[2])
      );
    }
    else {
      triple = new Triple(0, 0, 0);
    }
    tp.setValue(triple);
  }

  private void revertCoursePointProperty(ModelComponent model,
                                         String key,
                                         CourseObjectProperty property)
      throws ClassCastException {
    CoursePointProperty cpp = (CoursePointProperty) model.getProperty(key);
    String value = (String) property.getValue();
    cpp.setPointName(value);
  }

  private void revertPercentProperty(ModelComponent model,
                                     String key,
                                     CourseObjectProperty property)
      throws ClassCastException {
    PercentProperty pp = (PercentProperty) model.getProperty(key);
    double value = (double) property.getValue();
    pp.setValueAndUnit(value, PercentProperty.Unit.PERCENT);
  }

  private void revertAngleProperty(ModelComponent model,
                                   String key,
                                   CourseObjectProperty property)
      throws ClassCastException {
    AngleProperty ap = (AngleProperty) model.getProperty(key);
    double value = (double) property.getValue();
    ap.setValueAndUnit(value, AngleProperty.Unit.DEG);
  }

  private void revertBooleanProperty(ModelComponent model,
                                     String key,
                                     CourseObjectProperty property)
      throws ClassCastException {
    BooleanProperty bp = (BooleanProperty) model.getProperty(key);
    bp.setValue((boolean) property.getValue());
  }

  private void revertSelectionProperty(ModelComponent model,
                                       String key,
                                       CourseObjectProperty property)
      throws ClassCastException {
    SelectionProperty<?> sp = (SelectionProperty<?>) model.getProperty(key);
    String value = (String) property.getValue();
    for (Object possibleValue : sp.getPossibleValues()) {
      if (possibleValue instanceof Enum) {
        Enum<?> enumVal = (Enum<?>) possibleValue;
        if (enumVal.name().equals(value)) {
          sp.setValue(possibleValue);
          break;
        }
      }
    }
  }

  private void revertSpeedProperty(ModelComponent model,
                                   String key,
                                   CourseObjectProperty property)
      throws ClassCastException {
    SpeedProperty sp = (SpeedProperty) model.getProperty(key);
    sp.setValueAndUnit((double) property.getValue(), SpeedProperty.Unit.MM_S);
  }

  private void revertIntegerProerty(ModelComponent model,
                                    String key,
                                    CourseObjectProperty property)
      throws ClassCastException {
    IntegerProperty ip = (IntegerProperty) model.getProperty(key);
    ip.setValue(property.getValue());
  }

  private void revertStringProperty(ModelComponent model,
                                    String key,
                                    CourseObjectProperty property)
      throws ClassCastException {
    StringProperty sp = (StringProperty) model.getProperty(key);
    sp.setText((String) property.getValue());
  }

  private void revertSymbolProperty(ModelComponent model,
                                    String key,
                                    CourseObjectProperty property)
      throws ClassCastException {
    SymbolProperty sp = (SymbolProperty) model.getProperty(key);
    String value = (String) property.getValue();
    for (LocationRepresentation lp : LocationRepresentation.values()) {
      if (lp.name().equals(value)) {
        sp.setLocationRepresentation(lp);
        break;
      }
    }
  }

  private void revertLocationTypeProperty(ModelComponent model,
                                          String key,
                                          CourseObjectProperty property)
      throws ClassCastException {
    LocationTypeProperty ltp = (LocationTypeProperty) model.getProperty(key);
    ltp.setValue((String) property.getValue());
  }

  private void revertCoordinateProperty(ModelComponent model,
                                        String key,
                                        CourseObjectProperty property)
      throws ClassCastException {
    CoordinateProperty cp = (CoordinateProperty) model.getProperty(key);
    double value = (double) property.getValue();
    cp.setValueAndUnit(value, LengthProperty.Unit.MM);
  }

  private void revertStringSetProperty(ModelComponent model,
                                       String key,
                                       CourseObjectProperty property)
      throws ClassCastException {
    StringSetProperty ssp = (StringSetProperty) model.getProperty(key);
    StringListWrapper wrapper = (StringListWrapper) property.getValue();
    wrapper.getEntries().stream().forEach(item -> ssp.addItem(item));
  }

  private void revertMiscellaneosProperty(ModelComponent model,
                                          KeyValueSetListWrapper wrapper,
                                          KeyValueSetProperty property) {
    for (CourseObjectProperty.KeyValueCourseProperty keyValueCourseProperty
             : wrapper.getEntries()) {
      KeyValueProperty kvProperty = new KeyValueProperty(model);
      String value = (String) keyValueCourseProperty.getValue();
      String[] keyValueSplit = value.split("=");
      value = keyValueSplit.length > 1 ? keyValueSplit[1] : "";
      kvProperty.setKeyAndValue(keyValueCourseProperty.getKey(), value);
      property.addItem(kvProperty);
    }
  }

  private void revertColorProperty(ModelComponent model,
                                   CourseObjectProperty property) {
    Color color = new Color((int) property.getValue());
    ((ColorProperty) model.getProperty(BLOCK_COLOR)).setColor(color);
  }

  private void revertRouteColorProperty(ModelComponent model, String key,
                                        CourseObjectProperty property) {
    ColorProperty colorProp = (ColorProperty) model.getProperty(key);
    colorProp.setColor(new Color((int) property.getValue()));

  }

  private void revertLengthProperty(ModelComponent model, String key,
                                    CourseObjectProperty property)
      throws ClassCastException {
    LengthProperty lp = (LengthProperty) model.getProperty(key);
    double valueByMM = (double) property.getValue();
    lp.setValueAndUnit(valueByMM, LengthProperty.Unit.MM);
  }

  private StringCourseProperty convertStringProperty(
      String key,
      StringProperty stringProperty) {
    String value = stringProperty.getText();
    StringCourseProperty property = new StringCourseProperty(key, value);
    return property;
  }

  private IntegerCourseProperty convertIntegerProperty(String key,
                                                       IntegerProperty intProperty) {
    Object value = intProperty.getValue();
    IntegerCourseProperty property = new IntegerCourseProperty(key, value);
    return property;
  }

  private AngleCourseProperty convertAngleProperty(String key,
                                                   AngleProperty angleProperty) {
    Object value = angleProperty.getValueByUnit(AngleProperty.Unit.DEG);
    AngleCourseProperty property = new AngleCourseProperty(key, value);
    return property;
  }

  private BooleanCourseProperty convertBooleanProperty(String key,
                                                       BooleanProperty booleanProperty) {
    Object value = booleanProperty.getValue();
    BooleanCourseProperty property = new BooleanCourseProperty(key, value);
    return property;
  }

  private ColorCourseProperty convertColorProperty(String key,
                                                   ColorProperty colorProperty) {
    int value = colorProperty.getColor().getRGB();
    ColorCourseProperty property = new ColorCourseProperty(key, value);
    return property;
  }

  private CoordinateCourseProperty convertCoordinateProperty(String key,
                                                             CoordinateProperty coordProperty) {
    Object value = coordProperty.getValueByUnit(CoordinateProperty.Unit.MM);
    CoordinateCourseProperty property = new CoordinateCourseProperty(key, value);
    return property;
  }

  private CoursePointCourseProperty convertCoursePointProperty(String key,
                                                               CoursePointProperty pointProperty) {
    String value = pointProperty.getPointName();
    CoursePointCourseProperty property = new CoursePointCourseProperty(key, value);
    return property;
  }

  private KeyValueCourseProperty convertKeyValueProperty(String key,
                                                         KeyValueProperty keyValueProperty) {
    String value = keyValueProperty.toString();
    KeyValueCourseProperty property = new KeyValueCourseProperty(key, value);
    return property;
  }

  private KeyValueSetCourseProperty convertKeyValueSetProperty(String key,
                                                               KeyValueSetProperty keyValueSetProperty) {
    List<KeyValueCourseProperty> list = new KeyValueSetListWrapper();
    keyValueSetProperty.getItems().stream().forEach((kvProperty) -> {
      list.add(convertKeyValueProperty(kvProperty.getKey(), kvProperty));
    });
    KeyValueSetCourseProperty property = new KeyValueSetCourseProperty(key, list);
    return property;
  }

  private LengthCourseProperty convertLengthProperty(String key,
                                                     LengthProperty lengthProperty) {
    Object value = lengthProperty.getValueByUnit(LengthProperty.Unit.MM);
    LengthCourseProperty property = new LengthCourseProperty(key, value);
    return property;
  }

  private LinkActionsCourseProperty convertLinkActionsProperty(String key,
                                                               LinkActionsProperty linkActionsProperty) {
    List<String> value = new StringListWrapper();
    linkActionsProperty.getItems().stream().forEach(item
        -> value.add(item));
    LinkActionsCourseProperty property = new LinkActionsCourseProperty(key, value);
    return property;
  }

  private PercentCourseProperty convertPercentProperty(String key,
                                                       PercentProperty percentProperty) {
    Object value = percentProperty.getValueByUnit(PercentProperty.Unit.PERCENT);
    PercentCourseProperty property = new PercentCourseProperty(key, value);
    return property;
  }

  private SelectionCourseProperty convertSelectionProperty(String key,
                                                           SelectionProperty<?> selectionProperty) {
    Enum<?> value = (Enum<?>) selectionProperty.getValue();
    SelectionCourseProperty property = new SelectionCourseProperty(key, value.name());
    return property;
  }

  private LocationTypeCourseProperty convertLocationTypeProperty(String key,
                                                                 LocationTypeProperty locationTypeProperty) {
    Object value = locationTypeProperty.getValue();
    LocationTypeCourseProperty property = new LocationTypeCourseProperty(key, value);
    return property;
  }

  private SpeedCourseProperty convertSpeedProperty(String key,
                                                   SpeedProperty speedProperty) {
    Object value = speedProperty.getValueByUnit(SpeedProperty.Unit.MM_S);
    SpeedCourseProperty property = new SpeedCourseProperty(key, value);
    return property;
  }

  private StringSetCourseProperty convertStringSetProperty(String key,
                                                           StringSetProperty stringSetProperty) {
    List<String> value = new StringListWrapper();
    stringSetProperty.getItems().stream().forEach(item
        -> value.add(item));
    StringSetCourseProperty property = new StringSetCourseProperty(key, value);
    return property;
  }

  private SymbolCourseProperty convertSymbolProperty(String key,
                                                     SymbolProperty symbolProperty) {
    String value = symbolProperty.getLocationRepresentation() == null
        ? null : symbolProperty.getLocationRepresentation().name();
    SymbolCourseProperty property = new SymbolCourseProperty(key, value);
    return property;
  }

  private TripleCourseProperty convertTripleProperty(String key,
                                                     TripleProperty tripleProperty) {
    StringBuilder builder = new StringBuilder();
    Triple triple = tripleProperty.getValue();
    if (triple != null) {
      builder.append(triple.getX()).append("=").
          append(triple.getY()).append("=").
          append(triple.getZ());
    }
    TripleCourseProperty property = new TripleCourseProperty(key, builder.toString());
    return property;
  }

}
