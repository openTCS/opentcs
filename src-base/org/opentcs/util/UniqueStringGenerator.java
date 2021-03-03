/*
 * openTCS copyright information:
 * Copyright (c) 2008 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util;

import com.google.common.base.Strings;
import java.text.DecimalFormat;
import static java.util.Objects.requireNonNull;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Provides a way to acquire unique strings.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class UniqueStringGenerator {

  /**
   * All strings known to this generator, sorted lexicographically.
   */
  private final SortedSet<String> existingStrings = new TreeSet<>();

  /**
   * Creates a new instance.
   */
  public UniqueStringGenerator() {
    // Do nada.
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
    final String actualPrefix = Strings.nullToEmpty(prefix);
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
}
