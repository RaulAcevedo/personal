Then(/^I should see Safety Information Warning screen$/) do
	#id of OK button on Safety Info Warning is 'button1'
	wait_for(60) {element_exists("button id:'button1'")}
end

When(/^I dismiss Safety Warning$/) do
	macro 'I should see Safety Information Warning screen'
	wait_for(60) {element_exists("TextView id:'message'")}
	#use id 'button1' here, not text property "OK", because Team Driver #2 Login screen has an button marked "OK"
	#Since id of OK button on Team Driver #2 Login is 'btnloginSolo', this distinguishes the 2 OK buttons.
	touch("button id:'button1'")
	sleep(2)
end
