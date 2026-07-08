// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.modelmapper;

import static java.util.Objects.requireNonNull;

import java.awt.Color;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.modelmapper.AbstractConverter;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.ColorTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.ResourceTO;
import org.opentcs.util.Comparators;

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
   * Creates a converter that converts a set of {@link TCSObjectReference}s to a list of their
   * names.
   *
   * @return The converter.
   */
  public static Converter<Set<TCSObjectReference<?>>, List<String>>
      tcsObjectReferenceSetConverter() {
    return new AbstractConverter<>() {
      @Override
      protected List<String> convert(Set<TCSObjectReference<?>> source) {
        return source.stream()
            .sorted(Comparators.referencesByName())
            .map(ref -> ref == null ? null : ref.getName())
            .toList();
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
   * Creates a converter that converts a list of sets of {@link TCSResourceReference}s to a list of
   * lists of {@link ResourceTO}. Additionally, the entries in each list are sorted by resource
   * names.
   *
   * @return The converter.
   */
  public static Converter<List<Set<TCSResourceReference<?>>>, List<List<ResourceTO>>>
      resourceConverter() {
    return new AbstractConverter<>() {
      @Override
      protected List<List<ResourceTO>> convert(
          List<Set<TCSResourceReference<?>>> source
      ) {
        requireNonNull(source, "source");
        return source.stream()
            .map(
                set -> set.stream()
                    .sorted(Comparators.referencesByName())
                    .map(
                        resource -> new ResourceTO()
                            .setName(resource.getName())
                            .setType(toResourceType(resource))
                    )
                    .toList()
            )
            .toList();
      }
    };
  }

  /**
   * Creates a converter that converts a set of {@link TCSResourceReference}s to a list of
   * {@link ResourceTO}. Additionally, the entries in each list are sorted by resource names.
   *
   * @return The converter.
   */
  public static Converter<Set<TCSResourceReference<?>>, List<ResourceTO>>
      resourceSetConverter() {
    return new AbstractConverter<>() {
      @Override
      protected List<ResourceTO> convert(
          Set<TCSResourceReference<?>> source
      ) {
        requireNonNull(source, "source");
        return source.stream()
            .sorted(Comparators.referencesByName())
            .map(
                resource -> new ResourceTO()
                    .setName(resource.getName())
                    .setType(toResourceType(resource))
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
  public static Converter<Color, ColorTO> colorConverter() {
    return new AbstractConverter<>() {
      @Override
      protected ColorTO convert(Color source) {
        requireNonNull(source, "source");
        return new ColorTO()
            .setRed(source.getRed())
            .setGreen(source.getGreen())
            .setBlue(source.getBlue());
      }
    };
  }

  /**
   * Creates a converter that converts a {@link Double#NaN} to {@code null}.
   *
   * @return The converter.
   */
  public static Converter<Double, Double> nanToNullConverter() {
    return new AbstractConverter<>() {
      @Override
      protected Double convert(Double source) {
        return Double.isNaN(source) ? null : source;
      }
    };
  }

  /**
   * Creates a converter that handles the conversion of {@link Instant#MAX} to {@code null}. All
   * other instant values are not altered.
   *
   * @return The converter.
   */
  public static Converter<Instant, Instant> instantConverter() {
    return new AbstractConverter<>() {
      @Override
      public Instant convert(Instant source) {
        return Objects.equals(source, Instant.MAX) ? null : source;
      }
    };
  }

  private static ResourceTO.ResourceTypeTO toResourceType(TCSResourceReference<?> resource) {
    if (resource.getReferentClass().isAssignableFrom(Point.class)) {
      return ResourceTO.ResourceTypeTO.POINT;
    }
    else if (resource.getReferentClass().isAssignableFrom(Path.class)) {
      return ResourceTO.ResourceTypeTO.PATH;
    }
    else if (resource.getReferentClass().isAssignableFrom(Location.class)) {
      return ResourceTO.ResourceTypeTO.LOCATION;
    }
    else {
      throw new IllegalArgumentException("Could not determine resource type for: " + resource);
    }
  }
}
