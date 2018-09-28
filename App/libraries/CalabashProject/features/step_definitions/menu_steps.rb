When(/^I choose menu item Menu$/) do
	touch("ActionMenuItemView id:'NoResourceEntry-999999999'")
	sleep(2)
	#sometimes Menu is clicked but doesn't open, so try again if needed
	unless element_exists("TextView text:'System Menu'")
		touch("ActionMenuItemView id:'NoResourceEntry-999999999'")
		sleep(2)
	end
end

#Items under Menu

When(/^I choose menu item New Status$/) do
	touch("TextView text:'New Status'")
	sleep(2)
end 

When(/^I choose menu item System Menu$/) do
	touch("TextView text:'System Menu'")
	sleep(2)
	wait_for(60) {element_exists("TextView text:'Records'")}
end

When(/^I choose menu item Log Off$/) do
	touch("TextView text:'Log Off'")
	sleep(2)
end

Given(/^"(.*)" is present under Menu$/) do |menuItem|
	unless element_exists("TextView text:'#{menuItem}'")
		fail "#{menuItem} is not present"
	end
end

When(/^I choose menu item WiFi Settings$/) do
	touch("TextView text:'WiFi Settings'")
	sleep(2)
end


#Items under System Menu

When(/^I choose menu item Records$/) do
	touch("TextView text:'Records'")
	sleep(2)
end

When(/^I choose menu item Diagnostics$/) do
	touch("TextView text:'Diagnostics'")
	sleep(2)
end

When(/^I choose menu item File$/) do
	touch("TextView text:'File'")
	sleep(2)
end

#items under File

When(/^I choose menu item Admin$/) do
	touch("TextView text:'Admin'")
	sleep(2)
end

When(/^I choose menu item Roadside Inspection$/) do
	touch("TextView text:'Roadside Inspection'")
	sleep(2)
end

#Items under Admin

When(/^I choose menu button Dashboard$/) do
	touch("Button marked:'Dashboard'")
	sleep(2)
end

When(/^I choose menu button Reset Eobr Historical Data$/) do
	touch("Button marked:'Reset Eobr Historical Data'")
	sleep(2)
end

#Items under Records

When(/^I choose menu item Employee Rules$/) do
	touch("TextView text:'Employee Rules'")
end

#Left Menu Items

When(/^I choose menu item Done$/) do
	touch("CheckedTextView text:'Done'")
end

#RODS Icons

When(/^I choose View Log icon$/) do
	touch("ImageButton id:'btnViewLog'")
	wait_for(30) {element_exists("TextView id:'lblOffDuty'")}
end

When(/^I choose Vehicle Inspection icon$/) do
	touch("ImageButton id:'btnVehicleInspection'")
	wait_for(30) {element_exists("TextView text:'Vehicle Inspection'")}
end

When(/^I choose EOBR Status icon$/) do
	touch("ImageButton id:'btnEobrConnection'")
end

When(/^I choose Log Off icon$/) do
	touch("ImageButton id:'btnLogoff'")
end

#Items under Diagnostics

When(/^I choose Diagnostics menu item "(.*?)"$/) do |item|
	macro 'I choose menu item Menu'
	macro 'I choose menu item System Menu'
	macro 'I choose menu item Diagnostics'
	touch("TextView text:'#{item}'")
	sleep(2)
end

#Left Menu Items under View Log

When(/^I choose menu item View Hours$/) do
	touch("CheckedTextView text:'View Hours'")
	wait_for(30) {element_exists("TextView text:'Available Hours Report'")}
end

When(/^I choose menu item View Log Remarks$/) do
	touch("CheckedTextView text:'View Log Remarks'")
	wait_for(30) {element_exists("TextView text:'View Log Remarks'")}
end

When(/^I choose menu item View Trip Info$/) do
	touch("CheckedTextView text:'View Trip Info'")
	wait_for(30) {element_exists("TextView text:'Trip Information'")}
end

When(/^I choose menu item Edit Log$/) do
	touch("CheckedTextView text:'Edit Log'")
	wait_for(30) {element_exists("TextView text:'Edit Log'")}
end

#Items under Vehicle Inspection

When(/^I choose menu item New Pre-Trip Inspection$/) do
	touch("TextView text:'New Pre-Trip Inspection'")
	sleep(2)
end

#Left Menu Items under Vehicle Inspection

When(/^I choose menu item Submit$/) do
	touch("CheckedTextView text:'Submit'")
	sleep(15)
	if(element_exists("TextView text:'Network is unavailable. The inspection is saved and will be submitted later'"))
		touch("button marked:'OK'")
	end
end

#Left Menu Items under Edit Log Events

When(/^I choose menu item Apply$/) do
	wait_for(5) {element_exists("CheckedTextView text:'Apply'")}
	touch("CheckedTextView text:'Apply'")
end

When(/^I choose menu item Cancel$/) do
	touch("CheckedTextView text:'Cancel'")
end