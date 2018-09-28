Then(/^I should see RODS$/) do
	sleep(10)
	#sometimes Retrieving data message appears here for awhile, or Saving message from Set EOBR Config
	if(element_exists("TextView id:'message'"))
		message = query("TextView id:'message'",:text).first
		print "Message exists: '#{message}'"
		wait_for(120) {element_does_not_exist("TextView id:'message'")}
		sleep(5)
		#check for a second message and wait for it to disappear
		if(element_exists("TextView id:'message'"))
			message = query("TextView id:'message'",:text).first
			print "Message exists: '#{message}'"
			wait_for(120) {element_does_not_exist("TextView id:'message'")}
		end
	end
	#sometimes GPS signal is lost, and Edit location screen appears. If so, enter a location.
	if(element_exists("TextView text:'Edit Location'"))
		touch("EditText id:'tvActualLocation'")
		keyboard_enter_text "Appleton, WI"
		hide_soft_keyboard
		print "Entered location on Edit Location screen"
		touch("button marked:'OK'")
	end
	wait_for(60) {element_exists("ImageButton id:'btnViewLog'")}
end

Then(/^dismiss Account Configuration Message if it appears$/) do
	wait_for_element_exists("TextView id:'message'")
	message = query("TextView id:'message'",:text).first
	if (message.include? "Your account has been configured")
		touch("button marked:'OK'")
	end
end

Then(/^I should see Current Status as "(.*?)"$/) do |status|
 	actual_property = query("TextView id:'tvDSHDutyStatus'",:text).first
 	unless actual_property == status
		fail "Duty Status incorrect. Expected '#{status}' but was '#{actual_property}'"
	end
end

Then(/^I should see Current Status as Off Duty \- PC \(US60\)$/) do
 	actual_property = query("TextView id:'tvDSHDutyStatus'",:text).first
 	unless actual_property == "Off Duty - PC (US60)"
		fail "Duty Status incorrect. Expected Off Duty - PC (US60) but was '#{actual_property}'"
	end
end

Then(/^I should see Current Status as Off Duty \(US60\)$/) do
 	actual_property = query("TextView id:'tvDSHDutyStatus'",:text).first
 	unless actual_property == "Off Duty (US60)"
		fail "Duty Status incorrect. Expected Off Duty (US60) but was '#{actual_property}'"
	end
end

Then(/^I should see Current Status as On\-Duty \- Hyrail \(US60\)$/) do
 	actual_property = query("TextView id:'tvDSHDutyStatus'",:text).first
 	unless actual_property == "On-Duty - Hyrail (US60)"
		fail "Duty Status incorrect. Expected On-Duty - Hyrail (US60) but was '#{actual_property}'"
	end
end

Then(/^I should see Current Status as On-Duty \(US60\)$/) do
 	actual_property = query("TextView id:'tvDSHDutyStatus'",:text).first
 	unless actual_property == "On-Duty (US60)"
		fail "Duty Status incorrect. Expected On-Duty (US60) but was '#{actual_property}'"
	end
end

Then(/^I should see Current Status as On-Duty \(US70\)$/) do
 	actual_property = query("TextView id:'tvDSHDutyStatus'",:text).first
 	unless actual_property == "On-Duty (US70)"
		fail "Duty Status incorrect. Expected On-Duty (US70) but was '#{actual_property}'"
	end
end

Then(/^I should see Current Status contains "(.*?)"$/) do |status|
 	actual_property = query("TextView id:'tvDSHDutyStatus'",:text).first
 	unless actual_property.include? status
		fail "Duty Status incorrect. Expected it to contain '#{status}' but was '#{actual_property}'"
	end
end

Then(/^I wait for PC message$/) do
	wait_for(360) {element_exists("TextView id:'message'")}
	message = query("TextView id:'message'", :text).first
	unless message == "Vehicle is being operated under authorized use of Personal Conveyance."
		fail "PC message did not appear"
	end
end

Then(/^I should see prompt to end Hyrail$/) do
	wait_for(60) {element_exists("TextView id:'message'")}
	message = query("TextView id:'message'", :text).first
	unless message == "End Hyrail?"
		fail "Prompt to end Hyrail did not appear"
	end
end

When(/^I end Hyrail$/) do	
	macro 'I should see prompt to end Hyrail'
	message = query("TextView id:'message'", :text).first
	if message == "End Hyrail?"
	   	touch("button marked:'Yes'")
    	sleep(5)
    end
end

