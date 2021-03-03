/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */

package org.opentcs.guing.components.dockable;

import bibliothek.gui.DockController;
import bibliothek.gui.Dockable;
import bibliothek.gui.dock.action.DockActionSource;
import bibliothek.gui.dock.common.CLocation;
import bibliothek.gui.dock.common.intern.AbstractDockableCStation;
import bibliothek.gui.dock.common.intern.CControlAccess;
import bibliothek.gui.dock.common.mode.CNormalModeArea;
import bibliothek.gui.dock.common.mode.ExtendedMode;
import bibliothek.gui.dock.common.perspective.CStationPerspective;
import bibliothek.gui.dock.facile.mode.Location;
import bibliothek.gui.dock.facile.mode.LocationMode;
import bibliothek.gui.dock.facile.mode.ModeAreaListener;
import bibliothek.gui.dock.layout.DockableProperty;
import bibliothek.gui.dock.support.mode.AffectedSet;
import bibliothek.gui.dock.util.DockUtilities;
import bibliothek.util.Path;

/**
 * A tab dockable copied from the dockingframes examples.
 * 
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class CStack
    extends AbstractDockableCStation<CStackDockStation>
    implements CNormalModeArea {

  public CStack(String id) {
    CStackDockStation delegate = new CStackDockStation(this);

    @SuppressWarnings("deprecation")
    CLocation stationLocation = new CLocation() {

      @Override
      public CLocation getParent() {
        return null;
      }

      @Override
      public String findRoot() {
        return getUniqueId();
      }

      @Override
      public DockableProperty findProperty(DockableProperty successor) {
        return successor;
      }

      @Override
      public ExtendedMode findMode() {
        return ExtendedMode.NORMALIZED;
      }

      @Override
      public CLocation aside() {
        return this;
      }
    };

    init(delegate, id, stationLocation, delegate);
  }

  @Override
  protected void install(CControlAccess access) {
    access.getLocationManager().getNormalMode().add(this);
  }

  @Override
  protected void uninstall(CControlAccess access) {
    access.getLocationManager().getNormalMode().remove(getUniqueId());
  }

  @Override
  public CStationPerspective createPerspective() {
    throw new IllegalStateException("not implemented");
  }

  @Override
  public boolean isNormalModeChild(Dockable dockable) {
    return isChild(dockable);
  }

  @Override
  public DockableProperty getLocation(Dockable child) {
    return DockUtilities.getPropertyChain(getStation(), child);
  }

  @Override
  public boolean setLocation(Dockable dockable, DockableProperty location, AffectedSet set) {
    set.add(dockable);

    if (isChild(dockable)) {
      getStation().move(dockable, location);
    }
    else {
      boolean acceptable = DockUtilities.acceptable(getStation(), dockable);
      if (!acceptable) {
        return false;
      }

      if (!getStation().drop(dockable, location)) {
        getStation().drop(dockable);
      }
    }

    return true;
  }

  @Override
  public void addModeAreaListener(ModeAreaListener listener) {

  }

  @Override
  public boolean autoDefaultArea() {
    return true;
  }

  @Override
  public boolean isLocationRoot() {
    return true;
  }

  @Override
  public boolean isChild(Dockable dockable) {
    return dockable.getDockParent() == getStation();
  }

  @Override
  public void removeModeAreaListener(ModeAreaListener listener) {

  }

  @Override
  public void setController(DockController controller) {

  }

  @Override
  public void setMode(LocationMode mode) {

  }

  @Override
  public CLocation getCLocation(Dockable dockable) {
    DockableProperty property = DockUtilities.getPropertyChain(getStation(), dockable);
    return getStationLocation().expandProperty(getStation().getController(), property);
  }

  @Override
  public CLocation getCLocation(Dockable dockable, Location location) {
    DockableProperty property = location.getLocation();
    if (property == null) {
      return getStationLocation();
    }

    return getStationLocation().expandProperty(getStation().getController(), property);
  }

  @Override
  public boolean respectWorkingAreas() {
    return true;
  }

  @Override
  public boolean isCloseable() {
    return false;
  }

  @Override
  public boolean isExternalizable() {
    return false;
  }

  @Override
  public boolean isMinimizable() {
    return false;
  }

  @Override
  public boolean isStackable() {
    return false;
  }

  @Override
  public boolean isWorkingArea() {
    return true;
  }

  public DockActionSource[] getSources() {
    return new DockActionSource[] {getClose()};
  }

  @Override
  public boolean isMaximizable() {
    return false;
  }

  @Override
  public Path getTypeId() {
    return null;
  }
}
