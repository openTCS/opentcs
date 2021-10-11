/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.peripherals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import org.junit.*;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import org.opentcs.common.peripherals.NullPeripheralCommAdapterDescription;
import org.opentcs.components.kernel.services.InternalPeripheralService;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.peripherals.PeripheralJob;
import org.opentcs.drivers.peripherals.PeripheralAdapterCommand;
import org.opentcs.drivers.peripherals.PeripheralCommAdapter;
import org.opentcs.drivers.peripherals.PeripheralCommAdapterDescription;
import org.opentcs.drivers.peripherals.PeripheralCommAdapterFactory;
import org.opentcs.drivers.peripherals.PeripheralJobCallback;
import org.opentcs.drivers.peripherals.PeripheralProcessModel;
import org.opentcs.drivers.peripherals.management.PeripheralAttachmentInformation;
import org.opentcs.kernel.KernelApplicationConfiguration;
import org.opentcs.util.ExplainedBoolean;
import org.opentcs.util.event.EventHandler;

/**
 * Tests for the {@link PeripheralAttachmentManager}.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class PeripheralAttachmentManagerTest {

  private static final String LOCATION_NAME = "Location-01";

  private final PeripheralAttachmentManager attachmentManager;
  private final InternalPeripheralService peripheralService;
  private final PeripheralCommAdapterRegistry commAdapterRegistry;
  private final PeripheralCommAdapterFactory commAdapterFactory;
  private final PeripheralEntryPool peripheralEntryPool;

  private final Location location;

  public PeripheralAttachmentManagerTest() {
    peripheralService = mock(InternalPeripheralService.class);
    commAdapterRegistry = mock(PeripheralCommAdapterRegistry.class);
    commAdapterFactory = mock(PeripheralCommAdapterFactory.class);
    peripheralEntryPool = new PeripheralEntryPool(peripheralService);
    attachmentManager = spy(new PeripheralAttachmentManager(peripheralService,
                                                            mock(LocalPeripheralControllerPool.class),
                                                            commAdapterRegistry,
                                                            peripheralEntryPool,
                                                            mock(EventHandler.class),
                                                            mock(KernelApplicationConfiguration.class)));

    location = createLocation(LOCATION_NAME);
  }

  @Before
  public void setUp() {
    Set<Location> locations = new HashSet<>();
    locations.add(location);
    when(peripheralService.fetchObjects(Location.class)).thenReturn(locations);
    when(peripheralService.fetchObject(any(), eq(location.getReference()))).thenReturn(location);
  }

  @After
  public void tearDown() {
  }

  @Test
  public void shouldHaveInitializedPeripheralEntryPool() {
    attachmentManager.initialize();

    assertThat(peripheralEntryPool.getEntries().size(), is(1));
    assertThat(peripheralEntryPool.getEntryFor(location.getReference()).getCommAdapter(),
               is(instanceOf(NullPeripheralCommAdapter.class)));
  }

  @Test
  public void shouldAttachAdapterToLocation() {
    PeripheralCommAdapter commAdapter = new SimpleCommAdapter(location);
    PeripheralCommAdapterDescription description = new SimpleCommAdapterDescription();
    when(commAdapterRegistry.findFactoryFor(description)).thenReturn(commAdapterFactory);
    when(commAdapterFactory.getAdapterFor(location)).thenReturn(commAdapter);
    when(commAdapterFactory.getDescription()).thenReturn(description);

    attachmentManager.initialize();
    attachmentManager.attachAdapterToLocation(location.getReference(), description);

    assertNotNull(peripheralEntryPool.getEntryFor(location.getReference()));
    assertThat(peripheralEntryPool.getEntryFor(location.getReference()).getCommAdapter(),
               is(commAdapter));
    assertThat(peripheralEntryPool.getEntryFor(location.getReference()).getCommAdapterFactory(),
               is(commAdapterFactory));
  }

  @Test
  public void shouldAutoAttachAdapterToLocation() {
    PeripheralCommAdapterFactory factory = new SimpleCommAdapterFactory();
    when(commAdapterRegistry.findFactoriesFor(location)).thenReturn(Arrays.asList(factory));

    attachmentManager.initialize();

    assertNotNull(peripheralEntryPool.getEntryFor(location.getReference()));
    assertThat(peripheralEntryPool.getEntryFor(location.getReference()).getCommAdapter(),
               is(instanceOf(SimpleCommAdapter.class)));
    assertThat(peripheralEntryPool.getEntryFor(location.getReference()).getCommAdapterFactory(),
               is(factory));
  }

  @Test
  public void shouldGetAttachmentInformation() {
    attachmentManager.initialize();
    PeripheralAttachmentInformation result
        = attachmentManager.getAttachmentInformation(location.getReference());

    assertThat(result.getLocationReference(), is(location.getReference()));
    assertThat(result.getAttachedCommAdapter(),
               is(instanceOf(NullPeripheralCommAdapterDescription.class)));
  }

  private Location createLocation(String locationName) {
    LocationType locationType = new LocationType("LocationType-01");
    return new Location(locationName, locationType.getReference());
  }

  private class SimpleCommAdapter
      implements PeripheralCommAdapter {

    private final PeripheralProcessModel processModel;

    public SimpleCommAdapter(Location location) {
      this.processModel = new PeripheralProcessModel(location.getReference());
    }

    @Override
    public void initialize() {
    }

    @Override
    public boolean isInitialized() {
      return true;
    }

    @Override
    public void terminate() {
    }

    @Override
    public void enable() {
    }

    @Override
    public void disable() {
    }

    @Override
    public boolean isEnabled() {
      return false;
    }

    @Override
    public PeripheralProcessModel getProcessModel() {
      return processModel;
    }

    @Override
    public ExplainedBoolean canProcess(PeripheralJob job) {
      return new ExplainedBoolean(true, "");
    }

    @Override
    public void process(PeripheralJob job, PeripheralJobCallback callback) {
    }

    @Override
    public void execute(PeripheralAdapterCommand command) {
    }
  }

  private class SimpleCommAdapterFactory
      implements PeripheralCommAdapterFactory {

    @Override
    public void initialize() {
    }

    @Override
    public boolean isInitialized() {
      return true;
    }

    @Override
    public void terminate() {
    }

    @Override
    public PeripheralCommAdapterDescription getDescription() {
      return new SimpleCommAdapterDescription();
    }

    @Override
    public boolean providesAdapterFor(Location location) {
      return true;
    }

    @Override
    public PeripheralCommAdapter getAdapterFor(Location location) {
      return new SimpleCommAdapter(location);
    }

  }

  private class SimpleCommAdapterDescription
      extends PeripheralCommAdapterDescription {

    @Override
    public String getDescription() {
      return getClass().getName();
    }
  }
}
