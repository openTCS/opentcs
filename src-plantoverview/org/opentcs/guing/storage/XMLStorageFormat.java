/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.storage;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Point2D;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.Figure;
import org.opentcs.access.CredentialsException;
import org.opentcs.access.Kernel;
import org.opentcs.data.ObjectPropConstants;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Block;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.StaticRoute;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.model.visualization.ElementPropKeys;
import org.opentcs.data.model.visualization.LocationRepresentation;
import org.opentcs.data.model.visualization.ModelLayoutElement;
import org.opentcs.guing.components.drawing.course.Origin;
import org.opentcs.guing.components.drawing.figures.FigureConstants;
import org.opentcs.guing.components.drawing.figures.LabeledLocationFigure;
import org.opentcs.guing.components.drawing.figures.LabeledPointFigure;
import org.opentcs.guing.components.drawing.figures.LinkConnection;
import org.opentcs.guing.components.drawing.figures.LocationFigure;
import org.opentcs.guing.components.drawing.figures.PathConnection;
import org.opentcs.guing.components.drawing.figures.PointFigure;
import org.opentcs.guing.components.drawing.figures.TCSFigure;
import org.opentcs.guing.components.drawing.figures.TCSLabelFigure;
import org.opentcs.guing.components.properties.event.NullAttributesChangeListener;
import org.opentcs.guing.components.properties.type.ColorProperty;
import org.opentcs.guing.components.properties.type.LengthProperty;
import org.opentcs.guing.components.properties.type.SelectionProperty;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.components.properties.type.SymbolProperty;
import org.opentcs.guing.exchange.DefaultKernelProxyManager;
import org.opentcs.guing.exchange.KernelProxyManager;
import org.opentcs.guing.exchange.adapter.BlockAdapter;
import org.opentcs.guing.exchange.adapter.LinkAdapter;
import org.opentcs.guing.exchange.adapter.OpenTCSProcessAdapter;
import org.opentcs.guing.exchange.adapter.ProcessAdapter;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.SystemModel;
import org.opentcs.guing.model.elements.BlockModel;
import org.opentcs.guing.model.elements.LayoutModel;
import org.opentcs.guing.model.elements.LinkModel;
import org.opentcs.guing.model.elements.LocationModel;
import org.opentcs.guing.model.elements.LocationTypeModel;
import org.opentcs.guing.model.elements.OtherGraphicalElement;
import org.opentcs.guing.model.elements.PathModel;
import org.opentcs.guing.model.elements.PointModel;
import org.opentcs.guing.model.elements.StaticRouteModel;
import org.opentcs.guing.model.elements.VehicleModel;
import org.opentcs.guing.util.CourseObjectFactory;

/**
 *
 * @author Heinz Huber (Fraunhofer IML)
 */
public class XMLStorageFormat {

  /**
   * Creates a new instance.
   */
  private static final Logger log
      = Logger.getLogger(XMLStorageFormat.class.getName());
  /**
   * Die geladenen Figures, die zunächst in dieser Liste abgespeichert werden,
   * um anschließend in der richtigen Reihenfolge (z-Wert) dem Drawing
   * zugewiesen zu werden.
   */
  protected ArrayList<Figure> fRestoredFigures;
  protected SystemModel fSystemModel;
  protected double scaleX = 1.0;
  protected double scaleY = 1.0;
  /**
   * The kernel proxy/connection manager to be used.
   */
  private final KernelProxyManager kernelProxyManager;
  /**
   * The course object factory to be used.
   */
  private final CourseObjectFactory crsObjFactory;

  public XMLStorageFormat(CourseObjectFactory crsObjFactory) {
    this.kernelProxyManager = DefaultKernelProxyManager.instance();
    this.crsObjFactory = requireNonNull(crsObjFactory, "crsObjFactory");
  }

