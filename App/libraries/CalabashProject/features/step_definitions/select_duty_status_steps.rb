Then(/^I should see Select Duty Status screen$/) do
	wait_for(360) {element_exists("button marked:'Submit Status'")}
end

Given(/^On-Duty Not Driving is selected$/) do
  expected_property = true
  actual_property = query("RadioButton id:'radio_onDuty'",:checked).first
  unless expected_property == actual_property
    fail "On-Duty Not Driving is not checked."
  end
end

When(/^I click Submit Status button$/) do
	#first get Actual Location field, if empty, then enter a location
	location = query("EditText id:'tvActualLocation'",:text).first
	if location == ""
		touch("EditText id:'tvActualLocation'")
		keyboard_enter_text "Appleton, WI"
		hide_soft_keyboard
	end		
	touch("button marked:'Submit Status'")
end	

Then(/^I select an initial duty status of "(.*)"$/) do |dutyStatus|
  # touch spinner
  touch("spinner id:'initialDutyStatusSpinner'")
  # wait for your dutys status to appear
  wait_for(30) {element_exists("CheckedTextView text:'#{dutyStatus}'")}
  # touch your duty status
	touch("CheckedTextView text:'#{dutyStatus}'")
end

Then(/^I select a current duty status of "(.*)"$/) do |dutyStatus|
  touch("RadioButton text:'#{dutyStatus}'")
end

Then(/^I enter an Actual Location of "(.*)"$/) do |location|
  touch("EditText id:'tvActualLocation'")
  keyboard_enter_text location
  hide_soft_keyboard
end
