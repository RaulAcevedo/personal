Then(/^I claim all events on Unidentified ELD Events screen if it appears$/) do
	sleep(10)
	if(element_exists("TextView text:'Unidentified ELD Events'"))
		#check box to select all events
		touch("CheckBox id:'uee_cbo_claimall'")
		touch("button marked:'Claim'")
	end
end

Then(/^I dismiss the Unidentified ELD Events screen if it appears$/) do
	sleep(10)
	if(element_exists("TextView text:'Unidentified ELD Events'"))
		# Click Done
		print "Unidentified ELD events screen is present!".blue
		touch("CheckedTextView text:'Done'")
	end
end

Then(/^I dismiss the Review Unidentified Events prompt if it appears$/) do
	if(element_exists("DialogTitle id:'alertTitle', text:'Review Unidentified Events?'"))
		touch("Button id:'button2', text:'No'")
		print "Touching 'No' on prompt to review unidentified events".blue
	end
end