/*
 * openTCS copyright information:
 * Copyright (c) 2006 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.math;

/**
 * A matrix containing elements of a generic type.
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @param <E> The matrix's elements' type.
 */
public interface Matrix<E> {
  /**
   * Returns the number of elements on this matrix's X axis.
   *
   * @return The number of elements on this matrix's X axis.
   */
  int getXDimension();
  
  /**
   * Returns the number of elements on this matrix's Y axis.
   *
   * @return The number of elements on this matrix's Y axis.
   */
  int getYDimension();
  
  /**
   * Returns the element at the given position, or <code>null</code>, if the
   * given position is empty.
   *
   * @param x The X coordinate of the element to be returned.
   * @param y The Y coordinate of the element to be returned.
   * @return The element at the given position, or <code>null</code>, if the
   * given position is empty.
   * @throws IndexOutOfBoundsException If either parameter is out of the bounds
   * of the represented matrix.
   */
  E getElement(int x, int y);
  
  /**
   * Sets the element at the given position.
   *
   * @param x The element's X coordinate.
   * @param y The element's Y coordinate.
   * @param value The value to set the element at the given position to.
   * @throws IndexOutOfBoundsException If either parameter is out of the bounds
   * of the represented matrix.
   */
  void setElement(int x, int y, E value)
  throws IndexOutOfBoundsException;
  
  /**
   * Clears all positions of this matrix. Calling this method is effectively the
   * same as calling {@link #fill(Object) fill(E)} with a <code>null</code>
   * argument.
   */
  void clear();
  
  /**
   * Fills all positions of this matrix with the given value.
   *
   * @param value The value fill this matrix with.
   */
  void fill(E value);
  
  /**
   * Returns the transpose of this matrix.
   *
   * @return The transpose of this matrix.
   */
  Matrix<E> transpose();
  
  /**
   * Returns a copy of this matrix as a two-dimensional array.
   *
   * @return A copy of this matrix as a two-dimensional array.
   */
  Object[][] toArray();
  
  /**
   * Returns a copy of this matrix as a two-dimensional array. The runtime type
   * of the returned array is that of the specified array.
   *
   * @param array The array defining the runtime type of the returned array.
   * @param <T> The type of the array's elements.
   * @return A copy of this matrix as a two-dimensional array.
   */
  <T> T[][] toArray(T[][] array);
}
