/*
 * openTCS copyright information:
 * Copyright (c) 2006 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.math;

import java.io.Serializable;
import java.lang.reflect.Array;

/**
 * A matrix containing elements of a generic type.
 * <p>
 * This class is basically a wrapper around an array providing convenience
 * methods.
 * </p>
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @param <E> The matrix's elements' type.
 */
public class GenericMatrix<E>
    implements Matrix<E>, Serializable {

  /**
   * The number of elements on this matrix's X axis.
   */
  private final int xDimension;
  /**
   * The number of elements on this matrix's Y axis.
   */
  private final int yDimension;
  /**
   * The actual matrix.
   */
  private final E[][] matrix;

  /**
   * Creates a new GenericMatrix of the given dimensions, initially containing
   * only <code>null</code> elements.
   *
   * @param x The matrix's X dimension.
   * @param y The matrix's Y dimension.
   * @throws IndexOutOfBoundsException If either dimension parameter is less
   * than 1.
   */
  @SuppressWarnings("unchecked")
  public GenericMatrix(int x, int y) {
    if (x < 1) {
      throw new IndexOutOfBoundsException("x is less than 1");
    }
    if (y < 1) {
      throw new IndexOutOfBoundsException("y is less than 1");
    }
    xDimension = x;
    yDimension = y;
    matrix = (E[][]) new Object[x][y];
  }

  /**
   * Creates a new GenericMatrix of the same dimensions and containing the same
   * elements as the given one.
   *
   * @param original The original matrix.
   */
  @SuppressWarnings("unchecked")
  public GenericMatrix(Matrix<E> original) {
    xDimension = original.getXDimension();
    yDimension = original.getYDimension();
    matrix = (E[][]) original.toArray(new Object[xDimension][yDimension]);
  }

  @Override
  public int getXDimension() {
    return xDimension;
  }

  @Override
  public int getYDimension() {
    return yDimension;
  }

  @Override
  public E getElement(int x, int y)
      throws IndexOutOfBoundsException {
    return matrix[x][y];
  }

  @Override
  public void setElement(int x, int y, E value)
      throws IndexOutOfBoundsException {
    matrix[x][y] = value;
  }

  @Override
  public void clear() {
    fill(null);
  }

  @Override
  public void fill(E value) {
    for (int x = 0; x < xDimension; x++) {
      for (int y = 0; y < yDimension; y++) {
        matrix[x][y] = value;
      }
    }
  }

  @Override
  public GenericMatrix<E> transpose() {
    GenericMatrix<E> result = new GenericMatrix<>(yDimension, xDimension);
    for (int x = 0; x < yDimension; x++) {
      for (int y = 0; y < xDimension; y++) {
        result.matrix[x][y] = matrix[y][x];
      }
    }
    return result;
  }

  @Override
  public Object[][] toArray() {
    Object[][] result = new Object[xDimension][yDimension];
    for (int x = 0; x < xDimension; x++) {
      System.arraycopy(matrix[x], 0, result[x], 0, yDimension);
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T[][] toArray(T[][] array) {
    // Check if the given array can take the whole matrix.
    boolean bigEnough = true;
    if (array.length < matrix.length) {
      bigEnough = false;
    }
    for (int x = 0; bigEnough && x < xDimension; x++) {
      if (array[x].length < yDimension) {
        bigEnough = false;
      }
    }
    final T[][] result;
    // If the given array is big enough, use it.
    if (bigEnough) {
      result = array;
    }
    // If the given array is not big enough, create a new one.
    else {
      final Class<?> outerCompType = array.getClass().getComponentType();
      final Class<?> innerCompType = outerCompType.getComponentType();
      result = (T[][]) Array.newInstance(outerCompType, xDimension);
      for (int x = 0; x < xDimension; x++) {
        result[x] = (T[]) Array.newInstance(innerCompType, yDimension);
      }
    }
    // Copy the actual data.
    for (int x = 0; x < xDimension; x++) {
      System.arraycopy(matrix[x], 0, result[x], 0, yDimension);
      // If there is room, mark the next element in the array with null.
      if (matrix[x].length > yDimension) {
        matrix[x][yDimension] = null;
      }
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    for (int x = 0; x < xDimension; x++) {
      for (int y = 0; y < yDimension; y++) {
        result.append("[" + matrix[x][y].toString() + "]");
      }
      result.append("\n");
    }
    return result.toString();
  }

  /**
   * Returns a new square matrix of the given dimensions with the elements on
   * its main diagonal all set to the given value.
   *
   * @param <T> The element type of the returned matrix.
   * @param dimension The X and Y dimension of the matrix to be returned.
   * @param value The value to set all elements on the matrix's main diagonal
   * to.
   * @return A new square matrix of the given dimensions with the elements on
   * its main diagonal all set to the given value.
   */
  public static <T> GenericMatrix<T> diagonalMatrix(int dimension, T value) {
    GenericMatrix<T> result = new GenericMatrix<>(dimension, dimension);
    for (int i = 0; i < dimension; i++) {
      result.setElement(i, i, value);
    }
    return result;
  }

  /**
   * Returns a new square matrix with the elements on its main diagonal set to
   * the values in the given array.
   *
   * @param <T> The element type of the returned matrix.
   * @param values The diagonal's values.
   * @return A new square matrix with the elements on its main diagonal set to
   * the values in the given array.
   */
  public static <T> GenericMatrix<T> diagonalMatrix(T[] values) {
    GenericMatrix<T> result =
        new GenericMatrix<>(values.length, values.length);
    for (int i = 0; i < values.length; i++) {
      result.setElement(i, i, values[i]);
    }
    return result;
  }

  /**
   * This method implements the matrix multiplication.
   * Note: The matrix a has a larger y dimension. This is important
   * for the argument order, because the matrix multiplication
   * is not commutative.
   *
   * @param <T> The element type of the matrices.
   * @param a The first source matrix.
   * @param b The second source matrix.
   * @param arithmetic The arithmetic used for the elemental operations.
   * @return The matrix resulting from multiplying the two given matrices.
   */
  public static <T> GenericMatrix<T> multiply(Matrix<T> a,
                                              Matrix<T> b,
                                              Arithmetic<T> arithmetic) {
    GenericMatrix<T> result;
    if (a.getXDimension() != b.getYDimension()) {
      throw new ArithmeticException("matrix dimensions don't fit");
    }
    else {
      result = new GenericMatrix<>(a.getXDimension(), b.getYDimension());
      for (int i = 0; i < result.getXDimension(); i++) {
        for (int j = 0; j < result.getYDimension(); j++) {
          T entry = null;
          for (int n = 0; n < a.getYDimension(); n++) {
            T value1 = a.getElement(i, n);
            T value2 = b.getElement(n, j);
            entry = arithmetic.add(entry, arithmetic.multiply(value1, value2));
          }
          result.setElement(i, j, entry);
        }
      }
    }
    return result;
  }
}
