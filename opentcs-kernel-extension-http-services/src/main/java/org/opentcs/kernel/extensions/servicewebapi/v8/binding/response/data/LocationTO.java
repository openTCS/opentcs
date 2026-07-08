// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.opentcs.data.model.Location;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.CoupleTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.LinkTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.LocationRepresentationTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.TripleTO;

/**
 * A transfer object representing a {@link Location} instance.
 */
// CHECKSTYLE:OFF
@NoArgsConstructor
@Getter
@Setter
@Accessors(chain = true)
@JsonPropertyOrder(alphabetic = true)
public class LocationTO {

  @Nonnull
  private String name;
  @Nonnull
  @JsonPropertyOrder(alphabetic = true)
  private Map<String, String> properties;
  @Nonnull
  private String type;
  @Nonnull
  private TripleTO position;
  @Nonnull
  private List<LinkTO> attachedLinks;
  private boolean locked;
  @Nonnull
  private PeripheralInformationTO peripheralInformation;
  @Nonnull
  private LayoutTO layout;

  @NoArgsConstructor
  @Getter
  @Setter
  @Accessors(chain = true)
  @JsonPropertyOrder(alphabetic = true)
  public static class PeripheralInformationTO {

    @Nullable
    private String reservationToken;
    @Nonnull
    private StateTO state;
    @Nonnull
    private ProcStateTO procState;
    @Nullable
    private String peripheralJob;

    public enum StateTO {
      NO_PERIPHERAL,
      UNKNOWN,
      UNAVAILABLE,
      ERROR,
      IDLE,
      EXECUTING
    }

    public enum ProcStateTO {
      IDLE,
      PROCESSING_JOB
    }
  }

  @NoArgsConstructor
  @Getter
  @Setter
  @Accessors(chain = true)
  @JsonPropertyOrder(alphabetic = true)
  public static class LayoutTO {

    @Nonnull
    private CoupleTO labelOffset;
    @Nonnull
    private LocationRepresentationTO locationRepresentation;
    private int layerId;
  }
}
// CHECKSTYLE:ON
