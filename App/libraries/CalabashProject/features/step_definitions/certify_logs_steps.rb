Then(/^I should see Certify Logs screen$/) do
	wait_for(30) {element_exists("TextView text:'Certify Logs'")}
end

When(/^I certify first log$/) do
	touch("CheckBox id:'chkLogDate'")
	touch("button marked:'Submit'")
	wait_for(30) {element_exists("button marked:'Agree'")}
	touch("button marked:'Agree'")
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

When(/^I certify all logs$/) do
	checkboxes = query("CheckBox id:'chkLogDate'")
	checkboxes.each do |chkbx|
		touch(chkbx)
	end
	touch("button marked:'Submit'")
	wait_for(30) {element_exists("button marked:'Agree'")}
	touch("button marked:'Agree'")
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

#Dismiss the Certify Logs prompt
Then(/^I dismiss the Certify Logs prompt on RODS if it appears$/) do
	sleep(5)
	prompt = query("DialogTitle id:'alertTitle' text:'Certify/Recertify Logs'")
	# if it does not appear, do nothing
	unless prompt.empty?
		print "\nAttempting to touch No button\n"

		counter = 0;
		while query("Button text:'No'").length > 0 && counter < 5 do
			print "\nTouching No button\n"
			touch("Button text:'No'")
			counter = counter + 1
		end
	end
end




