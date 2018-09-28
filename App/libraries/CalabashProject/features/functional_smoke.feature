Feature: High-level functional smoke test
	Conducts a quick driving test and submit logs
	
@functional_smoke
Scenario: quick driving test
	When I turn on engine
	Then I wait for 15 seconds
	When I send bt discover to ELD
	When I try to activate KMB
	When I accept EULA
    When I dismiss Safety Warning
    When I login with user name "smoke1" and password "aaaaaa"
    Then I should see Select Duty Status screen
    Given On-Duty Not Driving is selected
    When I click Submit Status button
	When I click No in missing logs prompt
	When I enter Trailer Number "t1" and Shipment Info "s1"
	Given Unit Number is the connected ELD
	Given I press the "OK" button
	When I choose Cancel on Odometer Calibration screen
	Then dismiss Account Configuration Message if it appears
	Then I claim all events on Unidentified ELD Events screen if it appears
	Then I should see RODS
	Then I wait for 15 seconds
	Then I should see ELD Status as Online
	When I choose Diagnostics menu item "Set ELD Config"
	Then I should see ELD Configuration screen
	Given Unit Number on EOBR Configuration is the connected ELD
	When I choose Engine Data Bus "J1850 VPW (GMC)"
	Given I press the "OK" button	
	Then I should see RODS
	When I choose Diagnostics menu item "Odometer Calibration"
	Then I should see Odometer Calibration screen
	Given I press the "OK" button	
	Then I should see RODS
	When I start driving vss 100
	Then I should see Clocks
	Then I wait for 120 seconds
	When I stop driving
	Then I should see RODS
	When I turn off engine
	Then I should see RODS
	Then I perform a Data Transfer via Web Services with comment "smoke test" and certify logs	
	When I logout with Submitting Logs under mandate
	Then I should see Certify Logs screen
#	Defect 54663 prevents Certify Logs step from working here.  Workaround is to wait.
#	When I certify first log
	Then I wait for 45 seconds
	