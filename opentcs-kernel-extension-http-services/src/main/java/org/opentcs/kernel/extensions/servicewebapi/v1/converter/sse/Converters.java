// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.converter.sse;

import static java.util.Objects.requireNonNull;

import java.awt.Color;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.modelmapper.AbstractConverter;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.sse.VehicleEventTO;

/**
 * Provides helper methods to create converters.
 */
public class Converters {

  private Converters() {
  }

  /**
   * Creates a converter that converts a {@link TCSObjectReference} to its name.
   *
   * @return The converter.
   */
  public static Converter<TCSObjectReference<?>, String> tcsObjectReferenceConverter() {
    return new AbstractConverter<>() {
      @Override
      protected String convert(TCSObjectReference<?> source) {
        return source == null ? null : source.getName();
      }
    };
  }

  /**
   * Creates a converter that converts a {@link TCSResourceReference} to its name.
   *
   * @return The converter.
   */
  public static Converter<TCSResourceReference<?>, String> tcsResourceReferenceConverter() {
    return new AbstractConverter<>() {
      @Override
      protected String convert(TCSResourceReference<?> source) {
        return source == null ? null : source.getName();
      }
    };
  }

  /**
   * Creates a converter that converts a list of sets of {@link TCSObjectReference}s to a list of
   * lists of their names. Additionally, the names in each list are sorted.
   *
   * @return The converter.
   */
  public static Converter<List<Set<TCSObjectReference<?>>>, List<List<String>>>
      tcsObjectReferenceListConverter() {
    return new AbstractConverter<>() {
      @Override
      protected List<List<String>> convert(List<Set<TCSObjectReference<?>>> source) {
        requireNonNull(source, "source");
        return source.stream()
            .map(
                set -> set.stream()
                    .map(TCSObjectReference::getName)
                    .sorted()
                    .toList()
            )
            .toList();
      }
    };
  }

  /**
   * Creates a converter that converts a set of elements of one type to a list of elements of
   * another type. Additionally, the elements in the list are sorted using the provided comparator.
   *
   * @param comparator The comparator to use for sorting the elements.
   * @param destinationType The type of the elements in the resulting list.
   * @param modelMapper The {@link ModelMapper} instance to use for mapping the actual elements.
   * @return The converter.
   * @param <T> The type of the elements in the source set.
   * @param <U> The type of the elements in the resulting list.
   */
  public static <T, U> Converter<Set<T>, List<U>> setToListConverter(
      Comparator<T> comparator,
      Class<U> destinationType,
      ModelMapper modelMapper
  ) {
    return new AbstractConverter<>() {
      @Override
      protected List<U> convert(Set<T> source) {
        requireNonNull(source, "source");
        return source.stream()
            .sorted(comparator)
            .map(element -> modelMapper.map(element, destinationType))
            .toList();
      }
    };
  }

  /**
   * Creates a converter that converts a {@link Path} to its name or {@code null}, if the path
   * itself is {@code null}.
   *
   * @return The converter.
   */
  public static Converter<Path, String> pathConverter() {
    return new AbstractConverter<>() {
      @Override
      protected String convert(Path source) {
        return source == null ? null : source.getName();
      }
    };
  }

  /**
   * Creates a converter that converts a {@link Point} to its name or {@code null}, if the point
   * itself is {@code null}.
   *
   * @return The converter.
   */
  public static Converter<Point, String> pointConverter() {
    return new AbstractConverter<>() {
      @Override
      protected String convert(Point source) {
        return source == null ? null : source.getName();
      }
    };
  }

  /**
   * Creates a converter that converts a {@link Color} to its SSE representation.
   *
   * @return The converter.
   */
  public static Converter<Color, VehicleEventTO.LayoutTO.ColorTO> colorConverter() {
    return new AbstractConverter<>() {
      @Override
      protected VehicleEventTO.LayoutTO.ColorTO convert(Color source) {
        requireNonNull(source, "source");
        return new VehicleEventTO.LayoutTO.ColorTO()
            .setRed(source.getRed())
            .setGreen(source.getGreen())
            .setBlue(source.getBlue());
      }
    };
  }
}
