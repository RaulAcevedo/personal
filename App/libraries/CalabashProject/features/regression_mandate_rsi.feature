@RSI
Feature: Verify RSI Mode displays accurate and correct information with ELD mandate ON 

Scenario: Test RSI mode
# Begin Test
 #   Then I get user input
	When I turn on engine
	Then I wait for 30 seconds
	When I send bt discover to ELD
     # activate
	When I try to activate KMB
 	When I accept EULA
    When I dismiss Safety Warning
     # set ELD Mandate to ON > This should be done automatically by selective feature toggles
      # Login as Team Driver #1
    When I login team driver "rsiTeam" and password "bbbbbb"
    Then I dismiss Application Update message if it appears
	Then I select Shared Device on Device Type screen
    Then I should see Select Duty Status screen
     # Set duty status to On Duty
    Given On-Duty Not Driving is selected
     # Click Submit Status
    When I click Submit Status button
     # Choose "No" to creating Off Duty Logs
	When I click No in missing logs prompt
     # Set Trailer Number to "TD 1 Trailer"
     # Set Shipment Info to "TD 1 Shipment"
     # Unit Number is pre-populated
	When I enter Trailer Number "TD1 Trailer" and Shipment Info "TD1 Shipment"
     # Click OK
	Given I press the "OK" button
     # Dismiss Odometer Calibration menu
	When I choose Cancel on Odometer Calibration screen if it appears
     # Click Driver #2 Login
	Then I select Driver 2 Login Button
    When I dismiss Safety Warning
    Then I should see Login screen
     # Login as TD2
    When I login team driver 2 "rsiTest" and password "aaaaaa"
    Then I should see Select Duty Status screen
     # Set Duty Status to On Duty
    Given On-Duty Not Driving is selected
     # Click Submit Status
    When I click Submit Status button
     # Choose "No" to creating Off Duty logs
	When I click No in missing logs prompt
     # Set Trailer Number to "TD2 Trailer"
     # Set the Shipment Info to "TD2 Shipment"
     # Set Unit Number to "TD2 Unit"
	When I enter Trailer Number "TD2 Trailer" and Shipment Info "TD2 Shipment"
     # Click OK
	Given I press the "OK" button
	Given I am asked who will be the first to drive
	Then I choose "Inspection, Roadside"
     # Dismiss account config message
    Then dismiss Account Configuration Message if it appears
    Then I dismiss the Unidentified ELD Events screen if it appears
	Then I dismiss the Certify Logs prompt on RODS if it appears
     # Verify Driver #2 is the active user
	Then I should see RODS
	Then I wait for 15 seconds
	Then I should see ELD Status as Online
	Then I see designated driver is "Inspection, Roadside"
	Then I see shared device icon
	Then I see designated driver icon
    # Verify RSI mode button is present
    Given I see the Roadside Inspection button
 # Click RSI button on RODS
    Then I click the Roadside Inspection button
 # Verify options for Data Transfer and Roadside Inspection Mode
    Given I see options for Data Transfer and Roadside Inspection Mode
 # CLick Roadside Inspection Mode
    Then I press the Roadside Inspection Mode button
 # Enter the password for TD1 (note: you are currently logged in as TD2)
    Then I enter "bbbbbb" as my logon password 
    Then I press the "OK" button
 # Verify you get an error message
    Given I see the error text "Confirmation password does not match." on the RSI mode login screen
# Click OK
    Then I dismiss the RSI login error prompt
 # Enter the password for TD2
    Then I enter "aaaaaa" as my logon password 
    Then I press the "OK" button
 # Verify you enter RSI moode
 # Verify Top Bar says "Roadside Inspection Mode ELD" and is Orange
    Given I see the ELD Mandate RSI Mode screen
 # Verify Today's date appears in the dropdown
    Given I see the date "today" is displayed in the dropdown
 # Verify data for today
    # Driver Section
    Given I see the "Driver Name" is "Inspection, Roadside"
    Given I see the "Driver ID" is "rsitest"
    Given I see the "License State" is "WI"
    Given I see the "Co-Driver" is "Team, Roadside"
    Given I see the "Co-Driver ID" is "rsiteam"
    Given I see the "Unidentified Driving Records" is "None"
    Given I see the "Exempt Driver Status" is "0"
    Given I see the "Time Zone" is "Central"
    Given I see the "Shipping ID" is "TD2 Shipment"
    # Vehicle Section
    Given I see the "Truck Vehicle ID" is "#MYELD#"
    Given I see the "Truck Vehicle VIN" is "-TestVIN"
    # ELD Section
    # ELD ID is pending > Given I see the "ELD ID" is "20A122"
   # Given I see the "ELD ID" is "Pending From FMCSA"
   # Given I see the "ELD ID" is "20A003"
    Given I see the ELD ID is not blank
    Given I see the "ELD Provider" is "J. J. Keller"
    Given I see the "ELD Malfunctions" is "0"
    #Given I see the "Data Diagnostic Status" is "0"
    # Other Section
    Given I see the "Date of Record" is "Today"
    Given I see the "24 Hour Start Time" is "Midnight"
    Given I see the "Carrier" is "1234 [KMB Automation ELD Mandate]"
    Given I see the "Date of Display" is "Today"
    # Grid Section
    Given I see the "Miles Today" is "0"
    # need to figure out how to do an accurate time until now
    #Given I see the "Total Time in Working Day So Far" is "Time Since Midnight"
    
