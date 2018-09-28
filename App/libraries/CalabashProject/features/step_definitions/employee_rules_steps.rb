Then(/^I should see Employee Rules screen$/) do
	wait_for(30) {element_exists("TextView id:'eerules_lblEmployeeRules'")}
end

When(/^I go to Employee Rules screen$/) do
	macro 'I choose menu item Menu'
	macro 'I choose menu item System Menu'
	macro 'I choose menu item Records'
	macro 'I choose menu item Employee Rules'
	macro 'I should see Employee Rules screen'
end

When(/^I click Download on Employee Rules screen$/) do
	touch("button marked:'Download'")
end

Then(/^I should see success message$/) do
	wait_for(30) {element_exists("TextView id:'eerules_lblMessage'")}
end

Then(/^I should see "(.*?)" is disabled$/) do |rule|
	actual_property = query("CheckBox text:'#{rule}'",:checked).first
	unless actual_property == false
 		fail "'#{rule}' is enabled"
 	end
end

Then(/^I should see "(.*?)" is enabled$/) do |rule|
	actual_property = query("CheckBox text:'#{rule}'",:checked).first
 	unless actual_property == true
 		fail "'#{rule}' is disabled"
 	end
end

Then(/^I should see Ruleset is "(.*?)"$/) do |ruleset|
	actual_property = query("TextView id:'eerules_tvRuleset'",:text).first
 	unless actual_property == ruleset
 		fail "Ruleset is '#{actual_property}'. Expected '#{ruleset}'."
 	end
end

When(/^I choose Download$/) do
	q = query("button marked:'Download'")
	counter = 0
	while q.empty?
		break if counter == 5
		scroll_down
		q = query("button marked:'Download'")
		counter = counter + 1
	end
	if counter == 5
		fail("The Download button could not be found")
	else
		touch("button marked:'Download'")
		sleep(10)
		scroll_up
		check_element_exists("TextView text:'Successfully completed.'")
	end
end