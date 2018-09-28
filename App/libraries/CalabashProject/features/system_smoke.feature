Feature: High-level system availability smoke test
	Confirms KMB can be installed and communicate with Encompass
	
@system_smoke
Scenario: High-level system availability smoke test.
	Given I go to activation screen
	When I try to activate KMB
	When I accept EULA
	Then I should see Safety Information Warning screen
	When I dismiss Safety Warning
	Then I should see Login screen