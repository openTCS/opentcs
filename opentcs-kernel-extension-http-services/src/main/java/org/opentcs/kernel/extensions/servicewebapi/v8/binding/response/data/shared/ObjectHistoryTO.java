// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.time.Instant;
import java.util.List;
import javax.annotation.Nonnull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.opentcs.data.ObjectHistory;

/**
 * A transfer object representing an {@link ObjectHistory} instance.
 */
// CHECKSTYLE:OFF
@NoArgsConstructor
@Getter
@Setter
@Accessors(chain = true)
@JsonPropertyOrder(alphabetic = true)
public class ObjectHistoryTO {

  @Nonnull
  private List<ObjectHistoryEntryTO> entries;

  @NoArgsConstructor
  @Getter
  @Setter
  @Accessors(chain = true)
  public static class ObjectHistoryEntryTO {

    @Nonnull
    private Instant timestamp;
    @Nonnull
    private String eventCode;
    @Nonnull
    private List<String> supplements;
  }
}
// CHECKSTYLE:ON