  /**
   * Restore a model from a String (as InputStream).
   *
   * @param buffer der XML-Text, der wieder in ein Modell umgewandelt werden
   * muss, als InputStream
   * @param systemModel
   * @param modelManager
   * @throws Exception wenn beim Wiederherstellen des Modells etwas schiefgeht
   */
  public void restore(InputStream buffer, SystemModel systemModel, OpenTCSModelManager modelManager)
      throws Exception {
    fSystemModel = systemModel;

    SAXBuilder builder = new SAXBuilder();
    Document doc = builder.build(buffer);

    Element root = doc.getRootElement();
    fRestoredFigures = new ArrayList<>();

    // Versionsnummer
    String version = root.getChildText("Version");

    if (!version.equals("1.0")) {
      throw new Exception("Falsche Dateiversion.");
    }

    // Das "alte" Modell kennt kein Visual-Layout, daher hier ein Default-Layout 
    // aus den Angaben zum Reference Point erzeugen
    ModelComponent layoutComponent = systemModel.getMainFolder(SystemModel.LAYOUT);
    OpenTCSProcessAdapter adapter = modelManager.createLayoutAdapter(systemModel, layoutComponent);
    StringProperty pName = (StringProperty) layoutComponent.getProperty(LayoutModel.NAME);
    pName.setText("Default-Layout");
    pName.markChanged();

    // Zeichenmethode
    Element element = root.getChild("DrawingMethod");
    Element child = element.getChild("ReferencePoint");

    if (child != null) {
      String value = child.getChildText("MillimeterPerPixelHorizontal");
      scaleX = Double.parseDouble(value);
      value = child.getChildText("MillimeterPerPixelVertical");
      scaleY = Double.parseDouble(value);

      try {
        LengthProperty pScale = (LengthProperty) layoutComponent.getProperty(LayoutModel.SCALE_X);
        pScale.setValueAndUnit(scaleX, LengthProperty.Unit.MM);
        pScale = (LengthProperty) layoutComponent.getProperty(LayoutModel.SCALE_Y);
        pScale.setValueAndUnit(scaleY, LengthProperty.Unit.MM);
      }
      catch (IllegalArgumentException ex) {
        log.log(Level.WARNING, "", ex);
      }
    }
    // Den Maßstab für das Visual Layout übernehmen!
    if (scaleX != 0.0 && scaleY != 0.0) {
      adapter.updateProcessProperties(true);
      Origin origin = systemModel.getDrawingMethod().getOrigin();
      origin.setScale(scaleX, scaleY);
    }
    // Fahrzeugflotte
    // Die Fahrzeuge werden nicht in der Layout-Datei gespeichert, daher wird hier über die Kernel-Objekte iteriert
//    element = root.getChild("VehicleFleet");
    Iterator<Vehicle> iVehicles = kernel().getTCSObjects(Vehicle.class).iterator();
    while (iVehicles.hasNext()) {
      restoreVehicle(iVehicles.next());
    }

    // Fahrkurs
    element = root.getChild("Course");
    // Knoten
    Iterator<Element> iElements = element.getChild("Nodes").getChildren().iterator();
    // Alle Punkte, die der Kernel kennt
    Set<Point> allPoints = kernel().getTCSObjects(Point.class);

    while (iElements.hasNext()) {
      restorePoint(iElements.next(), allPoints);
    }

    // Stations-Typen
    if (element.getChild("StationTypes") == null) {
      Iterator<LocationType> iLocationTypes = kernel().getTCSObjects(LocationType.class).iterator();

      while (iLocationTypes.hasNext()) {
        restoreLocationType(iLocationTypes.next());
      }
    }
    else {
      iElements = element.getChild("StationTypes").getChildren().iterator();

      while (iElements.hasNext()) {
        restoreLocationType(iElements.next());
      }
    }

    // Stationen
    iElements = element.getChild("Stations").getChildren().iterator();
    Set<Location> allLocations = kernel().getTCSObjects(Location.class);

    while (iElements.hasNext()) {
      restoreLocation(iElements.next(), allLocations);
    }

    // Verbindungen zwischen Punkten
    iElements = element.getChild("Connections").getChildren().iterator();
    Set<Path> allPathes = kernel().getTCSObjects(Path.class);

    while (iElements.hasNext()) {
      restorePath(iElements.next(), allPathes);
    }

    // Links zwischen Punkt und Station - hierzu gibt es keine Kernel-Objekt
    iElements = element.getChild("References").getChildren().iterator();

    while (iElements.hasNext()) {
      restoreLink(iElements.next());
    }

    // Blockbereiche
    iElements = element.getChild("BlockAreas").getChildren().iterator();
    Set<Block> allBlocks = kernel().getTCSObjects(Block.class);

    while (iElements.hasNext()) {
      restoreBlock(iElements.next(), allBlocks);
    }

    // Statische Route
    Element e = element.getChild("StaticRoutes");
    Set<StaticRoute> allStaticRoutes = kernel().getTCSObjects(StaticRoute.class);

    if (e != null) {
      iElements = e.getChildren().iterator();

      while (iElements.hasNext()) {
        restoreStaticRoute(iElements.next(), allStaticRoutes);
      }
    }

    // Sonstige grafische Objekte
    iElements = root.getChild("OtherGraphicalObjects").getChildren().iterator();

    while (iElements.hasNext()) {
      restoreOtherGraphical(iElements.next());
    }

    addToDrawing(fRestoredFigures);
  }

  /**
   * Erstellt aus einem Fahrzeug in der Leitsteuerung ein Fahrzeug in der
   * Modellierung.
   *
   * @param vehicle das Fahrzeug in der Leitsteuerung
   * @return das Fahrzeug
   */
  protected VehicleModel restoreVehicle(Vehicle vehicle) throws CredentialsException {
    VehicleModel standardVehicle = crsObjFactory.createVehicleModel();
    OpenTCSProcessAdapter adapter = addProcessAdapter(vehicle.getReference(), standardVehicle);
    adapter.updateModelProperties();
    fSystemModel.getMainFolder(SystemModel.VEHICLES).add(standardVehicle);

    return standardVehicle;
  }

