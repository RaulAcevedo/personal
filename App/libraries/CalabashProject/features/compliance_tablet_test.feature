@ComplianceTablet
Feature: High-level test of the Compliance Tablet Features (ALK CoPilot and WiFi Settings)
	
Scenario: Verify WiFi Settings and ALK CoPilot Menus are present and Clocks appear
	# spin up the EOBR so that it is discoverable
	When I turn on engine
	Then I wait for 15 seconds
	When I send bt discover to ELD
	# activate KMB
	When I try to activate KMB
	When I accept EULA
    When I dismiss Safety Warning
	# Disable ELD Mandate
	When I choose Feature Toggle for nonmandate in Dev or QAS
	# Enable feature toggle for Force Compliance Tablet Mode
	When I choose Feature Toggle for Force Compliance Tablet Mode
	# Login as cttest
    When I login with user name "cttest" and password "a"
	Then I dismiss Application Update message if it appears
	# Select duty status of On Duty
	Given I press the "Submit Status" button
	When I click No in missing logs prompt
	When I enter Trailer Number "t1" and Shipment Info "s1"
	Given I press the "OK" button
	When I choose Cancel on Odometer Calibration screen if it appears
	Then I should see RODS
	Then I wait for 15 seconds
	Then I should see EOBR Status as Online
	# Check for "ALK CoPilot" and "WiFi Settings" menus
	When I choose menu item Menu
	Given "ALK CoPilot" is present under Menu
	Given "WiFi Settings" is present under Menu
	# Check that the device is a Compliance Tablet
	When I choose Diagnostics menu item "App Settings"
	When I scroll down to the bottom of the App Settings screen
	Given I see the label "Compliance Tablet"
	Given I see "Yes" under Compliance Tablet
	When I choose menu item Done
	# Check that WiFi is currently showing as enabled
	When I choose menu item Menu
	When I choose menu item WiFi Settings
	Given I see a checkbox for Enable WiFi
	Given I see WiFi is enabled
	When I choose menu item Done
	# Check that the ALK CoPilot clocks appear when driving
	When I start driving vss 100
	Given I see the ALK CoPilot clocks
	When I stop driving
	When I turn off engine
	Then I wait for 15 seconds
	Then I should see RODS
	When I Logout without Submitting Logs
