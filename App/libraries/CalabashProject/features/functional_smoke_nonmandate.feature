@nonmandate
Feature: High-level functional smoke test under non-mandate
	Conducts a quick driving test and submits logs
	
Scenario: quick driving test under non-mandate
	When I turn on engine
	Then I wait for 15 seconds
	When I send bt discover to ELD
	When I try to activate KMB
	When I accept EULA
    When I dismiss Safety Warning
    When I login with user name "smokenm" and password "aaaaaa"
    Then I should see Select Duty Status screen
    Given On-Duty Not Driving is selected
    When I click Submit Status button
	When I click No in missing logs prompt
	When I enter Trailer Number "t1" and Shipment Info "s1"
	Given I press the "OK" button
	When I choose Cancel on Odometer Calibration screen
	Then I should see RODS
	Then I wait for 15 seconds
	Then I should see EOBR Status as Online
	When I choose Diagnostics menu item "Set EOBR Config"
	Then I should see EOBR Configuration screen
	Given Unit Number on EOBR Configuration is the connected ELD
	When I choose Engine Data Bus "J1850 VPW (GMC)"
	Given I press the "OK" button	
	Then I should see RODS
	When I choose Diagnostics menu item "Odometer Calibration"
	Then I should see Odometer Calibration screen
	Given I press the "OK" button	
	Then I should see RODS
	Then I should see Current Status as On-Duty (US70)
	When I go to Employee Rules screen
	Then I should see Ruleset is "US 8 Day/70 Hour"
	When I choose Download
	When I choose menu item Done
	Then I should see RODS
	Then I should see Current Status as On-Duty (US70)
	When I choose Vehicle Inspection icon
	When I choose menu item New Pre-Trip Inspection
	Given Tractor Number on Vehicle Inspection is the connected ELD
	Given No Defects checkbox is checked
	When I choose menu item Submit
	Then I should see RODS
	When I start driving vss 100
	Then I should see Clocks
	Then I wait for 120 seconds
	When I stop driving
	Then I should see RODS
	When I turn off engine
	Then I should see RODS
	When I logout with Submitting Logs

Scenario: Login again and upload diagnostics
	When I turn on engine
	Then I wait for 15 seconds
	When I send bt discover to ELD
    When I dismiss Safety Warning
    When I login with user name "smokenm" and password "aaaaaa"
    Then I should see Select Duty Status screen
    Given On-Duty Not Driving is selected
    When I click Submit Status button
	When I click No in missing logs prompt
	When I enter Trailer Number "t1" and Shipment Info "s1"
	Given I press the "OK" button
	When I choose Cancel on Odometer Calibration screen if it appears
	Then I should see RODS
	Then I wait for 15 seconds
	Then I should see EOBR Status as Online
	When I choose Diagnostics menu item "Upload Diagnostic Info"
	When I upload diagnostics