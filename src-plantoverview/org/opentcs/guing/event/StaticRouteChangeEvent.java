/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.event;

import java.util.EventObject;
import org.opentcs.guing.model.elements.StaticRouteModel;

/**
 * An event that informs listener about changes of a static route.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class StaticRouteChangeEvent
    extends EventObject {

  /**
   * Creates a new instance of StaticRouteChangeEvent.
   *
   * @param staticRoute The static route model that has changed.
   */
  public StaticRouteChangeEvent(StaticRouteModel staticRoute) {
    super(staticRoute);
  }
}
