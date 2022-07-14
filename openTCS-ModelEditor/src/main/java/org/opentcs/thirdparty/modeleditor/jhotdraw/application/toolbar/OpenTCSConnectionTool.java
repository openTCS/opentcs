/**
 * (c): IML, JHotDraw.
 *
 */
package org.opentcs.thirdparty.modeleditor.jhotdraw.application.toolbar;

import com.google.inject.assistedinject.Assisted;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.swing.JOptionPane;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import org.jhotdraw.draw.ConnectionFigure;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.tool.ConnectionTool;
import org.opentcs.guing.base.components.layer.LayerWrapper;
import org.opentcs.guing.base.model.AbstractConnectableModelComponent;
import org.opentcs.guing.common.components.drawing.figures.FigureConstants;
import org.opentcs.guing.common.components.drawing.figures.LabeledLocationFigure;
import org.opentcs.guing.common.components.drawing.figures.LinkConnection;
import org.opentcs.guing.common.components.drawing.figures.ModelBasedFigure;
import org.opentcs.guing.common.components.drawing.figures.SimpleLineConnection;
import org.opentcs.modeleditor.components.layer.ActiveLayerProvider;
import static org.opentcs.modeleditor.util.I18nPlantOverviewModeling.TOOLBAR_PATH;
import org.opentcs.thirdparty.guing.common.jhotdraw.util.ResourceBundleUtil;

/**
 * A tool to connect two figures with a path for instance.
 *
 * @author Heinz Huber (Fraunhofer IML)
 */
public class OpenTCSConnectionTool
    extends ConnectionTool {

  /**
   * The resource bundle to use.
   */
  private static final ResourceBundleUtil BUNDLE = ResourceBundleUtil.getBundle(TOOLBAR_PATH);
  /**
   * Provides the currently active layer.
   */
  private final ActiveLayerProvider activeLayerProvider;
  /**
   * A localized name for this tool. The presentationName is displayed by the
   * UndoableEdit.
   */
  private final String presentationName;

  /**
   * Creates a new instance.
   *
   * @param activeLayerProvider Provides the currently active layer.
   * @param prototype The prototypical figure to be used.
   */
  @Inject
  public OpenTCSConnectionTool(ActiveLayerProvider activeLayerProvider,
                               @Assisted ConnectionFigure prototype) {
    super(prototype);
    this.activeLayerProvider = requireNonNull(activeLayerProvider, "activeLayerProvider");
    presentationName = BUNDLE.getString("openTcsConnectionTool.undo.presentationName");
  }

  @Override
  public void mousePressed(MouseEvent evt) {
    if (!activeLayerProvider.getActiveLayer().getLayer().isVisible()
        || !activeLayerProvider.getActiveLayer().getLayer().isVisible()) {
      JOptionPane.showMessageDialog(
          evt.getComponent(),
          BUNDLE.getString("openTcsConnectionTool.optionPane_activeLayerNotVisible.message"),
          BUNDLE.getString("openTcsConnectionTool.optionPane_activeLayerNotVisible.title"),
          JOptionPane.INFORMATION_MESSAGE
      );
      return;
    }

    super.mousePressed(evt);
  }

  @Override // ConnectionTool
  public void mouseReleased(MouseEvent event) {
    if (!isValidConnection()) {
      removeCreatedFigure();
      return;
    }

    createdFigure.willChange();
    createdFigure.setStartConnector(startConnector);
    createdFigure.setEndConnector(endConnector);
    if (createdFigure instanceof SimpleLineConnection) {
      ((SimpleLineConnection) createdFigure).getModel().updateName();
    }

    createdFigure.changed();

    final Figure addedFigure = createdFigure;
    final Drawing addedDrawing = getDrawing();

    getDrawing().fireUndoableEditHappened(new AbstractUndoableEdit() {

      @Override
      public String getPresentationName() {
        return presentationName;
      }

      @Override
      public void undo()
          throws CannotUndoException {
        super.undo();
        addedDrawing.remove(addedFigure);
      }

      @Override
      public void redo()
          throws CannotRedoException {
        super.redo();
        addedDrawing.add(addedFigure);
      }
    });

    targetFigure = null;
    Point2D.Double cAnchor = startConnector.getAnchor();
    Rectangle r = new Rectangle(getView().drawingToView(cAnchor));
    r.grow(getAnchorWidth(), getAnchorWidth());
    fireAreaInvalidated(r);
    cAnchor = endConnector.getAnchor();
    r = new Rectangle(getView().drawingToView(cAnchor));
    r.grow(getAnchorWidth(), getAnchorWidth());
    fireAreaInvalidated(r);
    startConnector = null;
    endConnector = null;
    Figure finishedFigure = createdFigure;
    createdFigure = null;
    creationFinished(finishedFigure);
  }

  @Override
  protected ConnectionFigure createFigure() {
    ConnectionFigure figure = super.createFigure();

    if (figure instanceof ModelBasedFigure) {
      if (figure instanceof LinkConnection) {
        // Rather than using the active layer for links as well, get the location's layer instead.
        LayerWrapper locationLayer
            = ((LabeledLocationFigure) startConnector.getOwner()).getPresentationFigure().getModel()
                .getPropertyLayerWrapper().getValue();
        ((ModelBasedFigure) figure).getModel().getPropertyLayerWrapper().setValue(locationLayer);
      }
      else {
        ((ModelBasedFigure) figure).getModel()
            .getPropertyLayerWrapper().setValue(activeLayerProvider.getActiveLayer());
      }
    }

    return figure;
  }

  private void removeCreatedFigure() {
    if (createdFigure != null) {
      getDrawing().remove(createdFigure);
      // TODO: Also remove figure from the model

      Point2D.Double cAnchor = startConnector.getAnchor();
      Rectangle r = new Rectangle(getView().drawingToView(cAnchor));
      r.grow(getAnchorWidth(), getAnchorWidth());
      fireAreaInvalidated(r);
      r = new Rectangle(getView().drawingToView(cAnchor));
      r.grow(getAnchorWidth(), getAnchorWidth());
      fireAreaInvalidated(r);
      startConnector = null;
      endConnector = null;
      createdFigure = null;
    }

    if (isToolDoneAfterCreation()) {
      fireToolDone();
    }
  }

  private boolean isValidConnection() {
    // Prevent the manual creation of multiple connections with the same start and end connector.
    return canConnect() && !connectionAlreadyPresent();
  }

  private boolean connectionAlreadyPresent() {
    AbstractConnectableModelComponent startComponent
        = (AbstractConnectableModelComponent) startConnector.getOwner().get(FigureConstants.MODEL);
    AbstractConnectableModelComponent endComponent
        = (AbstractConnectableModelComponent) endConnector.getOwner().get(FigureConstants.MODEL);

    return startComponent != null
        && endComponent != null
        && startComponent.hasConnectionTo(endComponent);
  }

  private boolean canConnect() {
    return createdFigure != null
        && startConnector != null
        && endConnector != null
        && createdFigure.canConnect(startConnector, endConnector);
  }
}
