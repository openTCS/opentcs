Kernel States

Meta:
@component kernel

Narrative:
As a openTCS user
I want to change the kernel's state
So that I am able to perform the operations that are permitted in the current state

Lifecycle:
Before:
Given a local kernel without extensions

Scenario: Change kernel State

When I change the kernel's state to <newState>
Then the kernel's reported state should be <actualState>

Examples:
| newState  | actualState |
| MODELLING | MODELLING   |
| OPERATING | OPERATING   |
| SHUTDOWN  | SHUTDOWN    |

Scenario: Created point exists in modelling mode

When I change the kernel's state to MODELLING 
And I create a point named SomePointName
Then the kernel should have a point named SomePointName

Scenario: Created point exists in operating mode

When I change the kernel's state to MODELLING
And I create a point named SomePointName
And I change the kernel's state to OPERATING
Then the kernel should have a point named SomePointName

Scenario: Create point with duplicate name

When I change the kernel's state to MODELLING
And I create a point named SomePointName
Then creating a point named SomePointName should fail with an exception

Scenario: Create point in operating mode

When I change the kernel's state to OPERATING
Then creating a point named SomePointName should fail with an exception

Scenario: Kernel reports correct vehicle driver state

Given a minimal model
And a vehicle named Racer
And a vehicle driver associated with vehicle Racer
When the vehicle driver for vehicle Racer reports state ERROR
Then the state of vehicle Racer as returned by the kernel should be ERROR
