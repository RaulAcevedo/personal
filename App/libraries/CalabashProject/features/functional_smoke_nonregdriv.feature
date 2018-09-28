@nonregdriv
Feature: High-level functional smoke test for non-regulated driving
	Conducts a quick driving test and submits logs
	
Scenario: Verify Non-Reg driving settings for employee with rule for non-reg driving disabled
	When I turn on engine
	Then I wait for 15 seconds
	When I send bt discover to ELD
	When I try to activate KMB
	When I accept EULA
    When I dismiss Safety Warning
    When I login with user name "smokeb" and password "aaaaaa"
    Then I should see Select Duty Status screen
    Given On-Duty Not Driving is selected
    When I click Submit Status button
	Then I click No in missing logs prompt
	When I enter Trailer Number "t1" and Shipment Info "s1"
	Given I press the "OK" button
	When I choose Cancel on Odometer Calibration screen
	Then I should see RODS
	Then I wait for 15 seconds
	Then I should see EOBR Status as Online
	When I go to Employee Rules screen
	Then I should see "Non-Regulated Driving Allowed" is disabled
	When I choose menu item Done
	Then I should see RODS
	When I go to New Status screen
	When I choose Status "Off Duty"
	Then I should not see "Utilize Non-Regulated Driving Functionality?" checkbox
	Given I press the "Cancel" button
	Then I should see RODS
	Then I should see EOBR Status as Online
	When I Logout without Submitting Logs
	
Scenario: Verify use of non-regulated driving	
	When I send bt discover to ELD
    When I dismiss Safety Warning
    When I login with user name "smokea" and password "aaaaaa"
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
	When I go to Employee Rules screen
	Then I should see "Non-Regulated Driving Allowed" is enabled
	When I choose menu item Done
	Then I should see RODS
	When I go to New Status screen
	When I choose Status "Sleeper Berth"
	Then I should not see "Utilize Non-Regulated Driving Functionality?" checkbox
	When I choose Status "On-Duty Not Driving"
	Then I should see "Utilize Non-Regulated Driving Functionality?" checkbox
	When I enable "Utilize Non-Regulated Driving Functionality?"
	Given I press the "OK" button 
	Then I should see RODS
	Then I should see Current Status as "On-Duty - NonReg"
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
	Then I see the text "You are currently operating this vehicle under Non-Regulated Driving functionality"
	Then I wait for 120 seconds
	When I stop driving
	Then I should see RODS
	Then I should see Current Status as "On-Duty - NonReg"
	When I turn off engine
	When I end Non-Regulated Driving
	Then I should see RODS
	Then I view Current Status after stopping Non-Reg driving
	When I choose View Log icon
	When I choose menu item View Hours 
	Then I should see Drive Hours Used are "0:00"
	When I choose menu item View Log Remarks 
	Then I should see a Log Remark "Non-Regulated started at"
	Then I click Done button
	Then I should see RODS
	When I logout with Submitting Logs
	
Scenario: Login again and upload diagnostics
	When I turn on engine
	Then I wait for 15 seconds
	When I send bt discover to ELD
    When I dismiss Safety Warning
    When I login with user name "smokea" and password "aaaaaa"
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
