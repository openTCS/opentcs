/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.workingset;

import com.google.common.collect.Ordering;
import java.util.ArrayList;
import java.util.List;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import org.junit.*;
import org.opentcs.access.to.CreationTO;

/**
 * Tests for a {@link PrefixedUlidObjectNameProvider}.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class PrefixedUlidObjectNameProviderTest {

  private PrefixedUlidObjectNameProvider nameProvider;

  @Before
  public void setUp() {
    this.nameProvider = new PrefixedUlidObjectNameProvider();
  }

  @Test
  public void shouldUsePrefixFromCreationTO() {
    assertThat(nameProvider.apply(new CreationTO("SomeName-")), startsWith("SomeName-"));
  }

  @Test
  public void shouldAppendSuffix() {
    assertThat(nameProvider.apply(new CreationTO("")), is(not("")));
  }

  @Test
  public void shouldProvideNamesInChronologicalOrder() {
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
