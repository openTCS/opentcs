// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.workingset;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;

import com.google.common.collect.Ordering;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.access.to.CreationTO;

/**
 * Tests for a {@link PrefixedUlidObjectNameProvider}.
 */
class PrefixedUlidObjectNameProviderTest {

  private PrefixedUlidObjectNameProvider nameProvider;

  @BeforeEach
  void setUp() {
    this.nameProvider = new PrefixedUlidObjectNameProvider();
  }

  @Test
  void shouldUsePrefixFromCreationTO() {
    assertThat(nameProvider.apply(new CreationTO("SomeName-")), startsWith("SomeName-"));
  }

  @Test
  void shouldAppendSuffix() {
    assertThat(nameProvider.apply(new CreationTO("")), is(not("")));
  }

  @Test
  void shouldProvideNamesInChronologicalOrder() {
    final int count = 100000;
    final CreationTO to = new CreationTO("SomeName-");

    List<String> namesInOrderOfCreation = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      namesInOrderOfCreation.add(nameProvider.apply(to));
    }

    List<String> namesLexicographic = Ordering.natural().sortedCopy(namesInOrderOfCreation);

    assertThat(namesInOrderOfCreation, is(equalTo(namesLexicographic)));
  }
}
