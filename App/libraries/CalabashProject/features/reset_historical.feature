Feature: Reset EOBR Historical Data
	
@reset_historical
Scenario: Reset EOBR Historical Data
	When I send bt discover to ELD
	When I stop driving
	When I turn off engine
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
	Then I wait for 15 seconds
	Then I see the text "Detected unassigned driving periods"
	Given I press the "OK" button
	Then I see the text "EOBR smoke Online"
	When I choose menu item Menu
	When I choose menu item System Menu
	When I choose menu item File
	When I choose menu item Admin
	When I choose menu button Reset Eobr Historical Data
	When I reset EOBR historical data
	When I choose menu item Menu
	When I choose menu item Log Off
	Then I should see Log Off screen
	When I click Logout on Log Off screen
