/*
 * openTCS copyright information:
 * Copyright (c) 2015 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.eventsystem;

import static java.util.Objects.requireNonNull;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A simple event listener that merely logs received events.
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @param <E> The type of events handled by this listener.
 */
public class EventLogger<E extends Event>
    implements EventListener<E> {

  /**
   * This instance's logger.
   */
  private final Logger log = Logger.getLogger(getClass().getName());
  /**
   * This listener's ID.
   */
  private final String id;
  /**
   * The level with which events are logged.
   */
  private final Level level;

  /**
   * Creates a new instance.
   *
   * @param id This listener's ID.
   * @param level The level with which events are logged.
   */
  public EventLogger(String id, Level level) {
    this.id = requireNonNull(id, "id");
    this.level = requireNonNull(level, "level");
  }

  @Override
  public void processEvent(E event) {
    requireNonNull(event, "event");

    log.log(level,
            () -> String.format("'%s' received event of type '%s': %s",
                                id,
                                event.getClass().getName(),
                                event.toString()));
  }
}
