/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.storage;

import com.google.inject.Guice;
import com.google.inject.Injector;
import java.io.File;
import java.io.IOException;
import org.jbehave.core.annotations.AfterScenario;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.opentcs.access.LocalKernel;
import org.opentcs.guing.application.ApplicationInjectionModule;
import org.opentcs.guing.exchange.adapter.PointAdapter;
import org.opentcs.guing.exchange.adapter.ProcessAdapter;
import org.opentcs.guing.model.ModelManager;
import org.opentcs.guing.model.elements.PointModel;
import static org.opentcs.guing.storage.OpenTCSModelManager.FILE_ENDING;
import static org.opentcs.guing.storage.OpenTCSModelManager.MODEL_DIRECTORY;
import org.opentcs.kernel.KernelInjectionModule;

/**
 *
 *
 * @author Philipp Seifert (Philipp.Seifert@iml.fraunhofer.de)
 */
public class ModelManagerSteps {

  private final String tempModelName = "test123";
  private ModelManager modelManager;
  private String actualUserDir;
  private LocalKernel kernel;

  @Given("a local kernel without extensions")
  public void givenALocalKernelWithoutExtensions() {
    Injector injector = Guice.createInjector(new KernelInjectionModule());
    kernel = injector.getInstance(LocalKernel.class);
    kernel.initialize();
  }

  @Given("a ModelManager")
  public void givenAModelManager() {
    Injector injector = Guice.createInjector(new ApplicationInjectionModule());
    modelManager = injector.getInstance(ModelManager.class);
  }
  
  @Given("the correct user.dir")
  public void givenATemporaryUserDir() throws IOException {
    actualUserDir = System.getProperty("user.dir");
    // ./build/tests-integration
    File srcCodeFile = new File(ModelManagerSteps.class.getProtectionDomain().getCodeSource().getLocation().getPath());
    // two times up in hierarchy returns project directory
    File projectDir = srcCodeFile.getParentFile().getParentFile();
    System.setProperty("user.dir", projectDir.getAbsolutePath());
  }
  
  @AfterScenario
  public void afterEachScenario() {
    File file
        = new File(System.getProperty("user.dir")
            + File.separator
            + MODEL_DIRECTORY
            + tempModelName
            + FILE_ENDING);
    if (file.exists()) {
       file.delete();
    }
    System.setProperty("user.dir", actualUserDir);
    modelManager.createEmptyModel();
  }

  @When("I save the model locally with name $name")
  public void saveTheModelLocally(String name) {
    modelManager.getModel().setName(name);
    modelManager.persistModel(false); 
  }

  @Then("a file named $name should exist.")
  public void aFileShouldExist(String fileName) {
    File file
        = new File(System.getProperty("user.dir")
            + File.separator
            + MODEL_DIRECTORY
            + fileName
            + FILE_ENDING);
    if (!file.exists()) {
      fail();
    }
  }
  
  @When("I create a new model") 
  public void createNewModel() {
    modelManager.createEmptyModel();
  }
  
  @When("I add one point")
  public void addOnePoint() {
    PointModel pointModel = new PointModel();
    pointModel.setName("someName");
    modelManager.getModel().getFolder(pointModel).add(pointModel);
    ProcessAdapter adapter = new PointAdapter(pointModel, 
        modelManager.getModel().getEventDispatcher());
    modelManager.getModel().getEventDispatcher().addProcessAdapter(adapter);
  }
  
  @When(value="I load $name", priority=0)
  public void loadModel(String modelName) {
    File file
        = new File(System.getProperty("user.dir")
            + File.separator
            + MODEL_DIRECTORY
            + modelName
            + FILE_ENDING);
    modelManager.loadModel(file);
  }
  
  @Then("the model should have one point.")
  public void shouldHaveOnePoint() {
    int i = modelManager.getModel().getPointModels().size();
    assertEquals(1, i);
  }
  
  @When("I persist the model in the kernel with name $name")
  public void persistInKernel(String name) {
    modelManager.getModel().setName(name);
    modelManager.persistModel(kernel);
  }
  
  @When(value="I load $name from the kernel", priority=1)
  public void loadFromKernel(String name) {
    modelManager.restoreModel(kernel);
  }
}
