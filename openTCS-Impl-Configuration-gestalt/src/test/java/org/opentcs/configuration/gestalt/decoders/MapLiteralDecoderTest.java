/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.configuration.gestalt.decoders;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.github.gestalt.config.Gestalt;
import org.github.gestalt.config.builder.GestaltBuilder;
import org.github.gestalt.config.decoder.DecoderContext;
import org.github.gestalt.config.decoder.DecoderRegistry;
import org.github.gestalt.config.decoder.DecoderService;
import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.ConfigNodeService;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.path.mapper.StandardPathMapper;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.source.MapConfigSourceBuilder;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.ValidateOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;

/**
 * Tests the map literal decoder {@link MapLiteralDecoder} for Gestalt.
 */
class MapLiteralDecoderTest {

  private ConfigNodeService configNodeService;
  private SentenceLexer lexer;
  private DecoderService decoderService;

  @BeforeEach
  void setup()
      throws GestaltConfigurationException {
    configNodeService = mock(ConfigNodeService.class);
    lexer = mock(SentenceLexer.class);
    decoderService = new DecoderRegistry(Collections.singletonList(new MapLiteralDecoder()),
                                         configNodeService,
                                         lexer,
                                         List.of(new StandardPathMapper()));
  }

  @Test
  void shouldDecodeMapLiteral()
      throws GestaltException {
    Map<String, String> config = Map.of("entry_path", "AAAA=1, BBBB=2, CCCC=3");
    Gestalt gestalt = buildGestaltConfig(config);

    Map<String, Integer> result = gestalt.getConfig("entry_path",
                                                    new TypeCapture<Map<String, Integer>>() {
                                                });

    assertEquals(result.get("AAAA"), 1);
    assertEquals(result.get("BBBB"), 2);
    assertEquals(result.get("CCCC"), 3);
  }

  @Test
  void shouldDecodeMapLiteralToEnum()
      throws GestaltException {
    Map<String, String> config = Map.of("entry_path", "Foo=1, Bar=2, Baz=3");
    Gestalt gestalt = buildGestaltConfig(config);

    Map<Things, Integer> result = gestalt.getConfig("entry_path",
                                                    new TypeCapture<Map<Things, Integer>>() {
                                                });

    assertEquals(result.get(Things.Foo), 1);
    assertEquals(result.get(Things.Bar), 2);
    assertEquals(result.get(Things.Baz), 3);
  }

  @Test
  void emptyLeafShouldGiveAnEmptyMap()
      throws GestaltException {
    Map<String, String> config = Map.of("entry_path", "");
    Gestalt gestalt = buildGestaltConfig(config);

    Map<String, Integer> result = gestalt.getConfig("entry_path",
                                                    new TypeCapture<Map<String, Integer>>() {
                                                });

    assertTrue(result.isEmpty());
  }

  @Test
  void shouldGiveErrorWhenWrongDelimiterIsUsed() {
    MapLiteralDecoder decoder = new MapLiteralDecoder();

    ValidateOf<Map<?, ?>> result = decoder.decode(
        "entry_path",
        Tags.of(),
        new LeafNode("AAAA=1; BBBB=2; CCCC=3"),
        new TypeCapture<Map<String, String>>() {
    },
        new DecoderContext(decoderService, null)
    );
    assertThat(result.getErrors(), hasSize(1));
    assertThat(result.getErrors().get(0),
               instanceOf(MapLiteralDecoder.MapEntryFormatInvalid.class));
  }

  @Test
  void shouldGiveErrorWhenWrongAsignmentIsUsed() {
    MapLiteralDecoder decoder = new MapLiteralDecoder();

    ValidateOf<Map<?, ?>> result = decoder.decode(
        "entry_path",
        Tags.of(),
        new LeafNode("AAAA~1, BBBB~2, CCCC~3"),
        new TypeCapture<Map<String, String>>() {
    },
        new DecoderContext(decoderService, null)
    );
    assertThat(result.getErrors(), hasSize(3));
    assertThat(result.getErrors().get(0),
               instanceOf(MapLiteralDecoder.MapEntryFormatInvalid.class));
    assertThat(result.getErrors().get(1),
               instanceOf(MapLiteralDecoder.MapEntryFormatInvalid.class));
    assertThat(result.getErrors().get(2),
               instanceOf(MapLiteralDecoder.MapEntryFormatInvalid.class));
  }

  private enum Things {
    Foo,
    Bar,
    Baz
  }

  private Gestalt buildGestaltConfig(Map<String, String> config)
      throws GestaltException {
    Gestalt gestalt = new GestaltBuilder()
        .addDefaultDecoders()
        .addDecoder(new ClassPathDecoder())
        .addDecoder(new MapLiteralDecoder())
        .addSource(MapConfigSourceBuilder.builder().setCustomConfig(config).build())
        .build();
    gestalt.loadConfigs();
    return gestalt;
  }
}
