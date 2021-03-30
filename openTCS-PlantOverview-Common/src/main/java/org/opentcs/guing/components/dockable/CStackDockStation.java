/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */

package org.opentcs.guing.components.dockable;

import bibliothek.gui.dock.StackDockStation;
import bibliothek.gui.dock.action.DockActionSource;
import bibliothek.gui.dock.common.CStation;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.common.intern.CommonDockable;
import bibliothek.gui.dock.common.intern.station.CommonDockStation;
import bibliothek.gui.dock.common.intern.station.CommonDockStationFactory;

/**
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class CStackDockStation
    extends StackDockStation
    implements
    CommonDockStation<StackDockStation, CStackDockStation>, CommonDockable {

  private final CStack delegate;

  public CStackDockStation(CStack stack) {
    this.delegate = stack;
  }

  @Override
  public String getFactoryID() {
    return CommonDockStationFactory.FACTORY_ID;
  }

  @Override
  public String getConverterID() {
    return super.getFactoryID();
  }

  @Override
  public CDockable getDockable() {
    return delegate;
  }

  @Override
  public DockActionSource[] getSources() {
    return delegate.getSources();
  }

  @Override
  public CStation<CStackDockStation> getStation() {
    return delegate;
  }

  @Override
  public StackDockStation getDockStation() {
    return this;
  }

  @Override
  public CStackDockStation asDockStation() {
    return this;
  }

  @Override
  public CommonDockable asDockable() {
    return this;
  }
}
