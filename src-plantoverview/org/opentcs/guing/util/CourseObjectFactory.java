/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.util;

import org.opentcs.guing.components.drawing.figures.LinkConnection;
import org.opentcs.guing.components.drawing.figures.LocationFigure;
import org.opentcs.guing.components.drawing.figures.PathConnection;
import org.opentcs.guing.components.drawing.figures.PointFigure;
import org.opentcs.guing.model.elements.BlockModel;
import org.opentcs.guing.model.elements.LayoutModel;
import org.opentcs.guing.model.elements.LinkModel;
import org.opentcs.guing.model.elements.LocationModel;
import org.opentcs.guing.model.elements.LocationTypeModel;
import org.opentcs.guing.model.elements.PathModel;
import org.opentcs.guing.model.elements.PointModel;
import org.opentcs.guing.model.elements.StaticRouteModel;
import org.opentcs.guing.model.elements.VehicleModel;

/**
 * A factory for Figures and ModelComponents.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class CourseObjectFactory {

  /**
   * Creates a new instance.
   */
  public CourseObjectFactory() {
  }

  public VehicleModel createVehicleModel() {
    return new VehicleModel();
  }

  public LayoutModel createLayoutModel() {
    return new LayoutModel();
  }

  public PointModel createPointModel() {
    return new PointModel();
  }

  public PathModel createPathModel() {
    return new PathModel();
  }

  public LocationModel createLocationModel() {
    return new LocationModel();
  }

  public LocationTypeModel createLocationTypeModel() {
    return new LocationTypeModel();
  }

  public LinkModel createLinkModel() {
    return new LinkModel();
  }

  public BlockModel createBlockModel() {
    return new BlockModel();
  }

  public StaticRouteModel createStaticRouteModel() {
    return new StaticRouteModel();
  }

  public PointFigure createPointFigure() {
    return new PointFigure(new PointModel());
  }

  public PathConnection createPathConnection() {
    return new PathConnection(new PathModel());
  }

  public LocationFigure createLocationFigure() {
    return new LocationFigure(new LocationModel());
  }

  public LinkConnection createLinkConnection() {
    return new LinkConnection(new LinkModel());
  }
}
