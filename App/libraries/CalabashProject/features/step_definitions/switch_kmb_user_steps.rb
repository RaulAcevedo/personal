When(/^I switch to driver "(.*?)" and assume driving$/) do |lastfirst|
	touch("TextView id:'tvDSHName'")
	wait_for(30) {element_exists("Spinner id:'spnswitchuser'")}
	touch("spinner id:'spnswitchuser'")
	wait_for(30) {element_exists("CheckedTextView text:'#{lastfirst}'")}
	touch("CheckedTextView text:'#{lastfirst}'")
	sleep(2)
	touch("CheckBox text:'Assume Driving Responsibilities'")
	touch("button marked:'OK'")
end	
