Then(/^I should see New Status screen$/) do
	sleep(5)
	wait_for(120) {element_does_not_exist("TextView id:'message'")}
	wait_for(120) {element_exists("TextView text:'New Status'")}
end

When(/^I go to New Status screen$/) do
	macro 'I choose menu item Menu'
	macro 'I choose menu item New Status'
	macro 'I should see New Status screen'
end

Given(/^Off Duty is selected$/) do
  expected_property = "Off Duty"
  actual_property = query("TextView id:'text1'",:text).first
  unless expected_property == actual_property
    fail "Off Duty Status is not selected."
  end
end

When(/^I choose Status "(.*?)"$/) do |status|
	touch("spinner id:'rns_spndutystatus'")
	wait_for(30) {element_exists("CheckedTextView text:'#{status}'")}
	touch("CheckedTextView text:'#{status}'")
	#enter a location if none exists (if gps not working)
	location = query("EditText id:'tvActualLocation'",:text).first
	if location == ""
		touch("EditText id:'tvActualLocation'")
		keyboard_enter_text "Appleton, WI"
		hide_soft_keyboard
	end		
end

#For Non-Regulated Driving checkbox or Personal Conveyance checkbox or Hyrail checkbox
Then(/^I should not see "(.*?)" checkbox$/) do |checkbox|
	check_element_does_not_exist("CheckBox text:'#{checkbox}'")
end

#For Non-Regulated Driving checkbox or Personal Conveyance checkbox or Hyrail checkbox
Then(/^I should see "(.*?)" checkbox$/) do |checkbox|
	element_exists("CheckBox text:'#{checkbox}'")
end

#For Non-Regulated Driving checkbox or Personal Conveyance checkbox or Hyrail checkbox
When(/^I enable "(.*?)"$/) do |checkbox|
	touch("CheckBox text:'#{checkbox}'")
end