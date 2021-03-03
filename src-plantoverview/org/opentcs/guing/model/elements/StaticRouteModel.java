/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.model.elements;

import com.google.common.collect.Lists;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jhotdraw.draw.Figure;
import org.opentcs.data.model.visualization.ElementPropKeys;
import org.opentcs.guing.components.properties.event.AttributesChangeListener;
import org.opentcs.guing.components.properties.type.ColorProperty;
import org.opentcs.guing.components.properties.type.KeyValueSetProperty;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.components.tree.elements.StaticRouteUserObject;
import org.opentcs.guing.event.StaticRouteChangeEvent;
import org.opentcs.guing.event.StaticRouteChangeListener;
import org.opentcs.guing.model.FiguresFolder;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * Eine statische Route.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class StaticRouteModel
    extends FiguresFolder {

  /**
   * Die registrierten Listener, die informiert werden, wenn sich an der
   * statischen Route etwas ändert.
   */
  private List<StaticRouteChangeListener> fListeners = new ArrayList<>();

  /**
   * Creates a new instance.
   */
  public StaticRouteModel() {
    createProperties();
  }

  @Override // FiguresFolder
  public StaticRouteUserObject createUserObject() {
    fUserObject = new StaticRouteUserObject(this);

    return (StaticRouteUserObject) fUserObject;
  }

  /**
   *
   * @return The first Point of this route.
   */
  public PointModel getStartPoint() {
    if (getChildComponents().isEmpty()) {
      return null;
    }
    return (PointModel) getChildComponents().get(0);
  }

  /**
   *
   * @return The last Point of this route.
   */
  public PointModel getEndPoint() {
    if (getChildComponents().isEmpty()) {
      return null;
    }
    return (PointModel) getChildComponents().get(getChildComponents().size() - 1);
  }

  /**
   * Returns an iterator of all figures representing the StaticRoute's Points
   * and connecting Paths.
   *
   * @return An iterator of all figures representing the StaticRoute's Points
   * and connecting Paths.
   */
  @Override // FiguresFolder
  public Iterator<Figure> figures() {
    List<Figure> figures = new ArrayList<>();
    
    Iterator<ModelComponent> ePoints = getChildComponents().iterator();
    if (ePoints.hasNext()) {
      PointModel startPoint = (PointModel) ePoints.next();
      figures.add(startPoint.getFigure());

      while (ePoints.hasNext()) {
        PointModel nextPoint = (PointModel) ePoints.next();
        AbstractConnection path = startPoint.getConnectionTo(nextPoint);
        // Wenn eine Strecke nur "rückwärts" befahrbar ist
        if (path == null) {
          path = nextPoint.getConnectionTo(startPoint);
        }
        // Sollte eigentlich nicht vorkommen
        if (path != null) {
          figures.add(path.getFigure());
        }

        figures.add(nextPoint.getFigure());
        startPoint = nextPoint;
      }
    }

    return figures.iterator();
  }

  /**
   * Entfernt einen Knoten aus der statischen Route.
   *
   * @param point der zu entfernende Knotenpunkt
   */
  public void removePoint(PointModel point) {
    if (contains(point)) {
      remove(point);
    }
  }

  /**
   * Fügt der statischen Route einen Knoten hinzu.
   *
   * @param point der hinzuzufügende Knotenpunkt
   */
  public void addPoint(PointModel point) {
    add(point);
  }

  /**
   * Entfernt alle Knoten aus der statischen Route.
   */
  public void removeAllPoints() {
    for (Object o : new ArrayList<>(Lists.reverse(getChildComponents()))) {
      remove((ModelComponent) o);
    }
  }

  /**
   * Liefert die Farbe der statischen Route.
   *
   * @return die Farbe der statischen Route
   */
  public Color getColor() {
    ColorProperty property = (ColorProperty) getProperty(ElementPropKeys.BLOCK_COLOR);

    return property.getColor();
  }

  /**
   * Benachrichtigt alle registrierten Listener, dass sich die Farbe des
   * Blockbereichs geändert hat.
   */
  public void colorChanged() {
    for (StaticRouteChangeListener listener : fListeners) {
      listener.colorChanged(new StaticRouteChangeEvent(this));
    }
  }

  @Override	// AbstractModelComponent
  public void propertiesChanged(AttributesChangeListener l) {
    if (getProperty(ElementPropKeys.BLOCK_COLOR).hasChanged()) {
      colorChanged();
    }

    super.propertiesChanged(l);
  }

  @Override	// AbstractModelComponent
  public String getTreeViewName() {
    String treeViewName = getName();

    return treeViewName;
  }

  @Override	// AbstractModelComponent
  public String getDescription() {
    return ResourceBundleUtil.getBundle().getString("staticRoute.description.text");
  }

  /**
   * Entfernt einen Listener, der ab sofort nicht mehr informiert wird, wenn es
   * Änderungen an den Fahrkurslementen gibt.
   *
   * @param listener der zu entfernende Listener
   */
  public void removeStaticRouteChangeListener(StaticRouteChangeListener listener) {
    fListeners.remove(listener);
  }

  /**
   * Registriert einen Listener, der fortan informiert wird, wenn sich die
   * Fahrkurselemente ändern.
   *
   * @param listener der zu registrierende Listener
   */
  public void addStaticRouteChangeListener(StaticRouteChangeListener listener) {
    if (fListeners == null) {
      fListeners = new ArrayList<>();
    }

    if (!fListeners.contains(listener)) {
      fListeners.add(listener);
    }
  }

  /**
   * Benachrichtigt alle registrierten Listener, dass sich bei den
   * Fahrkurselementen etwas geändert hat. Wird von einem Klienten aufgerufen,
   * der Änderungen an den Fahrkurselementen vorgenommen hat.
   */
  public void pointsChanged() {
    for (StaticRouteChangeListener listener : fListeners) {
      listener.pointsChanged(new StaticRouteChangeEvent(this));
    }
  }

  /**
   * The properties of a Static Route:
   * - The name shown in the "Components" tree
   * - The color used to decorate the hop-points in the DrawingView
   */
  private void createProperties() {
    ResourceBundleUtil bundle = ResourceBundleUtil.getBundle();
    // Name
    StringProperty pName = new StringProperty(this);
    pName.setDescription(bundle.getString("staticRoute.name.text"));
    pName.setHelptext(bundle.getString("staticRoute.name.helptext"));
    setProperty(NAME, pName);
    // Color
    ColorProperty pColor = new ColorProperty(this, Color.red);
    pColor.setDescription(bundle.getString("element.blockColor.text"));
    pColor.setHelptext(bundle.getString("element.blockColor.helptext"));
    setProperty(ElementPropKeys.BLOCK_COLOR, pColor);
    // Miscellaneous properties
    KeyValueSetProperty pMiscellaneous = new KeyValueSetProperty(this);
    pMiscellaneous.setDescription(bundle.getString("staticRoute.miscellaneous.text"));
    pMiscellaneous.setHelptext(bundle.getString("staticRoute.miscellaneous.helptext"));
    setProperty(MISCELLANEOUS, pMiscellaneous);
  }
}
