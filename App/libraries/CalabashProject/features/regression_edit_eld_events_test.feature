@EditEvents
Feature: Add/Edit/Delete ELD Events while utilizing KMB with the ELD Mandate toggle ON. 
    Verify the events behave correctly during all types of edits.

Scenario: Test Editing ELD Events
# 1.	Install KMB
    When I turn on engine
	Then I wait for 15 seconds
	When I send bt discover to ELD
# 2.	Launch and activate KMB using activation code code
	When I try to activate KMB
# 3.	Click Accept on the EULA
	When I accept EULA
# 4.	Click OK on the Safety warning
    When I dismiss Safety Warning
# 5.	Click Feature Toggles button
#	Then I choose Feature Toggles button
# 6.	Ensure ELD Mandate is ON
#	Then I select "ELD Mandate"
# 7.	Click OK
#	When I press the OK button on Feature Toggles screen
# 8.	Click OK when told it was successfully completed
#	Then I press OK on Successfully completed message
# 9.	Enter username in the User Name field
# 10.	Enter password in the Password field
# 11.	Click Login – Solo Driver
    When I login with user name "editevents" and password "aaaaaa"
	Then I dismiss Application Update message if it appears
# 12.	Set initial duty status to Off Duty
	Then I should see Select Duty Status screen
#	Then I select an initial duty status of "Off Duty"
# 13.	Set current duty status by clicking radio button for Sleeper Berth
	Then I select a current duty status of "Sleeper Berth"
# 14.	Click Submit Status button
	When I click Submit Status button
# 15.	Click No when prompted to create an off duty log
	When I click No in missing logs prompt
# 16.	Enter ELD Trailer for Trailer Number
# 17.	Enter Shipment of ELDs for Shipment Info
	When I enter Trailer Number "ELD Trailer" and Shipment Info "Shipment of ELDs"
# 18.	Enter ELD Unit for Unit Number
	Given Unit Number is the connected ELD
# 19.	Click OK
	Given I press the "OK" button
# 20.	Proceed to RODS
    When I choose Cancel on Odometer Calibration screen
	Then dismiss Account Configuration Message if it appears
	Then I dismiss the Unidentified ELD Events screen if it appears
	Then I dismiss the Certify Logs prompt on RODS if it appears
	Then I should see RODS
    Then I wait for 15 seconds
	Then I should see ELD Status as Online
# 21.	Go to View Log > View Hours
	When I choose View Log icon
	When I choose menu item View Hours 
# 22.	Verify Weekly Hours shows 55 hours
	Then I should see Weekly Hours Used are "5:00:00"
	Then I should see Weekly Hours Available are "55:00:00"
# 23.	Verify Duty Hours shows 14 hours
	Then I should see Duty Hours Used are "0:00:00"
	Then I should see Duty Hours Available are "14:00:00"
# 24.	Verify Drive Hours shows 11 hours
	Then I should see Drive Hours Used are "0:00:00"
	Then I should see Drive Hours Available are "11:00:00"
# 25.	Verify Break hours shows 8 hours
	Then I should see Rest Break Hours Used are "0:00:00"
	Then I should see Rest Break Hours Available are "8:00:00"
# 26.	Click Edit Log
	When I choose menu item Edit Log
# 27.	Click Edit for the Sleeper Berth event today
	Then I click the Edit button for "Sleeper Berth" Status "1"
	Then I wait for 3 seconds
# 28.	Attempt to leave fields blank > Verify Error
	# a.	Click Location field and blank it out
	When I set the event Location to "x"
	When I set the event Location to ""
	# b.	Click Unit Number field and blank it out
	When I set the event Unit Number to ""
	# c.	Click Trailer Number field and blank it out
	When I set the event Trailer Number to ""
	# d.	Click Shipment Info field and blank it out
	When I set the event Shipment Information to ""
	# e.	Click Apply
	When I choose menu item Apply
	# f.	Verify the following Error Messages:
		# i.	Location is required. <- this isn't appearing
		# ii.	Unit Number is required.
		# iii.	Trailer Number is required.
		# iv.	Shipment Info is required.
		# v.	Driver’s Annotation must be at least 4 characters long.
	#Given I see the error text "Location is required. Unit Number is required. Trailer Number is required. Shipment Info is required. Driver's Annotation must be at least four characters long." on the Edit ELD Event screen
	# As of PBI 54372, Unit Number, Trailer Number, and Shipment Info are no longer required fields.
	#Given I see the error text "Location is required. Driver's Annotation must be at least four characters long." on the Edit ELD Event screen
	Given I see the error text "Location must be at least 5 characters long. Driver's Annotation must be at least 4 characters long." on the Edit ELD Event screen
# 29.	Click Cancel
	When I choose menu item Cancel
# 30.	Attempt to set End Time prior to start time > Verify Error
	# End time is not editable on the current event
	Given I see the End Time is disabled
