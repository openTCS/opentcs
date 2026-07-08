// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.converter;

import org.modelmapper.ModelMapper;
import org.opentcs.data.notification.UserNotification;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.UserNotificationTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.modelmapper.Converters;

/**
 * Provides methods to convert {@link UserNotification}s to their web API representation.
 */
public class UserNotificationConverter {
  private final ModelMapper modelMapper = new ModelMapper();

  /**
   * Creates a new instance.
   */
  public UserNotificationConverter() {
    configureModelMapper();

    modelMapper.validate();
  }

  /**
   * Converts an {@link UserNotification} to its web API representation.
   *
   * @param userNotification The user notification to convert.
   * @return The converted user notification.
   */
  public UserNotificationTO convert(UserNotification userNotification) {
    return modelMapper.map(userNotification, UserNotificationTO.class);
  }

  private void configureModelMapper() {
    modelMapper.typeMap(UserNotification.class, UserNotificationTO.class);
    modelMapper.addConverter(Converters.instantConverter());
  }
}
