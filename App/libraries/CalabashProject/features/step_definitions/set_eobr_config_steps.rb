Then(/^I should see EOBR Configuration screen$/) do
	wait_for(30) {element_exists("TextView text:'EOBR Configuration'")}
end

Then(/^I should see ELD Configuration screen$/) do
	wait_for(30) {element_exists("TextView text:'ELD Configuration'")}
end

#This applies to ELD Configuration screen and EOBR Configuration screen.
Given(/^Unit Number on EOBR Configuration is the connected ELD$/) do
  expected_property = ENV['UNIT_ID']
  actual_property = query("EditText id:'txtTractorNumber'", :text).first
  unless expected_property == actual_property
    fail "Unit Number is incorrect."
  end
end

When(/^I choose Engine Data Bus "(.*?)"$/) do |data_bus|
	touch("spinner id:'eobrcnfg_spnenginedatabus'")
	wait_for(30) {element_exists("CheckedTextView text:'#{data_bus}'")}
	touch("CheckedTextView text:'#{data_bus}'")
end