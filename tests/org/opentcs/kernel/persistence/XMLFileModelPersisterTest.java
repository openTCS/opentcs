/*
 *
 * Created on June 12, 2006, 5:15 PM
 */

package org.opentcs.kernel.persistence;

import java.io.IOException;
import org.junit.*;
import static org.junit.Assert.*;
import org.opentcs.TestEnvironment;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.kernel.workingset.Model;
import org.opentcs.kernel.workingset.TCSObjectPool;
import org.opentcs.util.ModelGenerator;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class XMLFileModelPersisterTest{
  /**
   * The name of the test model.
   */
  private static final String modelName = "Testmodel";
  /**
   * The object pool backing the test model.
   */
  private TCSObjectPool globalPool;
  /**
   * The test model.
   */
  private Model testModel;
  /**
   * The persister instance for testing.
   */
  private XMLFileModelPersister persister;
  
  /** {@inheritDoc} */
  @Before
  public void setUp() {
    globalPool = new TCSObjectPool();
    testModel = new Model(globalPool);
    persister = new XMLFileModelPersister(TestEnvironment.getKernelHomeDirectory());
  }
  
  /** {@inheritDoc} */
  @After
  public void tearDown() {
    persister = null;
    testModel = null;
    globalPool = null;
  }
  
  /**
   * Test saving a simple model.
   */
  @Test
  public void testSaveModel() {
    Point point1 = testModel.createPoint(null);
    Point point2 = testModel.createPoint(null);
    Point point3 = testModel.createPoint(null);
    Path path1 = testModel.createPath(
          null, point1.getReference(), point2.getReference());
    Path path2 = testModel.createPath(
          null, point2.getReference(), point3.getReference());
    Path path3 = testModel.createPath(
          null, point3.getReference(), point1.getReference());
    testModel.addPointOutgoingPath(point1.getReference(), path1.getReference());
    testModel.addPointIncomingPath(point1.getReference(), path3.getReference());
    testModel.addPointOutgoingPath(point2.getReference(), path2.getReference());
    testModel.addPointIncomingPath(point2.getReference(), path1.getReference());
    testModel.addPointOutgoingPath(point3.getReference(), path3.getReference());
    testModel.addPointIncomingPath(point3.getReference(), path2.getReference());
    Vehicle vehicle1 = testModel.createVehicle(null);
    try {
      persister.saveModel(testModel, modelName, true);
    }
    catch (IOException exc) {
      fail("IOException trying to save model: " + exc.getMessage());
    }
    try {
      persister.saveModel(testModel, modelName, false);
      fail("Should raise an IOException not being allowed to overwrite");
    }
    catch (IOException exc) {
      assertTrue(true);
    }
  }
  
  /**
   *
   */
  @Test
  public void testSaveCircularGridModel()
  throws IOException {
    Model myModel = ModelGenerator.getCircularGridModel(5, 5);
    persister.saveModel(myModel, "CircularGridModel", true);
  }
}
