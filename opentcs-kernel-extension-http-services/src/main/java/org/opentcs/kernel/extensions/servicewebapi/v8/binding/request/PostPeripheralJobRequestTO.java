// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.request;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.shared.PeripheralOperationDescription;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.shared.Property;

// CHECKSTYLE:OFF
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Accessors(chain = true)
@JsonPropertyOrder(alphabetic = true)
public class PostPeripheralJobRequestTO {

  private boolean incompleteName;
  private String reservationToken;
  private String relatedVehicle;
  private String relatedTransportOrder;
  private PeripheralOperationDescription peripheralOperation;
  private List<Property> properties;
}
// CHECKSTYLE:ON
