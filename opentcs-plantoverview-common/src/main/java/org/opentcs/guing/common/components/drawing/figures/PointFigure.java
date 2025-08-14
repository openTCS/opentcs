// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.common.components.drawing.figures;

import static java.util.Objects.requireNonNull;

import com.google.inject.assistedinject.Assisted;
import jakarta.inject.Inject;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.jhotdraw.geom.Geom;
import org.opentcs.data.model.Couple;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.guing.base.AllocationState;
import org.opentcs.guing.base.model.elements.BlockModel;
import org.opentcs.guing.base.model.elements.PointModel;
import org.opentcs.guing.base.model.elements.VehicleModel;
import org.opentcs.guing.common.components.drawing.DrawingOptions;
import org.opentcs.guing.common.components.drawing.Strokes;
import org.opentcs.guing.common.components.drawing.ZoomPoint;
import org.opentcs.guing.common.exchange.AllocatedResourcesContainer;

/**
 * A figure that represents a decision point.
 */
public class PointFigure
    extends
      TCSFigure {

  /**
   * A color for parking positions.
   */
  private static final Color C_PARK = Color.BLUE;
  /**
   * A color for halt positions.
   */
  private static final Color C_HALT = Color.LIGHT_GRAY;
  /**
   * The figure's diameter in drawing units (pixels at 100% zoom).
   */
  private final int fDiameter;
  /**
   * The drawing options.
   */
  private final DrawingOptions drawingOptions;
  /**
   * Maintains a set of all currently allocated resources.
   */
  private final AllocatedResourcesContainer allocatedResourcesContainer;

  /**
   * Creates a new instance.
   *
   * @param model The model corresponding to this graphical object.
   * @param allocatedResourcesContainer A container that maintains currently allocated resources.
   * @param drawingOptions The drawing options.
   */
  @Inject
  public PointFigure(
      @Assisted
      PointModel model,
      AllocatedResourcesContainer allocatedResourcesContainer,
      DrawingOptions drawingOptions
  ) {
    super(model);
    this.allocatedResourcesContainer = requireNonNull(
        allocatedResourcesContainer,
        "allocatedResourcesContainer"
    );
    this.drawingOptions = requireNonNull(drawingOptions, "drawingOptions");

    fDiameter = 10;
    fDisplayBox = new Rectangle(fDiameter, fDiameter);
    fZoomPoint = new ZoomPoint(0.5 * fDiameter, 0.5 * fDiameter);
  }

  @Override
  public PointModel getModel() {
    return (PointModel) get(FigureConstants.MODEL);
  }

  public Point center() {
    return Geom.center(fDisplayBox);
  }

  public Ellipse2D.Double getShape() {
    Rectangle2D r2 = fDisplayBox.getBounds2D();
    Ellipse2D.Double shape
        = new Ellipse2D.Double(r2.getX(), r2.getY(), fDiameter - 1, fDiameter - 1);
    return shape;
  }

  @Override  // Figure
  public Rectangle2D.Double getBounds() {
    Rectangle2D r2 = fDisplayBox.getBounds2D();
    Rectangle2D.Double r2d = new Rectangle2D.Double();
    r2d.setRect(r2);

    return r2d;
  }

  @Override  // Figure
  public Object getTransformRestoreData() {
    // Never used?
    return fDisplayBox.clone();
  }

  @Override  // Figure
  public void restoreTransformTo(Object restoreData) {
    // Never used?
    Rectangle r = (Rectangle) restoreData;
    fDisplayBox.x = r.x;
    fDisplayBox.y = r.y;
    fDisplayBox.width = r.width;
    fDisplayBox.height = r.height;
    fZoomPoint.setX(r.x + 0.5 * r.width);
    fZoomPoint.setY(r.y + 0.5 * r.height);
  }

  @Override  // Figure
  public void transform(AffineTransform tx) {
    Point2D center = getZoomPoint().getPixelLocationExactly();
    Point2D lead = new Point2D.Double();  // not used
    setBounds(
        (Point2D.Double) tx.transform(center, center),
        (Point2D.Double) tx.transform(lead, lead)
    );
  }

  @Override  // AbstractFigure
  public void setBounds(Point2D.Double anchor, Point2D.Double lead) {
    fZoomPoint.setX(anchor.x);
    fZoomPoint.setY(anchor.y);
    fDisplayBox.x = (int) (anchor.x - 0.5 * fDiameter);
    fDisplayBox.y = (int) (anchor.y - 0.5 * fDiameter);
  }

  @Override
  protected void drawFigure(Graphics2D g) {
    if (drawingOptions.isBlocksVisible()) {
      drawBlockDecoration(g);
    }
    drawRouteDecoration(g);
    if (drawingOptions.isEnvelopesVisible()) {
      drawEnvelopesOfAllocatedBlocks(g);
    }

    super.drawFigure(g);
  }

  private void drawRouteDecoration(Graphics2D g) {
    for (Map.Entry<VehicleModel, AllocationState> entry : getModel().getAllocationStates()
        .entrySet()) {
      VehicleModel vehicleModel = entry.getKey();
      vehicleModel.getName();
      switch (entry.getValue()) {
        case CLAIMED:
          drawDecoration(
              g,
              Strokes.PATH_ON_ROUTE,
              transparentColor(vehicleModel.getDriveOrderColor(), 70)
          );
          if (drawingOptions.isEnvelopesVisible() && isNextClaimedResource(vehicleModel)) {
            drawEnvelope(
                g,
                Strokes.ENVELOPES,
                transparentColor(vehicleModel.getDriveOrderColor(), 70),
                vehicleModel.getPropertyEnvelopeKey().getText()
            );
          }
          break;
        case ALLOCATED:
          drawDecoration(g, Strokes.PATH_ON_ROUTE, vehicleModel.getDriveOrderColor());
          if (drawingOptions.isEnvelopesVisible()) {
            drawEnvelope(
                g,
                Strokes.ENVELOPES,
                vehicleModel.getDriveOrderColor(),
                vehicleModel.getPropertyEnvelopeKey().getText()
            );
          }
          break;
        case ALLOCATED_WITHDRAWN:
          drawDecoration(g, Strokes.PATH_ON_WITHDRAWN_ROUTE, Color.GRAY);
          if (drawingOptions.isEnvelopesVisible()) {
            drawEnvelope(
                g,
                Strokes.ENVELOPES,
                vehicleModel.getDriveOrderColor(),
                vehicleModel.getPropertyEnvelopeKey().getText()
            );
          }
          break;
        default:
          // Don't draw any decoration.
      }
    }
  }

  private boolean isNextClaimedResource(VehicleModel vehicleModel) {
    return vehicleModel.getClaimedResources().getItems().getFirst()
        .stream()
        .map(TCSResourceReference::getName)
        .anyMatch(name -> name.equals(getModel().getName()));
  }

  private void drawBlockDecoration(Graphics2D g) {
    for (BlockModel blockModel : getModel().getBlockModels()) {
      drawDecoration(g, Strokes.BLOCK_ELEMENT, transparentColor(blockModel.getColor(), 192));
    }
  }

  private void drawEnvelopesOfAllocatedBlocks(Graphics2D g) {
    getModel().getBlockModels().stream()
        .flatMap(blockModel -> getAllocatingVehicles(blockModel).stream())
        .forEach(
            vehicleModel -> drawEnvelope(
                g,
                Strokes.ENVELOPES,
                vehicleModel.getDriveOrderColor(),
                vehicleModel.getPropertyEnvelopeKey().getText()
            )
        );
  }

  private List<VehicleModel> getAllocatingVehicles(BlockModel blockModel) {
    return blockModel.getPropertyElements().getItems()
        .stream()
        .map(blockElement -> allocatedResourcesContainer.getAllocatedResources().get(blockElement))
        .filter(Objects::nonNull)
        .flatMap(component -> component.getAllocationStates().entrySet().stream())
        .filter(
            entry -> entry.getValue() == AllocationState.ALLOCATED
                || entry.getValue() == AllocationState.ALLOCATED_WITHDRAWN
        )
        .map(Map.Entry::getKey)
        .toList();
  }

  private Color transparentColor(Color color, int alpha) {
    return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
  }

  private void drawDecoration(Graphics2D g, Stroke stroke, Color color) {
    g.setStroke(stroke);
    g.setColor(color);
    g.draw(this.getShape());
  }

  private void drawEnvelope(Graphics2D g, Stroke stroke, Color color, String envelopeKey) {
    g.setStroke(stroke);
    g.setColor(color);
    getModel().getPropertyVehicleEnvelopes().getValue()
        .stream()
        .filter(env -> env.getKey().equals(envelopeKey))
        .findFirst()
        .ifPresent(envelopeModel -> {
          List<Couple> vertices = envelopeModel.getVertices();
          g.drawPolyline(
              vertices.stream()
                  .mapToInt(
                      couple -> (int) (couple.getX() / get(FigureConstants.ORIGIN).getScaleX())
                  )
                  .toArray(),
              vertices.stream()
                  .mapToInt(
                      couple -> (int) (couple.getY() / get(FigureConstants.ORIGIN).getScaleY()
                          * (-1))
                  )
                  .toArray(),
              vertices.size()
          );
        });
  }

  @Override
  protected void drawFill(Graphics2D g) {
    Rectangle rect = fDisplayBox;

    if (getModel().getPropertyType().getValue() == PointModel.Type.PARK) {
      g.setColor(C_PARK);
    }
    else {
      g.setColor(C_HALT);
    }

    if (rect.width > 0 && rect.height > 0) {
      g.fillOval(rect.x, rect.y, rect.width, rect.height);
    }

    if (getModel().getPropertyType().getValue() == PointModel.Type.PARK) {
      g.setColor(Color.white);
      Font oldFont = g.getFont();
      Font newFont = new Font(Font.DIALOG, Font.BOLD, 7);
      g.setFont(newFont);
      g.drawString("P", rect.x + 3, rect.y + rect.height - 3);
      g.setFont(oldFont);
    }
  }

  @Override  // AbstractAttributedFigure
  protected void drawStroke(Graphics2D g) {
    Rectangle r = fDisplayBox;

    if (r.width > 0 && r.height > 0) {
      g.drawOval(r.x, r.y, r.width - 1, r.height - 1);
    }
  }

  @Override // AbstractAttributedDecoratedFigure
  public PointFigure clone() {
    PointFigure thatFigure = (PointFigure) super.clone();
    thatFigure.setZoomPoint(new ZoomPoint(fZoomPoint.getX(), fZoomPoint.getY()));

    return thatFigure;
  }

  @Override // AbstractFigure
  public int getLayer() {
    return getModel().getPropertyLayerWrapper().getValue().getLayer().getOrdinal();
  }

  @Override
  public boolean isVisible() {
    return super.isVisible()
        && getModel().getPropertyLayerWrapper().getValue().getLayer().isVisible()
        && getModel().getPropertyLayerWrapper().getValue().getLayerGroup().isVisible();
  }

  private List<Set<TCSResourceReference<?>>> getCurrentDriveOrderClaim(VehicleModel vehicle) {
    List<Set<TCSResourceReference<?>>> result = new ArrayList<>();

    boolean driveOrderEndFound = false;
    for (Set<TCSResourceReference<?>> res : vehicle.getClaimedResources().getItems()) {
      result.add(res);

      if (containsDriveOrderDestination(res, vehicle)) {
        driveOrderEndFound = true;
        break;
      }
    }

    if (driveOrderEndFound) {
      return result;
    }
    else {
      // With the end of the drive order not found, there is nothing from the current drive order in
      // the claimed resources.
      return List.of();
    }
  }

  private boolean containsDriveOrderDestination(
      Set<TCSResourceReference<?>> resources,
      VehicleModel vehicle
  ) {
    if (vehicle.getDriveOrderDestination() == null) {
      return false;
    }

    return resources.stream()
        .anyMatch(
            resource -> Objects.equals(
                resource.getName(),
                vehicle.getDriveOrderDestination().getName()
            )
        );
  }
}
