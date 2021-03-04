/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching;

import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.inject.Inject;
import org.opentcs.data.order.TransportOrder;

/**
 * A collection of {@link TransportOrderSelectionVeto}s.
 * 
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class CompositeTransportOrderSelectionVeto
    implements TransportOrderSelectionVeto {

  /**
   * The {@link TransportOrderSelectionVeto}s.
   */
  private final Set<TransportOrderSelectionVeto> vetos;
  
  @Inject
  public CompositeTransportOrderSelectionVeto(Set<TransportOrderSelectionVeto> vetos) {
    this.vetos = requireNonNull(vetos, "vetos");
  }

  @Override
  public boolean test(TransportOrder t) {
    boolean result = false;
    for (TransportOrderSelectionVeto veto : vetos) {
      result |= veto.test(t);
    }
    return result;
  }
}
