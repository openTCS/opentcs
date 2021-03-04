/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.xmlhost.orders.binding;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import org.junit.*;
import static org.junit.Assert.assertTrue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class TCSResponseSetTest {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(TCSResponseSetTest.class);

  public TCSResponseSetTest() {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  @Test
  public void shouldOutputSampleResponseSet()
      throws IOException {
    TCSResponseSet responseSet = new TCSResponseSet();

    TransportResponse response = new TransportResponse();
    response.setId("TransportOrder-01");
    response.setOrderName("TOrder-0001");
    response.setExecutionSuccessful(true);

    responseSet.getResponses().add(response);

    response = new TransportResponse();
    response.setId("TransportOrder-02");
    response.setOrderName("TOrder-0002");
    response.setExecutionSuccessful(true);

    responseSet.getResponses().add(response);

    StringWriter writer = new StringWriter();
    responseSet.toXml(writer);
    String xmlOutput = writer.toString();

    LOG.info(xmlOutput);

    StringReader reader = new StringReader(xmlOutput);
    TCSResponseSet parsedResponseSet = TCSResponseSet.fromXml(reader);
    assertTrue("parsed set should have exactly two messages",
               parsedResponseSet.getResponses().size() == 2);
  }

  @Test
  public void shouldOutputSampleScriptResponseSet()
      throws IOException {
    TCSResponseSet responseSet = new TCSResponseSet();

    ScriptResponse response = new ScriptResponse();
    response.setId("test.tcs");
    response.setParsingSuccessful(true);

    TransportResponse transportResponse = new TransportResponse();
    transportResponse.setId("test.tcs");
    transportResponse.setOrderName("TOrder-0003");
    transportResponse.setExecutionSuccessful(true);

    response.getTransports().add(transportResponse);

    transportResponse = new TransportResponse();
    transportResponse.setId("test.tcs");
    transportResponse.setOrderName("TOrder-0004");
    transportResponse.setExecutionSuccessful(true);

    response.getTransports().add(transportResponse);

    responseSet.getResponses().add(response);

    StringWriter writer = new StringWriter();
    responseSet.toXml(writer);
    String xmlOutput = writer.toString();

    LOG.info(xmlOutput);

    StringReader reader = new StringReader(xmlOutput);
    TCSResponseSet parsedResponseSet = TCSResponseSet.fromXml(reader);
    assertTrue("parsed set should have exactly one message",
               parsedResponseSet.getResponses().size() == 1);
  }

}
