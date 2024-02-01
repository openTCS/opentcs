/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1;

import java.util.concurrent.Executors;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import org.opentcs.components.kernel.services.PlantModelService;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.kernel.extensions.servicewebapi.KernelExecutorWrapper;

/**
 * Tests for {@link PathHandler}.
 */
class PathHandlerTest {

  private TCSObjectService objectService;
  private PlantModelService plantModelService;
  private KernelExecutorWrapper executorWrapper;

  private PathHandler handler;

  private Path path;

  @BeforeEach
  void setUp() {
    objectService = mock();
    plantModelService = mock();
    executorWrapper = new KernelExecutorWrapper(Executors.newSingleThreadExecutor());

    handler = new PathHandler(objectService, executorWrapper, plantModelService);

    path = new Path("some-path",
                    new Point("some-point-1").getReference(),
                    new Point("some-point-2").getReference());
    given(objectService.fetchObject(Path.class, "some-path"))
        .willReturn(path);
  }

  @Test
  void lockPath() {
    handler.updatePathLock("some-path", "true");

    then(plantModelService).should().updatePathLock(path.getReference(), true);
  }

  @ParameterizedTest
  @ValueSource(strings = {"false", "flase", "some-value-that-is-not-true"})
  void unlockPathOnAnyNontrueValue(String value) {
    handler.updatePathLock("some-path", value);

    then(plantModelService).should().updatePathLock(path.getReference(), false);
  }

  @ParameterizedTest
  @ValueSource(strings = {"true", "false"})
  void throwOnLockUnknownPath(String value) {
    assertThatExceptionOfType(ObjectUnknownException.class)
        .isThrownBy(() -> handler.updatePathLock("some-unknown-path", value));
  }
}