  /**
   * Wandelt ein XML-Element in einen Knoten um.
   *
   * @param element das XML-Element
   * @param allPoints Alle Punkte, die der Kernel kennt.
   * @return den Knoten
   */
  protected PointModel restorePoint(Element element, Set<Point> allPoints)
      throws CredentialsException {
    // Neues Figure-Objekt
    PointFigure figure = crsObjFactory.createPointFigure();
    // Das zugehörige Modell
    PointModel pointModel = figure.getModel();
    // Die im xml-File gespeicherte ID
    String value = element.getChildText("ObjectId");
    int objectId = Integer.parseInt(value);
    // Eigenschaften für die Zeichnung:
    Element child = element.getChild("Graphic");
    // Position in der DrawingView
    value = child.getChildText("x");
    double xValue = Double.parseDouble(value);
    value = child.getChildText("y");
    double yValue = Double.parseDouble(value);
    // Maßstab berücksichtigen!
    Point2D.Double pos = new Point2D.Double(xValue / scaleX, yValue / scaleY);
    // Figur auf diese Position verschieben
    figure.setBounds(pos, null);
    // Farbe für Umriss
    child = child.getChild("FrameColor");
    figure.set(AttributeKeys.STROKE_COLOR, decodeColor(child));
    child = child.getParentElement();
    // ... und Füllung
    child = child.getChild("FillColor");
    figure.set(AttributeKeys.FILL_COLOR, decodeColor(child));
    child = child.getParentElement();
    // TODO: Wofür wird das benötigt?
//	int zValue = decodeZValue(child.getChild("ZValue"));
//	figure.setZValue(zValue);
    // Die zugehörige Beschriftung:
    TCSLabelFigure label = decodeLabel(child.getChild("Label"), figure, pointModel.getName());
    LabeledPointFigure lpf = new LabeledPointFigure(figure);
    pos = figure.getStartPoint();
    pos.x += label.getOffset().x;
    pos.y += label.getOffset().y;
    label.setBounds(pos, pos);
    lpf.setLabel(label);

    // Suche das zugehörige Kernel-Objekt mit der selben ID
    OpenTCSProcessAdapter adapter = addProcessAdapter(objectId, allPoints, pointModel);
    // Todo: if adapter == null:..
    ModelLayoutElement mle = new ModelLayoutElement(adapter.getProcessObject());

    Map<String, String> layoutProperties = mle.getProperties();
    layoutProperties.put(ElementPropKeys.POINT_POS_X, (int) xValue + "");
    layoutProperties.put(ElementPropKeys.POINT_POS_Y, (int) yValue + "");
    layoutProperties.put(ElementPropKeys.POINT_LABEL_OFFSET_X, label.getOffset().x + "");
    layoutProperties.put(ElementPropKeys.POINT_LABEL_OFFSET_Y, label.getOffset().y + "");
//	layoutProperties.put(ElementPropKeys.POINT_LABEL_ORIENTATION_ANGLE, ...);
    mle.setProperties(layoutProperties);
    adapter.setLayoutElement(mle);
//			kernel().setVisualLayoutElements(layout.getReference(), layoutElements);

    // Setze Typ, Koordinaten, ... aus dem Kernel-Modell
    adapter.updateModelProperties();

    pointModel.setFigure(lpf);
    pointModel.addAttributesChangeListener(lpf);
    fSystemModel.getMainFolder(SystemModel.POINTS).add(pointModel);
    fRestoredFigures.add(lpf);
    StringProperty spx = (StringProperty) pointModel.getProperty(ElementPropKeys.POINT_POS_X);
    spx.markChanged();
    pointModel.propertiesChanged(new NullAttributesChangeListener());
    // Koordinaten des Punktes ändern sich, wenn der Maßstab der Zeichnung verändert wird
    Origin origin = fSystemModel.getDrawingMethod().getOrigin();

    if (origin != null) {
      origin.addListener(lpf);
      lpf.set(FigureConstants.ORIGIN, origin);
    }

    return pointModel;
  }

  /**
   * Stellt für einen in der Leitsteuerung vorhandenen Stationstyp ein Pendant
   * in der Modellierung her.
   *
   * @param locationType
   * @return den Stationstyp in der Modellierung
   */
  protected LocationTypeModel restoreLocationType(LocationType locationType)
      throws CredentialsException {
    LocationTypeModel guiLocationType = new LocationTypeModel();
    OpenTCSProcessAdapter adapter = addProcessAdapter(locationType.getReference(), guiLocationType);
    adapter.updateModelProperties();

    fSystemModel.getMainFolder(SystemModel.LOCATION_TYPES).add(guiLocationType);

    return guiLocationType;
  }

