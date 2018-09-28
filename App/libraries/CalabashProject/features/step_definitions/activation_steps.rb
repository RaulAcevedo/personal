Given(/^I go to activation screen$/) do
	wait_for(180) {element_exists("EditText id:'txtactivationcode'")}
end	


When(/^I try to activate KMB$/) do
	macro 'I go to activation screen'
	touch("EditText id:'txtactivationcode'")
	keyboard_enter_text ENV['ACTIVATION_CODE']
	check_element_exists("button marked:'btnactivate'")
	touch("button marked:'btnactivate'")
end	
	

