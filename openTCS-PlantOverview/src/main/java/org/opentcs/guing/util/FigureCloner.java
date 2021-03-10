/*
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.util;

import java.awt.geom.AffineTransform;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.TreeSet;
import javax.inject.Inject;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.connector.Connector;
import org.opentcs.guing.components.drawing.OpenTCSDrawingEditor;
import org.opentcs.guing.components.drawing.OpenTCSDrawingView;
import org.opentcs.guing.components.drawing.figures.BitmapFigure;
import org.opentcs.guing.components.drawing.figures.FigureConstants;
import org.opentcs.guing.components.drawing.figures.LabeledFigure;
import org.opentcs.guing.components.drawing.figures.SimpleLineConnection;
import org.opentcs.guing.model.ConnectableModelComponent;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.elements.AbstractConnection;
import org.opentcs.guing.persistence.ModelManager;

/**
 * A helper class for cloning figures.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class FigureCloner {

  /**
   * The application's model manager.
   */
  private final ModelManager fModelManager;
  /**
   * The application's drawing editor.
   */
  private final OpenTCSDrawingEditor fDrawingEditor;

  /**
   * Creates a new instance.
   *
   * @param modelManager The application's model manager.
   * @param drawingEditor The application's drawing editor.
   */
  @Inject
  public FigureCloner(ModelManager modelManager, OpenTCSDrawingEditor drawingEditor) {
    this.fModelManager = requireNonNull(modelManager, "modelManager");
    this.fDrawingEditor = requireNonNull(drawingEditor, "drawingEditor");
  }

  public List<Figure> cloneFigures(List<Figure> figuresToClone) {
    requireNonNull(figuresToClone, "figuresToClone");

    // Buffer for Links and Paths associated with the cloned Points and Locations
    TreeSet<AbstractConnection> bufferedConnections
        = new TreeSet<>(Comparators.modelComponentsByName());
    // References the prototype Points and Locations to their clones
    Map<ModelComponent, ModelComponent> mClones = new HashMap<>();
    List<Figure> clonedFigures = new ArrayList<>();

    for (Figure figure : figuresToClone) {
      if (figure instanceof LabeledFigure) {
        // Location or Point
        ConnectableModelComponent model
            = (ConnectableModelComponent) figure.get(FigureConstants.MODEL);

        if (model != null) {
          bufferedConnections.addAll(model.getConnections());
        }

        LabeledFigure clonedFigure = (LabeledFigure) figure.clone();
        ModelComponent clonedModel = clonedFigure.get(FigureConstants.MODEL);

        if (model != null) {
          mClones.put(model, clonedModel);
        }
        // Paste cloned figure to the drawing
        AffineTransform tx = new AffineTransform();
        // TODO: Make the duplicate's distance configurable.
        // TODO: With multiple pastes, place the inserted figure relative to
        // the predecessor, not the original.
        tx.translate(50, 50);
        clonedFigure.transform(tx);
        getActiveDrawingView().getDrawing().add(clonedFigure);
        // The new tree component will be created by "figureAdded()"
        clonedFigures.add(clonedFigure);
      }
      else if (figure instanceof BitmapFigure) {
        BitmapFigure clonedFigure
            = new BitmapFigure(new File(((BitmapFigure) figure).getImagePath()));
        AffineTransform tx = new AffineTransform();
        // TODO: Make the duplicate's distance configurable.
        // TODO: With multiple pastes, place the inserted figure relative to
        // the predecessor, not the original.
        tx.translate(50, 50);
        clonedFigure.transform(tx);
        getActiveDrawingView().addBackgroundBitmap(clonedFigure);
      }
    }

    for (Figure figure : figuresToClone) {
      if (figure instanceof SimpleLineConnection) {
        // Link or Path
        SimpleLineConnection clonedFigure = (SimpleLineConnection) figure.clone();
        AbstractConnection model = (AbstractConnection) figure.get(FigureConstants.MODEL);
        AbstractConnection clonedModel
            = (AbstractConnection) clonedFigure.get(FigureConstants.MODEL);

        if (bufferedConnections.contains(model)) {
          if (model != null) {
            ModelComponent sourcePoint = model.getStartComponent();
            ModelComponent clonedSource = mClones.get(sourcePoint);
            Iterator<Connector> iConnectors
                = fModelManager.getModel().getFigure(clonedSource).getConnectors(null).iterator();
            clonedFigure.setStartConnector(iConnectors.next());

            ModelComponent destinationPoint = model.getEndComponent();
            ModelComponent clonedDestination = mClones.get(destinationPoint);
            iConnectors = fModelManager.getModel().getFigure(clonedDestination)
                .getConnectors(null).iterator();
            clonedFigure.setEndConnector(iConnectors.next());

            clonedModel.setConnectedComponents(clonedSource, clonedDestination);
            clonedModel.updateName();
          }
        }

        getActiveDrawingView().getDrawing().add(clonedFigure);
        // The new tree component will be created by "figureAdded()"
        clonedFigures.add(clonedFigure);
      }
    }

    return clonedFigures;
  }

  private OpenTCSDrawingView getActiveDrawingView() {
    return fDrawingEditor.getActiveView();
  }

}
