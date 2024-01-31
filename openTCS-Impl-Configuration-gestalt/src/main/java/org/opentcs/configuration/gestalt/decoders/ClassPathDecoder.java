/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.configuration.gestalt.decoders;

import org.github.gestalt.config.decoder.Decoder;
import org.github.gestalt.config.decoder.DecoderContext;
import org.github.gestalt.config.decoder.Priority;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.NodeType;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.ValidateOf;

/**
 * A decoder to decode fully qualified class names to their representative class object.
 *
 * This decoder looks through the class path to find a class with the specified class name
 * and returns the class object. It will fail if the specified class cannot be found or
 * the specified class cannot be assigned to the type that is expected to be returned.
 */
public class ClassPathDecoder
    implements Decoder<Class<?>> {

  /**
   * Creates a new instance.
   */
  public ClassPathDecoder() {
  }

  @Override
  public Priority priority() {
    return Priority.MEDIUM;
  }

  @Override
  public String name() {
    return ClassPathDecoder.class.getName();
  }

  @Override
  public boolean canDecode(String string, Tags tags, ConfigNode cn, TypeCapture<?> tc) {
    return Class.class.isAssignableFrom(tc.getRawType()) && tc.hasParameter();
  }

  @Override
  public ValidateOf<Class<?>> decode(String path,
                                     Tags tags,
                                     ConfigNode node,
                                     TypeCapture<?> type,
                                     DecoderContext context) {
    // This decoder only decodes nodes of type leaf. For other types the default decoders
    // `ArrayDecoder` and `ObjectDecoder` will eventually call this decoder if necessary.
    if (node.getNodeType() != NodeType.LEAF) {
      return ValidateOf.inValid(
          new ValidationError.DecodingExpectedLeafNodeType(path, node, this.name())
      );
    }
    // Look for a class with the configured name. The class must be assignable to the
    // class this decoder is expected to return via the type capture.
    return node.getValue().map(className -> {
      try {
        Class<?> configuredClass = Class.forName(className);
        if (type.getFirstParameterType().isAssignableFrom(configuredClass)) {
          return ValidateOf.<Class<?>>valid(configuredClass);
        }
        else {
          return ValidateOf.<Class<?>>inValid(
              new CannotCast(className, type.getFirstParameterType().getName())
          );
        }
      }
      catch (ClassNotFoundException e) {
        return ValidateOf.<Class<?>>inValid(new ClassNotFound(className));
      }
    }).orElse(ValidateOf.<Class<?>>inValid(
        new ValidationError.DecodingLeafMissingValue(path, this.name())
    ));
  }

  /**
   * The configured class cannot be cast to the class expected by the decoder.
   */
  public static class CannotCast
      extends ValidationError {

    private final String from;
    private final String to;

    public CannotCast(String from, String to) {
      super(ValidationLevel.ERROR);
      this.from = from;
      this.to = to;
    }

    @Override
    public String description() {
      return "The class `" + this.from + "` cannot be cast to `" + this.to + "`.";
    }
  }

  /**
   * The configured class cannot be found in the class path.
   */
  public static class ClassNotFound
      extends ValidationError {

    private final String className;

    public ClassNotFound(String className) {
      super(ValidationLevel.ERROR);
      this.className = className;
    }

    @Override
    public String description() {
      return "The class `" + this.className + "` cannot be found.";
    }
  }

}
