Then(/^I should see Log Off screen$/) do
	wait_for(30) {element_exists("Button marked:'Logout'")}
end

When(/^I click Logout on Log Off screen$/) do
	touch("button marked:'Logout'")
	sleep(45)
end

When(/^I Logout without Submitting Logs$/) do
	macro 'I choose Log Off icon'
	macro 'I should see Log Off screen'

	#uncheck box for submit Logs on Logout
 	actual_property = query("CheckBox text:'Submit Logs on Logout'",:checked).first
 	if actual_property == true
 		#box is checked, so uncheck it
 		touch("CheckBox text:'Submit Logs on Logout'")
 	end
	#Press logout
	touch("button marked:'Logout'")
	print "Pressed logout button"

	wait_for_element_exists("TextView id:'message'")
	message = query("TextView id:'message'",:text).first
	unless message.include? "Logging out"
		fail "Logging out message did not appear. This message appeared: '#{message}'"
	end
	print "Logging out message appeared"

	#Waits for submitting logs to complete and app to close.
	#I tried many other methods of waiting, but ran into problems with those
	sleep(45)
end

When(/^I logout with Submitting Logs$/) do
#for submitting logs under non-mandate
	macro 'I choose Log Off icon'
	macro 'I should see Log Off screen'

	#Press logout
	touch("button marked:'Logout'")
	print "Pressed logout button"

	wait_for_element_exists("TextView id:'message'")
		message = query("TextView id:'message'",:text).first
	unless message.include? "Logging out"
		fail "Logging out message did not appear. This message appeared: '#{message}'"
	end
	print "Logging out message appeared"

	#Waits for submitting logs to complete and app to close.
	#I tried many other methods of waiting, but ran into problems with those
	sleep(90)
end

When(/^I logout with Submitting Logs under mandate$/) do
#for submitting logs under non-mandate
	macro 'I choose Log Off icon'
	macro 'I should see Log Off screen'

	#Get value of Actual Location field, if empty (because no gps signal), then enter a location
	location = query("EditText id:'tvActualLocation'",:text).first
	if location == ""
		touch("EditText id:'tvActualLocation'")
		keyboard_enter_text "Appleton, WI"
		hide_soft_keyboard
	end		

	#Press Submit
	touch("button marked:'Submit'")
	print "Pressed Submit button"
end


When(/^I logout first team driver with Submitting Logs$/) do
	#Press logout
	touch("button marked:'Logout'")
	print "Pressed logout button"

	#removed check for "Logging out" message, because when these messages appear quickly,
	#the "Additional Team Drivers remain" message is missed.
	#wait_for_element_exists("TextView id:'message'")
	#	message = query("TextView id:'message'",:text).first
	#unless message.include? "Logging out"
	#	fail "Logging out message did not appear. This message appeared: '#{message}'"
	#end
	#print "Logging out message appeared"

	#removed check for "Additional Team Drivers remain logged in".  Message flashes too fast to see.
	#Submitting logs message appears, then the message that additional drivers remain logged in
	#wait_for(60) {element_exists("TextView text:'Additional Team Drivers remain logged in.'")}
end