  /**
   * Wandelt ein XML-Element in einen Stationstyp um.
   *
   * @param element das XML-Element
   * @return die Strecke
   */
  protected LocationTypeModel restoreLocationType(Element element)
      throws CredentialsException {

    Set<LocationType> allLocationTypes = kernel().getTCSObjects(LocationType.class);
    LocationTypeModel locationTypeModel = crsObjFactory.createLocationTypeModel();

    String value = element.getChildText("ObjectId");
    int objectId = Integer.parseInt(value);
    OpenTCSProcessAdapter adapter = addProcessAdapter(objectId, allLocationTypes, locationTypeModel);
    adapter.updateModelProperties();

    Element child = element.getChild("Symbol");
    SymbolProperty pSymbol = (SymbolProperty) locationTypeModel.getProperty(ObjectPropConstants.LOCTYPE_DEFAULT_REPRESENTATION);
    LocationRepresentation repr;

    switch (child.getText()) {
      case "ChargingStation20.png":
        repr = LocationRepresentation.RECHARGE_GENERIC;
        break;
      case "TransferStation20.png":
        repr = LocationRepresentation.LOAD_TRANSFER_GENERIC;
        break;
      case "WorkingStation20.png":
        repr = LocationRepresentation.WORKING_GENERIC;
        break;
      default:
        repr = null;
    }

    pSymbol.setLocationRepresentation(repr);
    pSymbol.markChanged();

    if (kernel().getState() == Kernel.State.MODELLING) {
      adapter.updateProcessProperties(true);
    }

    fSystemModel.getMainFolder(SystemModel.LOCATION_TYPES).add(locationTypeModel);

    return locationTypeModel;
  }

  /**
   * Wandelt ein XML-Element in eine Station um.
   *
   * @param element das XML-Element
   * @param allLocations Alle Stationen, die der Kernel kennt
   * @return die Station
   */
  protected LocationModel restoreLocation(Element element, Set<Location> allLocations)
      throws CredentialsException, NumberFormatException {
    // Neues Figure-Objekt
    LocationFigure figure = crsObjFactory.createLocationFigure();
    // Das zugehörige Modell
    LocationModel locationModel = figure.getModel();
    // Die im xml-File gespeicherte ID
    String value = element.getChildText("ObjectId");
    // Der Typ der Station
    LocationTypeModel type = null;
    // Suche das zugehörige Kernel-Objekt mit der selben ID
    int objectId = Integer.parseInt(value);
    Iterator<Location> i = allLocations.iterator();

    while (i.hasNext()) {
      Location l = i.next();

      if (l.getId() == objectId) {
        type = (LocationTypeModel) getModelComponent(l.getType());
        break;
      }
    }

    // Eigenschaften für die Zeichnung:
    Element child = element.getChild("Graphic");
    // Position in der DrawingView
    value = child.getChildText("x");
    double xValue = Double.parseDouble(value);
    value = child.getChildText("y");
    double yValue = Double.parseDouble(value);
    Point2D.Double pos = new Point2D.Double(xValue / scaleX, yValue / scaleY);
    // Figur auf diese Position verschieben
    figure.setBounds(pos, null);
    // Farbe für Umriss
    child = child.getChild("FrameColor");
    figure.set(AttributeKeys.STROKE_COLOR, decodeColor(child));
    child = child.getParentElement();
    // ... und Füllung
    child = child.getChild("FillColor");
    figure.set(AttributeKeys.FILL_COLOR, decodeColor(child));
    child = child.getParentElement();
    // TODO: Wofür wird das benötigt?
//	int zValue = decodeZValue(child.getChild("ZValue"));
//	figure.setZValue(zValue);
    // Die zugehörige Beschriftung:
    TCSLabelFigure label = decodeLabel(child.getChild("Label"), figure, locationModel.getName());
    LabeledLocationFigure llf = new LabeledLocationFigure(figure);
    pos = figure.getStartPoint();
    pos.x += label.getOffset().x;
    pos.y += label.getOffset().y;
    label.setBounds(pos, pos);
    llf.setLabel(label);

    // Suche das zugehörige Kernel-Objekt mit der selben ID
    OpenTCSProcessAdapter adapter = addProcessAdapter(objectId, allLocations, locationModel);
    ModelLayoutElement mle = new ModelLayoutElement(adapter.getProcessObject());

    Map<String, String> layoutProperties = mle.getProperties();
    layoutProperties.put(ElementPropKeys.LOC_POS_X, (int) xValue + "");
    layoutProperties.put(ElementPropKeys.LOC_POS_Y, (int) yValue + "");
    layoutProperties.put(ElementPropKeys.LOC_LABEL_OFFSET_X, label.getOffset().x + "");
    layoutProperties.put(ElementPropKeys.LOC_LABEL_OFFSET_Y, label.getOffset().y + "");
//	layoutProperties.put(ElementPropKeys.LOC_LABEL_ORIENTATION_ANGLE, ...);
    mle.setProperties(layoutProperties);
    adapter.setLayoutElement(mle);
//			kernel().setVisualLayoutElements(layout.getReference(), layoutElements);

    // Setze Typ, Koordinaten, ... aus dem Kernel-Modell
    adapter.updateModelProperties();
    StringProperty spx = (StringProperty) locationModel.getProperty(ElementPropKeys.LOC_POS_X);
    spx.markChanged();

    locationModel.setFigure(llf);
    locationModel.addAttributesChangeListener(llf);
    fSystemModel.getMainFolder(SystemModel.LOCATIONS).add(locationModel);
    fRestoredFigures.add(llf);
    // Den Stationstyp zuweisen
    locationModel.setLocationType(type);
    locationModel.updateTypeProperty(fSystemModel.getLocationTypeModels());
    locationModel.propertiesChanged(new NullAttributesChangeListener());

    Origin ref = fSystemModel.getDrawingMethod().getOrigin();

    if (ref != null) {
      ref.addListener(llf);
      llf.set(FigureConstants.ORIGIN, ref);
    }

    return locationModel;
  }

