Then(/^I should see EULA screen$/) do
	wait_for(60) {element_exists("* {text CONTAINS 'End User License Agreement'}")}
end

When(/^I accept EULA$/) do
	macro 'I should see EULA screen'
	touch("button marked:'Accept'")
	sleep(2)
end
