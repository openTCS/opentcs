// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.converter;

import org.modelmapper.ModelMapper;
import org.opentcs.data.peripherals.PeripheralJob;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.PeripheralJobTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.modelmapper.Converters;

/**
 * Provides methods to convert {@link PeripheralJob}s to their web API representation.
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
   * Converts an {@link PeripheralJob} to its web API representation.
   *
   * @param peripheralJob The peripheral job to convert.
   * @return The converted peripheral job.
   */
  public PeripheralJobTO convert(PeripheralJob peripheralJob) {
    return modelMapper.map(peripheralJob, PeripheralJobTO.class);
  }

  private void configureModelMapper() {
    modelMapper.addConverter(Converters.tcsObjectReferenceConverter());
    modelMapper.addConverter(Converters.tcsResourceReferenceConverter());
    modelMapper.addConverter(Converters.instantConverter());
    modelMapper.typeMap(PeripheralJob.class, PeripheralJobTO.class);
  }
}
