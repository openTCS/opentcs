/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.exchange;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.undo.CannotUndoException;
import org.opentcs.access.Kernel;
import org.opentcs.access.TCSKernelStateEvent;
import org.opentcs.access.TCSMessageEvent;
import org.opentcs.access.TCSModelTransitionEvent;
import org.opentcs.access.rmi.RemoteKernelConnection;
import org.opentcs.access.rmi.TCSProxyStateEvent;
import org.opentcs.data.TCSObjectEvent;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.exchange.adapter.OpenTCSProcessAdapter;
import org.opentcs.guing.exchange.adapter.ProcessAdapter;
import org.opentcs.guing.exchange.adapter.ProcessAdapterFactory;
import org.opentcs.util.eventsystem.EventFilter;
import org.opentcs.util.eventsystem.EventListener;
import org.opentcs.util.eventsystem.TCSEvent;

/**
 * The openTCS implementation of the abstract event dispatcher.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class OpenTCSEventDispatcher
    extends AbstractEventDispatcher
    implements EventListener<TCSEvent> {

  /**
   * This class's logger.
   */
  private static final Logger logger
      = Logger.getLogger(OpenTCSEventDispatcher.class.getName());
  /**
   * The OpenTCSView.
   */
  private OpenTCSView fOpenTCSView;
  /**
   * The transport order dispatcher.
   */
  private final TransportOrderDispatcher fTransportOrderDispatcher;
  /**
   * The transport order sequence dispatcher.
   */
  private final OrderSequenceDispatcher fOrderSequenceDispatcher;
  /**
   * Flag to show if an undo is requested because something went wrong.
   * If set following requests will be ignored.
   */
  private boolean fUndoRequested;
  /**
   * Flag if we are currently in a model transition.
   * Prevents flooding of the log with unneccessary messages.
   */
  private boolean isInTransition;

  /**
   * Creates a new instance.
   *
   * @param procAdapterFactory The process adapter factory to be used.
   */
  public OpenTCSEventDispatcher(ProcessAdapterFactory procAdapterFactory) {
    super(procAdapterFactory);
    fTransportOrderDispatcher = new TransportOrderDispatcher(this);
    fOrderSequenceDispatcher = new OrderSequenceDispatcher(this);
  }

  /**
   * Returns the transport order dispatcher.
   *
   * @return The {@link TransportOrderDispatcher}.
   */
  public TransportOrderDispatcher getTransportOrderDispatcher() {
    return fTransportOrderDispatcher;
  }

  /**
   * Returns the order sequence dispatcher.
   * 
   * @return The {@link OrderSequenceDispatcher}.
   */
  public OrderSequenceDispatcher getOrderSequenceDispatcher() {
    return fOrderSequenceDispatcher;
  }

  /**
   * Sets the view.
   *
   * @param view The openTCS view.
   */
  public void setView(OpenTCSView view) {
    fOpenTCSView = view;
  }

  @Override
  public void register() {
    Kernel kernel = getKernel();
    // Listener for TCSObjectEvents on TransportOrders
    EventFilter<TCSEvent> filter = new EventFilter<TCSEvent>() {
      @Override
      public boolean accept(TCSEvent event) {
        if (event instanceof TCSObjectEvent) {
          TCSObjectEvent objectEvent = (TCSObjectEvent) event;

          if (objectEvent.getCurrentOrPreviousObjectState() instanceof TransportOrder) {
            return true;
          }
        }

        return false;
      }
    };

    kernel.addEventListener(fTransportOrderDispatcher, filter);

    // Listener for TCSObjectEvents on OrderSequences
    filter = new EventFilter<TCSEvent>() {
      @Override
      public boolean accept(TCSEvent event) {
        if (event instanceof TCSObjectEvent) {
          TCSObjectEvent objectEvent = (TCSObjectEvent) event;

          if (objectEvent.getCurrentOrPreviousObjectState() instanceof OrderSequence) {
            return true;
          }
        }

        return false;
      }
    };

    kernel.addEventListener(fOrderSequenceDispatcher, filter);

    // Listener for TCSObjectEvents on Vehicle, Location, Point...
    filter = new EventFilter<TCSEvent>() {
      @Override
      public boolean accept(TCSEvent event) {
        if (event instanceof TCSKernelStateEvent) {
          return true;
        }

        if (event instanceof TCSProxyStateEvent) {
          return true;
        }

        if (event instanceof TCSObjectEvent) {
          TCSObjectEvent objectEvent = (TCSObjectEvent) event;

          if (objectEvent.getCurrentOrPreviousObjectState() instanceof Vehicle
              || objectEvent.getCurrentOrPreviousObjectState() instanceof Location
              || objectEvent.getCurrentOrPreviousObjectState() instanceof Point
              || objectEvent.getCurrentOrPreviousObjectState() instanceof Path) {
            return true;
          }
        }

        if (event instanceof TCSModelTransitionEvent) {
          return true;
        }

        if (event instanceof TCSMessageEvent) {
          return true;
        }

        return false;
      }
    };

    kernel.addEventListener(this, filter);
  }

  @Override
  public void release() {
    Kernel kernel = getKernel();
    kernel.removeEventListener(fTransportOrderDispatcher);
    kernel.removeEventListener(fOrderSequenceDispatcher);
    kernel.removeEventListener(this);
  }

  @Override
  public void processEvent(TCSEvent event) {
    if (event instanceof TCSObjectEvent) {
      TCSObjectEvent objectEvent = (TCSObjectEvent) event;
      OpenTCSProcessAdapter adapter
          = (OpenTCSProcessAdapter) findProcessAdapter(
              objectEvent.getCurrentOrPreviousObjectState().getReference());

      // If we are in a model transition there don't exist adapters for
      // the newly created objects, yet, but the kernel fires an event
      // for every object created, what causes the OpenTCSProcessAdapter
      // to find wrong adapters, because it only compares the ids.
      if (!isInTransition) {
        if (adapter == null) {
          if (objectEvent.getCurrentOrPreviousObjectState() instanceof Path) {
            // PointConnectorPanel is the only one creating paths in the kernel
            Path path = (Path) objectEvent.getCurrentOrPreviousObjectState();
            fOpenTCSView.insertPath(path.getSourcePoint(), path.getDestinationPoint());
            return;
          }
          else {
            logger.log(Level.INFO, "TCSObject without adapter: {0} Id: {1} Name: {2}",
                       new Object[] {
                         objectEvent.getType().toString(),
                         objectEvent.getCurrentOrPreviousObjectState().getId(),
                         objectEvent.getCurrentOrPreviousObjectState().getName()});
            return;
          }
        }

        adapter.processTCSObjectEvent(event);
      }
    }
    else if (event instanceof TCSKernelStateEvent) {
      TCSKernelStateEvent kse = (TCSKernelStateEvent) event;
      Kernel.State leftState = kse.getLeftState();
      Kernel.State enteredState = kse.getEnteredState();

      // React instantly on SHUTDOWN of the kernel, otherwise wait for
      // the transition to finish
      if (kse.isTransitionFinished() || enteredState == Kernel.State.SHUTDOWN) {
        fOpenTCSView.switchKernelState(leftState, enteredState);
      }
    }
    else if (event instanceof TCSProxyStateEvent) {
      TCSProxyStateEvent pse = (TCSProxyStateEvent) event;
      RemoteKernelConnection.State enteredState = pse.getEnteredState();

      if (enteredState == RemoteKernelConnection.State.DISCONNECTED) {
        fOpenTCSView.switchKernelState(enteredState);
      }
    }
    else if (event instanceof TCSModelTransitionEvent) {
      TCSModelTransitionEvent mte = (TCSModelTransitionEvent) event;

      if (mte.hasModelContentChanged()) {
        if (mte.isTransitionFinished()) {
          isInTransition = false;
          fOpenTCSView.loadCurrentKernelModel();
        }
        else {
          isInTransition = true;
        }
      }
    }
    else if (event instanceof TCSMessageEvent) {
      TCSMessageEvent tme = (TCSMessageEvent) event;
      fOpenTCSView.log(tme.getMessage());
    }
  }

  /**
   * Undoes the last change on the application side. This is neccessary
   * when the kernel declined a change because it was against its rules.
   *
   * @param message The message the kernel sent.
   */
  public void undo(final String message) {
    if (!fUndoRequested) {
      Timer timer = new Timer(200, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          realUndo(message);
          ((Timer) e.getSource()).stop();
        }
      });

      timer.start();
      fUndoRequested = true;
    }
  }

  /**
   * Update the model properties by copying the properties of the kernel
   * objects.
   */
  public void updateModelProperties() {
    for (ProcessAdapter adapter : fAdaptersByModel.values()) {
      ((OpenTCSProcessAdapter) adapter).updateModelProperties();
    }
  }

  /**
   * Actually undoes the last change.
   *
   * @param message The message the kernel sent.
   */
  private void realUndo(final String message) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        try {
          fOpenTCSView.getUndoRedoManager().undo();
        }
        catch (CannotUndoException | NullPointerException e) {
          logger.log(Level.WARNING, "Unexpected exception", e);
        }

        fUndoRequested = false;
      }
    });
  }
}
