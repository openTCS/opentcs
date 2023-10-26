/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.base.components.properties.type;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.opentcs.guing.base.model.EnvelopeModel;
import org.opentcs.guing.base.model.ModelComponent;

/**
 * A property that contains a list of envelopes.
 */
public class EnvelopesProperty
    extends AbstractComplexProperty {

  public EnvelopesProperty(ModelComponent model,
                           List<EnvelopeModel> envelopes) {
    super(model);
    fValue = envelopes;
  }

  @Override
  public Object getComparableValue() {
    return this.toString();
  }

  @Override
  public void copyFrom(Property property) {
    EnvelopesProperty other = (EnvelopesProperty) property;
    setValue(new ArrayList<>(other.getValue()));
  }

  @Override
  public Object clone() {
    EnvelopesProperty clone = (EnvelopesProperty) super.clone();
    clone.setValue(new ArrayList<>(getValue()));
    return clone;
  }

  @Override
  public String toString() {
    return getValue().stream()
        .map(envelope -> envelope.getKey() + ": " + envelope.getVertices())
        .collect(Collectors.joining(", "));
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<EnvelopeModel> getValue() {
    return (List) super.getValue();
  }
}