  /**
   * Wandelt ein XML-Element in eine Strecke um.
   *
   * @param element das XML-Element
   * @param allPaths Alle Pfade, die der Kernel kennt
   * @return die Strecke
   * @throws java.lang.Exception
   */
  protected PathModel restorePath(Element element, Set<Path> allPaths)
      throws CredentialsException, NumberFormatException, Exception {

    String value = element.getChildText("ObjectId");
    int objectId = Integer.parseInt(value);
    Path path = null;
    Iterator<Path> iPaths = allPaths.iterator();

    while (iPaths.hasNext()) {
      path = iPaths.next();

      if (path.getId() == objectId) {
        break;
      }
    }

    PointModel startPoint = (PointModel) getModelComponent(path.getSourcePoint());
    PointModel endPoint = (PointModel) getModelComponent(path.getDestinationPoint());

    Element child = element.getChild("Graphic");
    // Verbindung als "PathConnection"?
    PathConnection pathFigure = crsObjFactory.createPathConnection();
    pathFigure.set(AttributeKeys.STROKE_COLOR, decodeColor(child.getChild("FrameColor")));

    PathModel pathModel = pathFigure.getModel();

    pathFigure.connect(startPoint.getFigure(), endPoint.getFigure());

    OpenTCSProcessAdapter adapter = addProcessAdapter(objectId, allPaths, pathModel);
    adapter.updateModelProperties();
    pathFigure.updateDecorations();

    PathModel model = (PathModel) adapter.getModel();
    SelectionProperty property = (SelectionProperty) model.getProperty(ElementPropKeys.PATH_CONN_TYPE);

//	String connectionType = properties.get(ElementPropKeys.PATH_CONN_TYPE);
//	property.setValue(connectionType);
//	pathFigure.setLinerByName(connectionType);
//	if (connectionType.equals(GuiPath.BEZIER)) {
    // Stützpunkte - aus dem alten Modell werden z.Zt. nur Bezierkurven 
    // mit 1 oder 2 control points unterstützt
    child = child.getChild("Points");
    List<Element> lPoints = child.getChildren();
    int size = lPoints.size();

    if (size >= 1) {
      property.setValue(PathModel.LinerType.BEZIER);
      pathFigure.setLinerByType(PathModel.LinerType.BEZIER);

      Element point = lPoints.get(0);
      value = point.getChildText("x");
      double x = Double.parseDouble(value);
      value = point.getChildText("y");
      double y = Double.parseDouble(value);
      Point2D.Double cp1 = new Point2D.Double(x, y);

      if (size >= 2) {
        point = lPoints.get(1);
        value = point.getChildText("x");
        x = Double.parseDouble(value);
        value = point.getChildText("y");
        y = Double.parseDouble(value);
        Point2D.Double cp2 = new Point2D.Double(x, y);
        pathFigure.addControlPoints(cp1, cp2);	// Cubic curve
      }
      else {
        pathFigure.addControlPoints(cp1, cp1);	// Quadratic curve
      }
    }
    else {
      property.setValue(PathModel.LinerType.DIRECT);
    }

//		child = (Element) child.getParentElement();
//		if (child.getChild("Arrow") != null) {
//			value = child.getChildText("Arrow");
//			ImmutableUnitProperty property = (ImmutableUnitProperty) edge.getProperty("ArrowLocation");
//			property.setValueAndUnit(Double.parseDouble(value), property.getUnit());
//		}
//		int zValue = decodeZValue(child.getChild("ZValue"));
//		figure.setZValue(zValue);
    pathModel.setFigure(pathFigure);
    pathModel.addAttributesChangeListener(pathFigure);
    fSystemModel.getMainFolder(SystemModel.PATHS).add(pathModel);
    fRestoredFigures.add(pathFigure);

    Origin ref = fSystemModel.getDrawingMethod().getOrigin();

    if (ref != null) {
      ref.addListener(pathFigure);
      pathFigure.set(FigureConstants.ORIGIN, ref);
    }

    if (kernel().getState() == Kernel.State.MODELLING) {
      adapter.updateProcessProperties(true);
    }

    return pathModel;
  }