# 31.	Attempt to set Start Time later > Verify Error
	# a.	Set Start Time ahead slightly
	When I set the Start Time later
	When I set the event Location to "Fail Location"
	# b.	Enter Fail in Annotation field
	When I set the event Drivers Annotation to "Fail"
	# c.	Click Apply
	When I choose menu item Apply
	# d.	Verify error message appears: Start Time can only be changed to an earlier time.
	Given I see the error text "Start Time can only be changed to an earlier time." on the Edit ELD Event screen
# 32.	Verify End Time is not editable 
	# See Step #30
	When I choose menu item Cancel
# 33.	Attempt to set annotation to less than 4 characters > Verify Error
	# a.	Enter No in the Annotation field
	When I set the event Drivers Annotation to "No"
	When I set the event Location to "Fail Annotation"
	# b.	Click Apply
	When I choose menu item Apply
	# c.	Verify error message appears: Driver’s Annotation must be at least four characters long.
	Given I see the error text "Driver's Annotation must be at least 4 characters long." on the Edit ELD Event screen
# 34.	Set Status to On Duty, and all other fields to acceptable values
	# a.	Set Status dropdown to On-Duty Not Driving
	When I set the event Duty Status to "On-Duty Not Driving"
	#a2.	Set the time to be 5 minutes earlier
	When I set the Start Time earlier

	# b.	Set Location to Edited Location
	When I set the event Location to "Edited Location"
	# c.	Set Unit Number to Edited Unit
	When I set the event Unit Number to "Edited Unit"
	# d.	Set Trailer Number to Edited Trailer
	When I set the event Trailer Number to "Edited Trailer"
	# e.	Set Shipment Info to Edited Shipment
	When I set the event Shipment Information to "Edited Shipment"
	# f.	Set Annotation to Edited
	When I set the event Drivers Annotation to "Edited"
	# g.	Click Apply
	When I choose menu item Apply
	# h.	Verify success message appears: You have successfully changed your log.
	Given I see the success text "You have successfully changed your log." on the Edit ELD Event screen

	# verify the fields are what they are supposed to be
	Given I see the event Location is "Edited Location"
	Given I see the event Unit Number is "Edited Unit"
	Given I see the event Trailer Number is "Edited Trailer"
	Given I see the event Shipment Information is "Edited Shipment"
	Given I see the event Drivers Annotation is "Edited"
	
# 35.	Click Done 
	When I choose menu item Done
	Then I wait for 3 seconds
# 36.	Click Done again to return to RODS
	When I choose menu item Done
# 37.	Verify current duty status is On Duty, and the start time is accurate
	# a.	Verify current duty status is On-Duty (US60)
	Then I should see Current Status as On-Duty (US60)
# Store this value for later calculations
	Then I remember my last edited event time for later
	# b.	Verify current duty status start time is what was expected
	Given I see the time of current Duty Status is what I set it to when I edited my last ELD Event
	# Need to get the time offset for accurate calculations. Must be at RODS to start this
	When I calculate the time offset
# 38.	Click View Log
	When I choose View Log icon
# 39.	Click View Hours
	When I choose menu item View Hours
# 40.	Verify the clocks are what you expect them to be
	Then I calculate seconds since my last Edited Event
	# a.	Verify Weekly hours used = 5 + Time since logging in
	Given I see my Weekly Hours Used are accurately displaying for my Edit ELD Event Scenario
	# b.	Verify Weekly hours available = (60 – Used)
	Given I see my Weekly Hours Available are accurately displaying for my Edit ELD Event Scenario
	# c.	Verify Duty hours used = Time Since logging in
	Given I see my Duty Hours Used are accurately displaying for my Edit ELD Event Scenario
	# d.	Verify Duty hours available = 14 – Used
	Given I see my Duty Hours Available are accurately displaying for my Edit ELD Event Scenario
	# e.	Verify Drive hours used = 0
	Given I see my Drive Hours Used are accurately displaying for my Edit ELD Event Scenario
	# f.	Verify Drive hours available = 11
	Given I see my Drive Hours Available are accurately displaying for my Edit ELD Event Scenario
	# g.	Verify Break hours used = Time since logging in
	Given I see my Rest Break Hours Used are accurately displaying for my Edit ELD Event Scenario
	# h.	Verify Break hours available = 8 - Used
	Given I see my Rest Break Hours Available are accurately displaying for my Edit ELD Event Scenario
# 41.	Verify the KMB DB, for today’s log, has 3 duty status events: Off Duty at midnight, Sleeper at login (inactive), and On Duty at login (active)
	# a.	Need to attack this via command line
# 42.	Click Edit Log
	When I choose menu item Edit Log
# 43.	Click the < button to choose the log for yesterday
	When I switch to the previous days log
# 44.	Click Edit for the event prior to the driving event (8AM On-Duty Not Driving)
	Then I click the Edit button for "On-Duty Not Driving" Status "1"
# 45.	Change the End Time to be 9:15AM, shortening the driving time
	Then I set the End Time of the event to "9:15:00 AM"
	When I set the event Location to "Shorten Driving"
