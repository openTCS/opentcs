// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.converter.sse;

import org.modelmapper.ModelMapper;
import org.opentcs.data.peripherals.PeripheralJob;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.sse.PeripheralJobEventTO;

/**
 * Provides methods to convert {@link PeripheralJob}s to their SSE representation.
 */
public class PeripheralJobConverter {

  private final ModelMapper modelMapper = new ModelMapper();

  /**
   * Creates a new instance.
   */
  public PeripheralJobConverter() {
    configureModelMapper();

    modelMapper.validate();
  }

  /**
   * Converts an {@link PeripheralJob} to its SSE representation.
   *
   * @param peripheralJob The peripheral job to convert.
   * @return The converted peripheral job.
   */
  public PeripheralJobEventTO.PeripheralJobTO convert(PeripheralJob peripheralJob) {
    return modelMapper.map(peripheralJob, PeripheralJobEventTO.PeripheralJobTO.class);
  }

  private void configureModelMapper() {
    modelMapper.addConverter(Converters.tcsObjectReferenceConverter());
    modelMapper.addConverter(Converters.tcsResourceReferenceConverter());
    modelMapper.typeMap(PeripheralJob.class, PeripheralJobEventTO.PeripheralJobTO.class);
  }
}
