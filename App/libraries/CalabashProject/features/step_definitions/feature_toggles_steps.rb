When(/^I choose Feature Toggle for hyrail in Dev$/) do
	if ENV['ENVIRONMENT'] == "DEV"
		macro 'I should see Login screen'
		macro 'I choose Feature Toggles button'
		macro 'I deselect ELD mandate'

		#select "Hyrail Enabled"
		#first have to scroll until Hyrail Enabled checkbox is in view
		q = query("CheckBox text:'Hyrail Enabled'")
		counter = 0
		while q.empty?
			break if counter == 5
			scroll_down
			q = query("CheckBox text:'Hyrail Enabled'")
			counter = counter + 1
		end
		if counter == 5
			fail("The Hyrail Enabled button could not be found")
		else
			#Hyrail Enabled checkbox is found
		 	actual_property = query("CheckBox text:'Hyrail Enabled'",:checked).first
	 		if actual_property == false
 			#box is unchecked, so check it
 			touch("CheckBox text:'Hyrail Enabled'")
 			end
			macro 'I press the OK button on Feature Toggles screen'
			macro 'I press OK on Successfully completed message'
		end
	end
end

When(/I choose Feature Toggle for nonmandate in (Dev|QAS|Dev or QAS)$/) do |myEnv|
	# perform case insensitive comparison of Environment vs Dev/QAS, or allow both
	if ENV['ENVIRONMENT'].casecmp(myEnv).zero? || 'Dev or QAS' == myEnv
		macro 'I should see Login screen'
		macro 'I choose Feature Toggles button'
		macro 'I deselect ELD mandate'
		macro 'I press the OK button on Feature Toggles screen'
		macro 'I press OK on Successfully completed message'
	end
end

When(/^I choose Feature Toggle for mandate in QAS$/) do
	if ENV['ENVIRONMENT'] == "QAS"
		macro 'I should see Login screen'
		macro 'I choose Feature Toggles button'

		#select ELD mandate
	 	actual_property = query("CheckBox text:'ELD Mandate'",:checked).first
	 	if actual_property == false
 		#box is unchecked, so check it
 		touch("CheckBox text:'ELD Mandate'")
 		print "checked ELD Mandate "
		end

		macro 'I press the OK button on Feature Toggles screen'
		macro 'I press OK on Successfully completed message'
	end
end

When(/^I choose Feature Toggle for Force Compliance Tablet Mode$/) do
	macro 'I choose Feature Toggles button'
	macro 'I select "Force Compliance Tablet Mode"'
	print "checked Force Compliance Tablet Mode"
	macro 'I press the OK button on Feature Toggles screen'
	macro 'I press OK on Successfully completed message'
end

Then(/^I deselect "(.*?)"$/) do |option|
 	actual_property = query("CheckBox text:'#{option}'",:checked).first
 	if actual_property == true
 		#box is checked, so uncheck it
 		touch("CheckBox text:'#{option}'")
 	end
 end

Then(/^I select "(.*?)"$/) do |option|
 	actual_property = query("CheckBox text:'#{option}'",:checked).first
 	if actual_property == false
 		#box is unchecked, so check it
 		touch("CheckBox text:'#{option}'")
 	end
 end

When (/^I press the OK button on Feature Toggles screen$/) do
#Can't just use a simple "Given I press the "OK" button" in the feature file, because
#the OK button is so far down the screen that we have to insert scrolling steps to get to it
	q = query("button marked:'OK'")
	counter = 0
	while q.empty?
		break if counter == 5
		scroll_down
		q = query("button marked:'OK'")
		counter = counter + 1
	end
	if counter == 5
		fail("The OK button could not be found")
	else
		touch("button marked:'OK'")
	end
end

Then(/^I choose Feature Toggles button$/) do
		hide_soft_keyboard
		touch("button marked:'Feature Toggles'")
		wait_for(30) {element_exists("TextView id:'textView1'")}
end

Then(/^I deselect ELD mandate$/) do
	 	actual_property = query("CheckBox text:'ELD Mandate'",:checked).first
	 	if actual_property == true
 		#box is checked, so uncheck it
 		touch("CheckBox text:'ELD Mandate'")
		end
end

Then(/^I press OK on Successfully completed message$/) do
		#wait for message "Successfully completed"
		wait_for_element_exists("TextView id:'message'")
		message = query("TextView id:'message'",:text).first
		unless message.include? "Successfully completed"
			fail "Successfully completed message did not appear. This message appeared: '#{message}'"
		end
		#click OK button (have to click on button with id = button1, since "button marked ok" will find the other OK button
		touch("button id:'button1'")
		print "touched OK button on Sucessfully completed message "
		#wait for login screen
		wait_for(60) {element_exists("EditText id:'txtusername'")}
end

When(/^I choose Selective Feature Toggles if Feature Toggles button exists$/) do
		macro 'I should see Login screen'
		if element_exists("button marked:'Feature Toggles'")
			macro 'I choose Feature Toggles button'
			#select Selective Feature Toggles
		 	actual_property = query("CheckBox {text CONTAINS 'Selective Feature Toggles'}",:checked).first
	 		if actual_property == false
 			#box is unchecked, so check it
 				touch("CheckBox {text CONTAINS 'Selective Feature Toggles'}")
			end
			macro 'I press the OK button on Feature Toggles screen'
			macro 'I press OK on Successfully completed message'
		end
end