  /**
   * Wandelt ein XML-Element in einen Link um.
   *
   * @param element das XML-Element
   * @return der Link
   */
  protected LinkModel restoreLink(Element element)
      throws CredentialsException, NumberFormatException {

    PointModel pointModel = null;
    int objectId = Integer.parseInt(element.getChildText("Node"));

    for (Point p : kernel().getTCSObjects(Point.class)) {
      if (p.getId() == objectId) {
        pointModel = (PointModel) getModelComponent(p.getReference());
        break;
      }
    }

    objectId = Integer.parseInt(element.getChildText("Station"));
    LocationModel locationModel = null;

    for (Location location : kernel().getTCSObjects(Location.class)) {
      if (location.getId() == objectId) {
        locationModel = (LocationModel) getModelComponent(location.getReference());
        break;
      }
    }

    LinkConnection figure = crsObjFactory.createLinkConnection();
    LinkModel linkModel = figure.getModel();

    LabeledPointFigure lnf = pointModel.getFigure();
    LabeledLocationFigure lsf = locationModel.getFigure();
    // Verbindet Point und Location
    figure.connect(lnf, lsf);

    OpenTCSProcessAdapter adapter = addProcessAdapter(linkModel, pointModel, locationModel);
    adapter.updateModelProperties();

    // Grafik einer Referenz
    Element child = element.getChild("Graphic");
    figure.set(AttributeKeys.STROKE_COLOR, decodeColor(child.getChild("FrameColor")));

    // TODO: Soll die Referenzverbindung Stützpunkte haben?
//		child = child.getChild("Points");
//		Iterator j = child.getChildren().iterator();
//
//		while (j.hasNext()) {
//			Element grandChild = (Element) j.next();
//			value = grandChild.getChildText("x");
//			double x = Double.parseDouble(value);
//			value = grandChild.getChildText("y");
//			double y = Double.parseDouble(value);
//			figure.getZoomPoints().add(new ZoomPoint(x, y));
//		}
    // TODO: Wofür wird das benötigt?
//		child = child.getParentElement();
//		int zValue = decodeZValue(child.getChild("ZValue"));
////		figure.setZValue(zValue);
    linkModel.setFigure(figure);
    linkModel.addAttributesChangeListener(figure);
    fSystemModel.getMainFolder(SystemModel.LINKS).add(linkModel);
    fRestoredFigures.add(figure);

    if (kernel().getState() == Kernel.State.MODELLING) {
      adapter.updateProcessProperties(true);
    }

    return linkModel;
  }

  /**
   * Wandelt ein XML-Element in einen Blockbereich um.
   *
   * @param element das XML-Element
   * @param allBlocks Alle Blöcke, die der Kernel kennt
   * @return den Blockbereich
   */
  protected BlockModel restoreBlock(Element element, Set<Block> allBlocks)
      throws CredentialsException {

    BlockModel block = crsObjFactory.createBlockModel();
    String value = element.getChildText("ObjectId");
    int id = Integer.parseInt(value);

    BlockAdapter adapter = (BlockAdapter) addProcessAdapter(id, allBlocks, block);
    adapter.updateModelProperties();

    Block b = kernel().getTCSObject(Block.class, adapter.getProcessObject());

    for (TCSObjectReference<?> memberRef : b.getMembers()) {
      block.addCourseElement(getModelComponent(memberRef));
    }

    ColorProperty color = (ColorProperty) block.getProperty(ElementPropKeys.BLOCK_COLOR);
    color.setColor(decodeColor(element.getChild("Color")));

    fSystemModel.getMainFolder(SystemModel.BLOCKS).add(block);

    if (kernel().getState() == Kernel.State.MODELLING) {
      adapter.updateProcessProperties(true);
    }

    return block;
  }

  /**
   * Wandelt ein XML-Element in eine statische Route um.
   *
   * @param element das XML-Element
   * @param allStaticRoutes
   * @return die statische Route
   */
  protected StaticRouteModel restoreStaticRoute(Element element, Set<StaticRoute> allStaticRoutes)
      throws CredentialsException {

    StaticRouteModel staticRoute = crsObjFactory.createStaticRouteModel();
    String value = element.getChildText("ObjectId");
    int id = Integer.parseInt(value);
    OpenTCSProcessAdapter adapter = addProcessAdapter(id, allStaticRoutes, staticRoute);
    adapter.updateModelProperties();

    ColorProperty color = (ColorProperty) staticRoute.getProperty(ElementPropKeys.BLOCK_COLOR);
    color.setColor(decodeColor(element.getChild("Color")));

    fSystemModel.getMainFolder(SystemModel.STATIC_ROUTES).add(staticRoute);

    return staticRoute;
  }

