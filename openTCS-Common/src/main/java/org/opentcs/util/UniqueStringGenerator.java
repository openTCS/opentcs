/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.SortedSet;
import java.util.TreeSet;
import static org.opentcs.util.Assertions.checkArgument;

/**
 * Provides a way to acquire unique strings.
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @param <S> The type of the selectors/keys to be used for mapping to name
 * patterns.
 */
public class UniqueStringGenerator<S> {

  /**
   * Configured name patterns.
   */
  private final Map<S, NamePattern> namePatterns = new HashMap<>();
  /**
   * All strings known to this generator, sorted lexicographically.
   */
  private final SortedSet<String> existingStrings = new TreeSet<>();

  /**
   * Creates a new instance without any name patterns.
   */
  public UniqueStringGenerator() {
    // Do nada.
  }

  /**
   * Registers a name pattern for the given selector.
   *
   * @param selector The selector.
   * @param prefix The prefix of names to be used for the given selector.
   * @param suffixPattern The suffix pattern to be used for the given selector.
   */
  public void registerNamePattern(S selector,
                                  String prefix,
                                  String suffixPattern) {
    namePatterns.put(selector, new NamePattern(prefix, suffixPattern));
  }

  /**
   * Adds a String to those known by this generator. As a result, this generator
   * will never return a String that is equal to the given one.
   *
   * @param newString The string to be added.
   */
  public void addString(final String newString) {
    requireNonNull(newString, "newString is null");
    existingStrings.add(newString);
  }

  /**
   * Makes this generator forget a known String. As a result, this generator
   * might return a String that is equal to the given one in the future.
   *
   * @param rmString The string to be forgotten.
   */
  public void removeString(final String rmString) {
    requireNonNull(rmString, "rmString is null");
    existingStrings.remove(rmString);
  }

  /**
   * Removes all known Strings.
   */
  public void clear() {
    existingStrings.clear();
  }

  /**
   * Returns a string that is unique among all strings registered with this
   * generator.
   *
   * @param selector A selector for the name pattern to be used.
   * @return A string that is unique among all strings registered with this
   * generator.
   */
  public String getUniqueString(S selector) {
    requireNonNull(selector, "selector");

    NamePattern namePattern = namePatterns.get(selector);
    checkArgument(namePattern != null, "Unknown selector: %s", selector);

    return getUniqueString(namePattern.prefix, namePattern.suffixPattern);
  }

  /**
   * Returns a String that is unique among all known Strings in this generator.
   * The returned String will consist of the given prefix followed by an integer
   * formatted according to the given pattern. The pattern has to be of the form
   * understood by <code>java.text.DecimalFormat</code>.
   *
   * @param prefix The prefix of the String to be generated.
   * @param suffixPattern A pattern describing the suffix of the generated
   * String. Must be of the form understood by
   * <code>java.text.DecimalFormat</code>.
   * @return A String that is unique among all known Strings.
   */
  public String getUniqueString(final String prefix,
                                final String suffixPattern) {
    requireNonNull(suffixPattern, "suffixPattern is null");

    final String actualPrefix = prefix == null ? "" : prefix;
    final DecimalFormat format = new DecimalFormat(suffixPattern);
    final String lBound = actualPrefix + "0";
    final String uBound = actualPrefix + ":";
    final int prefixLength = actualPrefix.length();
    long maxSuffixValue = 0;
    // Get all existing strings with the same prefix and at least one digit
    // following it.
    for (String curName : existingStrings.subSet(lBound, uBound)) {
      // Check if the suffix contains only digits.
      boolean allDigits = containsOnlyDigits(curName.substring(prefixLength));
      // If the suffix contains only digits, parse it and remember the maximum
      // suffix value we found so far. (If the suffix contains other characters,
      // ignore this string - we generate suffixes with digits only, so there
      // can't be a collision.
      if (allDigits) {
        final long curSuffixValue = NumberParsers.parsePureDecimalLong(
            curName, prefixLength, curName.length() - prefixLength);
        maxSuffixValue
            = maxSuffixValue > curSuffixValue ? maxSuffixValue : curSuffixValue;
      }
    }
    // Increment the highest value found and use that as the suffix
    return actualPrefix + format.format(maxSuffixValue + 1);
  }

  /**
   * Checks if the given string contains only (decimal) digits.
   *
   * @param input The string to be checked.
   * @return <code>true</code> if, and only if, the given string contains only
   * (decimal) digits.
   */
  private boolean containsOnlyDigits(String input) {
    assert input != null;
    for (int i = 0; i < input.length(); i++) {
      int digit = input.charAt(i) - '0';
      if (digit < 0 || digit > 9) {
        return false;
      }
    }
    return true;
  }

  /**
   * A name pattern.
   */
  private static class NamePattern {

    /**
     * The prefix to be used.
     */
    private final String prefix;
    /**
     * The suffix pattern to be used.
     */
    private final String suffixPattern;

    /**
     * Creates a new instance.
     *
     * @param prefix The prefix to be used.
     * @param suffixPattern The suffix pattern to be used.
     */
    private NamePattern(String prefix, String suffixPattern) {
      this.prefix = requireNonNull(prefix, "prefix");
      this.suffixPattern = requireNonNull(suffixPattern, "suffixPattern");
    }
  }
}
