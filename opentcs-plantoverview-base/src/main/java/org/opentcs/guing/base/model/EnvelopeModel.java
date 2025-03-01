// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.base.model;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import java.util.List;
import org.opentcs.data.model.Couple;
import org.opentcs.data.model.Envelope;

/**
 * A representation of an {@link Envelope} with an additional (reference) key.
 */
public class EnvelopeModel {

  private final String key;
  private final List<Couple> vertices;

  /**
   * Creates a new instance.
   *
   * @param key The key to be used for referencing the envelope.
   * @param vertices The sequence of vertices the envelope consists of.
   */
  public EnvelopeModel(
      @Nonnull
      String key,
      @Nonnull
      List<Couple> vertices
  ) {
    this.key = requireNonNull(key, "key");
    this.vertices = requireNonNull(vertices, "vertices");
  }

  @Nonnull
  public String getKey() {
    return key;
  }

  @Nonnull
  public List<Couple> getVertices() {
    return vertices;
  }
}
