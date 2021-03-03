/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.exchange.adapter;

import java.util.Set;
import org.opentcs.access.Kernel;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.data.model.visualization.LayoutElement;
import org.opentcs.data.model.visualization.ModelLayoutElement;
import org.opentcs.data.model.visualization.VisualLayout;
import org.opentcs.guing.exchange.EventDispatcher;
import org.opentcs.guing.model.ModelComponent;

/**
 * Basic implementation of a EventManager. Receives messages from a
 * <code>ModelComponent</code> and its kernel equivalent and delegates them
 * to the respectively other one.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public abstract class AbstractProcessAdapter
    implements ProcessAdapter {

  /**
   * The <code>ModelComponent</code>.
   */
  private ModelComponent fModelComponent;
  /**
   * A reference to the kernel object.
   */
  private Object fProcessObject;
  /**
   * Maintains a map which relates the model component and the kernel object.
   */
  private EventDispatcher fEventDispatcher;
  /**
   * The corresponding <code>ModelLayoutElement</code>.
   */
  protected ModelLayoutElement fLayoutElement;

  /**
   * Creates a new instance of AbstractProcessAdapter.
   */
  public AbstractProcessAdapter() {
  }

  @Override // ProcessAdapter
  public void register() {
    getModel().addAttributesChangeListener(this);
    fEventDispatcher.addProcessAdapter(this);
  }

  @Override
  public EventDispatcher getEventDispatcher() {
    return fEventDispatcher;
  }

  @Override // ProcessAdapter
  public void setEventDispatcher(EventDispatcher eventDispatcher) {
    fEventDispatcher = eventDispatcher;
  }

  @Override // ProcessAdapter
  public void setModel(ModelComponent model) {
    fModelComponent = model;
  }

  @Override // ProcessAdapter
  public ModelComponent getModel() {
    return fModelComponent;
  }

  @Override // ProcessAdapter
  public Object createProcessObject() throws KernelRuntimeException {
    return null;
  }

  @Override // ProcessAdapter
  public void setProcessObject(Object processObject) {
    fProcessObject = processObject;
  }

  @Override // ProcessAdapter
  public Object getProcessObject() {
    return fProcessObject;
  }

  @Override // ProcessAdapter
  public void releaseProcessObject() {
    fEventDispatcher.removeProcessAdapter(this);
    getModel().removeAttributesChangeListener(this);
  }

  @Override // ProcessAdapter
  public void releaseLayoutElement() {
    if (fLayoutElement == null) {
      return;
    }
    Set<VisualLayout> layouts = kernel().getTCSObjects(VisualLayout.class);
    if (layouts.isEmpty()) {
      return;
    }

    for (VisualLayout layout : layouts) {
      Set<LayoutElement> layoutElements = layout.getLayoutElements();

      for (LayoutElement element : layoutElements) {
        ModelLayoutElement mle = (ModelLayoutElement) element;
        if (mle.getVisualizedObject().getId() == fLayoutElement.getVisualizedObject().getId()) {
          layoutElements.remove(element);
          break;
        }
      }

      kernel().setVisualLayoutElements(layout.getReference(), layoutElements);
    }
  }

  /**
   * Returns a reference to the kernel.
   *
   * @return A reference to the kernel.
   */
  protected Kernel kernel() {
    return fEventDispatcher.getKernel();
  }
}
