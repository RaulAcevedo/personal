Then(/^I enter TAB Command "(.*?)"$/) do |command|
	touch("EditText id:'txtTabCmd'")
	keyboard_enter_text command
	hide_soft_keyboard
end

Then(/^I click Send TAB Cmd button$/) do
	touch("CheckedTextView text:' Send TAB Cmd'")
	sleep(5)
end	

Then(/^I click Done button$/) do
	touch("CheckedTextView text:'Done'")
end	

Then(/^I should see Continue Driving message$/) do
	wait_for(30) {element_exists("TextView text:'Continue in driving status?'")}
end

When(/^I send bt discover to ELD$/) do
	system "'#{ENV['ELD_COMMANDER_PATH']}' --device '#{ENV['DEVICE']}' --command 'bt discover'"
	sleep(10)
end

When(/^I turn on engine$/) do
	system "'#{ENV['ELD_COMMANDER_PATH']}' --device '#{ENV['DEVICE']}' --command 'rpm 1500'"
	sleep(20)
end

When(/^I turn off engine$/) do
	system "'#{ENV['ELD_COMMANDER_PATH']}' --device '#{ENV['DEVICE']}' --command 'rpm 0'"
end

When(/^I start driving vss 100$/) do
	system "'#{ENV['ELD_COMMANDER_PATH']}' --device '#{ENV['DEVICE']}' --command 'vss 100'"
	sleep(20)
end

When(/^I stop driving$/) do
	system "'#{ENV['ELD_COMMANDER_PATH']}' --device '#{ENV['DEVICE']}' --command 'vss 0'"
	sleep(30)
end

When(/^I test variables$/) do
	print "testing variables "
	print ENV['DEVICE']
end

When(/^I reset EOBR historical data$/) do
	wait_for(360) {element_exists("Button marked:'OK'")}
	touch("button marked:'OK'")
end
