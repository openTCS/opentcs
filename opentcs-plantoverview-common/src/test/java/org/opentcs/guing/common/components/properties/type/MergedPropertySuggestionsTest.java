// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.common.components.properties.type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.components.plantoverview.PropertySuggestions;

/**
 */
class MergedPropertySuggestionsTest {

  private PropertySuggestions instance1;
  private PropertySuggestions instance2;
  private PropertySuggestions emptyInstance;

  @BeforeEach
  void setUp() {
    emptyInstance = new PropertySuggestionsImpl();

    instance1 = new PropertySuggestionsImpl();
    instance1.getKeySuggestions().add("key1");
    instance1.getKeySuggestions().add("key2");
    instance1.getKeySuggestions().add("key3");
    instance1.getKeySuggestions().add("doubleKeyTest");
    instance1.getValueSuggestions().add("value1");
    instance1.getValueSuggestions().add("value2");
    instance1.getValueSuggestions().add("value3");
    instance1.getValueSuggestions().add("doubleValueTest");

    instance2 = new PropertySuggestionsImpl();
    instance2.getKeySuggestions().add("doubleKeyTest");
    instance2.getKeySuggestions().add("key4");
    instance2.getKeySuggestions().add("key5");
    instance2.getKeySuggestions().add("key6");
    instance2.getValueSuggestions().add("doubleValueTest");
    instance2.getValueSuggestions().add("value1");
    instance2.getValueSuggestions().add("value2");
    instance2.getValueSuggestions().add("value3");
  }

  @Test
  void shouldRemainEmptyForNoSuggestions() {
    MergedPropertySuggestions mergedSuggestions = new MergedPropertySuggestions(new HashSet<>());
    assertTrue(mergedSuggestions.getKeySuggestions().isEmpty());
    assertTrue(mergedSuggestions.getValueSuggestions().isEmpty());
  }

  @Test
  void shouldRemainEmptyForEmptySuggestions() {
    Set<PropertySuggestions> sugSet = new HashSet<>(Arrays.asList(emptyInstance));
    MergedPropertySuggestions mergedSuggestions = new MergedPropertySuggestions(sugSet);
    assertTrue(mergedSuggestions.getKeySuggestions().isEmpty());
    assertTrue(mergedSuggestions.getValueSuggestions().isEmpty());
  }

  @Test
  void shouldMergeSuggestions() {
    Set<PropertySuggestions> sugSet = new HashSet<>(Arrays.asList(instance1, instance2));
    MergedPropertySuggestions mergedSuggestions = new MergedPropertySuggestions(sugSet);
    assertEquals(
        Sets.union(instance1.getKeySuggestions(), instance2.getKeySuggestions()),
        mergedSuggestions.getKeySuggestions()
    );
    assertEquals(
        Sets.union(instance1.getValueSuggestions(), instance2.getValueSuggestions()),
        mergedSuggestions.getValueSuggestions()
    );
  }

  private class PropertySuggestionsImpl
      implements
        PropertySuggestions {

    private final Set<String> keySuggestions = new HashSet<>();
    private final Set<String> valueSuggestions = new HashSet<>();

    PropertySuggestionsImpl() {
    }

    @Override
    public Set<String> getKeySuggestions() {
      return keySuggestions;
    }

    @Override
    public Set<String> getValueSuggestions() {
      return valueSuggestions;
    }

  }
}
