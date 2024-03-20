/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.configuration.gestalt.decoders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.github.gestalt.config.decoder.Decoder;
import org.github.gestalt.config.decoder.DecoderContext;
import org.github.gestalt.config.decoder.Priority;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.node.NodeType;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.ValidateOf;

/**
 * A decoder to read map literals in the form
 * {@code <KEY_1>=<VALUE_1>,<KEY_2>=<VALUE_2>,...,<KEY_N>=<VALUE_N>}, where the key-value pairs
 * (i.e. map entries) are separated by commas as a delimiter.
 */
public class MapLiteralDecoder
    implements Decoder<Map<?, ?>> {

  public MapLiteralDecoder() {
  }

  @Override
  public Priority priority() {
    return Priority.HIGH;
  }

  @Override
  public String name() {
    return MapLiteralDecoder.class.getName();
  }

  @Override
  public boolean canDecode(String path, Tags tags, ConfigNode node, TypeCapture<?> type) {
    return node.getNodeType() == NodeType.LEAF
        && Map.class.isAssignableFrom(type.getRawType())
        && type.getParameterTypes().size() == 2;
  }

  @Override
  public ValidateOf<Map<?, ?>> decode(String path,
                                      Tags tags,
                                      ConfigNode node,
                                      TypeCapture<?> type,
                                      DecoderContext decoderContext) {
    // This decoder only decodes nodes of type leaf. For other types the default decoders
    // `ArrayDecoder` and `ObjectDecoder` will eventually call this decoder if necessary.
    if (node.getNodeType() != NodeType.LEAF) {
      return ValidateOf.inValid(
          new ValidationError.DecodingExpectedLeafNodeType(path, node, this.name())
      );
    }

    if (node.getValue().isEmpty()) {
      return ValidateOf.inValid(
          new ValidationError.LeafNodesHaveNoValues(path)
      );
    }

    List<ValidationError> errors = new ArrayList<>();
    Map<Object, Object> result = new HashMap<>();

    // Split the node value on ',' to seperate it into `key=value` pairs and split those
    // again into the `key` and `value`. Then decode the key and value to the required types.
    for (String entry : node.getValue().get().split(",")) {
      if (entry.isBlank()) {
        continue;
      }

      String[] keyValuePair = entry.split("=");
      if (keyValuePair.length != 2) {
        errors.add(new MapEntryFormatInvalid(entry));
        continue;
      }

      // Decode the key string to the required key type.
      ValidateOf<?> key = decoderContext.getDecoderService()
          .decodeNode(path, tags, new LeafNode(keyValuePair[0].trim()),
                      type.getFirstParameterType(),
                      decoderContext);
      if (key.hasErrors()) {
        errors.addAll(key.getErrors());
        continue;
      }

      // Decode the value string to the required value type.
      ValidateOf<?> value = decoderContext.getDecoderService()
          .decodeNode(path, tags, new LeafNode(keyValuePair[1].trim()),
                      type.getSecondParameterType(),
                      decoderContext);
      if (value.hasErrors()) {
        errors.addAll(value.getErrors());
        continue;
      }

      result.put(key.results(), value.results());
    }

    return ValidateOf.validateOf(result, errors);
  }

  /**
   * A validation error for map entries not in the format {@code <KEY>=<VALUE>}.
   */
  public static class MapEntryFormatInvalid
      extends ValidationError {

    private final String rawEntry;

    public MapEntryFormatInvalid(String rawEntry) {
      super(ValidationLevel.ERROR);
      this.rawEntry = rawEntry;
    }

    @Override
    public String description() {
      return "Map entry is not in the format '<KEY>=<VALUE>':" + rawEntry;
    }
  }

}