  /**
   * Wandelt ein XML-Element in einen Blockbereich um.
   *
   * @param element das XML-Element
   * @return den Blockbereich
   */
  protected OtherGraphicalElement restoreOtherGraphical(Element element) {
    String name = element.getName();
    Figure graphicalFigure = null;

    if (name.equals("Text")) {
////			TransformableTextFigure figure = (TransformableTextFigure) getPrototypeManager().clonePrototype(PrototypeConstants.OtherGraphical.TEXT);
////			String value = element.getChildText("Text");
////			figure.setText(value);
////
////			ZoomPoint zoomPoint = new ZoomPoint();
////			decode(element, zoomPoint);
////			figure.setTopLeft(zoomPoint);
////			figure.transform(1.0);
////
////			value = element.getChildText("FontSize");
////			int fontSize = (int) Double.parseDouble(value);
////
////			value = element.getChildText("FontFamily");
////			figure.setFont(new Font(value, Font.PLAIN, fontSize));
////
////			Element child = element.getChild("FrameColor");
////			figure.setAttribute("FrameColor", decodeColor(child));
////
////			child = element.getChild("FillColor");
////			figure.setAttribute("FillColor", decodeColor(child));
////
////			int zValue = decodeZValue(element.getChild("ZValue"));
////			figure.setZValue(zValue);
////
////			graphicalFigure = figure;
    }
////		else {
////			String type = "";
////			if (name.equals("Line")) {
////				type = PrototypeConstants.OtherGraphical.LINE;
////			}
////			if (name.equals("Rectangle")) {
////				type = PrototypeConstants.OtherGraphical.RECTANGLE;
////			}
////			if (name.equals("Oval")) {
////				type = PrototypeConstants.OtherGraphical.ELLIPSE;
////			}
////			TransformableDecorator decorator = (TransformableDecorator) getPrototypeManager().clonePrototype(type);
////			decode(element.getChild("TopLeft"), decorator.topLeft());
////			decode(element.getChild("BottomRight"), decorator.bottomRight());
////
////			decorator.transform(1.0);
////
////			Element child = element.getChild("FrameColor");
////			decorator.setAttribute("FrameColor", decodeColor(child));
////
////			child = element.getChild("FillColor");
////			decorator.setAttribute("FillColor", decodeColor(child));
////
////			int zValue = decodeZValue(element.getChild("ZValue"));
////			decorator.setZValue(zValue);
////
////			graphicalFigure = decorator;
////		}

    OtherGraphicalElement graphical = (OtherGraphicalElement) graphicalFigure.get(FigureConstants.MODEL);
    fSystemModel.getMainFolder(SystemModel.OTHER_GRAPHICAL_ELEMENTS).add(graphical);
    fRestoredFigures.add(graphicalFigure);
    graphical.setFigure(graphicalFigure);

    return graphical;
  }

  /**
   * Wandelt ein XML-Element in eine Farbe um.
   *
   * @param element das XML-Element mit den Farbeigenschaften
   * @return die Farbe
   * @throws java.lang.NumberFormatException wenn beim Parsen ein Fehler
   * auftritt
   */
  public Color decodeColor(Element element) throws NumberFormatException {
    String value = element.getChildText("Red");
    int red = Integer.parseInt(value);
    value = element.getChildText("Green");
    int green = Integer.parseInt(value);
    value = element.getChildText("Blue");
    int blue = Integer.parseInt(value);

    return new Color(red, green, blue);
  }

  /**
   * Wandelt ein XML-Element zurück in eine LabeledPointFigure.
   *
   * @param element Das XML-Element
   * @param figure Die Figur mit der das Label zu verbinden ist
   * @param text Der Text des Labels
   * @return Die LabeledPointFigure
   * @throws java.lang.NumberFormatException Wenn beim Parsen ein Fehler
   * auftritt
   */
  public TCSLabelFigure decodeLabel(Element element, TCSFigure figure, String text)
      throws NumberFormatException {

    String value;
    Element child;
    TCSLabelFigure label = new TCSLabelFigure(text);

    value = element.getChildText("DiffX");
    int diffX = Integer.parseInt(value);
    value = element.getChildText("DiffY");
    int diffY = Integer.parseInt(value);
    // W, H werden nicht mehr ausgewertet
//	value = element.getChildText("Width");
//	int width = Integer.parseInt(value);
//	value = element.getChildText("Height");
//	int height = Integer.parseInt(value);
    label.setOffset(new Point2D.Double(diffX, diffY));

    child = element.getChild("Font");
    String fontName = child.getChildText("Name");
    value = child.getChildText("Style");
    int fontStyle = Integer.parseInt(value);
    value = child.getChildText("Size");
    int fontSize = Integer.parseInt(value);
    label.set(AttributeKeys.FONT_FACE, new Font(fontName, fontStyle, fontSize));

    child = child.getParentElement();
    Color color = decodeColor(child.getChild("FrameColor"));
    label.set(AttributeKeys.STROKE_COLOR, color);
    color = decodeColor(child.getChild("FillColor"));
    label.set(AttributeKeys.FILL_COLOR, color);
    // TODO: Wofür wird das benötigt?
//	int zValue = decodeZValue(child.getChild("ZValue"));
//	label.setZValue(zValue);

    return label;
  }

