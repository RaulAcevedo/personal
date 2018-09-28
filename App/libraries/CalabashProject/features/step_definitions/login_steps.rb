Then(/^I should see Login screen$/) do
	wait_for(60) {element_exists("EditText id:'txtusername'")}
end

When(/^I login with user name "(.*?)" and password "(.*?)"$/) do |name, password|
	clear_text_in("EditText id:'txtusername'")
	touch("EditText id:'txtusername'")
	keyboard_enter_text name
	touch("EditText id:'txtpassword'")
	keyboard_enter_text password
	touch("button marked:'Login - Solo Driver'")
end	

When(/^I login team driver "(.*?)" and password "(.*?)"$/) do |name, password|
	clear_text_in("EditText id:'txtusername'")
	touch("EditText id:'txtusername'")
	keyboard_enter_text name
	touch("EditText id:'txtpassword'")
	keyboard_enter_text password
	touch("button marked:'Login - Team Driver'")
end	

When(/^I login team driver 2 "(.*?)" and password "(.*?)"$/) do |name, password|
	clear_text_in("EditText id:'txtusername'")
	touch("EditText id:'txtusername'")
	keyboard_enter_text name
	touch("EditText id:'txtpassword'")
	keyboard_enter_text password
	touch("button id:'btnloginSolo'")
end	


Then (/^I dismiss Application Update message if it appears$/) do
	#wait 10 seconds for Authenticating message to disappear
	sleep(10)
	#is a message still displayed after 10 seconds?
	#could be Authenticating if slow, or Application Update, or Missing Logs, or something else
	if element_exists("TextView id:'message'")
		message = query("TextView id:'message'",:text).first
		print message
		if message == nil ||message.length == 0
			# do nothing
		else
			# this is throwing an error if no message appears.
			if message.include? "Application Update"
				touch("button marked:'No'")
				sleep(2)
			else
				print "Message is '#{message}'"
			end
		end
	else
		print "No message"
	end
end



#works
#wait_for(20) {element_exists("* {text CONTAINS 'Application Update'}")}

#didn't work
#	if (element_exists("* {text CONTAINS 'Application Update'}"))

#didn't work
#	if element_exists("* {text CONTAINS 'Application Update'}")

#didn't work
#	update = element_exists("* {text CONTAINS 'Application Update'}")
#	if update==true
#		touch("button marked:'No'")
#		sleep(2)
#	end

#didn't work
#	message = query("TextView id:'message'",:text).first
#	if message.include? "Application Update"
#		touch("button marked:'No'")
#		sleep(2)
#	end

#didn't work
#	if view_with_mark_exists("Application Update")
#		touch("button marked:'No'")
#		sleep(2)
#	end

#didn't work
#	update = element_exists("* {text CONTAINS 'Application Update'}")
#	if update
#		touch("button marked:'No'")
#		sleep(2)
#	end

#didn't work
#	if element_exists("TextView {text CONTAINS 'Application Update'}")
#		touch("button marked:'No'")
#		sleep(2)
#	end

#works
#	sleep(5)
#	if element_exists("TextView id:'message'")
#		touch("button marked:'No'")
#		sleep(2)
#	end

#works
#	sleep(5)
#	if element_exists("TextView text:'Driver Login'")
#		print "Driver Login page appears 1"
#	end
	
#	if element_exists("TextView text:'{text CONTAINS 'Driver Login'}")
#		print "Driver Login page appears 2"
#	end

#doesn't work	
#	if element_exists("TextView text:'{text CONTAINS 'Application Update'}'")
#		touch("button marked:'No'")
#		sleep(2)
#	end

#didn't try yet
#	if view_with_mark_exists("No")
#		touch("button marked:'No'")
#		sleep(2)
#	end


#	if element_exists("TextView text:'Driver Login'")
#		print "Driver Login page appears 1"
#	end
	
#	if element_exists("TextView text:'{text CONTAINS 'Driver Login'}")
#		print "Driver Login page appears 2"
#	end
