Then(/^I should see Perform EOBR Discovery screen$/) do
	wait_for(30) {element_exists("TextView text:'Perform EOBR Discovery'")}
end

When(/^I click Release on EOBR Discovery screen$/) do
	touch("button marked:'Release'")
	sleep(5)
end

Then(/^I should see text "Successfully released partnership"$/) do
	has_text?("Successfully released partnership")
end
