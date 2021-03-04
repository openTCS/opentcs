/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.xmlorders.binding;

import org.junit.*;
import static org.junit.Assert.assertTrue;
import org.opentcs.kernel.xmlhost.orders.binding.ScriptResponse;
import org.opentcs.kernel.xmlhost.orders.binding.TCSResponseSet;
import org.opentcs.kernel.xmlhost.orders.binding.TransportResponse;
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
  public void shouldOutputSampleResponseSet() {
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

    String xmlOutput = responseSet.toXml();

    LOG.info(xmlOutput);

    TCSResponseSet parsedResponseSet = TCSResponseSet.fromXml(xmlOutput);
    assertTrue("parsed set should have exactly two messages",
               parsedResponseSet.getResponses().size() == 2);
  }

  @Test
  public void shouldOutputSampleScriptResponseSet() {
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

    String xmlOutput = responseSet.toXml();

    LOG.info(xmlOutput);

    TCSResponseSet parsedResponseSet = TCSResponseSet.fromXml(xmlOutput);
    assertTrue("parsed set should have exactly one message",
               parsedResponseSet.getResponses().size() == 1);
  }

}