#     # ELD Event list
# Click < arrow to view yesterday's log
    Then I click the "<" key to view the previous day
    Then I wait for 10 seconds
    Given I see the date "yesterday" is displayed in the dropdown
# Verify data displayed
    Then I verify my table data for RSI Scenario Team Driver 2
    #Then I get user input
    #Then I verify my table data for RSI Scenario Team Driver 1
    # Driver Section
    Given I see the "Driver Name" is "Inspection, Roadside"
    Given I see the "Driver ID" is "rsitest"
    Given I see the "License State" is "WI"
    Given I see the "Co-Driver" is ""
    Given I see the "Co-Driver ID" is ""
    Given I see the "Unidentified Driving Records" is "None"
    Given I see the "Exempt Driver Status" is "0"
    Given I see the "Time Zone" is "Central"
    Given I see the "Shipping ID" is "Fake Shipment"
    # Vehicle Section
    Given I see the "Truck Vehicle ID" is "Fake Tractor"
    Given I see the "Truck Vehicle VIN" is ""
    # ELD Section
    Given I see the "ELD ID" is "-"
    Given I see the "ELD Provider" is "J. J. Keller"
    Given I see the "ELD Malfunctions" is "0"
    # Other Section
    Given I see the "Date of Record" is "Yesterday"
    Given I see the "24 Hour Start Time" is "Midnight"
    Given I see the "Carrier" is "1234 [KMB Automation ELD Mandate]"
    Given I see the "Date of Display" is "Today"
    # Grid Section
    Given I see the "Miles Today" is "10"
    # ELD Event List
# Click Exit Roadside Inspection Mode
    Then I click the Exit RSI Mode button
    # enter my password
    Then I enter "aaaaaa" as my logon password
    Then I press the "OK" button
    Then I should see RODS
# Switch active users to TD1
    When I switch to driver "Team, Roadside" and assume driving
    Then dismiss Account Configuration Message if it appears
	Then I dismiss the Certify Logs prompt on RODS if it appears
# Click menu
    When I choose menu item Menu
# Click System Menu
    When I choose menu item System Menu
# Click File
    When I choose menu item File
# Click Roadside Inspection
    When I choose menu item Roadside Inspection
# Enter password for TD2 (note: you are currently logged in as TD1)
    Then I enter "aaaaaa" as my logon password 
    Then I press the "OK" button
# Verify you get an error message
    Given I see the error text "Confirmation password does not match." on the RSI mode login screen
# Click Cancel
    Then I dismiss the RSI login error prompt
    Then I press the "Cancel" button
# Verify you are returned to RODS
    Then I should see RODS
# Navigate to RSI mode via menu
    When I choose menu item Menu
    When I choose menu item System Menu
    When I choose menu item File
    When I choose menu item Roadside Inspection
    Then I enter "bbbbbb" as my logon password
    Then I press the "OK" button
# Verify you are on the RSI mode screen
    Given I see the ELD Mandate RSI Mode screen
    When I turn off engine
# End Test


#Current Defects
# Unidentified Driving Records is showing as "None" even though there are records
# ELD Event list > Ceritifcation events have the Event Type FIeld showing the Date
    # and the Origin field is showing the text Driver instead of a number
# ELD Event list > only Duty Status events have the Record Status field populated
# Truck Vehicle VIN is not populating with what's in Encompass if VIN is not valid/set in KMB
    # Unless you manually download employee rules, in which case the Encompass value is
    # prefixed with a hyphen (  -myvin12345   )
    # Note: This is AS DESIGNED, see PBI 59136: If VIN is VALID on ELD, it iwll be displayed.
        # If not, then we use a value form Encompass, prefixed with a Hypehn