Then(/^I view Current Status after stopping Hyrail$/) do
	#Verify status is On-Duty (US60)" 
	actual_property = query("TextView id:'tvDSHDutyStatus'",:text).first
 	unless actual_property == "On-Duty (US60)"
		fail "Duty Status incorrect. Expected 'On-Duty (US60)' but was '#{actual_property}'"
	end
end

Then(/^I should see prompt to end Non-Regulated Driving$/) do
	wait_for(60) {element_exists("TextView id:'message'")}
	message = query("TextView id:'message'", :text).first
	unless message == "End Non-Regulated Driving use?"
		fail "Prompt to end Non-Regulated Driving did not appear"
	end
end
	
When(/^I end Non-Regulated Driving$/) do	
	macro 'I should see prompt to end Non-Regulated Driving'
	message = query("TextView id:'message'", :text).first
	if message == "End Non-Regulated Driving use?"
    	touch("button marked:'Yes'")
	end
end

Then(/^I view Current Status after stopping Non-Reg driving$/) do
	#verify status is On-Duty (US60)" 
	actual_property = query("TextView id:'tvDSHDutyStatus'",:text).first
 	unless actual_property == "On-Duty (US60)"
		fail "Duty Status incorrect. Expected 'On-Duty (US60)' but was '#{actual_property}'"
	end
end

Then(/^I should see prompt to end Personal Conveyance$/) do
	wait_for(60) {element_exists("TextView id:'message'")}
	message = query("TextView id:'message'", :text).first
	unless message == "End Personal Conveyance?"
		fail "Prompt to end Personal Conveyance did not appear"
	end
end
  
When(/^I end Personal Conveyance$/) do	
	macro 'I should see prompt to end Personal Conveyance'
	message = query("TextView id:'message'", :text).first
	if message == "End Personal Conveyance?"
	   	touch("button marked:'Yes'")
    	sleep(5)
  	end
end

Then(/^I view Current Status after stopping Personal Conveyance$/) do
	#Verify status is Off Duty (US60)" 
	actual_property = query("TextView id:'tvDSHDutyStatus'",:text).first
 	unless actual_property == "Off Duty (US60)"
		fail "Duty Status incorrect. Expected 'Off Duty (US60)' but was '#{actual_property}'"
	end
end

Then(/^I see designated driver is "(.*?)"$/) do |lastfirst|
	designated_driver = query("TextView id:'tvDSHName'", :text).first
	unless designated_driver == "#{lastfirst}"
		fail "Designated driver is not #{lastfirst}"
	end
end

Then (/^I see shared device icon$/) do
	check_element_exists("ImageView id:'imgrodsentryshareddevice'")
end

Then (/^I see designated driver icon$/) do
	check_element_exists("ImageView id:'kmbimageteamdriver'")
end

Then (/^I see the add driver icon$/) do
	check_element_exists("ImageView id:'kmbimageteamdriver'")
end

Then(/^I should see ELD Status as Online$/) do
 	actual_property = query("TextView id:'tvLabelEobrConnection'",:text).first
 	unless actual_property.include? "ELD #{ENV['UNIT_ID']} Online"
		fail "ELD Status incorrect. Expected it to contain 'ELD #{ENV['UNIT_ID']} Online' but was '#{actual_property}'"
	end
end

Then(/^I should see EOBR Status as Online$/) do
 	actual_property = query("TextView id:'tvLabelEobrConnection'",:text).first
 	unless actual_property.include? "EOBR #{ENV['UNIT_ID']} Online"
		fail "EOBR Status incorrect. Expected it to contain 'EOBR #{ENV['UNIT_ID']} Online' but was '#{actual_property}'"
	end
end

# This is used specifically in the Edit ELD event test, as it requires the use of an instance variable
Given(/^I see the time of current Duty Status is what I set it to when I edited my last ELD Event$/) do
	actualDutyStatusTime = query("TextView id:'tvDSHLogEventTimestamp'", :text).first

	# get the current local time
	currentDate = (Time.now).localtime
	# format the time to display only the date, and in mm/dd/yyyy format
	currentDate_formatted = currentDate.strftime("%m/%d/%Y")
	# add the date to the beginning of the saved event time of the last event we edited (Saved in an instance variable)
	expectedDateTime = "#{currentDate_formatted} #{@last_edited_event_time}"
	unless actualDutyStatusTime == expectedDateTime
		fail "Expected my current duty status start time to be \"#{expectedDateTime}\", but it is instead showing \"#{actualDutyStatusTime}\""
	end
end

Given(/^I see the Roadside Inspection button$/) do
	check_element_exists("ImageButton id:'btnRoadsideInspection'")
end

Then(/^I click the Roadside Inspection button$/) do
	touch("ImageButton id:'btnRoadsideInspection'")
end
