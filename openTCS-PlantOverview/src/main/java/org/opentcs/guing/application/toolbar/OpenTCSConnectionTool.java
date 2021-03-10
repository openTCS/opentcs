/**
 * (c): IML, JHotDraw.
 *
 */
package org.opentcs.guing.application.toolbar;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import org.jhotdraw.draw.ConnectionFigure;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.tool.ConnectionTool;
import org.opentcs.guing.components.drawing.figures.FigureConstants;
import org.opentcs.guing.components.drawing.figures.SimpleLineConnection;
import org.opentcs.guing.model.AbstractConnectableModelComponent;
import org.opentcs.guing.util.I18nPlantOverview;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * A tool to connect two figures with a path for instance.
 *
 * @author Heinz Huber (Fraunhofer IML)
 */
public class OpenTCSConnectionTool
    extends ConnectionTool {

  /**
   * A localized name for this tool. The presentationName is displayed by the
   * UndoableEdit.
   */
  private final String presentationName;

  /**
   * Creates a new instance.
   *
   * @param prototype The prototypical figure to be used.
   */
  public OpenTCSConnectionTool(ConnectionFigure prototype) {
    super(prototype);

    presentationName = ResourceBundleUtil.getBundle(I18nPlantOverview.TOOLBAR_PATH)
        .getString("openTcsConnectionTool.undo.presentationName");
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
