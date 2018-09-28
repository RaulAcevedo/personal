Then(/^I should see unassigned driving alert$/) do
	wait_for(60) {element_exists("TextView id:'message'")}
	m = query("* id:'message'", :text).first
	unless m == 'Detected unassigned driving periods that may impact the current log. Check the accuracy of your on duty time.'
		fail "message incorrect"
	end
end
