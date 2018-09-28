#Then(/^I should see missing logs prompt$/) do
#	wait_for(360) {element_exists("* {text CONTAINS 'missing logs'}")}
#end

When(/^I click No in missing logs prompt$/) do
	wait_for(360) {element_exists("* {text CONTAINS 'missing logs'}")}
	#other messages may also exist, so wait for those to go away
	#missing logs prompt has TextView id = message
	#Connected to <ELD name> has TextView id = message
	#Downloading log data... has TextView id = message
	#first wait for id=toast_layout_root to go away
	wait_for(360) {element_does_not_exist("LinearLayout id:'toast_layout_root'")}
	wait_for(360) {element_does_not_exist("* {text CONTAINS 'Connected to'}")}

	if element_exists("TextView id:'message'")
		message = query("TextView id:'message'",:text).first
		print "After toast_layout_root is gone, this message is found: '#{message}'"
		if message.include? "missing logs"
			touch("button marked:'No'")
			sleep(2)
		else
			print "Message is not missing logs. Message is: '#{message}'"
		end
	else
		print "After toast_layout_root is gone, no message is found"
	end
end
