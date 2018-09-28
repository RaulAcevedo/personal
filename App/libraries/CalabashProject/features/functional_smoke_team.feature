@team	
Feature: High-level smoke test for team driving

Scenario: team driving
	When I turn on engine
	Then I wait for 15 seconds
	When I send bt discover to ELD
	When I try to activate KMB
 	When I accept EULA
    When I dismiss Safety Warning
    When I login team driver "DriverA" and password "aaaaaa"
	Then I select Shared Device on Device Type screen
    Then I should see Select Duty Status screen
    Given On-Duty Not Driving is selected
    When I click Submit Status button
	When I click No in missing logs prompt
	When I enter Trailer Number "Trip for a" and Shipment Info "Trip for a"
	Given I press the "OK" button
	When I choose Cancel on Odometer Calibration screen
	Then I select Driver 2 Login Button
    When I dismiss Safety Warning
    Then I should see Login screen
    When I login team driver 2 "DriverB" and password "aaaaaa"
    Then I should see Select Duty Status screen
    Given On-Duty Not Driving is selected
    When I click Submit Status button
	When I click No in missing logs prompt
	When I enter Trailer Number "Trip for b" and Shipment Info "Trip for b"
	Given I press the "OK" button
	Given I am asked who will be the first to drive
	Then I choose "DriverB, DriverB"
	Then I should see RODS
	Then I wait for 15 seconds
	Then I should see EOBR Status as Online
	Then I see designated driver is "DriverB, DriverB"
	Then I see shared device icon
	Then I see designated driver icon
	When I choose View Log icon
	When I choose menu item View Trip Info
	Then I see Trailer Number "Trip for b" and Shipment Info "Trip for b"
	Then I click Done button
	Then I should see RODS
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
	When I start driving vss 100
	Then I should see Clocks
	Then I wait for 120 seconds
	When I stop driving
	Then I should see RODS
	When I turn off engine
	Then I should see RODS
	When I switch to driver "DriverA, DriverA" and assume driving
	Then I should see RODS
	Then I see designated driver is "DriverA, DriverA"
	Then I see shared device icon
	Then I see designated driver icon
	When I choose View Log icon
	When I choose menu item View Trip Info
	Then I see Trailer Number "Trip for a" and Shipment Info "Trip for a"
	Then I click Done button
	Then I should see RODS
	When I turn on engine
	Then I wait for 15 seconds
	When I start driving vss 100
	Then I should see Clocks
	Then I wait for 120 seconds
	When I stop driving
	Then I should see RODS
	When I turn off engine
	Then I should see RODS
	Then I should see Current Status contains "On-Duty"
	When I choose Log Off icon
	Then I should see Log Off screen
	When I logout first team driver with Submitting Logs
	Then I should see RODS
	Then I see designated driver is "DriverB, DriverB"
	Then I see designated driver icon
	Then I see the add driver icon
	When I logout with Submitting Logs
