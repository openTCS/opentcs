/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.statistics;

import java.util.Objects;
import org.opentcs.util.QueueProcessor;
import org.opentcs.util.eventsystem.EventListener;
import org.opentcs.util.eventsystem.TCSEvent;

/**
 * Handles events relevant for gathering data for statistical purposes.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class StatisticsEventListener
    implements EventListener<TCSEvent> {

  /**
   * The instance actually processing the events.
   */
  private final QueueProcessor<TCSEvent> eventProcessor;

  /**
   * Creates a new instance.
   * 
   * @param eventProcessor The instance processing the events we receive.
   */
  public StatisticsEventListener(QueueProcessor<TCSEvent> eventProcessor) {
    this.eventProcessor = Objects.requireNonNull(eventProcessor,
                                                 "eventProcessor is null");
  }

  @Override
  public void processEvent(TCSEvent event) {
    Objects.requireNonNull(event, "event is null");
    eventProcessor.addToQueue(event);
  }
}
