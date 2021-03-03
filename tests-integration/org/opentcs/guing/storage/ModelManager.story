Model Manager

Meta
@component OpenTCSModelManager

Narrative:
As an openTCS user
I want to save and load models

Lifecycle:
Before:
Given a ModelManager
Given the correct user.dir
Given a local kernel without extensions

Scenario: Save model locally

When I save the model locally with name test123
Then a file named test123 should exist.

Scenario: Load model locally

When I create a new model
And I add one point
And I save the model locally with name test123
And I create a new model
And I load test123
Then the model should have one point.

//Scenario: Save/load model in Kernel
//When I create a new model
//And I add one point
//And I persist the model in the kernel with name test123
//And I create a new model
//And I load test123 from the kernel
//Then the model should have one point.
