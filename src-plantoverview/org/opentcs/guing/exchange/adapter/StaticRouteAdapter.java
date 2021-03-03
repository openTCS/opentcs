/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.exchange.adapter;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.opentcs.access.CredentialsException;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.data.ObjectExistsException;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.StaticRoute;
import org.opentcs.data.model.visualization.ElementPropKeys;
import org.opentcs.data.model.visualization.LayoutElement;
import org.opentcs.data.model.visualization.ModelLayoutElement;
import org.opentcs.data.model.visualization.VisualLayout;
import org.opentcs.guing.components.properties.event.AttributesChangeEvent;
import org.opentcs.guing.components.properties.type.ColorProperty;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.event.StaticRouteChangeEvent;
import org.opentcs.guing.event.StaticRouteChangeListener;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.elements.PointModel;
import org.opentcs.guing.model.elements.StaticRouteModel;

/**
 * An adapter for static routes.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class StaticRouteAdapter
    extends OpenTCSProcessAdapter
    implements StaticRouteChangeListener {

  /**
   * This class's logger.
   */
  private static final Logger log
      = Logger.getLogger(StaticRouteAdapter.class.getName());

  /**
   * Creates a new instance.
   */
  public StaticRouteAdapter() {
    super();
  }

  @Override
  @SuppressWarnings("unchecked")
  public TCSObjectReference<StaticRoute> getProcessObject() {
    return (TCSObjectReference<StaticRoute>) super.getProcessObject();
  }

  @Override
  public StaticRouteModel getModel() {
    return (StaticRouteModel) super.getModel();
  }

  @Override
  public void setModel(ModelComponent model) {
    if (!StaticRouteModel.class.isInstance(model)) {
      throw new IllegalArgumentException(model + " is not a StaticRouteModel");
    }
    super.setModel(model);
  }

  @Override // AbstractProcessAdapter
  public void register() {
    super.register();
    getModel().addStaticRouteChangeListener(this);
  }

  @Override // AbstractProcessAdapter
  public void releaseProcessObject() {
    try {
      kernel().removeTCSObject(getProcessObject());
      super.releaseProcessObject(); // also delete the Adapter
    }
    catch (KernelRuntimeException e) {
      log.log(Level.WARNING, null, e);
    }
  }

  @Override // AbstractProcessAdapter
  public StaticRoute createProcessObject() throws KernelRuntimeException {
    if (!hasModelingState()) {
      return null;
    }
    StaticRoute staticRoute = kernel().createStaticRoute();
    setProcessObject(staticRoute.getReference());
    nameToModel(staticRoute);
    register();

    return staticRoute;
  }

  @Override // OpenTCSProcessAdapter
  public void propertiesChanged(AttributesChangeEvent event) {
    if (hasModelingState() && event.getInitiator() != this) {
      updateProcessProperties(false);
    }
  }

  @Override // OpenTCSProcessAdapter
  public void updateModelProperties() {
    TCSObjectReference<StaticRoute> reference = getProcessObject();

    synchronized (reference) {
      try {
        StaticRoute r = kernel().getTCSObject(StaticRoute.class, reference);

        StringProperty name = (StringProperty) getModel().getProperty(ModelComponent.NAME);
        name.setText(r.getName());

        getModel().removeAllPoints();

        Iterator<TCSObjectReference<Point>> i = r.getHops().iterator();

        while (i.hasNext()) {
          ProcessAdapter adapter = getEventDispatcher().findProcessAdapter(i.next());
          getModel().addPoint((PointModel) adapter.getModel());
        }

        updateMiscModelProperties(r);
      }
      catch (CredentialsException e) {
        log.log(Level.WARNING, null, e);
      }
    }
  }

  @Override // OpenTCSProcessAdapter
  public void updateProcessProperties(boolean updateAllProperties) {
    super.updateProcessProperties(updateAllProperties);
    TCSObjectReference<StaticRoute> reference = getProcessObject();

    if (isInTransition()) {
      return;
    }

    synchronized (reference) {
      StringProperty pName = (StringProperty) getModel().getProperty(ModelComponent.NAME);
      String name = pName.getText();

      try {
        if (updateAllProperties || pName.hasChanged()) {
          kernel().renameTCSObject(reference, name);
        }

        kernel().clearStaticRouteHops(reference);

        for (ModelComponent model : getModel().getChildComponents()) {
          PointAdapter adapter = (PointAdapter) getEventDispatcher().findProcessAdapter(model);
          kernel().addStaticRouteHop(reference, adapter.getProcessObject());
        }
        Set<VisualLayout> layouts = kernel().getTCSObjects(VisualLayout.class);

        for (VisualLayout layout : layouts) {
          updateLayoutElement(layout);
        }

        updateMiscProcessProperties(updateAllProperties);
      }
      catch (ObjectExistsException e) {
        undo(name, e);
      }
      catch (ObjectUnknownException | CredentialsException e) {
        log.log(Level.WARNING, null, e);
      }
    }
  }

  @Override // StaticRouteChangeListener
  public void staticRouteRemoved(StaticRouteChangeEvent e) {
  }

  @Override // StaticRouteChangeListener
  public void pointsChanged(StaticRouteChangeEvent e) {
    updateProcessProperties(false);
  }

  @Override // StaticRouteChangeListener
  public void colorChanged(StaticRouteChangeEvent e) {
  }

  private void updateLayoutElement(VisualLayout layout) {
    // Beim ersten Aufruf ein neues Model-Layout-Element erzeugen
    if (fLayoutElement == null) {
      fLayoutElement = new ModelLayoutElement(getProcessObject());
    }

    Map<String, String> layoutProperties = fLayoutElement.getProperties();

    ColorProperty pColor = (ColorProperty) getModel().getProperty(ElementPropKeys.BLOCK_COLOR);
    int rgb = pColor.getColor().getRGB() & 0x00FFFFFF;	// mask alpha bits
    layoutProperties.put(ElementPropKeys.BLOCK_COLOR, String.format("#%06X", rgb));
    fLayoutElement.setProperties(layoutProperties);

    Set<LayoutElement> layoutElements = layout.getLayoutElements();

    for (LayoutElement element : layoutElements) {
      ModelLayoutElement mle = (ModelLayoutElement) element;

      if (mle.getVisualizedObject().getId() == fLayoutElement.getVisualizedObject().getId()) {
        layoutElements.remove(element);
        break;
      }
    }

    layoutElements.add(fLayoutElement);
    kernel().setVisualLayoutElements(layout.getReference(), layoutElements);
  }
}
