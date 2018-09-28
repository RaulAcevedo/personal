Then(/^I should see Odometer Calibration screen$/) do
	wait_for(30) {element_exists("EditText id:'txtEditOdometer'")}
end

When(/^I choose Cancel on Odometer Calibration screen$/) do
	wait_for(30) {element_exists("EditText id:'txtEditOdometer'")}
	touch("button marked:'Cancel'")
end

When(/^I choose Cancel on Odometer Calibration screen if it appears$/) do
	sleep(5)
	if(element_exists("EditText id:'txtEditOdometer'"))
			touch("button marked:'Cancel'")
	end
end
