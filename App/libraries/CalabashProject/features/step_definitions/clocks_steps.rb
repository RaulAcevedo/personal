Then(/^I should see Clocks$/) do
	wait_for(60) {element_exists("TextView id:'drivingTitle'")}
end

Then(/^I click Dashboard button$/) do
	touch("CheckedTextView text:'Dashboard'")
end

Given(/^I see the ALK CoPilot clocks$/) do
	macro 'I should see Clocks'
	# Wait 5 more seconds for ALK to load
	sleep(5)
	alk = query("com.alk.copilot.NativeRenderer")
	clocks = query("com.jjkeller.kmb.share.DOTClock")
	unless alk.size == 1 && clocks.size == 3
		fail("Clocks screen is not displaying properly")
	end
end
