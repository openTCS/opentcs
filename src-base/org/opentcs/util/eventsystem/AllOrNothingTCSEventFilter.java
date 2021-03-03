/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.eventsystem;

import java.io.Serializable;

/**
 * An event filter for <code>TCSEvent</code>s that either accepts or refuses all
 * events.
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @deprecated Use AcceptingTCSEventFilter or RefusingTCSEventFilter instead.
 */
public final class AllOrNothingTCSEventFilter
    implements EventFilter<TCSEvent>, Serializable {

  /**
   * The instance accepting all events.
   */
  public static final AllOrNothingTCSEventFilter acceptingInstance =
      new AllOrNothingTCSEventFilter(true);
  /**
   * The instance refusing all events.
   */
  public static final AllOrNothingTCSEventFilter refusingInstance =
      new AllOrNothingTCSEventFilter(false);
  /**
   * A flag indicating whether this filter accepts any events.
   */
  private final boolean accepting;

  /**
   * Creates a new instance.
   * 
   * @param accepting A flag indicating whether this filter accepts any events.
   */
  private AllOrNothingTCSEventFilter(boolean accepting) {
    this.accepting = accepting;
  }

  @Override
  public boolean accept(TCSEvent event) {
    return accepting;
  }
}
