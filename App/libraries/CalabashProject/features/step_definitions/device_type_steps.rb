Then(/^I select Shared Device on Device Type screen$/) do
	sleep(5);
	#wait for "Connected to <ELD Name>" message to not exist
	wait_for(360) {element_does_not_exist("LinearLayout id:'toast_layout_root'")}
	wait_for(60) {element_exists("Button marked:'Shared Device'")}
	touch("Button marked:'Shared Device'")
end
