Then(/^I should see Trip Information screen$/) do
	wait_for(30) {element_exists("EditText id:'txtTrailer'")}
end

When(/^I enter Trailer Number "(.*?)" and Shipment Info "(.*?)"$/) do |trailer, shipment|
	macro 'I should see Trip Information screen'
	clear_text_in("EditText id:'txtTrailer'")
	touch("EditText id:'txtTrailer'")
	keyboard_enter_text trailer
	clear_text_in("EditText id:'txtShipmentInfo'")
	touch("EditText id:'txtShipmentInfo'")
	keyboard_enter_text shipment
	hide_soft_keyboard
end	

Given(/^Unit Number is the connected ELD$/) do
  expected_property = ENV['UNIT_ID']
  actual_property = query("EditText id:'txtTractorNumber'", :text).first
  unless expected_property == actual_property
    fail "Unit Number is incorrect.  Expected it to contain ELD '#{ENV['UNIT_ID']}' but was '#{actual_property}'"
  end
end
