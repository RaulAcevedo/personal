When(/^I scroll down to the bottom of the App Settings screen$/) do
	# Scroll down until you see Database version, then 1 more
	myQuery = "TextView text:'Database Version'"
	q = query(myQuery)
	counter = 0
	while (q.empty? && counter < 10)
		scroll_down
		q = query(myQuery)
		counter += 1
	end
	if counter == 10
		fail("The Database Version text could not be found")
	else
		# Database Version is the last item on the page, unless it is a compliance tablet
		scroll_down
	end
end

Given(/^I see the label "(.*)"$/) do |labelName|
	if (query("TextView text:'#{labelName}'").empty?)
		fail("The label '#{labelName}' could not be found")
	end
end

Given(/^I see "(.*)" under Compliance Tablet$/) do |status|
	actual_text = query("TextView id:'lblKMBComplianceTablet'", :text).first
	unless actual_text == status
		fail("Complaince Tablet text expected: #{status}, actual: #{actual_text}")
	end
end