  /**
   * Erzeugt zu einer gelesenen ObjektId und einem ModelComponent-Objekt einen
   * passenden ProcessAdapter und fügt ihn dem EventDispatcher hinzu.
   *
   * @param id die Objekt-Id
   * @param processObjects die möglichen Objekte in der Leitsteuerung
   * @param model das ModelComponent-Objekt
   * @return den erzeugten ProcessAdapter
   */
  private OpenTCSProcessAdapter addProcessAdapter(
      int id, Set<?> processObjects, ModelComponent model) {

    Iterator<?> i = processObjects.iterator();

    while (i.hasNext()) {
      TCSObject<?> o = (TCSObject<?>) i.next();

      if (o.getId() == id) {
        return addProcessAdapter(o.getReference(), model);
      }
    }

    return null;
  }

  /**
   * Erzeugt zu einem ModelComponent-Objekt und einem TCSObject einen passenden
   * ProcessAdapter und fügt ihn dem EventDispatcher hinzu.
   *
   * @param ref das Objekte in der Leitsteuerung
   * @param model das ModelComponent-Objekt
   * @return den erzeugten ProcessAdapter
   */
  private OpenTCSProcessAdapter addProcessAdapter(
      TCSObjectReference<?> ref, ModelComponent model) {

    ProcessAdapter adapter = fSystemModel.getEventDispatcher().createProcessAdapter(model.getClass());
    adapter.setModel(model);
    adapter.setEventDispatcher(fSystemModel.getEventDispatcher());
    adapter.setProcessObject(ref);
    adapter.register();

    return (OpenTCSProcessAdapter) adapter;
  }

  /**
   * Erzeugt zu einem gelesenen Link einen passenden ProcessAdapter und fügt ihn
   * dem EventDispatcher hinzu.
   *
   * @param link Die Verknüpfung
   * @param point Der Meldepunkt
   * @param location Die Station
   * @return Der erzeugte ProcessAdapter
   */
  @SuppressWarnings("unchecked")
  private LinkAdapter addProcessAdapter(LinkModel link,
                                                  PointModel point,
                                                  LocationModel location) {
    LinkAdapter adapter = (LinkAdapter) fSystemModel.getEventDispatcher()
        .createProcessAdapter(LinkModel.class);
    adapter.setModel(link);
    adapter.setEventDispatcher(fSystemModel.getEventDispatcher());
    adapter.register();

    ProcessAdapter pointAdapter
        = fSystemModel.getEventDispatcher().findProcessAdapter(point);
    ProcessAdapter locationAdapter
        = fSystemModel.getEventDispatcher().findProcessAdapter(location);
    adapter.setConnectedProcessObjects(
        (TCSObjectReference<Point>) pointAdapter.getProcessObject(),
        (TCSObjectReference<Location>) locationAdapter.getProcessObject());
    fSystemModel.getEventDispatcher().addProcessAdapter(adapter);

    return adapter;
  }

  /**
   * Fügt die wiederhergestellten Figures in der richtigen Reihenfolge dem
   * Drawing hinzu.
   *
   * @param figures die wiederhergestellten Figures
   */
  protected void addToDrawing(ArrayList<Figure> figures) {
//		Comparator<Figure> c = new Comparator<Figure>() {
//
//			public int compare(Figure f1, Figure f2) {
//				if (f1.getLayer() > f2.getLayer()) {
//					return 1;
//				}
//				else {
//					return -1;
//				}
//			}
//		};
//
//		Collections.sort(figures, c);
    Drawing drawing = fSystemModel.getDrawing();
    Iterator<Figure> iFigures = figures.iterator();

    while (iFigures.hasNext()) {
      Figure figure = iFigures.next();
      drawing.add(figure);
    }
  }

  /**
   * Liefert die Leitsteuerung.
   *
   * @return die Leitsteuerung
   */
  private Kernel kernel() {
    return kernelProxyManager.kernel();
  }

  /**
   * Findet zu einem TCSObject das passende Objekt in der Modellierung.
   *
   * @param ref die Referenz auf das TCSObject
   * @return das Objekt in der Modellierung
   */
  private ModelComponent getModelComponent(TCSObjectReference<?> ref) {
    ProcessAdapter adapter = fSystemModel.getEventDispatcher().findProcessAdapter(ref);

    if (adapter == null) {
      return null;
    }

    return adapter.getModel();
  }

  /**
   * Wandelt ein XML-Element in einen z-Wert um.
   *
   * @param element das XML-Element
   * @return den z-Wert
   * @throws NumberFormatException wenn beim Parsen ein Fehler auftritt
   */
  public int decodeZValue(Element element) throws NumberFormatException {
    if (element == null) {
      return 0;
    }

    return Integer.parseInt(element.getText());
  }
}