# 46.	Enter Shorten Drive in the Annotation
	When I set the event Drivers Annotation to "Shorten Drive"
# 47.	Click Apply
	When I choose menu item Apply
# 48.	Verify an error message appears
	Given I see the error text "Change not allowed. Performing this edit would overwrite automatically generated driving time." on the Edit ELD Event screen
# 49.	Click Cancel
	When I choose menu item Cancel
	When I choose menu item Done
# 50.	Click Edit for the 9AM Driving event
	Then I click the Edit button for "Driving" Status "1"
# 51.	Change the End Time to be 10:30AM
	Then I set the End Time of the event to "10:30:00 AM"
	When I set the event Location to "Error Location"
# 52.	Enter Error in the Annotation
	When I set the event Drivers Annotation to "Error"
# 53.	Click Apply
	When I choose menu item Apply
# 54.	Verify an error message appears
	Given I see the error text "End Time can only be changed to a later time." on the Edit ELD Event screen
# 55.	Change the End Time to be 11:15AM
	Then I set the End Time of the event to "11:15:00 AM"
	When I set the event Location to "OK Location"
# 56.	Enter Success in the Annotation
	When I set the event Drivers Annotation to "Success"
# 57.	Click Apply
	When I choose menu item Apply
# 58.	Verify success message appears
	Given I see the success text "You have successfully changed your log." on the Edit ELD Event screen
	When I choose menu item Done
# 59.	Click View Hours
	# Need to re-calculate these values before going to the View Hours screen so it is accurate
	Then I calculate seconds since my remmebered value
	When I choose menu item View Hours
# 60.	Verify the hours reflect the correct values
	# a.	Nothing will have changed
	Given I see my Weekly Hours Used are accurately displaying for my Edit ELD Event Scenario
	Given I see my Weekly Hours Available are accurately displaying for my Edit ELD Event Scenario
	Given I see my Duty Hours Used are accurately displaying for my Edit ELD Event Scenario
	Given I see my Duty Hours Available are accurately displaying for my Edit ELD Event Scenario
	Given I see my Drive Hours Used are accurately displaying for my Edit ELD Event Scenario
	Given I see my Drive Hours Available are accurately displaying for my Edit ELD Event Scenario
	Given I see my Rest Break Hours Used are accurately displaying for my Edit ELD Event Scenario
	Given I see my Rest Break Hours Available are accurately displaying for my Edit ELD Event Scenario
# 61.	Verify in the KMB DB that the log for yesterday has a new Driving event with the new start/end times, an inactive Driving event with the old start/end times, a new post-driving event with updated start/end times, and an inactive post-driving event with the original start/end times.
# 62.	Click Done to return to RODS
	When I choose menu item Done
# 62.a 	Verify Database
	# i.	Copy DB to SD Card
		When I choose Diagnostics menu item "Upload Diagnostic Info"
		Given I press the "Copy Files to SD Card" button
		When I choose menu item Done
	# ii.	Pull DB from device and save it to database files folder
		When I pull the db from my device as "eldEvents"
	# iii.	Run the db script to create the output .csv
		When I run the db script "query_eldEvents.txt" against the db named "eldEvents"
	# iv.	Compare actual DB to expected values
		Given I see the correct events for my Edit ELD Events scenario

# 63.	Click Log Off
	When I choose Log Off icon
	Then I should see Log Off screen
	Then I dismiss the Review Unidentified Events prompt if it appears
# 64.	Choose Off Duty as the Duty Status upon Logout

# 65.	Click Submit
	Given I press the "Submit" button
# 66.	Verify prompted to certify both logs (today an yesterday)
	Then I should see Certify Logs screen
# 67.	Check the boxes for both days
# 68.	Click Submit
	When I certify all logs
	When I turn off engine




# Encompass Steps
# 69.	Go to the Encompass Login page
# 70.	Ensure the ELD Mandate feature toggle is ON
# 71.	Login to Encompass as username/password
# 72.	Click Driver Management
# 73.	Click Hours of Service
# 74.	Click All active employees
# 75.	Click Driver Name
# 76.	Click the Log Date for Today
# 77.	Click View All ELD Events
# 78.	Verify all the following ELD Events exist
	# a.	12AM Off Duty
	# b.	XX On Duty at Login (active)
	# c.	XX Sleeper at Login (inactive)
	# d.	XX Login event
	# e.	XX Certify event
	# f.	XX Logout event
	# g.	XX Off Duty at Logout (active)
# 79.	Click Hours of Service
# 80.	Click All active employees
# 81.	Click Driver Name
# 82.	Click the Log Date for Yesterday
# 83.	Click View All ELD Events
# 84.	Verify all the following ELD Events exist
	# a.	12 AM Off Duty
	# b.	8 AM On-Duty Not Driving
	# c.	9 AM Driving (inactive)
	# d.	9 AM Driving
	# e.	11 AM On-Duty Not Driving (inactive)
	# f.	11:15 AM On-Duty Not Driving
	# g.	1 PM Off Duty


